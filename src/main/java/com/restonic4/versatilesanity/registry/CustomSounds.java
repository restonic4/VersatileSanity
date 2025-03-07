package com.restonic4.versatilesanity.registry;

import com.chaotic_loom.under_control.core.annotations.ExecutionSide;
import com.chaotic_loom.under_control.core.annotations.Registration;
import com.restonic4.versatilesanity.VersatileSanity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

@Registration(side = ExecutionSide.COMMON)
public class CustomSounds {
    public static SoundEvent CREAKING;
    public static SoundEvent WHISPER_LONG;

    public static void register() {
        ResourceLocation creakingLocation = new ResourceLocation(VersatileSanity.MOD_ID, "creaking");
        CREAKING = Registry.register(BuiltInRegistries.SOUND_EVENT, creakingLocation, SoundEvent.createVariableRangeEvent(creakingLocation));

        ResourceLocation whisperLocation = new ResourceLocation(VersatileSanity.MOD_ID, "whisper_long");
        WHISPER_LONG = Registry.register(BuiltInRegistries.SOUND_EVENT, whisperLocation, SoundEvent.createVariableRangeEvent(whisperLocation));
    }
}
