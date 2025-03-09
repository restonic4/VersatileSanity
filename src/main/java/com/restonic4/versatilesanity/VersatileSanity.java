package com.restonic4.versatilesanity;

import com.chaotic_loom.under_control.events.EventResult;
import com.chaotic_loom.under_control.events.types.LivingEntityExtraEvents;
import com.chaotic_loom.under_control.events.types.OtherEvents;
import com.chaotic_loom.under_control.events.types.PlayerExtraEvents;
import com.chaotic_loom.under_control.events.types.ServerPlayerExtraEvents;
import com.chaotic_loom.under_control.util.FabricHelper;
import com.chaotic_loom.under_control.util.MinecraftHelper;
import com.chaotic_loom.under_control.util.RandomHelper;
import com.chaotic_loom.under_control.util.WeightedRandomPicker;
import com.restonic4.versatilesanity.compatibility.CompatibleMods;
import com.restonic4.versatilesanity.compatibility.tough_as_nails.ToughAsNailsCompatibility;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import com.restonic4.versatilesanity.config.VersatileSanityConfig;
import com.restonic4.versatilesanity.modules.ActivityTrackingManager;
import com.restonic4.versatilesanity.modules.SanityEventHandler;
import com.restonic4.versatilesanity.modules.SleepHandler;
import com.restonic4.versatilesanity.networking.SanityStatusBarNetworking;
import com.restonic4.versatilesanity.registry.commands.SanityCommand;
import com.restonic4.versatilesanity.registry.effects.EffectManager;
import com.restonic4.versatilesanity.registry.effects.MindRestoration;
import com.restonic4.versatilesanity.util.LootQualityChecker;
import com.restonic4.versatilesanity.util.Utils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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

            int maxSanity = config.getMaxSanity();

            int requiredSanity = (int) (maxSanity / 2.5f);
            int numberOfHeals = (int) Math.ceil(requiredSanity / (double) MindRestoration.HEAL_AMOUNT);
            int ticks = numberOfHeals * MindRestoration.TICKS;

            newPlayer.addEffect(new MobEffectInstance(EffectManager.MIND_RESTORATION, ticks, 0));
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

        OtherEvents.LOOT_CONTAINER_GENERATED_LOOT.register((player, lootTable, container, isEntityContainer) -> {
            SanityEventHandler.onNewLootFound(player, LootQualityChecker.getQuality(container));
        });

        if (isModLoaded(CompatibleMods.TOUGH_AS_NAILS)) {
            ToughAsNailsCompatibility.onInitialize();
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, commandSelection) -> {
            SanityCommand.register(dispatcher);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            /*if (server.getTicks() % CHECK_INTERVAL != 0) return;

            for (ServerLevel level : server.getAllLevels()) {
                level.getEntitiesByType(EntityType.VILLAGER, entity -> true).forEach(villager -> {
                    checkAndFleeFromLowHealthPlayers((VillagerEntity) villager, level);
                });
            }*/
        });

        ActivityTrackingManager.init();
    }

    private boolean onPlayerAttackEntity(LivingEntity livingEntity, DamageSource damageSource, float amount) {
        if (damageSource.getEntity() instanceof Player player) {
            if (!livingEntity.isAlive()) {

                if (livingEntity instanceof Villager || livingEntity instanceof WanderingTrader) {
                    SanityEventHandler.onVillagerKilled(player);
                } else if (livingEntity instanceof Player) {
                    SanityEventHandler.onPlayerKilled(player);
                } else if (isCute(livingEntity)) {
                    SanityEventHandler.onCuteCreatureKilled(player);
                } else if (livingEntity instanceof Animal) {
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
    barra de cordura mal, ta 1 px arriba

    oceano
    blood moon
    temperatura



< 50
Sonidos de cueva y ocean adittions * 2 chance
Sonidos creepy leves sonaran random
Se ve algo mas gris

< 35
Sonidos creepy normales soanran random
Susurros ocasionales
Se ve algo mas gris
Corazon late poco y flojo

< 25
Susurros frecuentes
Se ve bastante mas gris
Desorintacion temporal leve
Sonidos de pasos
Corazon late mas

< 15
Desorintacion temporal severa
Desorientación climatica
Corazon late mas

     */
}
