package com.restonic4.versatilesanity.mixin;

import com.restonic4.versatilesanity.modules.SanityEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SoulSandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void onUseItemOn(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = cir.getReturnValue();
        if (!result.consumesAction()) {
            return;
        }

        BlockPos placedPos = blockHitResult.getBlockPos().offset(blockHitResult.getDirection().getNormal());
        BlockState placedState = level.getBlockState(placedPos);

        BlockState belowState = level.getBlockState(placedPos.below());
        boolean fertile = belowState.getBlock() instanceof FarmBlock || belowState.getBlock() instanceof SoulSandBlock;

        if (fertile && (placedState.getBlock() instanceof CropBlock || placedState.getBlock() instanceof BonemealableBlock)) {
            System.out.println("Crop planted by " + serverPlayer.getDisplayName());
            SanityEventHandler.onCropPlanted(serverPlayer, placedState);
        }
    }
}
