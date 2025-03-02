package com.restonic4.versatilesanity.networking;

public class ClientSanityManager {
    private static int sanity = 20;
    private static int glowTicks = 0;

    public static int getSanity() {
        return sanity;
    }

    public static void setSanity(int value) {
        sanity = value;
        glowTicks = 40;
    }

    public static int getGlowTicks() {
        return glowTicks;
    }

    public static void consumeGlowTick() {
        glowTicks--;
    }
}
