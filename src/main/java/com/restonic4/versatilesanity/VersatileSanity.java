package com.restonic4.versatilesanity;

import com.chaotic_loom.under_control.events.EventResult;
import com.chaotic_loom.under_control.events.types.LivingEntityExtraEvents;
import com.chaotic_loom.under_control.events.types.PlayerExtraEvents;
import com.chaotic_loom.under_control.events.types.ServerPlayerExtraEvents;
import com.chaotic_loom.under_control.util.FabricHelper;
import com.chaotic_loom.under_control.util.MinecraftHelper;
import com.chaotic_loom.under_control.util.RandomHelper;
import com.restonic4.versatilesanity.compatibility.CompatibleMods;
import com.restonic4.versatilesanity.compatibility.tough_as_nails.ToughAsNailsCompatibility;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import com.restonic4.versatilesanity.config.VersatileSanityConfig;
import com.restonic4.versatilesanity.modules.SanityEventHandler;
import com.restonic4.versatilesanity.modules.SleepHandler;
import com.restonic4.versatilesanity.networking.SanityStatusBarNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VersatileSanity implements ModInitializer {
    public static final String MOD_ID = "versatilesanity";

    private static final VersatileSanityConfig config = new VersatileSanityConfig();

    @Override
    public void onInitialize() {
        SanityStatusBarNetworking.register();
        config.register();

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            SanityStatusComponents.SANITY_STATUS.get(newPlayer).setSanityStatus(config.getReSpawnSanity());
        });

        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, packetSender, minecraftServer) -> {
            SanityStatusComponents.SANITY_STATUS.get(serverGamePacketListener.getPlayer()).sync();
        });

        ServerLivingEntityEvents.ALLOW_DEATH.register(this::onPlayerAttackEntity);

        LivingEntityExtraEvents.FISHING_HOOK_RETRIEVED.register((player, hook, itemStacks) -> {
            if (player == null || player.level().isClientSide()) return EventResult.SUCCEEDED;

            SanityEventHandler.onFishCaught(player, itemStacks);
            return EventResult.SUCCEEDED;
        });

        ServerPlayerExtraEvents.ADVANCEMENT_GRANTED.register((serverPlayer, advancement, criterionName) -> {
            if (MinecraftHelper.isNotInternal(advancement)) {
                SanityEventHandler.onAdvancementMade(serverPlayer);
            }

            return EventResult.SUCCEEDED;
        });

        PlayerExtraEvents.DAMAGED.register((player, damageSource, amount) -> {
            SanityEventHandler.onDamage(player, amount);
        });

        PlayerExtraEvents.SLEEPING_START.register((player, blockPos) -> {
            SleepHandler.recordSleepStart(player);
        });

        PlayerExtraEvents.SLEEPING_STOPPED.register((player, bl, bl2) -> {
            SleepHandler.handleSleepEnd(player);
        });

        //TODO: USAR EVENTO CUSTOM NUEVO DE LOOTTABLES

        if (isModLoaded(CompatibleMods.TOUGH_AS_NAILS)) {
            ToughAsNailsCompatibility.onInitialize();
        }
    }

    private boolean onPlayerAttackEntity(LivingEntity livingEntity, DamageSource damageSource, float amount) {
        if (damageSource.getEntity() instanceof Player player) {
            if (!livingEntity.isAlive()) {
                int randomChance = RandomHelper.randomBetween(0, 100);

                if (livingEntity instanceof Villager || livingEntity instanceof WanderingTrader) {
                    SanityEventHandler.onVillagerKilled(player);
                } else if (livingEntity instanceof Player) {
                    SanityEventHandler.onPlayerKilled(player);
                } else if (isCute(livingEntity) && randomChance <= 60) {
                    SanityEventHandler.onCuteCreatureKilled(player);
                } else if (livingEntity instanceof Animal && randomChance <= 40) {
                    SanityEventHandler.onAnimalKilled(player);
                }
            }
        }

        return true;
    }

    public boolean isCute(Entity entity) {
        return entity instanceof Wolf || entity instanceof Cat || entity instanceof Axolotl || entity instanceof Parrot || entity instanceof Sniffer;
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static VersatileSanityConfig getConfig() {
        if (!config.isRegistered()) {
            throw new RuntimeException("Config not registered yet!");
        }

        return config;
    }

    public static boolean isModLoaded(CompatibleMods compatibleMod) {
        return FabricLoader.getInstance().isModLoaded(compatibleMod.getId());
    }

    /*
    oceano
    blood moon
    temperatura
     */
}
