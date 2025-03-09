package com.restonic4.versatilesanity.components;

import com.restonic4.versatilesanity.VersatileSanity;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.resources.ResourceLocation;

public class HomeDetectionComponents implements EntityComponentInitializer {
    public static final ComponentKey<HomeDetectionComponent> HOME_DETECTION =
            ComponentRegistry.getOrCreate(
                    new ResourceLocation(VersatileSanity.MOD_ID, "home_detection"),
                    HomeDetectionComponent.class
            );

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(
                HOME_DETECTION,
                HomeDetectionComponent::new,
                RespawnCopyStrategy.ALWAYS_COPY
        );
    }
}