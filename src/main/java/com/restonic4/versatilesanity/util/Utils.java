package com.restonic4.versatilesanity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.Structures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
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

    public static boolean isPlayerNearHostileEntity(ServerPlayer player, double radius, double ignoreVisionRadius) {
        Level level = player.level();

        // Radio externo - todos los hostiles en este rango (con comprobación de visión)
        AABB boxOuter = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        // Radio interno - los hostiles en este rango se detectan siempre (sin comprobación de visión)
        AABB boxInner = new AABB(
                player.getX() - ignoreVisionRadius, player.getY() - ignoreVisionRadius, player.getZ() - ignoreVisionRadius,
                player.getX() + ignoreVisionRadius, player.getY() + ignoreVisionRadius, player.getZ() + ignoreVisionRadius
        );

        // Obtener todas las entidades en el radio máximo
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, boxOuter, e -> true);

        // Posición de los ojos del jugador para el cálculo de visión
        Vec3 eyePos = player.getEyePosition();

        for (Entity entity : entities) {
            if (isHostile(entity)) {
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

    private static boolean isHostile(Entity entity) {
        if (entity instanceof Enemy) {
            return true;
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
}
