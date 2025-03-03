package com.restonic4.versatilesanity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class WaterMassDetector {
    public static final int MIN_DISTANCE_FROM_SHORE = 15;
    public static final int MIN_DEPTH = 8;
    public static final int WATER_CHECK_SAMPLES = 8;
    public static final double MIN_WATER_PERCENTAGE = 0.8;

    private static BlockPos[] lastCheckedBlocks;

    /**
     * Comprueba si un jugador está en un océano profundo y a qué profundidad.
     *
     * @param player El jugador a comprobar
     * @return Un objeto WaterMassDetectionResult con los resultados
     */
    public static WaterMassDetectionResult isInDeepOcean(ServerPlayer player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // Comprobar si el bioma es un océano o un lago grande
        boolean isWaterBiome = isInWaterBiome(player);

        if (!isWaterBiome) {
            return new WaterMassDetectionResult(false, false);
        }

        // Comprobar si hay suficiente agua alrededor para considerarlo océano profundo
        if (!hasEnoughWaterAround(level, playerPos)) {
            return new WaterMassDetectionResult(false, false);
        }

        // Calcular la profundidad total en este punto
        int totalDepth = calculateWaterDepth(level, playerPos);

        // Si la profundidad es menor que el mínimo, no es un océano profundo
        if (totalDepth < MIN_DEPTH) {
            return new WaterMassDetectionResult(false, false);
        }

        // Comprobar si el jugador está sumergido más del 25% de la profundidad total
        boolean isSubmergedEnough = isPlayerSubmergedEnough(player, totalDepth);

        return new WaterMassDetectionResult(true, isSubmergedEnough);
    }

    /**
     * Comprueba si el jugador está en un bioma de agua (océano o río grande).
     */
    public static boolean isInWaterBiome(ServerPlayer player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();

        ResourceLocation biomeId = level.getBiome(pos).unwrapKey().map(ResourceKey::location).orElse(null);

        if (biomeId == null) {
            return false;
        }

        // Comprobar tipos de biomas de océano
        return biomeId.getPath().contains("ocean") ||
                biomeId.getPath().contains("river") ||
                biomeId.getPath().contains("lake");
    }

    /**
     * Comprueba si hay suficiente agua alrededor del jugador para considerarlo océano profundo.
     */
    public static boolean hasEnoughWaterAround(Level level, BlockPos pos) {
        int waterBlocks = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        lastCheckedBlocks = new BlockPos[WATER_CHECK_SAMPLES];

        // Usar muestreo radial en lugar de cuadricular para reducir comprobaciones
        for (int i = 0; i < WATER_CHECK_SAMPLES; i++) {
            // Calcular posiciones en un patrón radial
            double angle = (2 * Math.PI * i) / WATER_CHECK_SAMPLES;
            int checkX = pos.getX() + (int) (MIN_DISTANCE_FROM_SHORE * Math.cos(angle));
            int checkZ = pos.getZ() + (int) (MIN_DISTANCE_FROM_SHORE * Math.sin(angle));

            // Configurar posición mutable para evitar crear nuevos objetos BlockPos
            mutablePos.set(checkX, pos.getY(), checkZ);
            lastCheckedBlocks[i] = new BlockPos(checkX, pos.getY(), checkZ);

            // Obtener la altura de la columna en esta posición
            BlockPos waterPos = findWaterSurface(level, mutablePos);
            if (waterPos == null) {
                continue;
            }

            int y = waterPos.getY();

            if (y != -1) {
                mutablePos.setY(y);
                if (level.getBlockState(mutablePos).getBlock() == Blocks.WATER) {
                    waterBlocks++;
                }
            }
        }

        // Considerar océano profundo si suficientes puntos muestreados son agua
        return (double) waterBlocks / WATER_CHECK_SAMPLES >= MIN_WATER_PERCENTAGE;
    }

    /**
     * Encuentra la superficie del agua más alta en la columna del bloque dado.
     */
    public static BlockPos findWaterSurface(Level level, BlockPos pos) {
        BlockPos current = pos;

        // Buscar hacia arriba si el bloque actual no es agua
        if (!level.getBlockState(current).is(Blocks.WATER)) {
            current = current.above();
            while (current.getY() < level.getMaxBuildHeight() && !level.getBlockState(current).is(Blocks.WATER)) {
                current = current.above();
            }
            if (!level.getBlockState(current).is(Blocks.WATER)) {
                return null; // No se encontró agua
            }
        }

        // Subir hasta el bloque de agua más alto
        while (current.getY() < level.getMaxBuildHeight() && level.getBlockState(current.above()).is(Blocks.WATER)) {
            current = current.above();
        }

        return current;
    }

    /**
     * Calcula la profundidad total del agua en la posición actual.
     */
    public static int calculateWaterDepth(Level level, BlockPos pos) {
        BlockPos waterSurface = findWaterSurface(level, pos);
        if (waterSurface == null) {
            return 0;
        }

        // Encontrar el fondo del agua
        BlockPos waterBottom = waterSurface;
        while (waterBottom.getY() > level.getMinBuildHeight() && isFluid(level, waterBottom.below())) {
            waterBottom = waterBottom.below();
        }

        return waterSurface.getY() - waterBottom.getY() + 1; // +1 para incluir ambos extremos
    }

    private static boolean isFluid(Level level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        boolean isFluid = fluidState.is(FluidTags.WATER); // Detects any king of fluid

        if (isFluid) {
            return true;
        }

        BlockState blockState = level.getBlockState(pos);

        boolean isAir = blockState.getBlock() instanceof AirBlock;
        if (isAir) {
            return false;
        }

        return !blockState.isSolidRender(level, pos);
    }

    /**
     * Comprueba si el jugador está sumergido más del 15% en el agua.
     */
    public static boolean isPlayerSubmergedEnough(ServerPlayer player, int totalDepth) {
        if (totalDepth <= 0) {
            return false;
        }

        BlockPos pos = player.blockPosition();
        Level level = player.level();

        BlockPos waterSurface = findWaterSurface(level, pos);
        if (waterSurface == null) {
            return false;
        }

        // Verificar si el bloque sobre la superficie es sólido y no aire
        BlockPos surfaceAbove = waterSurface.above();
        BlockState aboveState = level.getBlockState(surfaceAbove);
        boolean isCovered = !aboveState.isAir() && aboveState.isSolid();

        // Calcular la posición de los ojos del jugador y la superficie del agua
        double eyeY = player.getY() + player.getEyeHeight();
        double waterSurfaceTop = waterSurface.getY() + 1; // La parte superior del bloque de agua

        // Profundidad del jugador desde la superficie del agua
        double playerDepth = waterSurfaceTop - eyeY;

        if (isCovered) {
            return playerDepth > 0;
        } else {
            return playerDepth > (totalDepth * 0.15);
        }
    }

    public static BlockPos[] getLastCheckedBlocks() {
        return lastCheckedBlocks;
    }

    public record WaterMassDetectionResult(boolean isInDeepOcean, boolean isSubmergedEnough) {
    }
}
