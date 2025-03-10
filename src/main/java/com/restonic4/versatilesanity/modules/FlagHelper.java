package com.restonic4.versatilesanity.modules;

import com.restonic4.versatilesanity.VersatileSanity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.IOException;

public class FlagHelper {
    private static final String FLAG_FILE = VersatileSanity.MOD_ID + "_flags.dat";
    private static CompoundTag flagData = new CompoundTag();

    public static boolean get(String flag) {
        loadFlags();
        return flagData.contains(flag) && flagData.getBoolean(flag);
    }

    public static String getString(String flag) {
        loadFlags();
        return flagData.getString(flag);
    }

    public static void set(String flag, boolean value) {
        loadFlags();
        flagData.putBoolean(flag, value);
        saveFlags();
    }

    public static void set(String flag, String value) {
        loadFlags();
        flagData.putString(flag, value);
        saveFlags();
    }

    public static void remove(String flag) {
        loadFlags();
        if (flagData.contains(flag)) {
            flagData.remove(flag);
            saveFlags();
        }
    }

    private static void loadFlags() {
        File flagFile = new File(FabricLoader.getInstance().getGameDir().toFile(), FLAG_FILE);
        if (flagFile.exists()) {
            try {
                CompoundTag loadedTag = NbtIo.read(flagFile);
                if (loadedTag != null) {
                    flagData = loadedTag;
                }
            } catch (IOException e) {
                System.out.println("Error loading flags");
                e.printStackTrace();
            }
        }
    }

    private static void saveFlags() {
        try {
            File flagFile = new File(FabricLoader.getInstance().getGameDir().toFile(), FLAG_FILE);
            NbtIo.write(flagData, flagFile);
        } catch (IOException e) {
            System.out.println("Error saving flags");
            e.printStackTrace();
        }
    }
}
