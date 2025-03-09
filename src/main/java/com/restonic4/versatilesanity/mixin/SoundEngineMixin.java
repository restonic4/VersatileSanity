package com.restonic4.versatilesanity.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Shadow protected abstract float calculateVolume(SoundInstance soundInstance);

    /*@Redirect(
            method = "updateCategoryVolume",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"
            )
    )
    public <K, V> void updateCategoryVolume(Map<K, V> instance, BiConsumer<? super K, ? super V> v) {
        Iterator<Map.Entry<K, V>> iterator = instance.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<K, V> entry = iterator.next();
            SoundInstance soundInstance = (SoundInstance) entry.getKey();
            ChannelAccess.ChannelHandle channelHandle = (ChannelAccess.ChannelHandle) entry.getValue();

            float volume = this.calculateVolume(soundInstance);

            channelHandle.execute(channel -> {
                if (volume <= 0.0F) {
                    channel.stop();
                } else {
                    channel.setVolume(volume);
                }
            });
        }
    }*/
}
