package com.restonic4.versatilesanity.modules;

import com.restonic4.versatilesanity.components.HomeDetectionComponent;
import com.restonic4.versatilesanity.components.HomeDetectionComponents;
import com.restonic4.versatilesanity.util.LootQualityChecker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerActivityTracker {

    // Tipos de actividades que rastreamos
    public enum ActivityType {
        WOKE_UP,
        CHECKED_CHEST,
        LEFT_HOME,
        ENTERED_HOME,
        MINING,
        VISITED_VILLAGE,
        FOUND_LOOT,
        TOOK_DAMAGE,
        TRADED_WITH_VILLAGER,
        FISHING,
        ACHIEVEMENT
    }

    // Clase para representar una actividad individual
    public static class Activity {
        private ActivityType type;
        private String details;
        private LocalDateTime timestamp;
        private BlockPos position;

        public Activity(ActivityType type, String details, BlockPos position) {
            this.type = type;
            this.details = details;
            this.timestamp = LocalDateTime.now();
            this.position = position;
        }

        public ActivityType getType() {
            return type;
        }

        public String getDetails() {
            return details;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public BlockPos getPosition() {
            return position;
        }
    }

    // Mapa para almacenar actividades por jugador
    private static Map<UUID, List<Activity>> playerActivities = new HashMap<>();

    // Contadores para condensar información
    private static Map<UUID, Map<ActivityType, Integer>> activityCounters = new HashMap<>();

    // Último tipo de actividad registrada para evitar repeticiones consecutivas
    private static Map<UUID, ActivityType> lastActivityType = new HashMap<>();

    // Método para iniciar el seguimiento
    public static void startTracking(Player player) {
        UUID playerId = player.getUUID();
        playerActivities.put(playerId, new ArrayList<>());
        activityCounters.put(playerId, new HashMap<>());

        System.out.println("Tracking " + player.getDisplayName().getString());

        // Registrar el inicio (despertar)
        recordActivity(player, ActivityType.WOKE_UP, "Despertó en su cama", player.blockPosition());
    }

    // Método para detener el seguimiento y generar resumen
    public static String stopTrackingAndGenerateSummary(Player player) {
        UUID playerId = player.getUUID();
        List<Activity> activities = playerActivities.getOrDefault(playerId, new ArrayList<>());

        System.out.println("Stopped tracking " + player.getDisplayName().getString());

        if (activities.isEmpty()) {
            return "No se registraron actividades.";
        }

        String summary = generateSummary(player, activities);

        // Limpiar los datos
        playerActivities.remove(playerId);
        activityCounters.remove(playerId);
        lastActivityType.remove(playerId);

        return summary;
    }

    // Método para registrar una actividad
    public static void recordActivity(Player player, ActivityType type, String details, BlockPos position) {
        UUID playerId = player.getUUID();

        // Verificar si estamos rastreando a este jugador
        if (!playerActivities.containsKey(playerId)) {
            return;
        }

        // Evitar actividades repetidas consecutivas del mismo tipo
        ActivityType last = lastActivityType.getOrDefault(playerId, null);
        if (last == type) {
            // Incrementar contador para esta actividad
            Map<ActivityType, Integer> counters = activityCounters.get(playerId);
            counters.put(type, counters.getOrDefault(type, 0) + 1);
            return;
        }

        // Registrar la nueva actividad
        Activity activity = new Activity(type, details, position);
        playerActivities.get(playerId).add(activity);
        lastActivityType.put(playerId, type);

        // Reiniciar contador para este tipo
        activityCounters.get(playerId).put(type, 1);
    }

    // Manejadores de eventos
    public static void handleChestOpened(Player player, BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity) {
            boolean isHome = HomeDetectionComponents.HOME_DETECTION.get(player).isHome(
                    player.blockPosition(), HomeDetectionComponent.DETECTION_RANGE);

            String location = isHome ? "en casa" : "fuera de casa";
            recordActivity(player, ActivityType.CHECKED_CHEST, "Revisó un cofre " + location, player.blockPosition());
        }
    }

    public static void handleHomeStatus(Player player, boolean wasHome, boolean isHome) {
        if (wasHome && !isHome) {
            recordActivity(player, ActivityType.LEFT_HOME, "Salió de casa", player.blockPosition());
        } else if (!wasHome && isHome) {
            recordActivity(player, ActivityType.ENTERED_HOME, "Regresó a casa", player.blockPosition());
        }
    }

    public static void handleMining(Player player, BlockPos pos) {
        recordActivity(player, ActivityType.MINING, "Minó un bloque", pos);
    }

    public static void handleVillageVisit(Player player) {
        recordActivity(player, ActivityType.VISITED_VILLAGE, "Visitó una aldea", player.blockPosition());
    }

    public static void handleLootFound(Player player, LootQualityChecker.Quality quality) {
        recordActivity(player, ActivityType.FOUND_LOOT, "Encontró loot de calidad: " + quality, player.blockPosition());
    }

    public static void handleDamage(Player player, DamageSource source, float amount) {
        if (amount > 4.0f) {  // Solo registrar daño significativo
            String damageDesc = "Recibió daño considerable (" + String.format("%.1f", amount) + " puntos)";
            recordActivity(player, ActivityType.TOOK_DAMAGE, damageDesc, player.blockPosition());
        }
    }

    public static void handleVillagerTrade(Player player, Entity merchant) {
        if (merchant instanceof Villager) {
            recordActivity(player, ActivityType.TRADED_WITH_VILLAGER, "Comerció con un aldeano", player.blockPosition());
        }
    }

    public static void handleFishing(Player player) {
        recordActivity(player, ActivityType.FISHING, "Pescó algo", player.blockPosition());
    }

    public static void handleAdvancement(ServerPlayer player, String advancementName) {
        recordActivity(player, ActivityType.ACHIEVEMENT, "Logró el avance: " + advancementName, player.blockPosition());
    }

    // Generar un resumen narrativo de las actividades
    private static String generateSummary(Player player, List<Activity> activities) {
        StringBuilder summary = new StringBuilder("Upon waking");

        // Agrupar actividades por tipo y ordenarlas cronológicamente
        Map<ActivityType, List<Activity>> groupedActivities = activities.stream()
                .collect(Collectors.groupingBy(Activity::getType));

        // Crear una narrativa basada en la secuencia de actividades
        List<String> narrativeElements = new ArrayList<>();

        // Procesar cada tipo de actividad en orden cronológico
        activities.stream()
                .map(Activity::getType)
                .distinct()
                .forEach(type -> {
                    switch (type) {
                        case WOKE_UP:
                            // Ya incluido en el inicio
                            break;
                        case CHECKED_CHEST:
                            narrativeElements.add("I went to check my chests");
                            break;
                        case LEFT_HOME:
                            narrativeElements.add("I left home");
                            break;
                        case MINING:
                            narrativeElements.add("I was mining");
                            break;
                        case VISITED_VILLAGE:
                            narrativeElements.add("I went to a village");
                            break;
                        case FOUND_LOOT:
                            narrativeElements.add("I found new loot");
                            break;
                        case TOOK_DAMAGE:
                            narrativeElements.add("I almost died");
                            break;
                        case TRADED_WITH_VILLAGER:
                            narrativeElements.add("I traded with villagers");
                            break;
                        case ENTERED_HOME:
                            narrativeElements.add("I returned home");
                            break;
                        case FISHING:
                            narrativeElements.add("I went fishing for a while");
                            break;
                        case ACHIEVEMENT:
                            int achievementCount = groupedActivities.get(ActivityType.ACHIEVEMENT).size();
                            if (achievementCount == 1) {
                                narrativeElements.add("I earned an achievement");
                            } else {
                                narrativeElements.add("I earned " + achievementCount + " achievements");
                            }
                            break;
                    }
                });

        // Construir la narrativa final
        for (int i = 0; i < narrativeElements.size(); i++) {
            String connector;
            if (i == 0) {
                connector = " ";
            } else if (i == narrativeElements.size() - 1) {
                connector = " and finally ";
            } else {
                String[] connectors = {", then ", ", after that ", ", later "};
                connector = connectors[i % connectors.length];
            }
            summary.append(connector).append(narrativeElements.get(i));
        }

        summary.append(".");
        return summary.toString();
    }
}
