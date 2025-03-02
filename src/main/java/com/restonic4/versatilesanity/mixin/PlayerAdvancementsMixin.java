package com.restonic4.versatilesanity.mixin;

import com.restonic4.versatilesanity.modules.SanityEventHandler;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
    @Shadow private ServerPlayer player;

    @Inject(method = "award", at = @At("HEAD"))
    public void award(Advancement advancement, String string, CallbackInfoReturnable<Boolean> cir) {
        if (isAnActualAdvancementInsteadOfARecipeForNoReasonAtAll(advancement)) {
            SanityEventHandler.onAdvancementMade(player);
        }
    }

    @Unique
    private static boolean isNormalAdvancement(Advancement advancement) {
        DisplayInfo display = advancement.getDisplay();
        return display != null && display.getFrame() == FrameType.TASK;
    }

    @Unique
    private static boolean isGoalAdvancement(Advancement advancement) {
        DisplayInfo display = advancement.getDisplay();
        return display != null && display.getFrame() == FrameType.GOAL;
    }

    @Unique
    private static boolean isChallengeAdvancement(Advancement advancement) {
        DisplayInfo display = advancement.getDisplay();
        return display != null && display.getFrame() == FrameType.CHALLENGE;
    }

    @Unique
    private static boolean isAnActualAdvancementInsteadOfARecipeForNoReasonAtAll(Advancement advancement) {
        return isNormalAdvancement(advancement) || isChallengeAdvancement(advancement) || isGoalAdvancement(advancement);
    }
}
