package com.restonic4.versatilesanity.modules;

import com.chaotic_loom.under_control.util.MathHelper;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.networking.ClientSanityManager;
import net.minecraft.world.entity.player.Player;

import java.util.logging.Level;

public class PlayerVisibilityController {
    public int getMissingPlayers(Player player) {
        if (!player.level().isClientSide() || player.getServer() == null) {
            return 0;
        }

        float progress = (float) (1 - MathHelper.normalize(ClientSanityManager.getSanity(), VersatileSanity.getConfig().getMinSanity(), 0));
        int numPlayers = player.getServer().getPlayerCount();

        int missingPlayers = (int) (numPlayers * progress);

        return missingPlayers;
    }
}
