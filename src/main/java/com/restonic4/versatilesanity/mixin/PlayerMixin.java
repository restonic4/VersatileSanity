package com.restonic4.versatilesanity.mixin;

import com.mojang.datafixers.util.Either;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.compatibility.CompatibleMods;
import com.restonic4.versatilesanity.compatibility.tough_as_nails.ToughAsNailsCompatibility;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import com.restonic4.versatilesanity.config.VersatileSanityConfig;
import com.restonic4.versatilesanity.modules.SanityEventHandler;
import com.restonic4.versatilesanity.modules.SleepHandler;
import com.restonic4.versatilesanity.util.UndergroundDetector;
import com.restonic4.versatilesanity.util.Utils;
import com.restonic4.versatilesanity.util.WaterMassDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

            /*int waterLevel = WaterMassDetector.calculateWaterDepth(serverPlayer.level(), serverPlayer.blockPosition());
            boolean is = WaterMassDetector.isPlayerSubmergedEnough(serverPlayer, waterLevel);
            boolean biome = WaterMassDetector.isInWaterBiome(serverPlayer);
            boolean around = WaterMassDetector.hasEnoughWaterAround(serverPlayer.level(), serverPlayer.blockPosition());

            System.out.println("Biome: " + biome + ", Around: " + around + ", Level: " + waterLevel + ", Submerged: " + is + ", Water: " + serverPlayer.isInWater());*/
            WaterMassDetector.WaterMassDetectionResult result = WaterMassDetector.isInDeepOcean(serverPlayer);
            System.out.println("Ocean: " + result.isInDeepOcean() + ", Submerged: " + result.isSubmergedEnough() + ", Trust: " + result.threshold());
        }
    }

    @Unique
    private boolean shouldTick(Player player, int ticks) {
        return player.tickCount % ticks == 0;
    }
}
