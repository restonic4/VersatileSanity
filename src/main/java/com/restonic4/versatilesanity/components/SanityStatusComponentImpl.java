package com.restonic4.versatilesanity.components;

import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.networking.SanityStatusBarNetworking;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class SanityStatusComponentImpl implements SanityStatusComponent, AutoSyncedComponent {

    private final Player player;
    private int sanity;
    private boolean wasKilledByGeo;

    public SanityStatusComponentImpl(Player player) {
        this.player = player;
        this.sanity = VersatileSanity.getConfig().getStartingSanity();
        this.wasKilledByGeo = false;
    }

    @Override
    public int getSanityStatus() {
        return sanity;
    }

    @Override
    public float getSanityPercentage() {
        return (float) sanity / VersatileSanity.getConfig().getMaxSanity();
    }

    @Override
    public void setSanityStatus(int value) {
        this.sanity = Math.max(VersatileSanity.getConfig().getMinSanity(), Math.min(value, VersatileSanity.getConfig().getMaxSanity()));
        sync();
    }

    @Override
    public void decrementSanityStatus(int amount) {
        this.setSanityStatus(this.sanity - amount);
        sync();
    }

    @Override
    public void incrementSanityStatus(int amount) {
        this.setSanityStatus(this.sanity + amount);
        sync();
    }

    @Override
    public void setKilledByGeo(boolean value) {
        this.wasKilledByGeo = value;
        sync();
    }

    @Override
    public boolean wasKilledByGeo() {
        return wasKilledByGeo;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("Sanity")) {
            this.sanity = tag.getInt("Sanity");
        } else {
            this.sanity = VersatileSanity.getConfig().getStartingSanity();
        }

        if (tag.contains("KilledByGeo")) {
            this.wasKilledByGeo = tag.getBoolean("KilledByGeo");
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putInt("Sanity", this.sanity);
        tag.putBoolean("KilledByGeo", this.wasKilledByGeo);
    }

    @Override
    public void sync() {
        SanityStatusBarNetworking.syncCustomStatus(player, sanity, wasKilledByGeo);
    }
}
