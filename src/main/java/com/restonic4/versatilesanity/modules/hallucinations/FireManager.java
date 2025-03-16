package com.restonic4.versatilesanity.modules.hallucinations;

import com.chaotic_loom.under_control.util.MathHelper;
import com.chaotic_loom.under_control.util.RandomHelper;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.networking.ClientSanityManager;
import com.restonic4.versatilesanity.registry.CustomSounds;
import com.restonic4.versatilesanity.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class FireManager {
    private static final Minecraft minecraft = Minecraft.getInstance();

    private static int currentTick = 0;
    private static int nextTick = 0;

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
            nextTick = 0;
            return;
        }

        currentTick ++;

        if (currentTick >= nextTick) {
            nextTick = (int) (currentTick + RandomHelper.randomBetween(2 * 60 * 20, 60 * 60 * 20));
            fire();
        }
    }

    public static void fire() {
        for (int i = 0; i < 256; i++) {
            Utils.placeRandomFire(minecraft.level, minecraft.player.blockPosition(), 32);
        }
    }
}
