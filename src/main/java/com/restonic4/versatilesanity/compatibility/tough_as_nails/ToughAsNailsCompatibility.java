package com.restonic4.versatilesanity.compatibility.tough_as_nails;

import net.minecraft.world.entity.player.Player;
import toughasnails.api.temperature.TemperatureHelper;
import toughasnails.api.temperature.TemperatureLevel;

public class ToughAsNailsCompatibility {
    public static void onInitialize() {
        System.out.println("Now temperature affects your sanity!");
    }

    public static boolean shouldTemperatureDecreaseSanity(Player player) {
        TemperatureLevel temperatureLevel = TemperatureHelper.getTemperatureForPlayer(player);
        return temperatureLevel == TemperatureLevel.ICY || temperatureLevel == TemperatureLevel.HOT;
    }
}
