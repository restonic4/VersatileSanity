package com.restonic4.versatilesanity.mixin;

import com.restonic4.versatilesanity.modules.SanityEventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEatEffect(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)V"))
    private void eat(Level level, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self instanceof Player player && !level.isClientSide()) {
            if (itemStack.is(Items.ROTTEN_FLESH) || itemStack.is(Items.POISONOUS_POTATO) || itemStack.is(Items.SPIDER_EYE)) {
                SanityEventHandler.onSpecialFoodEaten(player, false);
            } else if (itemStack.is(Items.ENCHANTED_GOLDEN_APPLE) || itemStack.is(Items.GOLDEN_APPLE) || itemStack.is(Items.GOLDEN_CARROT)) {
                SanityEventHandler.onSpecialFoodEaten(player, true);
            }
        }
    }
}
