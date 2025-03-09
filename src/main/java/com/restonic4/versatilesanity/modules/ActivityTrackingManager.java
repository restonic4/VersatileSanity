package com.restonic4.versatilesanity.modules;

import com.chaotic_loom.under_control.events.EventResult;
import com.chaotic_loom.under_control.events.types.LivingEntityExtraEvents;
import com.chaotic_loom.under_control.events.types.OtherEvents;
import com.chaotic_loom.under_control.events.types.PlayerExtraEvents;
import com.chaotic_loom.under_control.events.types.ServerPlayerExtraEvents;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.components.HomeDetectionComponent;
import com.restonic4.versatilesanity.components.HomeDetectionComponents;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import com.restonic4.versatilesanity.util.LootQualityChecker;
import com.restonic4.versatilesanity.util.Utils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ActivityTrackingManager {

    // Mapa para rastrear si cada jugador está en casa
    private static Map<UUID, Boolean> playerHomeStatus = new HashMap<>();

    // Mapa para rastrear si el jugador está siendo grabado actualmente
    private static Map<UUID, Boolean> isPlayerBeingTracked = new HashMap<>();

    // Temporizadores para control de grabación (en ticks)
    private static Map<UUID, Integer> timeSinceLastTracking = new HashMap<>();
    private static Map<UUID, Integer> currentTrackingDuration = new HashMap<>();

    // Intervalos de tiempo (en ticks)
    private static final int MINUTE_IN_TICKS = 1200; // 60 segundos * 20 ticks por segundo
    private static final int BASE_TRACKING_INTERVAL = 30 * MINUTE_IN_TICKS; // 30 minutos
    private static final int MIN_TRACKING_DURATION = 15 * MINUTE_IN_TICKS; // 15 minutos

    // Random para intervalos aleatorios
    private static final Random random = new Random();

    // Inicializar todos los eventos
    public static void init() {
        // Eventos del jugador
        registerPlayerEvents();

        // Evento de tick del servidor para verificaciones periódicas
        ServerTickEvents.END_SERVER_TICK.register(ActivityTrackingManager::onServerTick);

        // Eventos de conexión del jugador
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            UUID playerId = player.getUUID();

            // Inicializar estado de casa
            boolean isHome = checkIfPlayerIsHome(player);
            playerHomeStatus.put(playerId, isHome);

            // Inicializar temporizadores con valores aleatorios para evitar que todos los jugadores
            // sean rastreados al mismo tiempo
            int initialDelay = random.nextInt(BASE_TRACKING_INTERVAL);
            timeSinceLastTracking.put(playerId, initialDelay);
            currentTrackingDuration.put(playerId, 0);
            isPlayerBeingTracked.put(playerId, false);
        });

        // Registrar los eventos de Fabric
        registerFabricEvents();
    }

    private static void registerPlayerEvents() {
        // Eventos de interacción con bloques
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer)) {
                return InteractionResult.PASS;
            }

            UUID playerId = player.getUUID();
            if (!isPlayerBeingTracked.getOrDefault(playerId, false)) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof ChestBlockEntity) {
                PlayerActivityTracker.handleChestOpened(player, blockEntity);
            }

            return InteractionResult.PASS;
        });

        // Evento de romper bloques para minería
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            UUID playerId = player.getUUID();
            if (!world.isClientSide() && isPlayerBeingTracked.getOrDefault(playerId, false) && isOre(state.getBlock().toString())) {
                PlayerActivityTracker.handleMining(player, pos);
            }
        });

        // Evento de interacción con entidades (para aldeanos)
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer)) {
                return InteractionResult.PASS;
            }

            UUID playerId = player.getUUID();
            if (!isPlayerBeingTracked.getOrDefault(playerId, false)) {
                return InteractionResult.PASS;
            }

            if (entity instanceof Villager) {
                PlayerActivityTracker.handleVillagerTrade(player, entity);
            }

            return InteractionResult.PASS;
        });
    }

    private static void registerFabricEvents() {
        // Evento de daño
        PlayerExtraEvents.DAMAGED.register((player, damageSource, amount) -> {
            UUID playerId = player.getUUID();
            if (isPlayerBeingTracked.getOrDefault(playerId, false)) {
                PlayerActivityTracker.handleDamage(player, damageSource, amount);
            }
        });

        // Eventos de dormir - No iniciamos grabación al dormir como antes
        PlayerExtraEvents.SLEEPING_START.register((player, blockPos) -> {
            // No hacemos nada aquí
        });

        PlayerExtraEvents.SLEEPING_STOPPED.register((player, bl, bl2) -> {
            // No iniciamos grabación al despertar como antes
        });

        // Evento de loot encontrado
        OtherEvents.LOOT_CONTAINER_GENERATED_LOOT.register((player, lootTable, container, isEntityContainer) -> {
            UUID playerId = player.getUUID();
            if (isPlayerBeingTracked.getOrDefault(playerId, false)) {
                PlayerActivityTracker.handleLootFound(player, LootQualityChecker.getQuality(container));
            }
        });

        // Evento de pesca
        LivingEntityExtraEvents.FISHING_HOOK_RETRIEVED.register((player, hook, itemStacks) -> {
            UUID playerId = player.getUUID();
            if (isPlayerBeingTracked.getOrDefault(playerId, false)) {
                PlayerActivityTracker.handleFishing(player);
            }
            return EventResult.SUCCEEDED;
        });

        // Evento de logros
        ServerPlayerExtraEvents.ADVANCEMENT_GRANTED.register((serverPlayer, advancement, criterionName) -> {
            UUID playerId = serverPlayer.getUUID();
            if (isPlayerBeingTracked.getOrDefault(playerId, false)) {
                PlayerActivityTracker.handleAdvancement(serverPlayer, advancement.toString());
            }
            return EventResult.SUCCEEDED;
        });
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();

            // Verificar si el jugador está en casa
            boolean wasHome = playerHomeStatus.getOrDefault(playerId, false);
            boolean isHome = checkIfPlayerIsHome(player);

            // Si cambió el estado de estar en casa
            if (wasHome != isHome) {
                playerHomeStatus.put(playerId, isHome);

                // Si está siendo rastreado, registrar cambio de estado
                if (isPlayerBeingTracked.getOrDefault(playerId, false)) {
                    PlayerActivityTracker.handleHomeStatus(player, wasHome, isHome);

                    // Si entró a casa después del tiempo mínimo, terminar grabación
                    int trackingTime = currentTrackingDuration.getOrDefault(playerId, 0);
                    if (!wasHome && isHome && trackingTime >= MIN_TRACKING_DURATION) {
                        stopTracking(player);
                    }
                }
            }

            // Si el jugador NO está siendo rastreado actualmente
            if (!isPlayerBeingTracked.getOrDefault(playerId, false)) {
                // Incrementar el tiempo desde última grabación
                int timeElapsed = timeSinceLastTracking.getOrDefault(playerId, 0) + 1;
                timeSinceLastTracking.put(playerId, timeElapsed);

                // Verificar si es hora de iniciar un nuevo rastreo
                // Usamos BASE_TRACKING_INTERVAL ± una variación aleatoria
                //int intervalWithVariance = BASE_TRACKING_INTERVAL + (random.nextInt(RANDOM_INTERVAL_VARIANCE * 2) - RANDOM_INTERVAL_VARIANCE);

                if (timeElapsed >= MIN_TRACKING_DURATION) {
                    startTracking(player);
                }
            }
            // Si el jugador ESTÁ siendo rastreado actualmente
            else {
                // Incrementar duración del rastreo actual
                int trackingDuration = currentTrackingDuration.getOrDefault(playerId, 0) + 1;
                currentTrackingDuration.put(playerId, trackingDuration);

                // Verificar condiciones de finalización
                if (trackingDuration >= MIN_TRACKING_DURATION) {
                    // Si ya pasó el tiempo mínimo y el jugador está en casa, esperar a que salga
                    if (isHome) {
                        // No hacer nada, esperar a que salga y vuelva (se maneja en el cambio de estado)
                    }
                    // Si ha pasado bastante tiempo después del mínimo (añadimos 10 minutos de margen),
                    // terminar de todos modos para evitar grabaciones infinitas
                    else if (trackingDuration >= MIN_TRACKING_DURATION + (10 * MINUTE_IN_TICKS)) {
                        stopTracking(player);
                    }
                }
            }

            // Verificar si el jugador está cerca de una aldea
            if (isPlayerBeingTracked.getOrDefault(playerId, false) && checkIfNearVillage(player)) {
                PlayerActivityTracker.handleVillageVisit(player);
            }
        }
    }

    private static void startTracking(Player player) {
        UUID playerId = player.getUUID();

        if (SanityStatusComponents.SANITY_STATUS.get(player).getSanityStatus() > VersatileSanity.getConfig().getMaxSanity() / 2) {
            return;
        }

        // Iniciar rastreo
        isPlayerBeingTracked.put(playerId, true);
        currentTrackingDuration.put(playerId, 0);

        // Iniciar el seguimiento (asumiendo que el jugador está en casa al inicio)
        PlayerActivityTracker.startTracking(player);
    }

    private static void stopTracking(Player player) {
        UUID playerId = player.getUUID();

        if (!isPlayerBeingTracked.containsKey(player.getUUID())) {
            return;
        }

        // Generar resumen y enviarlo
        String summary = PlayerActivityTracker.stopTrackingAndGenerateSummary(player);
        sendCreepyMessage(player, summary);

        // Restablecer estado y temporizadores
        isPlayerBeingTracked.put(playerId, false);
        timeSinceLastTracking.put(playerId, 0); // Reiniciar el contador para próximo rastreo
    }

    private static boolean checkIfPlayerIsHome(Player player) {
        return HomeDetectionComponents.HOME_DETECTION.get(player).isHome(
                player.blockPosition(), HomeDetectionComponent.DETECTION_RANGE);
    }

    private static boolean checkIfNearVillage(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return Utils.isPlayerInOrNearVillage(serverPlayer, VersatileSanity.getConfig().getVillageRadius());
        }

        return false;
    }

    private static boolean isOre(String blockName) {
        return blockName.contains("_ore") ||
                blockName.contains("deepslate") ||
                blockName.contains("ancient_debris");
    }

    private static void sendCreepyMessage(Player player, String message) {
        if (player instanceof ServerPlayer serverPlayer) {
            Utils.spawnSignedBookAt(
                    serverPlayer.serverLevel(),
                    HomeDetectionComponents.HOME_DETECTION.get(player).getPrimaryHome(),
                    "Diary",
                    player.getDisplayName().getString(),
                    message
            );
        }
    }
}