package com.restonic4.versatilesanity.mixin;

import com.restonic4.versatilesanity.modules.SanityEventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Parrot.class)
public class ParrotMixin {
    @Inject(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Parrot;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    public void mobInteract(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.level().isClientSide()) {
            return;
        }

        SanityEventHandler.onParrotPoisoned(player);
    }
}
