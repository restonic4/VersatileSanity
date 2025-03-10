package com.restonic4.versatilesanity;

import com.chaotic_loom.under_control.api.config.ConfigAPI;
import com.chaotic_loom.under_control.client.gui.ConfigSelectorScreen;
import com.chaotic_loom.under_control.events.EventResult;
import com.chaotic_loom.under_control.events.types.ClientEvents;
import com.chaotic_loom.under_control.events.types.RegistrationEvents;
import com.restonic4.versatilesanity.config.VersatileSanityConfig;
import com.restonic4.versatilesanity.modules.*;
import com.restonic4.versatilesanity.networking.ClientSanityManager;
import com.restonic4.versatilesanity.networking.SanityStatusBarNetworking;
import com.restonic4.versatilesanity.registry.debuggers.ClientDebuggers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.joml.Vector3f;

public class VersatileSanityClient implements ClientModInitializer {
    private VersatileSanityConfig config = VersatileSanity.getConfig();
    private static DynamicSoundManager dynamicSoundManager;

    @Override
    public void onInitializeClient() {
        SanityStatusBarNetworking.register();
        ConfigAPI.registerConfigScreen(VersatileSanity.MOD_ID, ConfigSelectorScreen.Builder.class);

        ClientEvents.SOUND_PLAY.register((soundInstance) -> {
            if (soundInstance != null) {
                CaveSoundHandler.checkCaveSound(soundInstance);
            }

            return EventResult.SUCCEEDED;
        });

        GeoRenderer geoRenderer = new GeoRenderer();

        ClientTickEvents.START_CLIENT_TICK.register((minecraft) -> {
            if (minecraft.player != null && ClientSanityManager.getSanity() <= config.getMinSanity()) {
                geoRenderer.startKilling();
            }

            if (config.getDebugOcean()) {
                ClientDebuggers.OCEAN.enable();
            } else {
                ClientDebuggers.OCEAN.disable();
            }

            if (config.getDebugCave()) {
                ClientDebuggers.CAVE.enable();
            } else {
                ClientDebuggers.CAVE.disable();
            }
        });

        RegistrationEvents.SOUND_SOURCE_COMPLETED.register(() -> {
            SoundSource soundSource = SoundSource.valueOf("SANITY_AMBIENT");
            System.out.println(soundSource);

            dynamicSoundManager = new DynamicSoundManager();
        });

        ShaderManager.register();
    }

    public static DynamicSoundManager getDynamicSoundManager() {
        return dynamicSoundManager;
    }
}
