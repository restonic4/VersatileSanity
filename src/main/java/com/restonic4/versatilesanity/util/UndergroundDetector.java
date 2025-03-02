package com.restonic4.versatilesanity.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class UndergroundDetector {

    // Configuración ajustable
    private static final int VERTICAL_CHECK_DISTANCE = 20;
    private static final int HORIZONTAL_CHECK_RADIUS = 5;
    private static final int MIN_BLOCKS_ABOVE = 3;
    private static final int SKY_LIGHT_THRESHOLD = 4;
    private static final float CAVE_DETECTION_THRESHOLD = 0.65f; // 65% de criterios cumplidos

    /**
     * Método principal para determinar si un jugador está en una cueva/subterráneo
     */
    public static boolean isPlayerUnderground(Player player) {
        if (player == null) return false;

        Level world = player.level();
        BlockPos playerPos = player.blockPosition();

        // Contador de criterios cumplidos
        int criteriosCumplidos = 0;
        int criteriosTotales = 4;

        // Criterio 1: Verificar bloques sólidos arriba del jugador
        if (hasEnoughSolidBlocksAbove(world, playerPos)) {
            criteriosCumplidos++;
        }

        // Criterio 2: Verificar nivel de luz del cielo
        if (hasLowSkyLight(world, playerPos)) {
            criteriosCumplidos++;
        }

        // Criterio 3: Verificar si estamos bajo el nivel del mar (Y < 63 en la mayoría de dimensiones)
        if (world.dimension() == Level.OVERWORLD && playerPos.getY() < 63) {
            criteriosCumplidos++;
        }

        // Criterio 4: Verificar espacio abierto rodeado de bloques sólidos (característica de cueva)
        if (isInCaveFormation(world, playerPos)) {
            criteriosCumplidos++;
        }

        // Calcular proporción de criterios cumplidos
        float proporcionCumplida = (float) criteriosCumplidos / criteriosTotales;

        System.out.println(proporcionCumplida + " %");

        return proporcionCumplida >= CAVE_DETECTION_THRESHOLD;
    }

    /**
     * Verifica si hay suficientes bloques sólidos en línea recta hacia arriba
     */
    private static boolean hasEnoughSolidBlocksAbove(Level world, BlockPos pos) {
        int solidBlocks = 0;

        for (int y = 1; y <= VERTICAL_CHECK_DISTANCE; y++) {
            BlockPos checkPos = pos.above(y);
            BlockState state = world.getBlockState(checkPos);

            if (isSolidBlock(state)) {
                solidBlocks++;
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
    private static boolean hasLowSkyLight(Level world, BlockPos pos) {
        int skyLight = world.getBrightness(LightLayer.SKY, pos);
        return !world.canSeeSky(pos) && skyLight <= SKY_LIGHT_THRESHOLD;
    }

    /**
     * Verifica si el espacio actual está rodeado por suficientes bloques sólidos para ser una cueva
     */
    private static boolean isInCaveFormation(Level world, BlockPos pos) {
        int solidBlockCount = 0;
        int totalCheckedBlocks = 0;

        // Revisamos en un radio horizontal, principalmente a la altura de los ojos
        BlockPos eyePos = pos.above(1);

        for (int x = -HORIZONTAL_CHECK_RADIUS; x <= HORIZONTAL_CHECK_RADIUS; x++) {
            for (int z = -HORIZONTAL_CHECK_RADIUS; z <= HORIZONTAL_CHECK_RADIUS; z++) {
                // Solo checkear el perímetro y no el centro
                if (Math.abs(x) >= HORIZONTAL_CHECK_RADIUS - 1 || Math.abs(z) >= HORIZONTAL_CHECK_RADIUS - 1) {
                    BlockPos checkPos = eyePos.offset(x, 0, z);
                    BlockState state = world.getBlockState(checkPos);

                    if (isSolidBlock(state)) {
                        solidBlockCount++;
                    }

                    totalCheckedBlocks++;
                }
            }
        }

        // Si al menos 50% de los bloques del perímetro son sólidos, probablemente estamos en una cueva
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
}