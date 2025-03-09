package com.restonic4.versatilesanity.registry.effects;

import com.chaotic_loom.under_control.core.annotations.ExecutionSide;
import com.chaotic_loom.under_control.core.annotations.Registration;
import com.restonic4.versatilesanity.VersatileSanity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

@Registration(side = ExecutionSide.COMMON)
public class EffectManager {
    public static final MobEffect MIND_RESTORATION = Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation(VersatileSanity.MOD_ID, "mind_restoration"), new MindRestoration());

    public static void register() {
        System.out.println("MobEffects registered");
    }
}
