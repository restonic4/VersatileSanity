package com.restonic4.versatilesanity.mixin;

import com.google.common.collect.ImmutableMap;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(VillagerHostilesSensor.class)
public class VillagerHostilesSensorMixin {
    @Inject(method = "isHostile", at = @At("HEAD"), cancellable = true)
    private void onIsHostile(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player) {
            if (SanityStatusComponents.SANITY_STATUS.get(player).getSanityPercentage() <= 0.35f) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }

    @Redirect(
            method = "isClose",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableMap;get(Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            )
    )
    private Object getPlayerHostileDistance(ImmutableMap<EntityType<?>, Float> map, Object entityType) {
        EntityType<?> type = (EntityType<?>) entityType;
        if (type == EntityType.PLAYER) {
            return 6.0F;
        }
        return map.get(entityType);
    }
}
