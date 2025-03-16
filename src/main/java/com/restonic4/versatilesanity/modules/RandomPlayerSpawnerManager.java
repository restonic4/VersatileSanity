package com.restonic4.versatilesanity.modules;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.border.WorldBorder;

import java.util.Random;

public class RandomPlayerSpawnerManager {
    public static final int MAX_TRIES = 100;

    public static final double MIN_TELEPORT_RADIUS = 10000;
    public static final double MAX_TELEPORT_RADIUS = 50000;

    public static void forceSpawnRandomly(ServerPlayer player) {
        boolean couldSpawn = false;
        int currentTry = 0;

        while (!couldSpawn && currentTry < MAX_TRIES) {
            currentTry++;
            couldSpawn = spawnRandomly(player);
        }

        if (currentTry >= MAX_TRIES) {
            System.out.println("Max tries reached for " + player.getName());
        }
    }

    public static boolean spawnRandomly(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        WorldBorder worldBorder = level.getWorldBorder();
        Random random = new Random();

        double borderSize = worldBorder.getSize() / 2.0;
        BlockPos center = new BlockPos((int) worldBorder.getCenterX(), 0, (int) worldBorder.getCenterZ());

        int x, z;
        // Si el worldborder es menor que el área mínima deseada, se ignoran las restricciones.
        if (borderSize < MIN_TELEPORT_RADIUS) {
            x = center.getX() + random.nextInt((int)(borderSize * 2)) - (int) borderSize;
            z = center.getZ() + random.nextInt((int)(borderSize * 2)) - (int) borderSize;
        } else {
            // Se asegura que el radio máximo no exceda el límite del worldborder.
            double validMax = Math.min(borderSize, MAX_TELEPORT_RADIUS);
            // Se genera un ángulo al azar
            double angle = random.nextDouble() * 2 * Math.PI;
            // Se genera una distancia aleatoria en el rango [MIN_TELEPORT_RADIUS, validMax]
            double distance = MIN_TELEPORT_RADIUS + random.nextDouble() * (validMax - MIN_TELEPORT_RADIUS);
            x = center.getX() + (int) Math.round(distance * Math.cos(angle));
            z = center.getZ() + (int) Math.round(distance * Math.sin(angle));
        }

        int y = findValidY(level, x, z);

        if (y != level.getMinBuildHeight()) {
            BlockPos spawnPos = new BlockPos(x, y, z);
            teleport(player, spawnPos);
            return true;
        }

        System.out.println("Could not spawn at " + x + ", ?, " + z);
        return false;
    }

    private static void teleport(ServerPlayer player, BlockPos spawnPos) {
        player.teleportTo(player.serverLevel(), spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    private static int findValidY(Level level, int x, int z) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, level.getMaxBuildHeight(), z);

        while (mutablePos.getY() > level.getMinBuildHeight()) {
            mutablePos.move(0, -1, 0);

            if (isEmpty(level, mutablePos) && isEmpty(level, mutablePos.above()) &&
                    level.getBlockState(mutablePos.below()).isSolidRender(level, mutablePos.below())) {
                // Por ejemplo, se fuerza que el nivel Y sea mayor o igual a 60, sino se rechaza.
                return mutablePos.getY() >= 60 ? mutablePos.getY() : level.getMinBuildHeight();
            }
        }

        return level.getMinBuildHeight();
    }

    private static boolean isEmpty(Level level, BlockPos blockPos) {
        return (level.getBlockState(blockPos).isAir() ||
                !level.getBlockState(blockPos).isSolidRender(level, blockPos))
                && !(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock);
    }
}
