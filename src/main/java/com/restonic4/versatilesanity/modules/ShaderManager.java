package com.restonic4.versatilesanity.modules;

import com.chaotic_loom.under_control.util.MathHelper;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.networking.ClientSanityManager;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

public class ShaderManager {
    private static final ManagedShaderEffect GREYSCALE_SHADER = ShaderEffectManager.getInstance()
            .manage(new ResourceLocation(VersatileSanity.MOD_ID, "shaders/post/greyscale.json"));

    private static final ManagedShaderEffect VIGNETTE_SHADER = ShaderEffectManager.getInstance()
            .manage(new ResourceLocation(VersatileSanity.MOD_ID, "shaders/post/vignette.json"));

    private static final ManagedShaderEffect BAD_VISION_SHADER = ShaderEffectManager.getInstance()
            .manage(new ResourceLocation(VersatileSanity.MOD_ID, "shaders/post/bad_vision.json"));

    private static final ManagedShaderEffect NOISE_SHADER = ShaderEffectManager.getInstance()
            .manage(new ResourceLocation(VersatileSanity.MOD_ID, "shaders/post/noise.json"));


    private static int ticks = 0;

    public static void register() {
        Minecraft minecraft = Minecraft.getInstance();

        ClientTickEvents.END_CLIENT_TICK.register((mc) -> {
            ++ticks;
        });

        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            ClientLevel level = minecraft.level;

            float sanity = ClientSanityManager.getSanity();
            float maxSanity = VersatileSanity.getConfig().getMaxSanity();

            float intensityBase = (float) MathHelper.normalize(sanity, 0, maxSanity);
            float intensity = 1 - Math.max(0, Math.min(1, intensityBase));
            float intensityRemappedForVignette = 1 - Math.max(0, Math.min(1, (float) MathHelper.normalize(intensityBase, 0, 0.65f)));
            float intensityRemappedForNoise = 1 - Math.max(0, Math.min(1, (float) MathHelper.normalize(intensityBase, 0, 0.75f)));

            updateNoise(intensityRemappedForNoise);
            updateBadVision(intensity);
            updateGrayscale(intensity);
            updateVignette(tickDelta, level, intensityRemappedForVignette);

            NOISE_SHADER.render(tickDelta);
            BAD_VISION_SHADER.render(tickDelta);
            GREYSCALE_SHADER.render(tickDelta);
            VIGNETTE_SHADER.render(tickDelta);
        });
    }

    private static void updateNoise(float intensity) {
        NOISE_SHADER.setUniformValue("uIntensity", intensity);
    }

    private static void updateBadVision(float intensity) {
        BAD_VISION_SHADER.setUniformValue("uIntensity", intensity);
    }

    private static void updateGrayscale(float intensity) {
        GREYSCALE_SHADER.setUniformValue("uIntensity", intensity);
    }

    private static void updateVignette(float tickDelta, ClientLevel level, float intensity) {
        if (level == null) {
            return;
        }

        VIGNETTE_SHADER.setUniformValue("uIntensity", intensity);
        VIGNETTE_SHADER.setUniformValue("Time", getTime(tickDelta));
    }

    public static float getTime() {
        Minecraft client = Minecraft.getInstance();
        float tickDelta = client.getFrameTime();

        if (client.level == null) {
            return tickDelta;
        }

        return getTime(tickDelta);
    }

    public static float getTime(float tickDelta) {
        return (ticks + tickDelta) / 20;
    }
}
