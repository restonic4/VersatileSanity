package com.restonic4.versatilesanity.modules;

import com.chaotic_loom.under_control.client.rendering.screen_shake.ScreenShake;
import com.chaotic_loom.under_control.client.rendering.screen_shake.ScreenShakeManager;
import com.chaotic_loom.under_control.client.rendering.screen_shake.types.SumShakeCombiner;
import com.chaotic_loom.under_control.util.EasingSystem;
import com.chaotic_loom.under_control.util.MathHelper;
import com.chaotic_loom.under_control.util.RandomHelper;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.networking.ClientSanityManager;
import com.restonic4.versatilesanity.registry.CustomSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class WhispersManager {
    private static final Minecraft minecraft = Minecraft.getInstance();

    private static int currentTick = 0;
    private static int nextWhisperTick = 0;

    private static float channelVolume;

    public static void tick() {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        float sanity = ClientSanityManager.getSanity();
        float maxSanity = VersatileSanity.getConfig().getMaxSanity();

        float intensityBase = (float) MathHelper.normalize(sanity, 0, maxSanity);
        float intensity = 1 - Math.max(0, Math.min(1, intensityBase));

        if (intensity <= 0.7f) {
            currentTick = 0;
            nextWhisperTick = 0;
            channelVolume = 0;
            return;
        }

        float progress = normalize(intensity, 0.7f);
        progress = Math.min(Math.max(progress, 0f), 1f);

        channelVolume = 0.3f * progress;

        currentTick ++;

        if (currentTick >= nextWhisperTick) {
            nextWhisperTick = (int) (currentTick + RandomHelper.randomBetween(12 * 20, 16 * 20));

            SimpleSoundInstance instance = new SimpleSoundInstance(CustomSounds.WHISPER_LONG, SoundSource.valueOf("WHISPERS"), 1, 1, RandomSource.create(), minecraft.player.blockPosition());
            minecraft.execute(() -> minecraft.getSoundManager().play(instance));
        }
    }

    public static float normalize(float x, float min) {
        return (x - min) / (1.0f - min);
    }

    public static float getChannelVolume() {
        return channelVolume;
    }
}
