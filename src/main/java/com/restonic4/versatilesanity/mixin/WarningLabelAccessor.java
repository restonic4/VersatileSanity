package com.restonic4.versatilesanity.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TitleScreen.WarningLabel.class)
public interface WarningLabelAccessor {
    @Invoker("<init>")
    static TitleScreen.WarningLabel create(Font font, MultiLineLabel label, int x, int y) {
        throw new AssertionError();
    }
}
