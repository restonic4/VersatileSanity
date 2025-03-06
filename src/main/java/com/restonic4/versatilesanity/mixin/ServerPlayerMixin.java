package com.restonic4.versatilesanity.mixin;

import com.restonic4.versatilesanity.modules.SanityEventHandler;
import com.restonic4.versatilesanity.util.MovingTracker;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements MovingTracker {
    @Unique
    private double lastTrackedX;
    @Unique
    private double lastTrackedY;
    @Unique
    private double lastTrackedZ;
    @Unique
    private boolean isMoving;


    @Inject(method = "dismountTo", at = @At("HEAD"))
    public void dismountTo(double x, double y, double z, CallbackInfo ci) {
        SanityEventHandler.onTeleport((ServerPlayer) (Object) this);
    }

    @Inject(method = "teleportTo(DDD)V", at = @At(value = "HEAD"))
    public void teleportTo(double d, double e, double f, CallbackInfo ci) {
        SanityEventHandler.onTeleport((ServerPlayer) (Object) this);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        double currentX = player.getX();
        double currentY = player.getY();
        double currentZ = player.getZ();

        // First tick, then 0
        if (lastTrackedX == 0 && lastTrackedY == 0 && lastTrackedZ == 0) {
            lastTrackedX = currentX;
            lastTrackedY = currentY;
            lastTrackedZ = currentZ;
            isMoving = false;
            return;
        }

        final double EPSILON = 0.0001;
        double dx = Math.abs(currentX - lastTrackedX);
        double dy = Math.abs(currentY - lastTrackedY);
        double dz = Math.abs(currentZ - lastTrackedZ);

        isMoving = dx > EPSILON || dy > EPSILON || dz > EPSILON;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        lastTrackedX = player.getX();
        lastTrackedY = player.getY();
        lastTrackedZ = player.getZ();
    }

    @Override
    public boolean versatileSanity$isMoving() {
        return isMoving;
    }
}
