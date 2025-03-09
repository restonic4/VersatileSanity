package com.restonic4.versatilesanity.registry.effects;

import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import com.restonic4.versatilesanity.networking.ClientSanityManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class MindRestoration extends MobEffect {
    public static final int TICKS = 20;
    public static final int HEAL_AMOUNT = 4;

    protected MindRestoration() {
        super(MobEffectCategory.BENEFICIAL, 0xB799FF);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity livingEntity, int i) {
        if (livingEntity instanceof Player player) {
            float sanity = SanityStatusComponents.SANITY_STATUS.get(player).getSanityStatus();
            float maxSanity = VersatileSanity.getConfig().getMaxSanity();

            if (sanity < maxSanity) {
                SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(HEAL_AMOUNT);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int i, int j) {
        int k;

        k = TICKS >> j;
        if (k > 0) {
            return i % k == 0;
        } else {
            return true;
        }
    }
}