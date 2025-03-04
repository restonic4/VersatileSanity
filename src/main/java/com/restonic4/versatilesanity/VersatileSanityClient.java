package com.restonic4.versatilesanity;

import com.chaotic_loom.under_control.api.config.ConfigAPI;
import com.chaotic_loom.under_control.client.gui.ConfigSelectorScreen;
import com.chaotic_loom.under_control.client.rendering.effects.EffectManager;
import com.chaotic_loom.under_control.client.rendering.effects.Sphere;
import com.chaotic_loom.under_control.events.EventResult;
import com.chaotic_loom.under_control.events.types.ClientEvents;
import com.chaotic_loom.under_control.util.MathHelper;
import com.restonic4.versatilesanity.config.VersatileSanityConfig;
import com.restonic4.versatilesanity.modules.CaveSoundHandler;
import com.restonic4.versatilesanity.registry.debuggers.ClientDebuggers;
import com.restonic4.versatilesanity.util.WaterMassDetector;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class VersatileSanityClient implements ClientModInitializer {
    private VersatileSanityConfig config = VersatileSanity.getConfig();

    @Override
    public void onInitializeClient() {
        ConfigAPI.registerConfigScreen(VersatileSanity.MOD_ID, ConfigSelectorScreen.Builder.class);

        ClientEvents.SOUND_PLAY.register((soundInstance) -> {
            if (soundInstance != null) {
                CaveSoundHandler.checkCaveSound(soundInstance);
            }

            return EventResult.SUCCEEDED;
        });

        ClientTickEvents.START_CLIENT_TICK.register((minecraft) -> {
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
    }
}
