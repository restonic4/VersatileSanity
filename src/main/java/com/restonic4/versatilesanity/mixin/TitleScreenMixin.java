package com.restonic4.versatilesanity.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.modules.FlagHelper;
import com.restonic4.versatilesanity.registry.CustomSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Shadow @Nullable private TitleScreen.@Nullable WarningLabel warningLabel;
    @Unique private boolean loaded = false;
    @Unique private boolean minimal = false;
    @Unique private boolean playedWhispers = false;

    @Unique private ResourceLocation black = new ResourceLocation(VersatileSanity.MOD_ID, "textures/black.png");
    @Unique private Minecraft client = Minecraft.getInstance();

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PanoramaRenderer;render(FF)V"))
    public void render(PanoramaRenderer instance, float f, float g, Operation<Void> original, @Local(argsOnly = true) GuiGraphics guiGraphics) {
        if (shouldRenderNormally()) {
            original.call(instance, f, g);
        } else {
            int width = this.client.getWindow().getWidth();
            int height = this.client.getWindow().getHeight();
            guiGraphics.blit(black, 0, 0, 0, 0, width, height, 1, 1);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LogoRenderer;renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IF)V"))
    public void render(LogoRenderer instance, GuiGraphics guiGraphics, int i, float f, Operation<Void> original) {
        if (shouldRenderNormally()) {
            original.call(instance, guiGraphics, i, f);
        }
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/SplashRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/client/gui/Font;I)V"))
    public void render(SplashRenderer instance, GuiGraphics guiGraphics, int i, Font font, int j, Operation<Void> original) {
        if (shouldRenderNormally()) {
            original.call(instance, guiGraphics, i, font, j);
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (!shouldRenderNormally() && !playedWhispers) {
            playedWhispers = true;

            SimpleSoundInstance instance = SimpleSoundInstance.forUI(CustomSounds.WHISPER_LONG, 1, 0.25f);
            client.execute(() -> client.getSoundManager().play(instance));
        }
    }

    @Unique
    private boolean shouldRenderNormally() {
        if (!loaded) {
            loaded = true;
            minimal = FlagHelper.get("geo_death");

            FlagHelper.set("geo_death", false);
        }

        return !minimal;
    }
}
