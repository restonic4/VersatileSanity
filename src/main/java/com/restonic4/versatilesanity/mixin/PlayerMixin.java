package com.restonic4.versatilesanity.mixin;

import com.mojang.datafixers.util.Either;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import com.restonic4.versatilesanity.config.VersatileSanityConfig;
import com.restonic4.versatilesanity.modules.SanityEventHandler;
import com.restonic4.versatilesanity.modules.SleepHandler;
import com.restonic4.versatilesanity.util.UndergroundDetector;
import com.restonic4.versatilesanity.util.Utils;
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
        }
    }

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    public void hurt(DamageSource damageSource, float amount, CallbackInfo ci) {
        VersatileSanity.onEntityDamage((Player) (Object) this, amount);
    }

    @Inject(method = "startSleepInBed", at = @At("RETURN"))
    private void onStartSleepInBed(BlockPos blockPos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        Player player = (Player) (Object) this;
        SleepHandler.recordSleepStart(player);
    }

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void onStopSleepInBed(boolean bl, boolean bl2, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        SleepHandler.handleSleepEnd(player);
    }

    @Unique
    private boolean shouldTick(Player player, int ticks) {
        return player.tickCount % ticks == 0;
    }
}
