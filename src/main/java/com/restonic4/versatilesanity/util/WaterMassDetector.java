package com.restonic4.versatilesanity.util;

import com.chaotic_loom.under_control.util.WeightedEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class WaterMassDetector {
    public static final int MIN_DISTANCE_FROM_SHORE = 15;
    public static final int MIN_DEPTH = 8;
    public static final int WATER_CHECK_SAMPLES = 14;
    public static final double MIN_WATER_PERCENTAGE = 0.8;
    public static final double WATER_SUBMERGED_PERCENTAGE = 0.15;
    public static final double APPROVAL_THRESHOLD = 75;

    //Debug
    private static BlockPos[] lastCheckedBlocks;
    private static boolean[] lastCheckedBlocksResults;

    private static int lastSubmersionHeight = 0;

    /**
     * Comprueba si un jugador está en un océano profundo y a qué profundidad.
     *
     * @param player El jugador a comprobar
     * @return Un objeto WaterMassDetectionResult con los resultados
     */
    public static WaterMassDetectionResult isInDeepOcean(Player player) {
        WeightedEvaluator evaluator = new WeightedEvaluator();

        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        int totalDepth = calculateWaterDepth(level, playerPos);

        boolean isInWater = player.isInWater();
        boolean isWaterBiome = isInWaterBiome(player);
        boolean hasEnoughWaterAround = hasEnoughWaterAround(level, playerPos);
        boolean requiredDepth = totalDepth >= MIN_DEPTH;

        evaluator.addResult("in water", 2, isInWater);
        evaluator.addResult("biome", 2, isWaterBiome);
        evaluator.addResult("water around", 1, hasEnoughWaterAround);
        evaluator.addResult("required depth", 3, requiredDepth);

        evaluator.evaluateAll();

        boolean isInDeepOcean = evaluator.isApproved(APPROVAL_THRESHOLD);
        boolean isSubmergedEnough = isPlayerSubmergedEnough(player, totalDepth);

        return new WaterMassDetectionResult(isInDeepOcean, isSubmergedEnough, evaluator.getApprovalPercentage());
    }

    /**
     * Comprueba si el jugador está en un bioma de agua (océano o río grande).
     */
    public static boolean isInWaterBiome(Player player) {
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
        lastCheckedBlocksResults = new boolean[WATER_CHECK_SAMPLES];

        // Usar muestreo radial en lugar de cuadricular para reducir comprobaciones
        for (int i = 0; i < WATER_CHECK_SAMPLES; i++) {
            // Calcular posiciones en un patrón radial
            double angle = (2 * Math.PI * i) / WATER_CHECK_SAMPLES;
            int checkX = pos.getX() + (int) (MIN_DISTANCE_FROM_SHORE * Math.cos(angle));
            int checkZ = pos.getZ() + (int) (MIN_DISTANCE_FROM_SHORE * Math.sin(angle));

            // Configurar posición mutable para evitar crear nuevos objetos BlockPos
            mutablePos.set(checkX, pos.getY(), checkZ);

            lastCheckedBlocks[i] = new BlockPos(checkX, pos.getY(), checkZ);
            lastCheckedBlocksResults[i] = false;

            // Obtener la altura de la columna en esta posición
            BlockPos waterSurface = findWaterSurface(level, mutablePos);
            if (waterSurface == null) {
                continue;
            }

            BlockPos waterBottom = findWaterBottom(level, waterSurface);
            // Verificar si la Y del jugador está dentro de la columna de agua
            if (pos.getY() < waterBottom.getY() || pos.getY() > waterSurface.getY()) {
                continue;
            }

            int y = waterSurface.getY();

            if (y != -1) {
                mutablePos.setY(y);
                if (level.getBlockState(mutablePos).getBlock() == Blocks.WATER) {
                    waterBlocks++;
                    lastCheckedBlocksResults[i] = true;
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

    public static BlockPos findWaterBottom(Level level, BlockPos waterSurface) {
        BlockPos waterBottom = waterSurface;
        while (waterBottom.getY() > level.getMinBuildHeight() && isFluid(level, waterBottom.below())) {
            waterBottom = waterBottom.below();
        }
        return waterBottom;
    }

    /**
     * Comprueba si el jugador está sumergido más del X% en el agua.
     */
    public static boolean isPlayerSubmergedEnough(Player player, int totalDepth) {
        BlockPos pos = player.blockPosition();
        Level level = player.level();

        if (!player.isInWater()) {
            return false;
        }

        if (totalDepth <= 0) {
            return false;
        }

        BlockPos waterSurface = findWaterSurface(level, pos);
        if (waterSurface == null) {
            lastSubmersionHeight = level.getMinBuildHeight();
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
        double minimumHeight = (totalDepth * WATER_SUBMERGED_PERCENTAGE);

        lastSubmersionHeight = (int) (waterSurfaceTop - minimumHeight);

        if (isCovered) {
            return playerDepth > 0;
        } else {
            return playerDepth > minimumHeight;
        }
    }

    public static BlockPos[] getLastCheckedBlocks() {
        return lastCheckedBlocks;
    }

    public static boolean[] getLastCheckedBlocksResults() {
        return lastCheckedBlocksResults;
    }

    public static int getLastSubmersionHeight() {
        return lastSubmersionHeight;
    }

    public record WaterMassDetectionResult(boolean isInDeepOcean, boolean isSubmergedEnough, double threshold) {
    }
}
