package com.restonic4.versatilesanity.modules.hallucinations;

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

public class ScreenShaker {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final ScreenShakeManager screenShakeManager = ScreenShakeManager.create(new SumShakeCombiner(), 1f);

    private static int currentTick = 0;
    private static int nextShakeTick = 0;

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
            nextShakeTick = 0;
            return;
        }

        float progress = 1 - normalize(intensity, 0.7f);
        progress = Math.max(progress, 0.15f);

        currentTick ++;

        if (currentTick >= nextShakeTick) {
            nextShakeTick = (int) (currentTick + RandomHelper.randomBetween(progress * 20, progress * 15 * 20));

            screenShakeManager.addShake(new ScreenShake(60, 0.5f, EasingSystem.EasingType.LINEAR));

            if (RandomHelper.randomBetween(0, 100) <= 25) {
                SimpleSoundInstance instance = new SimpleSoundInstance(CustomSounds.CREAKING, SoundSource.PLAYERS, 0.5f, RandomHelper.randomBetween(0.8f, 1.2f), RandomSource.create(), minecraft.player.blockPosition());
                minecraft.getSoundManager().play(instance);
            }
        }
    }

    public static float normalize(float x, float min) {
        return (x - min) / (1.0f - min);
    }
}
