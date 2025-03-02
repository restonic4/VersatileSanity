package com.restonic4.versatilesanity.modules;

import com.chaotic_loom.under_control.util.MathHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SleepHandler {
    public static final int WHOLE_DAY_TICKS = 24000;
    public static final int NIGHT_TICKS = WHOLE_DAY_TICKS / 2;
    public static final int NIGHT_TICKS_REQUIRED = NIGHT_TICKS / 4;

    private static final int MIN_SLEEP_DEPRIVATION_TICKS = WHOLE_DAY_TICKS - (WHOLE_DAY_TICKS / 3);
    private static final int MAX_SLEEP_DEPRIVATION_TICKS = MIN_SLEEP_DEPRIVATION_TICKS + WHOLE_DAY_TICKS * 3;

    private static final Map<UUID, Long> sleepStartTicks = new HashMap<>();

    /**
     * Registra el momento en que un jugador empieza a dormir
     */
    public static void recordSleepStart(Player player) {
        UUID playerId = player.getUUID();
        long currentDayTime = player.level().getDayTime(); // Cambio aquí

        sleepStartTicks.put(playerId, currentDayTime);
        System.out.println("Player " + player.getName().getString() + " started sleeping on day time: " + currentDayTime);
    }

    /**
     * Maneja la lógica cuando un jugador termina de dormir
     */
    public static void handleSleepEnd(Player player) {
        UUID playerId = player.getUUID();

        // Si no tenemos registro de inicio, salimos
        if (!sleepStartTicks.containsKey(playerId)) {
            return;
        }

        long startTicks = sleepStartTicks.get(playerId);
        long endDayTime = player.level().getDayTime(); // Cambio aquí

        // Cálculo ajustado para manejar el cambio de día
        long sleepDuration;
        if (endDayTime < startTicks) {
            // Ha pasado por el ciclo completo de 24000 ticks
            sleepDuration = (NIGHT_TICKS - startTicks) + endDayTime;
        } else {
            sleepDuration = endDayTime - startTicks;
        }

        // Limpiar el registro para evitar fugas de memoria
        sleepStartTicks.remove(playerId);

        // Para debugging
        System.out.println("Player " + player.getName().getString() + " slept for " + sleepDuration + " day ticks");

        // Comprobar si realmente durmió lo suficiente
        if (sleepDuration >= NIGHT_TICKS_REQUIRED) {
            // Calcular porcentaje de noche completada (0.0 a 1.0)
            float completionPercentage = Math.min(1.0f, (float)sleepDuration / NIGHT_TICKS);

            onCompleteSleep(player, sleepDuration, completionPercentage);
        }
    }

    /**
     * Ejecuta la lógica cuando un jugador ha completado un sueño válido
     */
    private static void onCompleteSleep(Player player, long sleepDuration, float completionPercentage) {
        SanityEventHandler.onCompleteSleep(player, completionPercentage);
    }

    // I am a profesional modder, trust me
    public static void applyEepySleepy(ServerPlayer player) {
        ServerStatsCounter serverStatsCounter = player.getStats();
        long lastSleepTime = serverStatsCounter.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));

        long sleepDeprivationTicks =  Math.min(lastSleepTime, MAX_SLEEP_DEPRIVATION_TICKS);

        long effectiveTicks = Math.max(0, sleepDeprivationTicks - MIN_SLEEP_DEPRIVATION_TICKS);
        int dangerousPeriod = MAX_SLEEP_DEPRIVATION_TICKS - MIN_SLEEP_DEPRIVATION_TICKS;

        float progress = (float) MathHelper.normalize((float) effectiveTicks / dangerousPeriod, 0.0F, 1.0F);

        SanityEventHandler.onSleepDeprivedTick(player, progress);
    }
}