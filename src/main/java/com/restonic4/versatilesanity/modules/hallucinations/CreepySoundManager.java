package com.restonic4.versatilesanity.modules.hallucinations;

import com.chaotic_loom.under_control.util.MathHelper;
import com.chaotic_loom.under_control.util.RandomHelper;
import com.chaotic_loom.under_control.util.WeightedRandomPicker;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.networking.ClientSanityManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CreepySoundManager {
    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final Random random = new Random();
    private static final WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();

    private static int currentTick = 0;
    private static int nextSoundTick = 0;

    static {
        picker.addItem("cave", 1);
        picker.addItem("creeper", 2);
        picker.addItem("footsteps", 3);
    }

    public static void tick() {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        float sanity = ClientSanityManager.getSanity();
        float maxSanity = VersatileSanity.getConfig().getMaxSanity();

        float intensityBase = (float) MathHelper.normalize(sanity, 0, maxSanity);
        float intensity = 1 - Math.max(0, Math.min(1, intensityBase));

        if (intensity <= 0.35f) {
            currentTick = 0;
            nextSoundTick = 0;
            return;
        }

        int tickAcceleration = (int) (intensity * 10);
        currentTick += 1 + tickAcceleration;

        if (currentTick >= nextSoundTick) {
            nextSoundTick = currentTick + RandomHelper.randomBetween(240 * 20, 900 * 20);

            String selected = picker.getRandomItem();

            switch (selected) {
                case "creeper":
                    playCreeperSoundBehindPlayer();
                    break;
                case "footsteps":
                    playFootstepSequence();
                    break;
                case "cave":
                    playCaveSound();
                    break;
            }
        }
    }

    // Reproduce el sonido de un creeper detrás del jugador.
    public static void playCreeperSoundBehindPlayer() {
        Player player = minecraft.player;
        // Sumamos 180 para obtener la dirección contraria (detrás)
        float yaw = player.getYRot();
        double radians = Math.toRadians(yaw + 180);
        double offsetDistance = 5.0;
        double xOffset = -Math.sin(radians) * offsetDistance;
        double zOffset = Math.cos(radians) * offsetDistance;
        double soundX = player.getX() + xOffset;
        double soundY = player.getY();
        double soundZ = player.getZ() + zOffset;
        BlockPos soundPos = new BlockPos(Mth.floor(soundX), Mth.floor(soundY), Mth.floor(soundZ));
        playSound(SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 1.0F, 1.0F, soundPos);
    }

    public static void playFootstepSequence() {
        playFootstepSequence(random.nextBoolean());
    }

    public static void playFootstepSequence(boolean isRunning) {
        Player player = minecraft.player;
        double minFarDistance = 10.0;
        double maxFarDistance = 20.0;
        double farDistance = minFarDistance + random.nextDouble() * (maxFarDistance - minFarDistance);
        double angle = random.nextDouble() * 2 * Math.PI;
        double farX = player.getX() + Math.cos(angle) * farDistance;
        double farZ = player.getZ() + Math.sin(angle) * farDistance;
        int farY = getGroundY(farX, farZ);
        BlockPos farPos = new BlockPos(Mth.floor(farX), farY, Mth.floor(farZ));

        double minNearDistance = 2.0;
        double maxNearDistance = farDistance / 2.0;
        double nearDistance = minNearDistance + random.nextDouble() * (maxNearDistance - minNearDistance);
        double nearX = player.getX() + Math.cos(angle) * nearDistance;
        double nearZ = player.getZ() + Math.sin(angle) * nearDistance;
        int nearY = getGroundY(nearX, nearZ);
        BlockPos nearPos = new BlockPos(Mth.floor(nearX), nearY, Mth.floor(nearZ));

        double dx = nearPos.getX() - farPos.getX();
        double dz = nearPos.getZ() - farPos.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        int steps = (int) Math.ceil(distance);
        steps = Math.max(steps, 1);

        // Define el intervalo base entre pasos en milisegundos
        long baseStepInterval = isRunning ? 250 : 500; // 250 ms para correr, 500 ms para caminar

        // Crea un ScheduledExecutorService para programar la reproducción de los sonidos
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Random random = new Random();

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double stepX = farPos.getX() + dx * t;
            double stepY = farPos.getY() + (nearPos.getY() - farPos.getY()) * t;
            double stepZ = farPos.getZ() + dz * t;
            BlockPos stepPos = new BlockPos(Mth.floor(stepX), Mth.floor(stepY), Mth.floor(stepZ));
            BlockPos groundPos = stepPos.below();
            SoundEvent stepSound;
            try {
                BlockState state = minecraft.level.getBlockState(groundPos);
                stepSound = state.getSoundType().getStepSound();
            } catch (Exception e) {
                stepSound = SoundEvents.STONE_STEP;
            }

            // Introduce una variación aleatoria en el intervalo entre pasos
            long stepIntervalVariation = (long) (baseStepInterval * 0.05); // 5% de variación
            long stepInterval = baseStepInterval + (random.nextLong() % stepIntervalVariation) - (stepIntervalVariation / 2);

            // Programa la reproducción del sonido con un retraso
            SoundEvent finalStepSound = stepSound;
            scheduler.schedule(() -> playSound(finalStepSound, SoundSource.HOSTILE, 1.0F, 1.0F, stepPos), i * stepInterval, TimeUnit.MILLISECONDS);
        }

        // Cierra el scheduler después de programar todos los sonidos
        scheduler.shutdown();
    }


    // Método auxiliar que intenta determinar la coordenada Y de un suelo válido en (x,z)
    private static int getGroundY(double x, double z) {
        int y = (int) minecraft.player.getY();
        for (int i = y; i > y - 64; i--) {
            BlockPos pos = new BlockPos(Mth.floor(x), i, Mth.floor(z));
            if (!minecraft.level.getBlockState(pos).isAir()) {
                return i;
            }
        }
        return y; // fallback en caso de no encontrar nada
    }

    // Reproduce un sonido ambiente de cueva en la posición del jugador.
    public static void playCaveSound() {
        Player player = minecraft.player;
        BlockPos pos = new BlockPos(Mth.floor(player.getX()), Mth.floor(player.getY()), Mth.floor(player.getZ()));
        playSound(SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 1.0F, 1.0F, pos);
    }

    // Método auxiliar que crea y reproduce una instancia de sonido en el cliente.
    private static void playSound(SoundEvent event, SoundSource source, float volume, float pitch, BlockPos pos) {
        SimpleSoundInstance instance = new SimpleSoundInstance(event, source, volume, pitch, RandomSource.create(), pos);
        minecraft.getSoundManager().play(instance);
    }
}
