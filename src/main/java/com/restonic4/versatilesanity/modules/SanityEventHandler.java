package com.restonic4.versatilesanity.modules;

import com.chaotic_loom.under_control.util.RandomHelper;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.components.HomeDetectionComponent;
import com.restonic4.versatilesanity.components.HomeDetectionComponents;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import com.restonic4.versatilesanity.config.VersatileSanityConfig;
import com.restonic4.versatilesanity.util.LootQualityChecker;
import com.restonic4.versatilesanity.util.Utils;
import com.restonic4.versatilesanity.util.WaterMassDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SanityEventHandler {
    public static ResourceLocation FISH_LOOT_TABLE = new ResourceLocation("minecraft", "gameplay/fishing/fish");
    public static ResourceLocation JUNK_LOOT_TABLE = new ResourceLocation("minecraft", "gameplay/fishing/junk");
    public static ResourceLocation TREASURE_LOOT_TABLE = new ResourceLocation("minecraft", "gameplay/fishing/treasure");

    public static VersatileSanityConfig config = VersatileSanity.getConfig();

    public static void onDarknessTick(Player player) {
        System.out.println("[-] Darkness");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getDarknessDecreaseFactor());
    }

    public static void onNearHostileMobTick(Player player) {
        System.out.println("[-] Hostile mob");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getHostileMobDecreaseFactor());
    }

    public static void onNearVillageTick(Player player) {
        System.out.println("[+] Village");
        SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(config.getVillageGainFactor());
    }

    public static void onNearPortalTick(Player player) {
        System.out.println("[-] Portal");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getPortalDecreaseFactor());
    }

    public static void onRainTick(Player player) {
        System.out.println("[-] Rain");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getRainDecreaseFactor());
    }

    public static void onThunderTick(Player player) {
        System.out.println("[-] Thunder");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getThunderDecreaseFactor());
    }

    public static void onNetherTick(Player player) {
        System.out.println("[-] Nether");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getNetherDecreaseFactor());
    }

    public static void onTheEndTick(Player player) {
        System.out.println("[-] End");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getEndDecreaseFactor());
    }

    public static void onBeingAloneTick(Player player) {
        System.out.println("[-] Alone");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getAloneDecreaseFactor());
    }

    public static void onScarySoundTick(Player player) {
        System.out.println("[-] Scary sound");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(VersatileSanity.getConfig().getScarySoundDecreaseFactor());
    }

    public static void onVillagerKilled(Player player) {
        System.out.println("[-] Kill villager");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getKillVillagerDecreaseFactor());
    }

    public static void onPlayerKilled(Player player) {
        System.out.println("[-] Kill player");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getKillPlayerDecreaseFactor());
    }

    public static void onAnimalKilled(Player player) {
        System.out.println("[-] Kill animal");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getKillAnimalDecreaseFactor());
    }

    public static void onCuteCreatureKilled(Player player) {
        System.out.println("[-] Kill cute");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getKillCuteDecreaseFactor());
    }

    public static void onDamage(Player player, float amount) {
        System.out.println("[-] Damage");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus((int) (config.getDamageDecreaseFactor() * amount));
    }

    public static void onUndergroundTick(Player player) {
        System.out.println("[-] Underground");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getUndergroundDecreaseFactor());
    }

    public static void onSatietyTick(Player player) {
        float mult = Utils.calculateHungerValue(player);
        int value = (int) (config.getSatietyIncreaseFactor() * mult);

        System.out.println("[+] Satiety " + value);
        SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(value);
    }

    public static void onCompleteSleep(Player player, float completionPercentage) {
        int value = (int) (config.getSleepIncreaseFactor() * completionPercentage);

        System.out.println("[+] Sleep: " + value + ", Sleeping %: " + completionPercentage);
        SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(value);
    }

    public static void onSleepDeprivedTick(Player player, float progress) {
        int value = (int) (config.getSleepDeprivedDecreaseFactor() * progress);

        if (value <= 0) {
            return;
        }

        System.out.println("[-] Sleep deprived: " + value + ", Progress %: " + progress);
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(value);
    }

    public static void onNearBossTick(Player player) {
        System.out.println("[-] Boss");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getBossDecreaseFactor());
    }

    public static void onNearPetTick(Player player) {
        System.out.println("[+] Pet");
        SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(config.getPetIncreaseFactor());
    }

    public static void onFishCaught(Player player, List<ItemStack> items) {
        MinecraftServer server = player.level().getServer();

        int accumulatedMult = 0;
        int multAppliedTimes = 0;

        for (ItemStack item : items) {
            if (Utils.isOnLootTable(item, JUNK_LOOT_TABLE, server)) {
                accumulatedMult += 0;
                multAppliedTimes++;
            } else if (Utils.isOnLootTable(item, FISH_LOOT_TABLE, server)) {
                accumulatedMult += 1;
                multAppliedTimes++;
            } else if (Utils.isOnLootTable(item, TREASURE_LOOT_TABLE, server)) {
                accumulatedMult += 2;
                multAppliedTimes++;
            }
        }

        int finalMult = accumulatedMult / multAppliedTimes;

        int value = config.getFishingIncreaseFactor() * finalMult;

        System.out.println("[+] Fish " + value);
        SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(value);
    }

    public static void onCropPlanted(ServerPlayer serverPlayer, BlockState placedState) {
        if (RandomHelper.randomBetween(0, 100) <= 25) {
            System.out.println("[+] Crop planted");
            SanityStatusComponents.SANITY_STATUS.get(serverPlayer).incrementSanityStatus(config.getPlantingIncreaseFactor());
        }
    }

    public static void onMusicDiscPlaying(Player player, boolean shouldDecreaseSanity) {
        if (shouldDecreaseSanity) {
            System.out.println("[-] Music disc");
            SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getMusicIncreaseFactor());
        } else {
            System.out.println("[+] Music disc");
            SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(config.getMusicIncreaseFactor());
        }
    }

    public static void onAdvancementMade(ServerPlayer player) {
        System.out.println("[+] Advancement");
        SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(config.getAdvancementIncreaseFactor());
    }

    public static void onTemperatureTick(Player player) {
        System.out.println("[-] Temperature");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getTemperatureDecreaseFactor());
    }

    public static void onOceanTick(Player player, WaterMassDetector.WaterMassDetectionResult result) {
        float mult = (result.isSubmergedEnough()) ? config.getOceanSubmergedMult() : 1;

        System.out.println("[-] Ocean");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus((int) (config.getOceanDecreaseFactor() * mult));
    }

    public static void onNewLootFound(Player player, LootQualityChecker.Quality quality) {
        float value = config.getNewLootFactor();

        if (quality == LootQualityChecker.Quality.TERRIBLE) {
            value = value * -2;
        } else if (quality == LootQualityChecker.Quality.BAD) {
            value = value * -1;
        } else if (quality == LootQualityChecker.Quality.NORMAL) {
            value = value * 1;
        } else if (quality == LootQualityChecker.Quality.GOOD) {
            value = value * 2;
        } else if (quality == LootQualityChecker.Quality.AMAZING) {
            value = value * 3;
        }

        if (value < 0) {
            System.out.println("[-] New loot");
        } else {
            System.out.println("[+] New loot");
        }

        SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus((int) value);
    }

    public static void onExperienceLevel(Player player, int levels) {
        if (levels < 0) {
            System.out.println("[-] Experience level");
            SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(config.getNewExperienceLevelIncreaseFactor() * (levels / 3));
        } else {
            System.out.println("[+] Experience level");
            SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(config.getNewExperienceLevelIncreaseFactor() * levels);
        }
    }

    public static void onCobWebTick(Player player) {
        System.out.println("[-] Cobweb");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getReducedMovementDecreaseFactor());
    }

    public static void onSpecialFoodEaten(Player player, boolean good) {
        if (good) {
            System.out.println("[+] Special food eaten");
            SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(config.getEatingSpecialFactor());
        } else {
            System.out.println("[-] Special food eaten");
            SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getEatingSpecialFactor());
        }
    }

    public static void onTeleport(Player player) {
        System.out.println("[-] Teleport");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getTeleportedDecreaseFactor());
    }

    public static void onVillagerTrade(Player player) {
        System.out.println("[+] Villager trade");
        SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(config.getVillagerTradeIncreaseFactor());
    }

    public static void onAnimalFeed(Player player) {
        System.out.println("[+] Animal feed");
        SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(config.getAnimalFeedIncreaseFactor());
    }

    public static void onParrotPoisoned(Player player) {
        System.out.println("[-] Parrot poisoned");
        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getAnimalFeedIncreaseFactor());
    }

    public static void onHomeTick(Player player) {
        System.out.println("[+] At home ( Score: " + HomeDetectionComponents.HOME_DETECTION.get(player).getScoreAtCurrentPosition(player.blockPosition(), HomeDetectionComponent.DETECTION_RANGE) + " )");
        SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(config.getVillageGainFactor());
    }

    public static void geoKill(Player player) {
        if (player == null) return;

        SanityStatusComponents.SANITY_STATUS.get(player).setKilledByGeo(true);

        Inventory inventory = player.getInventory();

        int removedItemCount = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (isSlotProtected(player, i)) continue;

            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                removedItemCount += stack.getCount();
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }

        if (removedItemCount > 0) {
            ItemStack rottenFleshStack = new ItemStack(Items.ROTTEN_FLESH, removedItemCount);
            inventory.add(rottenFleshStack);
        }

        player.containerMenu.broadcastChanges();
        player.kill();
    }

    private static boolean isSlotProtected(Player player, int slot) {
        return slot >= 36 || slot == player.getInventory().selected;
    }
}
