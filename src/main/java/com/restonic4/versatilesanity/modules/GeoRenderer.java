package com.restonic4.versatilesanity.modules;

import com.chaotic_loom.under_control.api.cutscene.CutsceneAPI;
import com.chaotic_loom.under_control.client.rendering.RenderingHelper;
import com.chaotic_loom.under_control.client.rendering.render_layers.CubeGeometry;
import com.chaotic_loom.under_control.client.rendering.render_layers.RenderLayerRenderer;
import com.chaotic_loom.under_control.util.EasingSystem;
import com.chaotic_loom.under_control.util.MathHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.restonic4.versatilesanity.VersatileSanityClient;
import com.restonic4.versatilesanity.networking.packets.GeoKill;
import com.restonic4.versatilesanity.registry.CustomSounds;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GeoRenderer {
    private final RenderLayerRenderer<CubeGeometry> cube;

    private long startAnim = 0;
    private long endAnim = 0;

    private final Random random = new Random();
    private boolean isAnimating = false;

    private Vector3f geoSpawnedPosition = new Vector3f();

    private Vector3f animStartPos = new Vector3f();
    private boolean wasAnimating = false;

    public GeoRenderer() {
        this.cube = new RenderLayerRenderer<>(
                "geo",
                new ResourceLocation(""),
                RenderType.endPortal(),
                new CubeGeometry()
        );

        WorldRenderEvents.END.register(this::render);
    }

    public void startKilling() {
        if (isAnimating()) {
            return;
        }

        startAnim = System.currentTimeMillis();
        endAnim = startAnim + 25000;

        Minecraft client = Minecraft.getInstance();

        SimpleSoundInstance instance = new SimpleSoundInstance(CustomSounds.GEO, SoundSource.valueOf("SANITY_AMBIENT"), 1, 1, RandomSource.create(), client.player.blockPosition());
        client.execute(() -> client.getSoundManager().play(instance));

        this.geoSpawnedPosition.set(getRandomPositionInCircle(client.player.blockPosition(), 200).getCenter().toVector3f().add(0, 100, 0));

        startAnimation();
    }

    public void render(WorldRenderContext worldRenderContext) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();

        if (!isAnimating()) {
            CutsceneAPI.stop();
            wasAnimating = false;
            return;
        } else if (!wasAnimating) {
            animStartPos.set(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
            wasAnimating = true;
        }

        cube.getPosition().set(geoSpawnedPosition);

        cube.getRotation().set(
                (float) (Math.sin((double) System.currentTimeMillis() / 2000) * 360),
                (float) (Math.sin((double) System.currentTimeMillis() / 1000) * 360),
                0
        );

        cube.getScale().set(10 + random.nextFloat() * 2, 10 + random.nextFloat() * 2, 10 + random.nextFloat() * 2);

        CutsceneAPI.clear();
        CutsceneAPI.play();
        float[] rotation = CutsceneAPI.calculateCameraRotation(cube.getPosition().x, cube.getPosition().y, cube.getPosition().z);
        CutsceneAPI.setRotation(rotation[0], rotation[1]);

        float progress = MathHelper.getProgress(getStartAnim(), getEndAnim());

        if (progress >= 1) {
            stopAnimation();

            GeoKill.sendToServer();
            ErrorWindow.execute(I18n.get("fate.versatilesanity.death"));
            Minecraft client = Minecraft.getInstance();
            client.execute(client::stop);
            //NativeDialog.showConfirmDialog("HAHA", "hello");
        }

        float progressX = EasingSystem.getEasedValue(progress, animStartPos.x, cube.getPosition().x, EasingSystem.EasingType.EXPONENTIAL_IN);
        float progressY = EasingSystem.getEasedValue(progress, animStartPos.y, cube.getPosition().y, EasingSystem.EasingType.EXPONENTIAL_IN);
        float progressZ = EasingSystem.getEasedValue(progress, animStartPos.z, cube.getPosition().z, EasingSystem.EasingType.EXPONENTIAL_IN);

        CutsceneAPI.setPosition(progressX, progressY, progressZ);
    }

    public void startAnimation() {
        this.isAnimating = true;
        RenderingHelper.RenderLayers.registerRenderLayer(this.cube);
    }

    public void stopAnimation() {
        this.isAnimating = false;
        RenderingHelper.RenderLayers.removeRenderLayer(this.cube);
    }

    public boolean isAnimating() {
        return this.isAnimating;
    }

    public long getStartAnim() {
        return startAnim;
    }

    public long getEndAnim() {
        return endAnim;
    }

    public BlockPos getRandomPositionInCircle(BlockPos center, double radius) {
        int centerX = center.getX();
        int centerZ = center.getZ();
        double radiusSq = radius * radius;

        while(true) {
            // Generar offset dentro del cuadrado circunscrito
            int dx = (int) (random.nextDouble() * 2 * radius - radius);
            int dz = (int) (random.nextDouble() * 2 * radius - radius);

            // Verificar si está dentro del círculo
            if(dx*dx + dz*dz <= radiusSq) {
                return new BlockPos(
                        centerX + dx,
                        center.getY(),
                        centerZ + dz
                );
            }
        }
    }
}
