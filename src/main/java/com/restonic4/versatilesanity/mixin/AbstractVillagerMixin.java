package com.restonic4.versatilesanity.mixin;

import com.restonic4.versatilesanity.modules.SanityEventHandler;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractVillager.class)
public class AbstractVillagerMixin {
    @Shadow private @Nullable Player tradingPlayer;

    @Inject(method = "notifyTrade", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/TradeTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/npc/AbstractVillager;Lnet/minecraft/world/item/ItemStack;)V"))
    public void notifyTrade(MerchantOffer merchantOffer, CallbackInfo ci) {
        SanityEventHandler.onVillagerTrade(this.tradingPlayer);
    }
}
