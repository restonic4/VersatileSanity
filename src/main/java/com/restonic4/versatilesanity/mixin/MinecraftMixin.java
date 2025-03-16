package com.restonic4.versatilesanity.mixin;

import com.mojang.blaze3d.platform.Window;
import com.restonic4.versatilesanity.util.ExtendedMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VirtualScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin implements ExtendedMinecraft {
    private boolean closing = true;

    @Override
    public void setClosing(boolean closing) {
        this.closing = closing;
    }

    @Inject(
            method = "close",
            at = @At("HEAD"),
            cancellable = true
    )
    private void redirectWindowClose(CallbackInfo ci) {
        if (!this.closing) {
            System.out.println("Window close intercepted!");
            ci.cancel();
        }
    }

    @Redirect(
            method = "destroy",
            at = @At(value = "INVOKE", target = "Ljava/lang/System;exit(I)V")
    )
    private void redirectWindowClose2(int status) {
        if (!this.closing) {
            System.out.println("Window close intercepted!");
            return;
        }

        System.exit(status);
    }
}
