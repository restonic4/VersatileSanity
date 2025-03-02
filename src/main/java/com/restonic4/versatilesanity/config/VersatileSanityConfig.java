package com.restonic4.versatilesanity.config;

import com.chaotic_loom.under_control.config.ModConfig;
import com.restonic4.versatilesanity.VersatileSanity;

public class VersatileSanityConfig extends ModConfig {
    public VersatileSanityConfig() {
        super(VersatileSanity.MOD_ID);
    }

    @Override
    protected void registerClientOptions() {

    }

    /*

    Tengo una mecánica en mi mod de Minecraft, el problema es que no se como balancearla, hay mil valores, es una mecánica de cordura, hay muchas cosas que dan o restan.

    ¿Hay algún método o planificación eficaz que pueda usar para poner unos valores aceptables?

    Valores de ejemplo:

    Sanity maxima = 100
    Sanity al unirte por primera vez = 100
    Sanity en cuanto re-spawneas = 0

    Reducción en oscuridad: -2 cada 200 ticks
    Reducción cerca de mobs hostiles: -1 cada 30 ticks en un radio de 24 bloques
    Reducción cerca de bosses: -3 cada 200 ticks en un radio de 64 bloques
    Reducción cerca de portales: -1 cada 200 ticks en un radio de 14 bloques
    Reducción cerca al escuchar sonidos de cueva: -10
    Reducción en lluvia: -1 cada 1200 ticks
    Reducción en tormenta: -2 cada 1200 ticks
    Reducción en nether: -1 cada 1200 ticks
    Reducción en end: -2 cada 1200 ticks
    Reducción por daño recibido: -2 multiplicado por el daño
    Reducción por matar aldeano: -10
    Reducción por matar jugador: -10
    Reducción por matar animal: -4
    Reducción por matar criatura cute: -6
    Reducción por estar solo: -2 cada 1200 ticks en un radio de 24 bloques
    Reducción por estar bajo tierra: -2 cada 1200 ticks
    Reducción por nor dormir: -6 cada 1200 ticks multiplicado por la cantidad de tiempo sin dormir (Normalizado entre 0 y 1, 0 cuanto mas cerca de 1 dia, 1 cuanto mas cerca de 3 dias)
    Reducción por temperature (Tough as nails): -2 cada 1200 ticks

    Incremento cerca de aldeas: 1 cada 1200 ticks en un radio de 16 bloques
    Incremento al tener el estomago lleno: 2 cada 1200 ticks a partir del 50% de la barra llena (Se le aplica un multiplicador extra entre 0 y 1, cuanto mas cerca del porcentaje dado mas cerca de 0 esta, cuanto mas cerca del 100%, mas cerca del 1 es)
    Incremento al dormir: 20 multiplicado por cuanto % de noche has dormido
    Incremento cerca de mascotas: 2 cada 1200 ticks en un radio de 24 bloques
    Incremento al pescar: 2 por cada pez (Multiplicado dependiendo si es basura (x0), normal (x1) o tesoro (x2))
    Incremento al plantar: 1 cada cultivo (Se aplica un % aleatorio de veces, no siempre se aplica)
    Incremento al escuchar discos de musica: 1 cada 600 ticks en un radio de 24 bloques
    Incremento al obtener un logro: 6

     */

    @Override
    protected void registerServerOptions() {
        getServerConfig().registerOption("starting_sanity_value", 100, "Sanity given when the player first joins the world");
        getServerConfig().registerOption("max_sanity_value", 100, "Max sanity value");
        getServerConfig().registerOption("respawn_sanity_value", 0, "Sanity given when the player re-spawns");

        getServerConfig().registerOption("darkness_exposure_decrease_factor", 2, "How much sanity you lose at darkness");
        getServerConfig().registerOption("darkness_exposure_ticks", 200, "Every this amount of ticks the sanity reduction is applied");

        getServerConfig().registerOption("hostile_exposure_decrease_factor", 1, "How much sanity you lose near hostile mobs");
        getServerConfig().registerOption("hostile_check_ticks", 30, "Every this amount of ticks the sanity reduction is applied");
        getServerConfig().registerOption("hostile_check_radius", 24, "Radius in which the hostile mobs are detected");

        getServerConfig().registerOption("village_exposure_increase_factor", 1, "How much sanity you gain near a village");
        getServerConfig().registerOption("village_check_ticks", 1200, "Every this amount of ticks the sanity gain is applied");
        getServerConfig().registerOption("village_check_radius", 16, "Radius in which the villages are detected");

        getServerConfig().registerOption("portal_exposure_decrease_factor", 1, "How much sanity you lose near a portal");
        getServerConfig().registerOption("portal_check_ticks", 200, "Every this amount of ticks the sanity reduction is applied");
        getServerConfig().registerOption("portal_check_radius", 14, "Radius in which the portals are detected");

        getServerConfig().registerOption("scary_sound_decrease_factor", 10, "How much sanity you lose if you hear something strange");

        getServerConfig().registerOption("rain_decrease_factor", 1, "How much sanity you lose when raining");
        getServerConfig().registerOption("rain_check_ticks", 1200, "Every this amount of ticks the sanity reduction is applied");

        getServerConfig().registerOption("thunder_decrease_factor", 2, "How much sanity you lose when thundering");

        getServerConfig().registerOption("nether_decrease_factor", 1, "How much sanity you lose on the nether");
        getServerConfig().registerOption("nether_check_ticks", 1200, "Every this amount of ticks the sanity reduction is applied");

        getServerConfig().registerOption("end_decrease_factor", 2, "How much sanity you lose on the end");
        getServerConfig().registerOption("end_check_ticks", 1200, "Every this amount of ticks the sanity reduction is applied");

        getServerConfig().registerOption("damage_decrease_factor", 2, "How much sanity you lose per damage");

        getServerConfig().registerOption("kill_animal_decrease_factor", 4, "How much sanity you lose by killing animals");
        getServerConfig().registerOption("kill_villager_decrease_factor", 10, "How much sanity you lose by killing villagers");
        getServerConfig().registerOption("kill_player_decrease_factor", 10, "How much sanity you lose by killing players");
        getServerConfig().registerOption("kill_cute_decrease_factor", 6, "How much sanity you lose by killing cute creatures");

        getServerConfig().registerOption("alone_decrease_factor", 2, "How much sanity you lose being alone");
        getServerConfig().registerOption("alone_check_ticks", 1200, "Every this amount of ticks the sanity reduction is applied");
        getServerConfig().registerOption("alone_check_radius", 24, "Radius in which the players and villagers are detected");

        getServerConfig().registerOption("underground_decrease_factor", 2, "How much sanity you lose underground");
        getServerConfig().registerOption("underground_check_ticks", 1200, "Every this amount of ticks the sanity reduction is applied");

        getServerConfig().registerOption("satiety_increase_factor", 2, "How much sanity you gain with satiety");
        getServerConfig().registerOption("satiety_check_ticks", 1200, "Every this amount of ticks the sanity gain is applied");
        getServerConfig().registerOption("satiety_threshold", 0.50f, "Satiety threshold");

        getServerConfig().registerOption("sleep_increase_factor", 20, "How much sanity you gain by sleeping");

        getServerConfig().registerOption("sleep_deprived_decrease_factor", 6, "How much sanity you lose by not sleeping");
        getServerConfig().registerOption("sleep_deprived_check_ticks", 1200, "Every this amount of ticks the sanity reduction is applied");

        getServerConfig().registerOption("near_boss_decrease_factor", 3, "How much sanity you lose nearby a boss");
        getServerConfig().registerOption("near_boss_check_ticks", 200, "Every this amount of ticks the sanity reduction is applied");
        getServerConfig().registerOption("near_boss_check_radius", 64, "Radius in which the bosses are detected");

        getServerConfig().registerOption("near_pet_increase_factor", 2, "How much sanity you gain nearby a pet");
        getServerConfig().registerOption("near_pet_check_ticks", 1200, "Every this amount of ticks the sanity reduction is applied");
        getServerConfig().registerOption("near_pet_check_radius", 24, "Radius in which the pets are detected");

        getServerConfig().registerOption("fishing_increase_factor", 2, "How much sanity you gain by fishing");
        getServerConfig().registerOption("plating_increase_factor", 1, "How much sanity you gain by plating");

        getServerConfig().registerOption("music_increase_factor", 1, "How much sanity you gain by listing music disc");
        getServerConfig().registerOption("music_check_ticks", 600, "Every this amount of ticks the sanity gain is applied");
        getServerConfig().registerOption("music_check_radius", 24, "Radius in which the jukeboxes are detected");

        getServerConfig().registerOption("advancement_increase_factor", 6, "How much sanity you gain by getting an advancement");

        getServerConfig().registerOption("temperature_decrease_factor", 1, "How much sanity you lose with temperature");
        getServerConfig().registerOption("temperature_check_ticks", 1200, "Every this amount of ticks the sanity reduction is applied");
    }

    public int getMaxSanity() {
        return getServerConfig().get("max_sanity_value", Integer.class);
    }

    public int getStartingSanity() {
        return getServerConfig().get("starting_sanity_value", Integer.class);
    }

    public int getReSpawnSanity() {
        return getServerConfig().get("respawn_sanity_value", Integer.class);
    }

    public int getDarknessDecreaseFactor() {
        return getServerConfig().get("darkness_exposure_decrease_factor", Integer.class);
    }

    public int getDarknessTicks() {
        return getServerConfig().get("darkness_exposure_ticks", Integer.class);
    }

    public int getHostileMobDecreaseFactor() {
        return getServerConfig().get("hostile_exposure_decrease_factor", Integer.class);
    }

    public int getHostileMobTicks() {
        return getServerConfig().get("hostile_check_ticks", Integer.class);
    }

    public int getHostileMobRadius() {
        return getServerConfig().get("hostile_check_radius", Integer.class);
    }

    public int getEntityRadiusNoVision() {
        return 6;
    }

    public int getVillageGainFactor() {
        return getServerConfig().get("village_exposure_increase_factor", Integer.class);
    }

    public int getVillageTicks() {
        return getServerConfig().get("village_check_ticks", Integer.class);
    }

    public int getVillageRadius() {
        return getServerConfig().get("village_check_radius", Integer.class);
    }

    public int getPortalDecreaseFactor() {
        return getServerConfig().get("portal_exposure_decrease_factor", Integer.class);
    }

    public int getPortalTicks() {
        return getServerConfig().get("portal_check_ticks", Integer.class);
    }

    public int getPortalRadius() {
        return getServerConfig().get("portal_check_radius", Integer.class);
    }

    public int getScarySoundDecreaseFactor() {
        return getServerConfig().get("scary_sound_decrease_factor", Integer.class);
    }

    public int getRainDecreaseFactor() {
        return getServerConfig().get("rain_decrease_factor", Integer.class);
    }

    public int getRainTicks() {
        return getServerConfig().get("rain_check_ticks", Integer.class);
    }

    public int getThunderDecreaseFactor() {
        return getServerConfig().get("thunder_decrease_factor", Integer.class);
    }

    public int getNetherDecreaseFactor() {
        return getServerConfig().get("nether_decrease_factor", Integer.class);
    }

    public int getNetherTicks() {
        return getServerConfig().get("nether_check_ticks", Integer.class);
    }

    public int getEndDecreaseFactor() {
        return getServerConfig().get("end_decrease_factor", Integer.class);
    }

    public int getEndTicks() {
        return getServerConfig().get("end_check_ticks", Integer.class);
    }

    public int getDamageDecreaseFactor() {
        return getServerConfig().get("damage_decrease_factor", Integer.class);
    }

    public int getKillAnimalDecreaseFactor() {
        return getServerConfig().get("kill_animal_decrease_factor", Integer.class);
    }

    public int getKillVillagerDecreaseFactor() {
        return getServerConfig().get("kill_villager_decrease_factor", Integer.class);
    }

    public int getKillPlayerDecreaseFactor() {
        return getServerConfig().get("kill_player_decrease_factor", Integer.class);
    }

    public int getKillCuteDecreaseFactor() {
        return getServerConfig().get("kill_cute_decrease_factor", Integer.class);
    }

    public int getAloneDecreaseFactor() {
        return getServerConfig().get("alone_decrease_factor", Integer.class);
    }

    public int getAloneTicks() {
        return getServerConfig().get("alone_check_ticks", Integer.class);
    }

    public int getAloneRadius() {
        return getServerConfig().get("alone_check_radius", Integer.class);
    }

    public int getUndergroundDecreaseFactor() {
        return getServerConfig().get("underground_decrease_factor", Integer.class);
    }

    public int getUndergroundTicks() {
        return getServerConfig().get("underground_check_ticks", Integer.class);
    }

    public int getSatietyIncreaseFactor() {
        return getServerConfig().get("satiety_increase_factor", Integer.class);
    }

    public int getSatietyTicks() {
        return getServerConfig().get("satiety_check_ticks", Integer.class);
    }

    public float getSatietyThreshold() {
        return getServerConfig().get("satiety_threshold", Float.class);
    }

    public int getSleepIncreaseFactor() {
        return getServerConfig().get("sleep_increase_factor", Integer.class);
    }

    public int getSleepDeprivedDecreaseFactor() {
        return getServerConfig().get("sleep_deprived_decrease_factor", Integer.class);
    }

    public int getSleepDeprivedTicks() {
        return getServerConfig().get("sleep_deprived_check_ticks", Integer.class);
    }

    public int getBossDecreaseFactor() {
        return getServerConfig().get("near_boss_decrease_factor", Integer.class);
    }

    public int getBossTicks() {
        return getServerConfig().get("near_boss_check_ticks", Integer.class);
    }

    public int getBossRadius() {
        return getServerConfig().get("near_boss_check_radius", Integer.class);
    }

    public int getPetIncreaseFactor() {
        return getServerConfig().get("near_pet_increase_factor", Integer.class);
    }

    public int getPetTicks() {
        return getServerConfig().get("near_pet_check_ticks", Integer.class);
    }

    public int getPetRadius() {
        return getServerConfig().get("near_pet_check_radius", Integer.class);
    }

    public int getFishingIncreaseFactor() {
        return getServerConfig().get("fishing_increase_factor", Integer.class);
    }

    public int getPlatingIncreaseFactor() {
        return getServerConfig().get("plating_increase_factor", Integer.class);
    }

    public int getMusicIncreaseFactor() {
        return getServerConfig().get("music_increase_factor", Integer.class);
    }

    public int getMusicTicks() {
        return getServerConfig().get("music_check_ticks", Integer.class);
    }

    public int getMusicRadius() {
        return getServerConfig().get("music_check_radius", Integer.class);
    }

    public int getAdvancementIncreaseFactor() {
        return getServerConfig().get("advancement_increase_factor", Integer.class);
    }

    public int getTemperatureDecreaseFactor() {
        return getServerConfig().get("temperature_decrease_factor", Integer.class);
    }

    public int getTemperatureTicks() {
        return getServerConfig().get("temperature_check_ticks", Integer.class);
    }
}
