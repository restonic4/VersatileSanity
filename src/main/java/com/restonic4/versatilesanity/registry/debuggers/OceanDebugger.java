package com.restonic4.versatilesanity.registry.debuggers;

import com.chaotic_loom.under_control.client.rendering.effects.EffectManager;
import com.chaotic_loom.under_control.client.rendering.effects.Sphere;
import com.chaotic_loom.under_control.core.annotations.ExecutionSide;
import com.chaotic_loom.under_control.debug.TickingDebugger;
import com.chaotic_loom.under_control.util.MathHelper;
import com.chaotic_loom.under_control.util.data_holders.RenderingFlags;
import com.restonic4.versatilesanity.util.WaterMassDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.awt.*;

public class OceanDebugger extends TickingDebugger {
    private static final Sphere[] waterMassDetectorAroundCheckPoints = new Sphere[WaterMassDetector.WATER_CHECK_SAMPLES];
    private static final Sphere submersionSphere = new Sphere("debug_ocean_" + MathHelper.getUniqueID());
    private static final Sphere statusSphere = new Sphere("debug_ocean_" + MathHelper.getUniqueID());

    private static final Color waterColor = new Color(0x00FF00);
    private static final Color waterFailColor = new Color(0xFFD900);
    private static final Color wrongColor = new Color(0xFF0000);

    private static final Color onOcean = waterColor;
    private static final Color onOceanAndSubmerged = new Color(0x8200FF);
    private static final Color notOnOcean = wrongColor;

    static {
        submersionSphere.setColor(onOceanAndSubmerged);
        statusSphere.setRenderingFlags(RenderingFlags.ON_TOP);
        statusSphere.setScale(0.5f, 0.5f, 0.5f);
    }

    public OceanDebugger(ExecutionSide executionSide) {
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

        boolean hasEnoughWaterAround = WaterMassDetector.hasEnoughWaterAround(level, player.blockPosition());
        BlockPos[] debugPos = WaterMassDetector.getLastCheckedBlocks();
        boolean[] results = WaterMassDetector.getLastCheckedBlocksResults();

        for (int i = 0; i < WaterMassDetector.WATER_CHECK_SAMPLES; i++) {
            BlockPos pos = debugPos[i];

            if (pos == null) {
                continue;
            }

            waterMassDetectorAroundCheckPoints[i].setPosition(pos.getX(), pos.getY(), pos.getZ());

            if (results[i]) {
                if (hasEnoughWaterAround) {
                    waterMassDetectorAroundCheckPoints[i].setColor(waterColor);
                } else {
                    waterMassDetectorAroundCheckPoints[i].setColor(waterFailColor);
                }

                waterMassDetectorAroundCheckPoints[i].setScale(1, 1, 1);
                waterMassDetectorAroundCheckPoints[i].setRenderingFlags(0);
            } else {
                waterMassDetectorAroundCheckPoints[i].setColor(wrongColor);
                waterMassDetectorAroundCheckPoints[i].setScale(1, 3, 1);
                waterMassDetectorAroundCheckPoints[i].setRenderingFlags(RenderingFlags.ON_TOP);
            }
        }

        submersionSphere.setPosition(player.blockPosition().getX(), WaterMassDetector.getLastSubmersionHeight(), player.blockPosition().getZ());
        statusSphere.setPosition((float) player.position().x(), (float) (player.position().y() + player.getEyeHeight() + 1), (float) player.position().z());

        WaterMassDetector.WaterMassDetectionResult result = WaterMassDetector.isInDeepOcean(player);

        if (result.isInDeepOcean()) {
            if (result.isSubmergedEnough()) {
                statusSphere.setColor(onOceanAndSubmerged);
            } else {
                statusSphere.setColor(onOcean);
            }
        } else {
            statusSphere.setColor(notOnOcean);
        }
    }

    @Override
    public void onInitialize() {
        for (int i = 0; i < WaterMassDetector.WATER_CHECK_SAMPLES; i++) {
            Sphere sphere = new Sphere("debug_ocean_" + MathHelper.getUniqueID());
            waterMassDetectorAroundCheckPoints[i] = sphere;
        }
    }

    @Override
    public void onStart() {
        for (Sphere sphere : waterMassDetectorAroundCheckPoints) {
            EffectManager.add(sphere);
        }

        EffectManager.add(submersionSphere);
        EffectManager.add(statusSphere);
    }

    @Override
    public void onStop() {
        for (Sphere sphere : waterMassDetectorAroundCheckPoints) {
            EffectManager.delete(sphere.getId());
        }

        EffectManager.delete(submersionSphere.getId());
        EffectManager.delete(statusSphere.getId());
    }
}
