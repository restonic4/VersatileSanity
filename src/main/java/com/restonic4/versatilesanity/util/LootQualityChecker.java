package com.restonic4.versatilesanity.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashMap;
import java.util.Map;

public class LootQualityChecker {
    private static final Map<Item, Integer> ITEM_SCORES = new HashMap<>();
    static {
        // Good Items:
        ITEM_SCORES.put(Items.ENCHANTED_GOLDEN_APPLE, 8);
        ITEM_SCORES.put(Items.GOLDEN_APPLE, 6);
        ITEM_SCORES.put(Items.ENCHANTED_BOOK, 6);
        ITEM_SCORES.put(Items.GOLDEN_CARROT, 4);

        ITEM_SCORES.put(Items.GOLD_BLOCK, 4);
        ITEM_SCORES.put(Items.IRON_BLOCK, 3);

        ITEM_SCORES.put(Items.NETHERITE_INGOT, 7);
        ITEM_SCORES.put(Items.NETHERITE_SCRAP, 5);
        ITEM_SCORES.put(Items.ANCIENT_DEBRIS, 4);
        ITEM_SCORES.put(Items.DIAMOND, 5);
        ITEM_SCORES.put(Items.EMERALD, 3);
        ITEM_SCORES.put(Items.GOLD_INGOT, 3);
        ITEM_SCORES.put(Items.IRON_INGOT, 2);
        ITEM_SCORES.put(Items.REDSTONE, 2);
        ITEM_SCORES.put(Items.LAPIS_LAZULI, 2);
        ITEM_SCORES.put(Items.COAL, 1);
        ITEM_SCORES.put(Items.AMETHYST_SHARD, 1);

        ITEM_SCORES.put(Items.NAME_TAG, 3);
        ITEM_SCORES.put(Items.SADDLE, 3);
        ITEM_SCORES.put(Items.LEAD, 2);
        ITEM_SCORES.put(Items.EXPERIENCE_BOTTLE, 4);
        ITEM_SCORES.put(Items.ECHO_SHARD, 4);
        ITEM_SCORES.put(Items.HEART_OF_THE_SEA, 3);

        ITEM_SCORES.put(Items.DIAMOND_HORSE_ARMOR, 6);
        ITEM_SCORES.put(Items.IRON_HORSE_ARMOR, 3);
        ITEM_SCORES.put(Items.GOLDEN_HORSE_ARMOR, 4);
        ITEM_SCORES.put(Items.LEATHER_HORSE_ARMOR, 1);

        ITEM_SCORES.put(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, 3);
        ITEM_SCORES.put(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, 3);

        addDisc(ITEM_SCORES, Items.MUSIC_DISC_13, 4);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_11, 4);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_5, 4);

        addDisc(ITEM_SCORES, Items.MUSIC_DISC_CAT, 2);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_BLOCKS, 2);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_CHIRP, 2);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_FAR, 1);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_MALL, 1);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_MELLOHI, 1);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_STAL, 3);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_STRAD, 3);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_WARD, 1);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_WAIT, 2);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_OTHERSIDE, 3);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_PIGSTEP, 3);
        addDisc(ITEM_SCORES, Items.MUSIC_DISC_RELIC, 2);

        ITEM_SCORES.put(Items.DIAMOND_HELMET, 6);
        ITEM_SCORES.put(Items.DIAMOND_CHESTPLATE, 6);
        ITEM_SCORES.put(Items.DIAMOND_LEGGINGS, 6);
        ITEM_SCORES.put(Items.DIAMOND_BOOTS, 6);

        ITEM_SCORES.put(Items.DIAMOND_SWORD, 6);
        ITEM_SCORES.put(Items.DIAMOND_AXE, 6);
        ITEM_SCORES.put(Items.DIAMOND_PICKAXE, 6);
        ITEM_SCORES.put(Items.DIAMOND_SHOVEL, 6);
        ITEM_SCORES.put(Items.DIAMOND_HOE, 6);

        ITEM_SCORES.put(Items.IRON_HELMET, 3);
        ITEM_SCORES.put(Items.IRON_CHESTPLATE, 3);
        ITEM_SCORES.put(Items.IRON_LEGGINGS, 3);
        ITEM_SCORES.put(Items.IRON_BOOTS, 3);

        ITEM_SCORES.put(Items.IRON_SWORD, 3);
        ITEM_SCORES.put(Items.IRON_AXE, 3);
        ITEM_SCORES.put(Items.IRON_PICKAXE, 3);
        ITEM_SCORES.put(Items.IRON_SHOVEL, 3);
        ITEM_SCORES.put(Items.IRON_HOE, 3);

        ITEM_SCORES.put(Items.GOLDEN_HELMET, 2);
        ITEM_SCORES.put(Items.GOLDEN_CHESTPLATE, 2);
        ITEM_SCORES.put(Items.GOLDEN_LEGGINGS, 2);
        ITEM_SCORES.put(Items.GOLDEN_BOOTS, 2);

        ITEM_SCORES.put(Items.GOLDEN_SWORD, 2);
        ITEM_SCORES.put(Items.GOLDEN_AXE, 2);
        ITEM_SCORES.put(Items.GOLDEN_PICKAXE, 2);
        ITEM_SCORES.put(Items.GOLDEN_SHOVEL, 2);
        ITEM_SCORES.put(Items.GOLDEN_HOE, 2);

        ITEM_SCORES.put(Items.BOW, 3);
        ITEM_SCORES.put(Items.CROSSBOW, 3);
        ITEM_SCORES.put(Items.MAP, 3);

        ITEM_SCORES.put(Items.BREAD, 2);
        ITEM_SCORES.put(Items.BAKED_POTATO, 2);
        ITEM_SCORES.put(Items.GLOW_BERRIES, 1);
        ITEM_SCORES.put(Items.TORCH, 1);

        // Bad Items:
        ITEM_SCORES.put(Items.ROTTEN_FLESH, -2);
        ITEM_SCORES.put(Items.POISONOUS_POTATO, -2);

        ITEM_SCORES.put(Items.BONE, -2);
        ITEM_SCORES.put(Items.BONE_BLOCK, -3);
        ITEM_SCORES.put(Items.BONE_MEAL, -1);

        ITEM_SCORES.put(Items.SPIDER_EYE, -3);
        ITEM_SCORES.put(Items.FERMENTED_SPIDER_EYE, -4);

        ITEM_SCORES.put(Items.MAGMA_CREAM, -2);
        ITEM_SCORES.put(Items.SLIME_BALL, -1);
        ITEM_SCORES.put(Items.SLIME_BLOCK, -2);
        ITEM_SCORES.put(Items.ENDER_PEARL, -4);

        ITEM_SCORES.put(Items.COBWEB, -1);

        ITEM_SCORES.put(Items.SOUL_TORCH, -1);
        ITEM_SCORES.put(Items.SOUL_LANTERN, -2);
        ITEM_SCORES.put(Items.SOUL_SAND, -2);
        ITEM_SCORES.put(Items.SOUL_SOIL, -2);

        ITEM_SCORES.put(Items.SCULK, -1);
        ITEM_SCORES.put(Items.SCULK_CATALYST, -3);
        ITEM_SCORES.put(Items.SCULK_SENSOR, -3);
        ITEM_SCORES.put(Items.SCULK_VEIN, -2);
        ITEM_SCORES.put(Items.SCULK_SHRIEKER, -4);

        ITEM_SCORES.put(Items.TNT, -3);
        ITEM_SCORES.put(Items.GUNPOWDER, -1);
        ITEM_SCORES.put(Items.TRIPWIRE_HOOK, -1);
        ITEM_SCORES.put(Items.FIRE_CHARGE, -2);

        ITEM_SCORES.put(Items.MOSS_BLOCK, -1);
    }

    private static void addDisc(Map<Item, Integer> items, Item disc, int value) {
        ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(disc);
        int realValue = (Utils.isMusicDiscBadForSanity(resourceLocation.toString())) ? -value : value;

        items.put(disc, realValue);
    }

    public static Quality getQuality(Container container) {
        int score = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack itemStack = container.getItem(i);
            Item item = itemStack.getItem();

            score += getItemScore(item) * itemStack.getCount();
        }

        if (score < - 30) {
            return Quality.TERRIBLE;
        } else if (score < 0) {
            return Quality.BAD;
        } else if (score < 18) {
            return Quality.NORMAL;
        } else if (score < 30) {
            return Quality.GOOD;
        } else {
            return Quality.AMAZING;
        }
    }

    private static int getItemScore(Item item) {
        Integer score = ITEM_SCORES.get(item);
        return score != null ? score : 0;
    }

    public enum Quality {
        TERRIBLE, BAD, NORMAL, GOOD, AMAZING
    }
}
