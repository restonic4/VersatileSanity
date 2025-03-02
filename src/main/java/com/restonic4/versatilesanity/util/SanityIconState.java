package com.restonic4.versatilesanity.util;

public class SanityIconState {
    private float currentOffset;
    private float targetOffset;
    private long startTick;
    private long targetTick;

    public float getCurrentOffset() {
        return currentOffset;
    }

    public void setCurrentOffset(float currentOffset) {
        this.currentOffset = currentOffset;
    }

    public float getTargetOffset() {
        return targetOffset;
    }

    public void setTargetOffset(float targetOffset) {
        this.targetOffset = targetOffset;
    }

    public long getTargetTick() {
        return targetTick;
    }

    public void setTargetTick(long targetTick) {
        this.targetTick = targetTick;
    }

    public long getStartTick() {
        return startTick;
    }

    public void setStartTick(long startTick) {
        this.startTick = startTick;
    }
}
