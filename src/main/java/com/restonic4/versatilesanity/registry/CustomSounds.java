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
    public static SoundEvent CREAKING = register("creaking");
    public static SoundEvent WHISPER_LONG = register("whisper_long");
    public static SoundEvent SANITY_AMBIENT_LOOP = register("sanity_ambient_loop");
    public static SoundEvent GEO = register("geo");

    public static void register() {
        System.out.println("Sounds registered!");
    }

    public static SoundEvent register(String id) {
        ResourceLocation location = new ResourceLocation(VersatileSanity.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, location, SoundEvent.createVariableRangeEvent(location));
    }
}
