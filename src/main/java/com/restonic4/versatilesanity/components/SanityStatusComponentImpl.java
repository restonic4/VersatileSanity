package com.restonic4.versatilesanity.components;

import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.networking.SanityStatusBarNetworking;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class SanityStatusComponentImpl implements SanityStatusComponent, AutoSyncedComponent {

    private final Player player;
    private int sanity;

    public SanityStatusComponentImpl(Player player) {
        this.player = player;
        this.sanity = VersatileSanity.getConfig().getStartingSanity();
    }

    @Override
    public int getSanityStatus() {
        return sanity;
    }

    @Override
    public void setSanityStatus(int value) {
        this.sanity = Math.min(value, VersatileSanity.getConfig().getMaxSanity());
        sync();
    }

    @Override
    public void decrementSanityStatus(int amount) {
        this.sanity = Math.max(-(VersatileSanity.getConfig().getMaxSanity() / 10), this.sanity - amount);
        sync();
    }

    @Override
    public void incrementSanityStatus(int amount) {
        this.sanity = Math.min(VersatileSanity.getConfig().getMaxSanity(), this.sanity + amount);
        sync();
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("Sanity")) {
            this.sanity = tag.getInt("Sanity");
        } else {
            this.sanity = VersatileSanity.getConfig().getStartingSanity();
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putInt("Sanity", this.sanity);
    }

    @Override
    public void sync() {
        SanityStatusBarNetworking.syncCustomStatus(player, sanity);
    }
}
