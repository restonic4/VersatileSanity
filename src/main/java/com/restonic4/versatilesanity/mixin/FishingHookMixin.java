package com.restonic4.versatilesanity.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.restonic4.versatilesanity.modules.SanityEventHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    @Shadow
    @Nullable
    public abstract Player getPlayerOwner(); // Obtiene el jugador dueño de la caña

    @Inject(
            method = "retrieve",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", ordinal = 0)
    )
    private void retrieve(ItemStack itemStack, CallbackInfoReturnable<Integer> cir, @Local ItemEntity itemEntity) {
        FishingHook hook = (FishingHook) (Object) this;
        Player player = this.getPlayerOwner();
        Level world = hook.level();

        if (player == null || world.isClientSide()) return;

        onFishingSuccess(player, hook, itemEntity);
    }

    @Unique
    private void onFishingSuccess(Player player, FishingHook bobber, ItemEntity itemEntity) {
        SanityEventHandler.onFishCaught(player, itemEntity.getItem());
    }
}
