package com.restonic4.versatilesanity.mixin;

import com.chaotic_loom.under_control.api.cutscene.CutsceneAPI;
import com.chaotic_loom.under_control.util.EasingSystem;
import com.chaotic_loom.under_control.util.MathHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.restonic4.versatilesanity.VersatileSanityClient;
import com.restonic4.versatilesanity.modules.GeoRenderer;
import com.restonic4.versatilesanity.modules.ErrorWindow;
import com.restonic4.versatilesanity.networking.packets.GeoKill;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.language.I18n;
import org.joml.Matrix4f;
import org.joml.Random;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    private Vector3f geoPosition = new Vector3f(0, 100, 0);
    private Vector3f geoScale = new Vector3f(10, 10, 10);
    private GeoRenderer.Rotation geoRotation = new GeoRenderer.Rotation(0, 0, 0);

    private Vector3f animStartPos = new Vector3f();
    private boolean wasRendering = false;

    private Random random = new Random();

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/FogRenderer;setupNoFog()V"
            )
    )
    private void renderManagers(PoseStack poseStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci) {
        if (VersatileSanityClient.geoRenderer == null || !VersatileSanityClient.geoRenderer.shouldRender()) {
            CutsceneAPI.stop();
            wasRendering = false;
            return;
        } else if (!wasRendering) {
            animStartPos.set(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
            wasRendering = true;
        }

        geoPosition = VersatileSanityClient.geoRenderer.getPosition();

        geoRotation.y = (float) (Math.sin((double) System.currentTimeMillis() / 1000) * 360);
        geoRotation.x = (float) (Math.sin((double) System.currentTimeMillis() / 2000) * 360);

        geoScale.set(10 + random.nextFloat() * 2, 10 + random.nextFloat() * 2, 10 + random.nextFloat() * 2);

        VersatileSanityClient.geoRenderer.updateCubeTransformation(
                "geo",
                geoPosition,
                geoScale,
                geoRotation
        );

        CutsceneAPI.clear();
        CutsceneAPI.play();
        float[] rotation = CutsceneAPI.calculateCameraRotation(geoPosition.x, geoPosition.y, geoPosition.z);
        CutsceneAPI.setRotation(rotation[0], rotation[1]);

        float progress = MathHelper.getProgress(VersatileSanityClient.geoRenderer.getStartAnim(), VersatileSanityClient.geoRenderer.getEndAnim());

        if (progress >= 1) {
            VersatileSanityClient.geoRenderer.setShouldRender(false);

            GeoKill.sendToServer();
            ErrorWindow.execute(I18n.get("fate.versatilesanity.death"));
            Minecraft client = Minecraft.getInstance();
            client.execute(client::stop);
            //NativeDialog.showConfirmDialog("HAHA", "hello");
        }

        float progressX = EasingSystem.getEasedValue(progress, animStartPos.x, geoPosition.x, EasingSystem.EasingType.EXPONENTIAL_IN);
        float progressY = EasingSystem.getEasedValue(progress, animStartPos.y, geoPosition.y, EasingSystem.EasingType.EXPONENTIAL_IN);
        float progressZ = EasingSystem.getEasedValue(progress, animStartPos.z, geoPosition.z, EasingSystem.EasingType.EXPONENTIAL_IN);

        CutsceneAPI.setPosition(progressX, progressY, progressZ);
    }
}
