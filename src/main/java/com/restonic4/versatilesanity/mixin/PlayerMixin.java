package com.restonic4.versatilesanity.mixin;

import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.compatibility.CompatibleMods;
import com.restonic4.versatilesanity.compatibility.tough_as_nails.ToughAsNailsCompatibility;
import com.restonic4.versatilesanity.components.HomeDetectionComponent;
import com.restonic4.versatilesanity.components.HomeDetectionComponents;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import com.restonic4.versatilesanity.config.VersatileSanityConfig;
import com.restonic4.versatilesanity.modules.*;
import com.restonic4.versatilesanity.modules.hallucinations.CreepySoundManager;
import com.restonic4.versatilesanity.modules.hallucinations.FireManager;
import com.restonic4.versatilesanity.modules.hallucinations.ScreenShaker;
import com.restonic4.versatilesanity.modules.hallucinations.WhispersManager;
import com.restonic4.versatilesanity.util.UndergroundDetector;
import com.restonic4.versatilesanity.util.Utils;
import com.restonic4.versatilesanity.util.WaterMassDetector;
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
                SanityEventHandler.onDarknessTick(player);
            }

            if (shouldTick(player, config.getHostileMobTicks()) && Utils.isPlayerNearHostileEntity(serverPlayer, config.getHostileMobRadius(), config.getEntityRadiusNoVision())) {
                SanityEventHandler.onNearHostileMobTick(player);
            }

            if (shouldTick(player, config.getVillageTicks()) && Utils.isPlayerInOrNearVillage(serverPlayer, config.getVillageRadius())) {
                SanityEventHandler.onNearVillageTick(player);
            }

            if (shouldTick(player, config.getPortalTicks()) && Utils.isPlayerNearPortal(serverPlayer, config.getPortalRadius())) {
                SanityEventHandler.onNearPortalTick(player);
            }

            if (shouldTick(player, config.getRainTicks()) && level.isThundering()) {
                if (level.isThundering()) {
                    SanityEventHandler.onThunderTick(player);
                } else if (level.isRaining()) {
                    SanityEventHandler.onRainTick(player);
                }
            }

            if (level.dimension() == Level.NETHER && shouldTick(player, config.getNetherTicks())) {
                SanityEventHandler.onNetherTick(player);
            } else if (level.dimension() == Level.END && shouldTick(player, config.getEndTicks())) {
                SanityEventHandler.onTheEndTick(player);
            }

            if (shouldTick(player, config.getAloneTicks()) && !Utils.isPlayerNearPlayerOrVillagerEntity(serverPlayer, config.getAloneRadius(), config.getEntityRadiusNoVision())) {
                SanityEventHandler.onBeingAloneTick(player);
            }

            if (shouldTick(player, config.getUndergroundTicks()) && UndergroundDetector.isPlayerUnderground(player)) {
                SanityEventHandler.onUndergroundTick(player);
            }

            if (shouldTick(player, config.getSatietyTicks())) {
                SanityEventHandler.onSatietyTick(player);
            }

            if (shouldTick(player, config.getSleepDeprivedTicks())) {
                SleepHandler.applyEepySleepy(serverPlayer);
            }

            if (shouldTick(player, config.getBossTicks()) && Utils.isPlayerNearBossEntity(serverPlayer, config.getBossRadius(), config.getEntityRadiusNoVision())) {
                SanityEventHandler.onNearBossTick(player);
            }

            if (shouldTick(player, config.getPetTicks())) {
                if (Utils.isPlayerNearPetEntity(serverPlayer, config.getPetRadius(), config.getEntityRadiusNoVision())) {
                    SanityEventHandler.onNearPetTick(player);
                } else if(Utils.hasParrotsOnShoulders(player)) {
                    SanityEventHandler.onNearPetTick(player);
                }
            }

            if (shouldTick(player, config.getMusicTicks())) {
                boolean[] flags = Utils.isNearPlayingJukebox(player, config.getMusicRadius());

                if (flags[0]) {
                    SanityEventHandler.onMusicDiscPlaying(player, flags[1]);
                }
            }

            if (shouldTick(player, config.getTemperatureTicks())) {
                if (VersatileSanity.isModLoaded(CompatibleMods.TOUGH_AS_NAILS) && ToughAsNailsCompatibility.shouldTemperatureDecreaseSanity(player)) {
                    SanityEventHandler.onTemperatureTick(player);
                }
            }

            if (shouldTick(player, config.getOceanTicks())) {
                WaterMassDetector.WaterMassDetectionResult result = WaterMassDetector.isInDeepOcean(serverPlayer);

                if (result.isInDeepOcean()) {
                    SanityEventHandler.onOceanTick(player, result);
                }
            }

            if (shouldTick(player, config.getReducedMovementTicks()) && Utils.isPlayerMoving(player) && Utils.isInCobweb(player)) {
                SanityEventHandler.onCobWebTick(player);
            }

            if (SanityStatusComponents.SANITY_STATUS.get(player).getSanityStatus() < config.getMaxSanity() / 2 && shouldTick(player, config.getInventoryShuffleTicks())) {
                Utils.shuffleInventory(player, 6);
            }

            if (shouldTick(player, config.getVillageTicks()) && HomeDetectionComponents.HOME_DETECTION.get(player).isHome(player.blockPosition(), HomeDetectionComponent.DETECTION_RANGE)) {
                SanityEventHandler.onHomeTick(player);
            }
        } else if (player.level().isClientSide()) {
            CreepySoundManager.tick();
            ScreenShaker.tick();
            WhispersManager.tick();
            FireManager.tick();
        }
    }

    @Inject(method = "giveExperienceLevels", at = @At("HEAD"))
    public void giveExperienceLevels(int levels, CallbackInfo ci) {
        SanityEventHandler.onExperienceLevel((Player) (Object) this, levels);
    }

    @Unique
    private boolean shouldTick(Player player, int ticks) {
        return player.tickCount % ticks == 0;
    }
}
