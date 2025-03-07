package com.restonic4.versatilesanity.mixin;

import com.chaotic_loom.under_control.util.MathHelper;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.networking.ClientSanityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public class LightTextureMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 2), method = "updateLightTexture")
    public float updateLightTexture(float a, float b) {
        float sanity = ClientSanityManager.getSanity();
        float maxSanity = VersatileSanity.getConfig().getMaxSanity();

        float intensity = 1 - (float) MathHelper.normalize(sanity, 0, maxSanity);

        double gamma = Minecraft.getInstance().options.gamma().get();

        return interpolar(intensity, (float) gamma, -1);
    }

    @Unique
    private static float interpolar(float a, float b, float c) {
        return (1 - a) * b + a * c;
    }
}
