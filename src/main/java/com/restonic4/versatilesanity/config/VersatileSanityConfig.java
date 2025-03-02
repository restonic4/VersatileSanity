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
        getServerConfig().registerOption("thunder_check_ticks", 1200, "Every this amount of ticks the sanity reduction is applied");

        getServerConfig().registerOption("nether_decrease_factor", 1, "How much sanity you lose on the nether");
        getServerConfig().registerOption("nether_check_ticks", 1200, "Every this amount of ticks the sanity reduction is applied");

        getServerConfig().registerOption("end_decrease_factor", 2, "How much sanity you lose on the end");
        getServerConfig().registerOption("end_check_ticks", 1200, "Every this amount of ticks the sanity reduction is applied");

        getServerConfig().registerOption("damage_decrease_factor", 2, "How much sanity you lose per damage");

        getServerConfig().registerOption("kill_animal_decrease_factor", 4, "How much sanity you lose by killing animals");
        getServerConfig().registerOption("kill_villager_decrease_factor", 10, "How much sanity you lose by killing villagers");
        getServerConfig().registerOption("kill_player_decrease_factor", 10, "How much sanity you lose by killing players");
        getServerConfig().registerOption("kill_cute_decrease_factor", 6, "How much sanity you lose by killing cute creatures");
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

    public int getHostileMobRadiusNoVision() {
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

    public int getThunderTicks() {
        return getServerConfig().get("thunder_check_ticks", Integer.class);
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
}
