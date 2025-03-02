package com.restonic4.versatilesanity;

import com.chaotic_loom.under_control.api.config.ConfigAPI;
import com.chaotic_loom.under_control.client.gui.ConfigSelectorScreen;
import com.chaotic_loom.under_control.client.gui.GenericConfigScreen;
import com.restonic4.versatilesanity.util.CaveSoundHandler;
import net.fabricmc.api.ClientModInitializer;

public class VersatileSanityClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigAPI.registerConfigScreen(VersatileSanity.MOD_ID, ConfigSelectorScreen.Builder.class);
    }
}
