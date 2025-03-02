package com.restonic4.versatilesanity.util;

import com.restonic4.versatilesanity.networking.packets.ScarySoundPlayed;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class CaveSoundHandler {
    // Lista de sonidos de cueva vanilla
    private static final Set<ResourceLocation> VANILLA_CAVE_SOUNDS = Set.of(
            new ResourceLocation("minecraft:ambient.cave"),
            new ResourceLocation("minecraft:ambient.basalt_deltas.mood"),
            new ResourceLocation("minecraft:ambient.crimson_forest.mood"),
            new ResourceLocation("minecraft:ambient.soul_sand_valley.mood"),
            new ResourceLocation("minecraft:ambient.soul_sand_valley.additions"),
            new ResourceLocation("minecraft:ambient.warped_forest.mood"),
            new ResourceLocation("minecraft:ambient.nether_wastes.mood"),
            new ResourceLocation("minecraft:ambient.underwater.loop.additions.rare"),
            new ResourceLocation("minecraft:ambient.underwater.loop.additions.ultra_rare")
    );

    public static void checkCaveSound(SoundInstance sound) {
        if (sound != null && isCaveSound(sound.getLocation())) {
            Minecraft client = Minecraft.getInstance();
            onCaveSoundPlayed(
                    client,
                    sound.getLocation(),
                    sound.getX(),
                    sound.getY(),
                    sound.getZ()
            );
        }
    }

    private static boolean isCaveSound(ResourceLocation soundId) {
        String path = soundId.getPath();

        // Verificar si es un sonido de cueva vanilla conocido
        if (VANILLA_CAVE_SOUNDS.contains(soundId)) {
            return true;
        }

        return false;
    }

    private static void onCaveSoundPlayed(Minecraft client, ResourceLocation sound, double x, double y, double z) {
        Player player = client.player;

        if (player != null) {
            Vec3 soundPos = new Vec3(x, y, z);
            double distance = player.position().distanceTo(soundPos);

            if (distance <= 16) {
                System.out.println("Scary sound detected on client: " + sound + " en " + x + ", " + y + ", " + z + "!");
                ScarySoundPlayed.sendToServer(sound, soundPos.toVector3f());
            }
        }
    }
}