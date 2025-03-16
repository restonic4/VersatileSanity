package com.restonic4.versatilesanity.modules;

import com.chaotic_loom.under_control.api.cutscene.CutsceneAPI;
import com.chaotic_loom.under_control.client.rendering.RenderingHelper;
import com.chaotic_loom.under_control.client.rendering.render_layers.CubeGeometry;
import com.chaotic_loom.under_control.client.rendering.render_layers.RenderLayerRenderer;
import com.chaotic_loom.under_control.util.EasingSystem;
import com.chaotic_loom.under_control.util.MathHelper;
import com.restonic4.versatilesanity.networking.packets.GeoKill;
import com.restonic4.versatilesanity.registry.CustomSounds;
import com.restonic4.versatilesanity.util.ExtendedMinecraft;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

import java.util.Random;

public class GeoClockRenderer {
    private final RenderLayerRenderer<CubeGeometry> minuteHand;
    private final RenderLayerRenderer<CubeGeometry> secondHand;

    private final Random random = new Random();

    private long startTimeMillis = 0;
    private long endTimeMillis = 0;

    private Vector3f geoSpawnedPosition = new Vector3f(0, 200, 0);

    public GeoClockRenderer() {
        this.minuteHand = new RenderLayerRenderer<>(
                "geoClockMinutes",
                new ResourceLocation(""),
                RenderType.endPortal(),
                new CubeGeometry()
        );

        this.secondHand = new RenderLayerRenderer<>(
                "geoClockSeconds",
                new ResourceLocation(""),
                RenderType.endPortal(),
                new CubeGeometry()
        );

        WorldRenderEvents.END.register(this::render);
    }

    public void render(WorldRenderContext worldRenderContext) {
        long currentTime = System.currentTimeMillis();

        if (currentTime > startTimeMillis && currentTime < endTimeMillis) {
            updateClockHands(currentTime);
            RenderingHelper.RenderLayers.registerRenderLayer(this.minuteHand);
            RenderingHelper.RenderLayers.registerRenderLayer(this.secondHand);
        } else {
            RenderingHelper.RenderLayers.removeRenderLayer(this.minuteHand);
            RenderingHelper.RenderLayers.removeRenderLayer(this.secondHand);
        }
    }

    public void updateClockHands(long currentTime) {
        // Duración total entre start y end.
        long duration = endTimeMillis - startTimeMillis;
        long elapsedTime = (currentTime - startTimeMillis) % duration;

        // La manecilla de minutos debe completar 360° entre start y end.
        float minuteRotation = (elapsedTime / (float) duration) * 360f;
        // La manecilla de segundos debe dar 360° cada segundo (60 vueltas en un minuto).
        float secondRotation = ((currentTime - startTimeMillis) / 1000f) * 360f;

        // Ajustamos los ángulos para que la posición "0" apunte hacia arriba (12 en punto)
        float adjustedMinuteRotation = minuteRotation + 90f;
        float adjustedSecondRotation = secondRotation + 90f;

        // Dimensiones aleatorias para las manecillas.
        float thickness = 1 + random.nextFloat() * 1;

        float minuteHandLength = 3 + random.nextFloat() * 1.5f;
        float secondHandLength = 1 + random.nextFloat() * 1.5f;

        // Calculamos el offset para desplazar la aguja de modo que su "extremo interior"
        // (la mitad de la longitud) quede en geoSpawnedPosition. Se asume que la aguja se extiende a lo largo del eje Z.
        float minuteOffset = minuteHandLength / 2f;
        float secondOffset = secondHandLength / 2f;

        // Convertimos los ángulos ajustados a radianes.
        double minuteRad = Math.toRadians(adjustedMinuteRotation);
        double secondRad = Math.toRadians(adjustedSecondRotation);

        // Para una rotación alrededor del eje X, al rotar el vector (0, 0, offset) se obtiene:
        // (0, -sin(θ)*offset, cos(θ)*offset)
        Vector3f minutePivotCorrection = new Vector3f(
                0,
                (float)(-Math.sin(minuteRad) * minuteOffset),
                (float)(Math.cos(minuteRad) * minuteOffset)
        );
        Vector3f secondPivotCorrection = new Vector3f(
                0,
                (float)(-Math.sin(secondRad) * secondOffset),
                (float)(Math.cos(secondRad) * secondOffset)
        );

        // Actualizamos la transformación de la manecilla de minutos.
        minuteHand.getRotation().set(adjustedMinuteRotation, 0, 0);
        minuteHand.getPosition().set(geoSpawnedPosition).sub(minutePivotCorrection);
        minuteHand.getScale().set(thickness, thickness, minuteHandLength);

        // Actualizamos la transformación de la manecilla de segundos.
        secondHand.getRotation().set(adjustedSecondRotation, 0, 0);
        secondHand.getPosition().set(geoSpawnedPosition).sub(secondPivotCorrection);
        secondHand.getScale().set(thickness, thickness, secondHandLength);
    }

    public void startTime() {
        this.startTimeMillis = System.currentTimeMillis();
        this.endTimeMillis = this.startTimeMillis + 60000;
    }
}
