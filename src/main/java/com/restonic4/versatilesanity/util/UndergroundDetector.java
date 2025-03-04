package com.restonic4.versatilesanity.util;

import com.chaotic_loom.under_control.util.WeightedEvaluator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UndergroundDetector {

    // Configuración ajustable
    public static final int VERTICAL_CHECK_DISTANCE = 20;
    public static final int HORIZONTAL_CHECK_RADIUS = 15;
    public static final int MIN_BLOCKS_ABOVE = 3;
    public static final int SKY_LIGHT_THRESHOLD = 4;
    public static final float CAVE_DETECTION_THRESHOLD = 65f; // 65% de criterios cumplidos
    public static final int RING_SPACING = 5; // Bloques entre cada anillo
    public static final float RING_DECAY_FACTOR = 0.5f; // Factor de decrecimiento por anillo

    public static final int totalLoopTimes = (int) Math.pow((2 * UndergroundDetector.HORIZONTAL_CHECK_RADIUS + 1), 2);

    // Debug

    private static BlockPos[] lastCheckPoints;
    private static boolean[] lastCheckPointsResults;

    private static BlockPos[] lastCheckPointsAbove;
    private static boolean[] lastCheckPointsResultsAbove;

    /**
     * Método principal para determinar si un jugador está en una cueva/subterráneo
     */
    public static boolean isPlayerUnderground(Player player) {
        if (player == null) return false;

        WeightedEvaluator evaluator = new WeightedEvaluator();

        Level world = player.level();
        BlockPos playerPos = player.blockPosition();

        evaluator.addResult("solid blocks above", 1, hasEnoughSolidBlocksAbove(world, playerPos));
        evaluator.addResult("sky light", 1, hasLowSkyLight(world, playerPos));
        evaluator.addResult("bellow sea", 1, isBellowSea(world, playerPos.getY()));
        evaluator.addResult("cave formation", 1, isInCaveFormation(world, playerPos));

        evaluator.evaluateAll();

        return evaluator.isApproved(CAVE_DETECTION_THRESHOLD);
    }

    public static boolean isBellowSea(Level world, int height) {
        return world.dimension() == Level.OVERWORLD && height < 63;
    }

    /**
     * Verifica si hay suficientes bloques sólidos en línea recta hacia arriba
     */
    public static boolean hasEnoughSolidBlocksAbove(Level world, BlockPos pos) {
        int solidBlocks = 0;

        lastCheckPointsAbove = new BlockPos[VERTICAL_CHECK_DISTANCE];
        lastCheckPointsResultsAbove = new boolean[VERTICAL_CHECK_DISTANCE];

        BlockPos empty = new BlockPos(pos.getX(), world.getMinBuildHeight(), pos.getZ());
        Arrays.fill(lastCheckPointsAbove, empty);

        for (int y = 1; y <= VERTICAL_CHECK_DISTANCE; y++) {
            BlockPos checkPos = pos.above(y);
            BlockState state = world.getBlockState(checkPos);

            lastCheckPointsAbove[y - 1] = checkPos;
            lastCheckPointsResultsAbove[y - 1] = false;

            if (isSolidBlock(state)) {
                solidBlocks++;
                lastCheckPointsResultsAbove[y - 1] = true;

                if (solidBlocks >= MIN_BLOCKS_ABOVE) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Verifica si el nivel de luz del cielo es lo suficientemente bajo
     */
    public static boolean hasLowSkyLight(Level world, BlockPos pos) {
        int skyLight = world.getBrightness(LightLayer.SKY, pos);
        return !world.canSeeSky(pos) && skyLight <= SKY_LIGHT_THRESHOLD;
    }

    public static List<Integer> getRingsForCaveFormation() {
        List<Integer> rings = new ArrayList<>();
        for (int d = RING_SPACING; d <= HORIZONTAL_CHECK_RADIUS; d += RING_SPACING) {
            rings.add(d);
        }

        return rings;
    }

    public static int getTotalLoopTimesForCaveFormation(List<Integer> rings) {
        int total = 0;
        for (int d : rings) {
            int ringIndex = (d / RING_SPACING) - 1;
            float decayFactor = (float) Math.pow(RING_DECAY_FACTOR, ringIndex);
            int desiredPoints = (int) (8 * d * decayFactor);
            desiredPoints = Math.max(desiredPoints, 1);
            total += desiredPoints;
        }
        return total;
    }

    /**
     * Verifica si el espacio actual está rodeado por suficientes bloques sólidos para ser una cueva
     */
    public static boolean isInCaveFormation(Level world, BlockPos pos) {
        int solidBlockCount = 0;
        int totalCheckedBlocks = 0;

        List<Integer> rings = getRingsForCaveFormation();
        int totalLoopTimes = getTotalLoopTimesForCaveFormation(rings);

        lastCheckPoints = new BlockPos[totalLoopTimes];
        lastCheckPointsResults = new boolean[totalLoopTimes];
        int currentIndex = 0;

        BlockPos eyePos = pos.above(1);

        for (int ring : rings) {
            int perimeterPoints = 8 * ring;
            int ringIndex = (ring / RING_SPACING) - 1;
            float decayFactor = (float) Math.pow(RING_DECAY_FACTOR, ringIndex);
            int desiredPoints = (int) (perimeterPoints * decayFactor);
            desiredPoints = Math.max(desiredPoints, 1);

            // Calcular ángulo entre puntos
            double angleStep = 360.0 / desiredPoints;

            for (int i = 0; i < desiredPoints; i++) {
                double angle = Math.toRadians(i * angleStep);

                // Convertir coordenadas polares a cartesianas (redondeando al bloque más cercano)
                int x = (int) Math.round(ring * Math.cos(angle));
                int z = (int) Math.round(ring * Math.sin(angle));

                // Asegurar que estamos en el perímetro
                x = Math.max(-ring, Math.min(ring, x));
                z = Math.max(-ring, Math.min(ring, z));
                if (Math.abs(x) != ring && Math.abs(z) != ring) {
                    // Ajustar a perímetro cuadrado
                    if (Math.abs(x) > Math.abs(z)) x = x > 0 ? ring : -ring;
                    else z = z > 0 ? ring : -ring;
                }

                BlockPos checkPos = eyePos.offset(x, 0, z);
                BlockState state = world.getBlockState(checkPos);

                // Registrar punto
                lastCheckPoints[currentIndex] = checkPos;
                lastCheckPointsResults[currentIndex] = false;

                if (isSolidBlock(state)) {
                    solidBlockCount++;
                    lastCheckPointsResults[currentIndex] = true;
                }

                totalCheckedBlocks++;
                currentIndex++;
            }
        }

        return totalCheckedBlocks > 0 && (float) solidBlockCount / totalCheckedBlocks >= 0.5f;
    }

    /**
     * Determina si un bloque es considerado "sólido" para nuestro propósito
     */
    private static boolean isSolidBlock(BlockState state) {
        // Ignoramos bloques como aire, agua, lava, vegetación, etc.
        if (state.isAir()) return false;
        if (state.liquid()) return false;
        if (!state.blocksMotion()) return false;

        // Lista de bloques específicos para ignorar (pueden ser transparentes pero bloquear movimiento)
        if (state.is(Blocks.GLASS) ||
                state.is(Blocks.GLASS_PANE) ||
                state.is(Blocks.ICE) ||
                state.is(Blocks.BARRIER) ||
                state.is(Blocks.COBWEB) ||
                state.is(Blocks.IRON_BARS) ||
                state.is(Blocks.CHAIN)) {
            return false;
        }

        return true;
    }

    public static BlockPos[] getLastCheckPoints() {
        return lastCheckPoints;
    }

    public static boolean[] getLastCheckPointsResults() {
        return lastCheckPointsResults;
    }

    public static BlockPos[] getLastCheckPointsAbove() {
        return lastCheckPointsAbove;
    }

    public static boolean[] getLastCheckPointsResultsAbove() {
        return lastCheckPointsResultsAbove;
    }
}