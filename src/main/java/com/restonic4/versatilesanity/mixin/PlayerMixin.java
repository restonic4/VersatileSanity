package com.restonic4.versatilesanity.mixin;

import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import com.restonic4.versatilesanity.config.VersatileSanityConfig;
import com.restonic4.versatilesanity.util.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        Level level = player.level();
        VersatileSanityConfig config = VersatileSanity.getConfig();

        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (shouldTick(player, config.getDarknessTicks()) && Utils.getLightLevel(player) <= 0) {
                System.out.println("[-] Darkness");
                SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getDarknessDecreaseFactor());
            }

            if (shouldTick(player, config.getHostileMobTicks()) && Utils.isPlayerNearHostileEntity(serverPlayer, config.getHostileMobRadius(), config.getHostileMobRadiusNoVision())) {
                System.out.println("[-] Hostile mob");
                SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getHostileMobDecreaseFactor());
            }

            if (shouldTick(player, config.getVillageTicks()) && Utils.isPlayerInOrNearVillage(serverPlayer, config.getVillageRadius())) {
                System.out.println("[+] Village");
                SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(config.getVillageGainFactor());
            }

            if (shouldTick(player, config.getPortalTicks()) && Utils.isPlayerNearPortal(serverPlayer, config.getPortalRadius())) {
                System.out.println("[-] Portal");
                SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getPortalDecreaseFactor());
            }

            if (shouldTick(player, config.getRainTicks()) && level.isThundering()) {
                if (level.isThundering()) {
                    System.out.println("[-] Thunder");
                    SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getThunderDecreaseFactor());
                } else if (level.isRaining()) {
                    System.out.println("[-] Rain");
                    SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getRainDecreaseFactor());
                }
            }

            if (level.dimension() == Level.NETHER && shouldTick(player, config.getNetherTicks())) {
                System.out.println("[-] Nether");
                SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getNetherDecreaseFactor());
            } else if (level.dimension() == Level.END && shouldTick(player, config.getEndTicks())) {
                System.out.println("[-] End");
                SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getEndDecreaseFactor());
            }

            SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(1);
        }
    }

    @Unique
    private boolean shouldTick(Player player, int ticks) {
        return player.tickCount % ticks == 0;
    }
}
