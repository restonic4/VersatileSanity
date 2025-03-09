package com.restonic4.versatilesanity.config;

import com.chaotic_loom.under_control.config.ModConfig;
import com.restonic4.versatilesanity.VersatileSanity;

public class VersatileSanityConfig extends ModConfig {
    public VersatileSanityConfig() {
        super(VersatileSanity.MOD_ID);
    }

    @Override
    protected void registerClientOptions() {
        getClientConfig().registerOption("debug", "debug_ocean", false);
        getClientConfig().registerOption("debug", "debug_cave", false);
    }

    public boolean getDebugOcean() {
        return getClientConfig().get("debug_ocean", Boolean.class);
    }

    public boolean getDebugCave() {
        return getClientConfig().get("debug_cave", Boolean.class);
    }

    /*
    Tengo una mecánica en mi mod de Minecraft, cordura, siento que faltan posibles metodos/aciones/eventos en los que la cordura suba o baje.
    Dame mas ideas, ya sean vanilla o compatibilidad con otros mods.

    Eventos actuales:

    ###########

    Tengo una mecánica en mi mod de Minecraft, cordura, el problema es que no se como balancearla, hay mil valores, hay muchas cosas que dan o restan cordura.

    ¿Hay algún método o planificación eficaz que pueda usar para poner unos valores aceptables?

    Valores de ejemplo:

    ###########

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
    Reducción por estar en el oceano: -1 cada 80 ticks (Multiplicado x 3 si estas sumergido)
    Reducción por caminar en cobweb
    Reducción al tp
    Reducción al alimentar una galleta a un loro

    Incremento cerca de aldeas: 1 cada 1200 ticks en un radio de 16 bloques
    Incremento al tener el estomago lleno: 2 cada 1200 ticks a partir del 50% de la barra llena (Se le aplica un multiplicador extra entre 0 y 1, cuanto mas cerca del porcentaje dado mas cerca de 0 esta, cuanto mas cerca del 100%, mas cerca del 1 es)
    Incremento al dormir: 20 multiplicado por cuanto % de noche has dormido
    Incremento cerca de mascotas: 2 cada 1200 ticks en un radio de 24 bloques
    Incremento al pescar: 2 por cada pez (Multiplicado dependiendo si es basura (x0), normal (x1) o tesoro (x2))
    Incremento al plantar: 1 cada cultivo (Se aplica un % aleatorio de veces, no siempre se aplica)
    Incremento al escuchar discos de musica: 1 cada 600 ticks en un radio de 24 bloques
    Incremento al obtener un logro: 6
    Incremento al obtener 1 nivel de experiencia: 1
    Incremento al tradear con aldeanos
    Incremento al alimentar animales
    Incremento al estar en casa

    Incremento o Reduction al encontrar loot en cofres: Terrible: -2, Malo: -1, Normal: 1, Bueno: 2, Increible: 3 (Calidad del loot)
    Incremento o reduction al comer items especiales, rotten flesh, golden apple...

     */

    @Override
    protected void registerServerOptions() {
        getServerConfig().registerOption("global", "starting_sanity_value", 1000);
        getServerConfig().registerOption("global", "max_sanity_value", 1000);
        getServerConfig().registerOption("global", "respawn_sanity_value", 0);

        getServerConfig().registerOption("darkness", "darkness_exposure_decrease_factor", 2);
        getServerConfig().registerOption("darkness", "darkness_exposure_ticks", 200);

        getServerConfig().registerOption("hostile_mobs", "hostile_exposure_decrease_factor", 4);
        getServerConfig().registerOption("hostile_mobs", "hostile_check_ticks", 50);
        getServerConfig().registerOption("hostile_mobs", "hostile_check_radius", 24);

        getServerConfig().registerOption("village", "village_exposure_increase_factor", 1);
        getServerConfig().registerOption("village", "village_check_ticks", 1200);
        getServerConfig().registerOption("village", "village_check_radius", 16);
        getServerConfig().registerOption("village", "villager_trade_increase_factor", 2);

        getServerConfig().registerOption("portals", "portal_exposure_decrease_factor", 2);
        getServerConfig().registerOption("portals", "portal_check_ticks", 200);
        getServerConfig().registerOption("portals", "portal_check_radius", 14);

        getServerConfig().registerOption("sounds", "scary_sound_decrease_factor", 50);

        getServerConfig().registerOption("weather", "rain_decrease_factor", 1);
        getServerConfig().registerOption("weather", "rain_check_ticks", 1200);

        getServerConfig().registerOption("weather", "thunder_decrease_factor", 2);

        getServerConfig().registerOption("dimensions", "nether_decrease_factor", 2);
        getServerConfig().registerOption("dimensions", "nether_check_ticks", 1200);

        getServerConfig().registerOption("dimensions", "end_decrease_factor", 4);
        getServerConfig().registerOption("dimensions", "end_check_ticks", 1200);

        getServerConfig().registerOption("damage", "damage_decrease_factor", 10);

        getServerConfig().registerOption("killing", "kill_animal_decrease_factor", 20);
        getServerConfig().registerOption("killing", "kill_villager_decrease_factor", 50);
        getServerConfig().registerOption("killing", "kill_player_decrease_factor", 50);
        getServerConfig().registerOption("killing", "kill_cute_decrease_factor", 35);

        getServerConfig().registerOption("alone", "alone_decrease_factor", 4);
        getServerConfig().registerOption("alone", "alone_check_ticks", 1200);
        getServerConfig().registerOption("alone", "alone_check_radius", 24);

        getServerConfig().registerOption("underground", "underground_decrease_factor", 2);
        getServerConfig().registerOption("underground", "underground_check_ticks", 1200);

        getServerConfig().registerOption("hunger_satiety", "satiety_increase_factor", 4);
        getServerConfig().registerOption("hunger_satiety", "satiety_check_ticks", 1200);
        getServerConfig().registerOption("hunger_satiety", "satiety_threshold", 0.50f);
        getServerConfig().registerOption("hunger_satiety", "eating_special_factor", 2);

        getServerConfig().registerOption("sleeping", "sleep_increase_factor", 500);

        getServerConfig().registerOption("sleeping", "sleep_deprived_decrease_factor", 6);
        getServerConfig().registerOption("sleeping", "sleep_deprived_check_ticks", 1200);

        getServerConfig().registerOption("bosses", "near_boss_decrease_factor", 10);
        getServerConfig().registerOption("bosses", "near_boss_check_ticks", 200);
        getServerConfig().registerOption("bosses", "near_boss_check_radius", 64);

        getServerConfig().registerOption("pet", "near_pet_increase_factor", 2);
        getServerConfig().registerOption("pet", "near_pet_check_ticks", 1200);
        getServerConfig().registerOption("pet", "near_pet_check_radius", 24);

        getServerConfig().registerOption("farming", "fishing_increase_factor", 2);
        getServerConfig().registerOption("farming", "planting_increase_factor", 1);
        getServerConfig().registerOption("farming", "animal_feed_increase_factor", 1);

        getServerConfig().registerOption("music", "music_increase_factor", 1);
        getServerConfig().registerOption("music", "music_check_ticks", 600);
        getServerConfig().registerOption("music", "music_check_radius", 24);

        getServerConfig().registerOption("advancements", "advancement_increase_factor", 6);

        getServerConfig().registerOption("temperature", "temperature_decrease_factor", 1);
        getServerConfig().registerOption("temperature", "temperature_check_ticks", 1200);

        getServerConfig().registerOption("ocean", "ocean_decrease_factor", 1);
        getServerConfig().registerOption("ocean", "ocean_check_ticks", 100);
        getServerConfig().registerOption("ocean", "ocean_submerged_mult", 3);

        getServerConfig().registerOption("loot", "new_loot_factor", 2);
        getServerConfig().registerOption("loot", "new_experience_level_increase_factor", 2);

        getServerConfig().registerOption("movement", "reduced_movement_decrease_factor", 1);
        getServerConfig().registerOption("movement", "reduced_movement_decrease_ticks", 40);
        getServerConfig().registerOption("movement", "teleported_decrease_factor", 6);
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

    public int getPlantingIncreaseFactor() {
        return getServerConfig().get("planting_increase_factor", Integer.class);
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

    public int getOceanDecreaseFactor() {
        return getServerConfig().get("ocean_decrease_factor", Integer.class);
    }

    public int getOceanTicks() {
        return getServerConfig().get("ocean_check_ticks", Integer.class);
    }

    public int getOceanSubmergedMult() {
        return getServerConfig().get("ocean_submerged_mult", Integer.class);
    }

    public int getNewLootFactor() {
        return getServerConfig().get("new_loot_factor", Integer.class);
    }

    public int getNewExperienceLevelIncreaseFactor() {
        return getServerConfig().get("new_experience_level_increase_factor", Integer.class);
    }

    public int getReducedMovementDecreaseFactor() {
        return getServerConfig().get("reduced_movement_decrease_factor", Integer.class);
    }

    public int getReducedMovementTicks() {
        return getServerConfig().get("reduced_movement_decrease_ticks", Integer.class);
    }

    public int getEatingSpecialFactor() {
        return getServerConfig().get("eating_special_factor", Integer.class);
    }

    public int getTeleportedDecreaseFactor() {
        return getServerConfig().get("teleported_decrease_factor", Integer.class);
    }

    public int getVillagerTradeIncreaseFactor() {
        return getServerConfig().get("villager_trade_increase_factor", Integer.class);
    }

    public int getAnimalFeedIncreaseFactor() {
        return getServerConfig().get("animal_feed_increase_factor", Integer.class);
    }

    public int getInventoryShuffleTicks() {
        return 400;
    }
}
