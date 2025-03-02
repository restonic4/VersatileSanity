package com.restonic4.versatilesanity;

import com.chaotic_loom.under_control.api.config.ConfigAPI;
import com.chaotic_loom.under_control.client.gui.ConfigSelectorScreen;
import net.fabricmc.api.ClientModInitializer;

public class VersatileSanityClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigAPI.registerConfigScreen(VersatileSanity.MOD_ID, ConfigSelectorScreen.Builder.class);
    }
}
