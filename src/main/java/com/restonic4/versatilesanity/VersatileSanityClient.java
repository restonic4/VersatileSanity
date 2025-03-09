package com.restonic4.versatilesanity;

import com.chaotic_loom.under_control.api.config.ConfigAPI;
import com.chaotic_loom.under_control.client.gui.ConfigSelectorScreen;
import com.chaotic_loom.under_control.events.EventResult;
import com.chaotic_loom.under_control.events.types.ClientEvents;
import com.chaotic_loom.under_control.events.types.RegistrationEvents;
import com.restonic4.versatilesanity.config.VersatileSanityConfig;
import com.restonic4.versatilesanity.modules.*;
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

    public static GeoRenderer geoRenderer;

    @Override
    public void onInitializeClient() {
        ConfigAPI.registerConfigScreen(VersatileSanity.MOD_ID, ConfigSelectorScreen.Builder.class);

        ClientEvents.SOUND_PLAY.register((soundInstance) -> {
            if (soundInstance != null) {
                CaveSoundHandler.checkCaveSound(soundInstance);
            }

            return EventResult.SUCCEEDED;
        });

        geoRenderer = new GeoRenderer();
        WorldRenderEvents.END.register(geoRenderer::renderCubes);

        geoRenderer.addCube(
                "geo",
                new Vector3f(0, 100, 0),
                new Vector3f(10, 10, 10),
                new GeoRenderer.Rotation(0, 0, 0),
                new ResourceLocation(VersatileSanity.MOD_ID, "textures/black.png"),
                1f, 1f, 1f, 1f
        );

        ClientTickEvents.START_CLIENT_TICK.register((minecraft) -> {
            if (minecraft.player != null && minecraft.player.isCrouching()) {
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
