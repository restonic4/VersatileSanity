package com.restonic4.versatilesanity.modules;

import com.chaotic_loom.under_control.util.MathHelper;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.networking.ClientSanityManager;
import com.restonic4.versatilesanity.registry.CustomSounds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class DynamicSoundManager {
    private static final float BASE_VOLUME = 0.2f;
    private final Minecraft client;
    private final ModLoopingSound loopingSound;
    private float volumeModifier = 1.0f;

    boolean wasStopped = true;

    public DynamicSoundManager() {
        this.client = Minecraft.getInstance();
        this.loopingSound = new ModLoopingSound(client, CustomSounds.SANITY_AMBIENT_LOOP, SoundSource.valueOf("SANITY_AMBIENT"));

        // Registrar en los eventos del cliente
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    public void tick(Minecraft client) {
        if (client.level == null) return;

        float sanity = ClientSanityManager.getSanity();
        float maxSanity = VersatileSanity.getConfig().getMaxSanity();

        float intensityBase = (float) MathHelper.normalize(sanity, 0, maxSanity);
        float intensity = 1 - Math.max(0, Math.min(1, intensityBase));

        if (intensity <= 0.7f) {
            volumeModifier = 0;
            wasStopped = true;
            return;
        }

        float progress = normalize(intensity, 0.7f);
        progress = Math.min(Math.max(progress, 0f), 1f);

        volumeModifier = BASE_VOLUME * progress;

        // Actualizar el sonido
        updateSoundState();
    }

    private void updateSoundState() {
        SoundManager soundManager = client.getSoundManager();

        if (volumeModifier > 0.0f) {
            if (!soundManager.isActive(loopingSound) || wasStopped) {
                wasStopped = false;
                client.execute(() -> soundManager.play(loopingSound));
                System.out.println("Sanity ambient playing");
            }
            loopingSound.updateVolume(volumeModifier);
        } else {
            wasStopped = true;
            System.out.println("Sanity ambient stopping");
            client.execute(() -> soundManager.stop(loopingSound));
        }

    }

    public void setVolumeModifier(float modifier) {
        this.volumeModifier = org.joml.Math.clamp(modifier, 0.0f, 1.0f);
    }

    public float getVolumeModifier() {
        return volumeModifier;
    }

    private static class ModLoopingSound extends AbstractSoundInstance {
        private final Minecraft client;
        private final SoundSource soundSource;

        public ModLoopingSound(Minecraft client, SoundEvent sound, SoundSource soundSource) {
            super(sound, soundSource, SoundInstance.createUnseededRandom());
            this.client = client;
            this.looping = true;
            this.soundSource = soundSource;
            this.attenuation = Attenuation.NONE;
        }

        public void updateVolume(float newVolume) {
            this.client.getSoundManager().updateSourceVolume(this.soundSource, newVolume);
        }
    }

    public static float normalize(float x, float min) {
        return (x - min) / (1.0f - min);
    }
}
