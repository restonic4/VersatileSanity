package com.restonic4.versatilesanity.mixin;

import com.restonic4.versatilesanity.VersatileSanityClient;
import com.restonic4.versatilesanity.modules.hallucinations.WhispersManager;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Shadow protected abstract OptionInstance<Double> createSoundSliderOptionInstance(String string, SoundSource soundSource);

    @Inject(method = "getSoundSourceVolume", at = @At("HEAD"), cancellable = true)
    public final void getSoundSourceVolume(SoundSource soundSource, CallbackInfoReturnable<Float> cir) {
        if (soundSource == SoundSource.valueOf("SANITY_AMBIENT") && VersatileSanityClient.getDynamicSoundManager() != null) {
            cir.setReturnValue(VersatileSanityClient.getDynamicSoundManager().getVolumeModifier());
            cir.cancel();
        } else if (soundSource == SoundSource.valueOf("WHISPERS")) {
            cir.setReturnValue(WhispersManager.getChannelVolume());
            cir.cancel();
        }
    }
}
