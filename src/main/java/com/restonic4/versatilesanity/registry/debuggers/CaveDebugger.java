package com.restonic4.versatilesanity.registry.debuggers;

import com.chaotic_loom.under_control.client.rendering.effects.Cube;
import com.chaotic_loom.under_control.client.rendering.effects.EffectManager;
import com.chaotic_loom.under_control.client.rendering.effects.Sphere;
import com.chaotic_loom.under_control.core.annotations.ExecutionSide;
import com.chaotic_loom.under_control.debug.TickingDebugger;
import com.chaotic_loom.under_control.util.MathHelper;
import com.chaotic_loom.under_control.util.data_holders.RenderingFlags;
import com.restonic4.versatilesanity.util.UndergroundDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.awt.*;

public class CaveDebugger extends TickingDebugger {
    private static final Sphere[] caveFormationCheckPoints = new Sphere[UndergroundDetector.totalLoopTimes];
    private static Sphere[] aboveCheckPoints;
    private static final Sphere statusSphere = new Sphere("debug_cave_" + MathHelper.getUniqueID());
    private static final Cube skyCube = new Cube("debug_cave_" + MathHelper.getUniqueID());
    private static final Sphere seaSphere = new Sphere("debug_cave_" + MathHelper.getUniqueID());

    private static final Color onWallsColor = new Color(0x00FF00);
    private static final Color onWallsColorButFalse = new Color(0xFFD900);
    private static final Color wrongColor = new Color(0xFF0000);

    static {
        statusSphere.setRenderingFlags(RenderingFlags.ON_TOP);
        statusSphere.setScale(0.5f, 0.5f, 0.5f);

        skyCube.setRenderingFlags(RenderingFlags.ON_TOP);
        skyCube.setScale(0.15f, 0.15f, 0.15f);

        seaSphere.setRenderingFlags(RenderingFlags.ON_TOP);
        seaSphere.setScale(0.15f, 0.15f, 0.15f);
    }

    public CaveDebugger(ExecutionSide executionSide) {
        super(executionSide);
    }

    @Override
    public void onTick() {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        Player player = minecraft.player;

        if (level == null || player == null) {
            return;
        }

        boolean isPlayerUnderground = UndergroundDetector.isPlayerUnderground(player);
        boolean inCaveFormation = UndergroundDetector.isInCaveFormation(level, player.blockPosition());
        boolean hasBlocksAbove = UndergroundDetector.hasEnoughSolidBlocksAbove(level, player.blockPosition());
        boolean hasSkyLight = UndergroundDetector.hasLowSkyLight(level, player.blockPosition());
        boolean isBellowSea = UndergroundDetector.isBellowSea(level, player.blockPosition().getY());

        BlockPos[] debugPosAround = UndergroundDetector.getLastCheckPoints();
        boolean[] debugPosAroundResults = UndergroundDetector.getLastCheckPointsResults();

        for (int i = 0; i < debugPosAround.length; i++) {
            BlockPos pos = debugPosAround[i];

            if (pos == null) {
                continue;
            }

            caveFormationCheckPoints[i].setPosition(pos.getX(), pos.getY(), pos.getZ());

            if (debugPosAroundResults[i]) {
                if (inCaveFormation) {
                    caveFormationCheckPoints[i].setColor(onWallsColor);
                } else {
                    caveFormationCheckPoints[i].setColor(onWallsColorButFalse);
                }

                caveFormationCheckPoints[i].setRenderingFlags(RenderingFlags.ON_TOP);
            } else {
                caveFormationCheckPoints[i].setColor(wrongColor);
                caveFormationCheckPoints[i].setRenderingFlags(0);
            }
        }

        BlockPos[] debugPosAbove = UndergroundDetector.getLastCheckPointsAbove();
        boolean[] debugPosAboveResults = UndergroundDetector.getLastCheckPointsResultsAbove();

        for (int i = 0; i < debugPosAbove.length; i++) {
            BlockPos pos = debugPosAbove[i];

            if (pos == null) {
                continue;
            }

            aboveCheckPoints[i].setPosition(pos.getX(), pos.getY(), pos.getZ());

            if (debugPosAboveResults[i]) {
                if (hasBlocksAbove) {
                    aboveCheckPoints[i].setColor(onWallsColor);
                } else {
                    aboveCheckPoints[i].setColor(onWallsColorButFalse);
                }

                aboveCheckPoints[i].setRenderingFlags(RenderingFlags.ON_TOP);
            } else {
                aboveCheckPoints[i].setColor(wrongColor);
                aboveCheckPoints[i].setRenderingFlags(0);
            }
        }

        statusSphere.setPosition((float) player.position().x(), (float) (player.position().y() - 1), (float) player.position().z());
        skyCube.setPosition((float) player.position().x() - 1, (float) (player.position().y() - 1), (float) player.position().z());
        seaSphere.setPosition((float) player.position().x() + 1, (float) (player.position().y() - 1), (float) player.position().z());

        if (isPlayerUnderground) {
            statusSphere.setColor(onWallsColor);
        } else {
            statusSphere.setColor(wrongColor);
        }

        if (hasSkyLight) {
            skyCube.setColor(onWallsColor);
        } else {
            skyCube.setColor(wrongColor);
        }

        if (isBellowSea) {
            seaSphere.setColor(onWallsColor);
        } else {
            seaSphere.setColor(wrongColor);
        }
    }

    @Override
    public void onInitialize() {
        for (int i = 0; i < UndergroundDetector.totalLoopTimes; i++) {
            Sphere sphere = new Sphere("debug_cave_" + MathHelper.getUniqueID());
            sphere.setScale(0.25f, 0.25f, 0.25f);
            caveFormationCheckPoints[i] = sphere;
        }

        aboveCheckPoints = new Sphere[UndergroundDetector.getTotalLoopTimesForCaveFormation(UndergroundDetector.getRingsForCaveFormation())];

        for (int i = 0; i < aboveCheckPoints.length; i++) {
            Sphere sphere = new Sphere("debug_cave_" + MathHelper.getUniqueID());
            sphere.setScale(0.25f, 0.25f, 0.25f);
            aboveCheckPoints[i] = sphere;
        }
    }

    @Override
    public void onStart() {
        for (Sphere sphere : caveFormationCheckPoints) {
            EffectManager.add(sphere);
        }

        for (Sphere sphere : aboveCheckPoints) {
            EffectManager.add(sphere);
        }

        EffectManager.add(statusSphere);
        EffectManager.add(skyCube);
        EffectManager.add(seaSphere);
    }

    @Override
    public void onStop() {
        for (Sphere sphere : caveFormationCheckPoints) {
            EffectManager.delete(sphere.getId());
        }

        for (Sphere sphere : aboveCheckPoints) {
            EffectManager.delete(sphere.getId());
        }

        EffectManager.delete(statusSphere.getId());
        EffectManager.delete(skyCube.getId());
        EffectManager.delete(seaSphere.getId());
    }
}
