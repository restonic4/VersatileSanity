package com.restonic4.versatilesanity.components;

import com.restonic4.versatilesanity.VersatileSanity;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.minecraft.world.entity.player.Player;

public class SanityStatusComponents implements EntityComponentInitializer {
    public static final ComponentKey<SanityStatusComponent> SANITY_STATUS =
            ComponentRegistryV3.INSTANCE.getOrCreate(VersatileSanity.id("sanity"), SanityStatusComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Player.class, SANITY_STATUS, SanityStatusComponentImpl::new);
    }
}
