package com.restonic4.versatilesanity.mixin;

import com.chaotic_loom.under_control.util.EasingSystem;
import com.chaotic_loom.under_control.util.MathHelper;
import com.chaotic_loom.under_control.util.RandomHelper;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.networking.ClientSanityManager;
import com.restonic4.versatilesanity.util.SanityIconState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Gui.class)
public class GuiMixin {
    private static final ResourceLocation SANITY_STATUS_BAR_TEXTURE = VersatileSanity.id("textures/gui/sanity_status_bar.png");

    private static final int MAX_ICONS  = 10;

    private static final int ICON_SIZE  = 16;
    private static final int RENDER_SIZE = 8;

    private static final int TEXTURE_WIDTH  = ICON_SIZE * 8;
    private static final int TEXTURE_HEIGHT  = ICON_SIZE;

    @Unique
    private static final SanityIconState[] iconStates = new SanityIconState[MAX_ICONS];
    private static final Random rand = new Random();

    @Unique
    private long lastGuiTick = 0;

    @Unique
    private int sanityRegenTimer = 0;

    @Unique
    private int prevSanity = 0;

    static {
        for (int i = 0; i < MAX_ICONS; i++) {
            iconStates[i] = new SanityIconState();
        }
    }

    @Inject(method = "renderPlayerHealth", at = @At("TAIL"))
    private void renderPlayerHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        Gui hud = (Gui) (Object) this;

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        int left = width / 2 - 91;
        int top = height - 49;

        int currentSanity = ClientSanityManager.getSanity();
        int maxSanity = VersatileSanity.getConfig().getMaxSanity();

        long currentTick = hud.getGuiTicks();
        long tickDelta = currentTick - lastGuiTick;
        lastGuiTick = currentTick;

        int normalizedSanity = (int) normalizeSanity(MAX_ICONS * 2, maxSanity, currentSanity);
        int fullHearts = normalizedSanity / 2;
        boolean hasHalfHeart = normalizedSanity % 2 != 0;

        int uOffset = 0;
        if (sanityRegenTimer > 0) {
            uOffset = ((currentTick / 2) % 2 == 0) ? 0 : ICON_SIZE;
            sanityRegenTimer -= tickDelta;
            if (sanityRegenTimer < 0) sanityRegenTimer = 0;
        }

        if (currentSanity > prevSanity) {
            sanityRegenTimer = 10;
        } else if (currentSanity < prevSanity) {
            for (SanityIconState state : iconStates) {
                boolean shouldShake = rand.nextBoolean();

                if (shouldShake) {
                    int amount = RandomHelper.randomBetween(1, 4);;

                    state.setTargetOffset(amount);
                    state.setStartTick(currentTick);
                    state.setTargetTick(currentTick + 20);
                }
            }
        }
        prevSanity = currentSanity;

        updateIconStates(currentTick);

        for (int i = 0; i < MAX_ICONS; i++) {
            int yOffset = calculateYOffset(i, normalizedSanity);
            int iconTop = top + yOffset;

            guiGraphics.blit(
                    SANITY_STATUS_BAR_TEXTURE,
                    left + i * RENDER_SIZE, iconTop,
                    RENDER_SIZE, RENDER_SIZE,
                    uOffset, 0, // U, V
                    ICON_SIZE, ICON_SIZE,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT
            );

            int uFactor;
            if (i < fullHearts) {
                uFactor = 4;
            } else if (i == fullHearts && hasHalfHeart) {
                uFactor = 5;
            } else {
                continue;
            }

            guiGraphics.blit(
                    SANITY_STATUS_BAR_TEXTURE,
                    left + i * RENDER_SIZE, iconTop,
                    RENDER_SIZE, RENDER_SIZE,
                    ICON_SIZE * uFactor, 0,
                    ICON_SIZE, ICON_SIZE,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT
            );
        }
    }

    @Unique
    private void updateIconStates(long currentTick) {
        for (SanityIconState state : iconStates) {
            if (currentTick <= state.getTargetTick() && currentTick >= state.getStartTick()) {
                double progress = MathHelper.normalize(currentTick, state.getStartTick(), state.getTargetTick());
                float easedProgress = EasingSystem.getEasedValue((float) progress, 1, 0, EasingSystem.EasingType.BACK_OUT);

                state.setCurrentOffset(state.getTargetOffset() * easedProgress);
            } else {
                state.setCurrentOffset(0);
            }
        }
    }

    @Unique
    private static double normalizeSanity(double maxNormalized, double maxOriginal, double value) {
        if (maxOriginal == 0) {
            throw new IllegalArgumentException("Max can't be 0");
        }

        return (value / maxOriginal) * maxNormalized;
    }

    @Unique
    private static double normalize(double x, double y) {
        if (y <= 0) {
            throw new IllegalArgumentException("y must be bigger than 0");
        }

        return Math.max(0, Math.min(1, x / y));
    }

    @Unique
    private int calculateYOffset(int iconIndex, float normalizedSanity) {
        int yOffset = 0;

        // Random shaker if decrease
        SanityIconState state = iconStates[iconIndex];
        yOffset += (int) state.getCurrentOffset();

        // Constant shaker if low

        float intensity = (float) MathHelper.normalize(normalizedSanity, 0, MAX_ICONS);
        float easedIntensity = EasingSystem.getEasedValue(intensity, 1, 0, EasingSystem.EasingType.CIRC_IN);

        yOffset += (int) ((rand.nextFloat() - 0.5f) * 4.0f * easedIntensity);

        return yOffset;
    }
}
