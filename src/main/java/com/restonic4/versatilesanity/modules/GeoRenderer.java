package com.restonic4.versatilesanity.modules;

import com.chaotic_loom.under_control.api.cutscene.CutsceneAPI;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.restonic4.versatilesanity.registry.CustomSounds;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
    private final Map<String, CustomCube> cubes = new HashMap<>();
    private boolean shouldRender = false;
    private long startAnim = 0;
    private long endAnim = 0;

    private static final Random RANDOM = new Random();

    private Vector3f position = new Vector3f();

    public void startKilling() {
        startAnim = System.currentTimeMillis();
        endAnim = startAnim + 25000;

        Minecraft client = Minecraft.getInstance();

        SimpleSoundInstance instance = new SimpleSoundInstance(CustomSounds.GEO, SoundSource.valueOf("SANITY_AMBIENT"), 1, 1, RandomSource.create(), client.player.blockPosition());
        client.execute(() -> client.getSoundManager().play(instance));

        position = getRandomPositionInCircle(client.player.blockPosition(), 200).getCenter().toVector3f().add(0, 100, 0);

        setShouldRender(true);
    }

    public void setShouldRender(boolean value) {
        this.shouldRender = value;
    }

    public boolean shouldRender() {
        return shouldRender;
    }

    public long getStartAnim() {
        return startAnim;
    }

    public long getEndAnim() {
        return endAnim;
    }

    /**
     * Método principal de renderizado
     */
    public void renderCubes(WorldRenderContext context) {
        if (!shouldRender) {
            return;
        }

        PoseStack matrixStack = context.matrixStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPos = context.camera().getPosition();

        matrixStack.pushPose();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (CustomCube cube : cubes.values()) {
            renderCube(matrixStack, bufferSource, cube);
        }

        matrixStack.popPose();
        bufferSource.endBatch();
    }

    /**
     * Renderiza un cubo individual
     */
    private void renderCube(PoseStack matrixStack, MultiBufferSource bufferSource, CustomCube cube) {
        matrixStack.pushPose();

        // Aplicamos transformaciones
        matrixStack.translate(cube.position.x, cube.position.y, cube.position.z);
        matrixStack.mulPose(Axis.XP.rotationDegrees(cube.rotation.x));
        matrixStack.mulPose(Axis.YP.rotationDegrees(cube.rotation.y));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(cube.rotation.z));
        matrixStack.scale((float) cube.scale.x, (float) cube.scale.y, (float) cube.scale.z); // Escala independiente por eje

        // Configuración de textura por cubo
        RenderType renderType = RenderType.entityTranslucentCull(cube.texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        drawCube(matrixStack, vertexConsumer, cube.r, cube.g, cube.b, cube.alpha);
        matrixStack.popPose();
    }

    /**
     * Dibuja un cubo centrado en el origen de coordenadas
     */
    private void drawCube(PoseStack matrixStack, VertexConsumer vertexConsumer, float r, float g, float b, float alpha) {
        Matrix4f matrix = matrixStack.last().pose();
        float size = 0.5f; // Tamaño medio del cubo - resultará en un cubo de 1x1x1

        // Cara frontal
        addVertex(vertexConsumer, matrix, -size, -size, size, 0, 1, r, g, b, alpha, 0, 0, 1);
        addVertex(vertexConsumer, matrix, size, -size, size, 1, 1, r, g, b, alpha, 0, 0, 1);
        addVertex(vertexConsumer, matrix, size, size, size, 1, 0, r, g, b, alpha, 0, 0, 1);
        addVertex(vertexConsumer, matrix, -size, size, size, 0, 0, r, g, b, alpha, 0, 0, 1);

        // Cara trasera
        addVertex(vertexConsumer, matrix, size, -size, -size, 0, 1, r, g, b, alpha, 0, 0, -1);
        addVertex(vertexConsumer, matrix, -size, -size, -size, 1, 1, r, g, b, alpha, 0, 0, -1);
        addVertex(vertexConsumer, matrix, -size, size, -size, 1, 0, r, g, b, alpha, 0, 0, -1);
        addVertex(vertexConsumer, matrix, size, size, -size, 0, 0, r, g, b, alpha, 0, 0, -1);

        // Cara superior
        addVertex(vertexConsumer, matrix, -size, size, -size, 0, 1, r, g, b, alpha, 0, 1, 0);
        addVertex(vertexConsumer, matrix, -size, size, size, 0, 0, r, g, b, alpha, 0, 1, 0);
        addVertex(vertexConsumer, matrix, size, size, size, 1, 0, r, g, b, alpha, 0, 1, 0);
        addVertex(vertexConsumer, matrix, size, size, -size, 1, 1, r, g, b, alpha, 0, 1, 0);

        // Cara inferior
        addVertex(vertexConsumer, matrix, -size, -size, size, 0, 1, r, g, b, alpha, 0, -1, 0);
        addVertex(vertexConsumer, matrix, -size, -size, -size, 0, 0, r, g, b, alpha, 0, -1, 0);
        addVertex(vertexConsumer, matrix, size, -size, -size, 1, 0, r, g, b, alpha, 0, -1, 0);
        addVertex(vertexConsumer, matrix, size, -size, size, 1, 1, r, g, b, alpha, 0, -1, 0);

        // Cara izquierda
        addVertex(vertexConsumer, matrix, -size, -size, -size, 0, 1, r, g, b, alpha, -1, 0, 0);
        addVertex(vertexConsumer, matrix, -size, -size, size, 1, 1, r, g, b, alpha, -1, 0, 0);
        addVertex(vertexConsumer, matrix, -size, size, size, 1, 0, r, g, b, alpha, -1, 0, 0);
        addVertex(vertexConsumer, matrix, -size, size, -size, 0, 0, r, g, b, alpha, -1, 0, 0);

        // Cara derecha
        addVertex(vertexConsumer, matrix, size, -size, size, 0, 1, r, g, b, alpha, 1, 0, 0);
        addVertex(vertexConsumer, matrix, size, -size, -size, 1, 1, r, g, b, alpha, 1, 0, 0);
        addVertex(vertexConsumer, matrix, size, size, -size, 1, 0, r, g, b, alpha, 1, 0, 0);
        addVertex(vertexConsumer, matrix, size, size, size, 0, 0, r, g, b, alpha, 1, 0, 0);
    }

    /**
     * Helper para agregar un vértice al builder
     */
    private void addVertex(VertexConsumer vertexConsumer, Matrix4f matrix, float x, float y, float z,
                           float u, float v, float r, float g, float b, float alpha,
                           float nx, float ny, float nz) {
        vertexConsumer.vertex(matrix, x, y, z)
                .color(r, g, b, alpha)
                .uv(u, v)
                .overlayCoords(0, 0)
                .uv2(240, 240) // Iluminación máxima
                .normal(nx, ny, nz)
                .endVertex();
    }

    /**
     * Método para agregar un nuevo cubo a la lista de renderizado
     */
    public void addCube(String id, Vector3f position, Vector3f scale, Rotation rotation,
                        ResourceLocation texture, float r, float g, float b, float alpha) {
        cubes.put(id, new CustomCube(
                position,
                scale,
                rotation,
                texture,
                r, g, b, alpha
        ));
    }

    /**
     * Método para actualizar un cubo existente
     */
    public void updateCube(String id, Vector3f position, Vector3f scale, Rotation rotation, ResourceLocation texture, float r, float g, float b, float alpha) {
        if (cubes.containsKey(id)) {
            CustomCube cube = cubes.get(id);
            cube.position = position;
            cube.scale = scale;
            cube.rotation = rotation;
            cube.texture = texture;
            cube.r = r;
            cube.g = g;
            cube.b = b;
            cube.alpha = alpha;
        }
    }

    public void updateCubeTransformation(String id, Vector3f position, Vector3f scale, Rotation rotation) {
        if (cubes.containsKey(id)) {
            CustomCube cube = cubes.get(id);
            updateCube(id, position, scale, rotation, cube.texture, cube.r, cube.g, cube.b, cube.alpha);
        }
    }

    public void updateCubeColors(String id, float r, float g, float b, float alpha) {
        if (cubes.containsKey(id)) {
            CustomCube cube = cubes.get(id);
            updateCube(id, cube.position, cube.scale, cube.rotation, cube.texture, r, g, b, alpha);
        }
    }

    public static BlockPos getRandomPositionInCircle(BlockPos center, double radius) {
        int centerX = center.getX();
        int centerZ = center.getZ();
        double radiusSq = radius * radius;

        while(true) {
            // Generar offset dentro del cuadrado circunscrito
            int dx = (int) (RANDOM.nextDouble() * 2 * radius - radius);
            int dz = (int) (RANDOM.nextDouble() * 2 * radius - radius);

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

    /**
     * Método para eliminar un cubo
     */
    public void removeCube(String id) {
        cubes.remove(id);
    }

    public CustomCube getCube(String id) {
        return cubes.get(id);
    }

    public Vector3f getPosition() {
        return position;
    }

    /**
     * Clase para representar un cubo personalizado
     */
    public static class CustomCube {
        public Vector3f position;
        public Vector3f scale;
        public Rotation rotation;
        public ResourceLocation texture;
        public float r, g, b, alpha;

        public CustomCube(Vector3f position, Vector3f scale, Rotation rotation,
                          ResourceLocation texture, float r, float g, float b, float alpha) {
            this.position = position;
            this.scale = scale;
            this.rotation = rotation;
            this.texture = texture;
            this.r = r;
            this.g = g;
            this.b = b;
            this.alpha = alpha;
        }
    }

    /**
     * Clase para representar rotación en grados
     */
    public static class Rotation {
        public float x, y, z;

        public Rotation(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
