package com.restonic4.versatilesanity.util;

import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.modules.SanityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Utils {
    public static int getLightLevel(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        // Obtener la luz de bloques (antorchas, lámparas, etc.)
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);

        // Obtener la luz del cielo sin modificar
        int rawSkyLight = level.getBrightness(LightLayer.SKY, pos);

        // Si no hay acceso al cielo, no hay luz del cielo
        if (rawSkyLight <= 0 || level.isNight()) {
            return blockLight;
        }

        return Math.max(blockLight, rawSkyLight);
    }

    public static boolean isPlayerNearEntity(ServerPlayer player, double radius, double ignoreVisionRadius, Predicate<Entity> filter) {
        Level level = player.level();

        // Radio externo - todos los hostiles en este rango (con comprobación de visión)
        AABB boxOuter = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        // Obtener todas las entidades en el radio máximo
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, boxOuter, e -> true);

        // Posición de los ojos del jugador para el cálculo de visión
        Vec3 eyePos = player.getEyePosition();

        for (Entity entity : entities) {
            if (!entity.equals(player) && filter.test(entity)) {
                // Calcular la distancia al cuadrado (más eficiente que raíz cuadrada)
                double distanceSq = entity.distanceToSqr(player);

                // Si está dentro del radio interno, detectarlo siempre
                if (distanceSq <= ignoreVisionRadius * ignoreVisionRadius) {
                    return true;
                }

                // Para entidades en el radio externo, comprobar visión
                // Calculamos el punto central de la entidad para raytracing
                Vec3 entityCenter = entity.getBoundingBox().getCenter();

                // Crear un rayo desde los ojos del jugador hasta el centro de la entidad
                ClipContext clipContext = new ClipContext(
                        eyePos,
                        entityCenter,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                );

                // Realizar el raytracing
                BlockHitResult hitResult = level.clip(clipContext);

                // Si el rayo llega hasta la entidad sin chocar con bloques, está en visión
                if (hitResult.getType() == HitResult.Type.MISS ||
                        hitResult.getLocation().distanceToSqr(entityCenter) < 0.25) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isPlayerNearHostileEntity(ServerPlayer player, double radius, double ignoreVisionRadius) {
        return isPlayerNearEntity(player, radius, ignoreVisionRadius, Utils::isHostile);
    }

    private static boolean isHostile(Entity entity) {
        if (entity instanceof Enemy) {
            return true;
        }

        // Add custom compatibility if needed
        return false;
    }

    public static boolean isPlayerNearPlayerOrVillagerEntity(ServerPlayer player, double radius, double ignoreVisionRadius) {
        return isPlayerNearEntity(player, radius, ignoreVisionRadius, Utils::isPlayerOrVillager);
    }

    private static boolean isPlayerOrVillager(Entity entity) {
        if (entity instanceof Player || entity instanceof Villager || entity instanceof WanderingTrader) {
            return true;
        }

        // Add custom compatibility if needed
        return false;
    }

    public static boolean isPlayerNearBossEntity(ServerPlayer player, double radius, double ignoreVisionRadius) {
        return isPlayerNearEntity(player, radius, ignoreVisionRadius, Utils::isBoss);
    }

    private static boolean isBoss(Entity entity) {
        if (entity instanceof EnderDragon || entity instanceof WitherBoss || entity instanceof Warden) {
            return true;
        }

        // Add custom compatibility if needed
        return false;
    }

    public static boolean isPlayerNearPetEntity(ServerPlayer player, double radius, double ignoreVisionRadius) {
        return isPlayerNearEntity(player, radius, ignoreVisionRadius, Utils::isPet);
    }

    private static boolean isPet(Entity entity) {
        if (entity instanceof TamableAnimal tamable) {
            return tamable.isTame();
        }

        else if (entity instanceof AbstractHorse horse) {
            return horse.isTamed();
        }

        // Add custom compatibility if needed
        return false;
    }

    public static boolean isPlayerInOrNearVillage(ServerPlayer player, double radius) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        // Check if the player is on a village
        boolean isInVillage = serverLevel.structureManager().getStructureWithPieceAt(
                playerPos,
                StructureTags.VILLAGE
        ).isValid();

        if (isInVillage) {
            return true;
        }

        // Check if the player is near a village
        if (radius > 0) {
            int searchRadius = (int) Math.ceil(radius);

            for (int x = -searchRadius; x <= searchRadius; x++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    if (x*x + z*z > radius*radius) {
                        continue;
                    }

                    BlockPos checkPos = playerPos.offset(x, 0, z);

                    if (serverLevel.structureManager().getStructureWithPieceAt(
                            checkPos,
                            StructureTags.VILLAGE
                    ).isValid()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isPlayerNearPortal(ServerPlayer player, double radius) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // Radio de búsqueda (redondeado hacia arriba)
        int searchRadius = (int) Math.ceil(radius);

        // Comparamos el cuadrado de las distancias para eficiencia
        double radiusSq = radius * radius;

        // Buscar en un área cúbica alrededor del jugador
        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-searchRadius, -searchRadius, -searchRadius),
                playerPos.offset(searchRadius, searchRadius, searchRadius))) {

            // Verificar distancia euclidiana
            double distSq = pos.distSqr(playerPos);
            if (distSq > radiusSq) {
                continue;
            }

            Block block = level.getBlockState(pos).getBlock();
            boolean isPortal = isPortal(block);

            if (isPortal) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPortal(Block block) {
        // Verificar portales vanilla
        if (block instanceof NetherPortalBlock ||
                block instanceof EndPortalBlock ||
                block instanceof EndGatewayBlock) {
            return true;
        }

        // Compatibilidad con portales de mods
        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
        if (blockId.contains("portal") ||
                block.getClass().getName().toLowerCase().contains("portal")) {
            return true;
        }

        return false;
    }

    public static float calculateHungerValue(Player player) {
        float threshold = VersatileSanity.getConfig().getSatietyThreshold();

        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();

        float currentHungerPercentage = foodLevel / 20.0f;

        if (currentHungerPercentage < threshold) {
            return 0.0f;
        }

        return (currentHungerPercentage - threshold) / (1.0f - threshold);
    }

    public static boolean isOnLootTable(ItemStack stack, ResourceLocation lootTableId, MinecraftServer server) {
        LootTable lootTable = server.getLootData().getLootTable(lootTableId);

        for (LootPool pool : lootTable.pools) {
            for (LootPoolEntryContainer entry : pool.entries) {
                if (entry instanceof LootItem lootItem) {
                    if (lootItem.item == stack.getItem()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasParrotsOnShoulders(Player player) {
        CompoundTag leftShoulder = player.getShoulderEntityLeft();
        CompoundTag rightShoulder = player.getShoulderEntityRight();
        return isParrotTag(leftShoulder) || isParrotTag(rightShoulder);
    }

    private static boolean isParrotTag(CompoundTag tag) {
        return tag != null && !tag.isEmpty() && "minecraft:parrot".equals(tag.getString("id"));
    }

    public static boolean[] isNearPlayingJukebox(Player player, int radius) {
        BlockPos playerPos = player.blockPosition();

        boolean[] result = new boolean[] { false, false };

        BlockPos.betweenClosedStream(
                playerPos.offset(-radius, -radius, -radius),
                playerPos.offset(radius, radius, radius)
        ).forEach(pos -> {
            BlockState state = player.level().getBlockState(pos);

            if (state.getBlock() == Blocks.JUKEBOX) {
                BlockEntity blockEntity = player.level().getBlockEntity(pos);

                if (blockEntity instanceof JukeboxBlockEntity jukebox && jukebox.isRecordPlaying()) {
                    ItemStack discStack = jukebox.getFirstItem();

                    if (!discStack.isEmpty() && discStack.getItem() instanceof RecordItem recordItem) {
                        ResourceLocation discId = BuiltInRegistries.ITEM.getKey(recordItem);

                        if (isMusicDiscBadForSanity(discId.toString())) {
                            result[0] = true;
                            result[1] = true;
                        } else {
                            result[0] = true;
                            result[1] = false;
                        }
                    }
                }
            }
        });

        return result;
    }

    public static boolean isMusicDiscBadForSanity(String discId) {
        if ("minecraft:music_disc_13".equals(discId.toString())) {
            return true;
        } else if("minecraft:music_disc_11".equals(discId.toString())) {
            return true;
        } else if("minecraft:music_disc_5".equals(discId.toString())) {
            return true;
        }

        return false;
    }
}
