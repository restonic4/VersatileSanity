package com.restonic4.versatilesanity;

import com.chaotic_loom.under_control.util.RandomHelper;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import com.restonic4.versatilesanity.config.VersatileSanityConfig;
import com.restonic4.versatilesanity.networking.SanityStatusBarNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

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

        ServerLivingEntityEvents.ALLOW_DAMAGE.register(this::onEntityDamage);
        AttackEntityCallback.EVENT.register(this::onPlayerAttackEntity);
    }

    private InteractionResult onPlayerAttackEntity(Player player, Level level, InteractionHand interactionHand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (entity instanceof LivingEntity livingEntity) {
            level.getServer().execute(() -> {
                if (!livingEntity.isAlive()) {
                    int randomChance = RandomHelper.randomBetween(0, 100);

                    if (entity instanceof Villager || entity instanceof WanderingTrader) {
                        System.out.println("[-] Kill villager");
                        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getKillVillagerDecreaseFactor());
                    } else if (entity instanceof Player) {
                        System.out.println("[-] Kill player");
                        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getKillPlayerDecreaseFactor());
                    } else if (isCute(entity) && randomChance <= 60) {
                        System.out.println("[-] Kill cute");
                        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getKillCuteDecreaseFactor());
                    } else if (entity instanceof Animal && randomChance <= 40) {
                        System.out.println("[-] Kill animal");
                        SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(config.getKillAnimalDecreaseFactor());
                    }
                }
            });
        }
        return InteractionResult.PASS;
    }

    public boolean isCute(Entity entity) {
        return entity instanceof Wolf || entity instanceof Cat || entity instanceof Axolotl || entity instanceof Parrot || entity instanceof Sniffer;
    }

    private boolean onEntityDamage(LivingEntity livingEntity, DamageSource damageSource, float amount) {
        if (livingEntity instanceof Player player) {
            System.out.println("[-] Damage");
            SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus((int) (config.getDamageDecreaseFactor() * amount));
        }

        return true;
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

    /*
    Actividades que bajan la cordura:

Permanecer en la oscuridad: -1 por minuto, -2 en cuevas profundas
Estar cerca de monstruos: -1 por cada monstruo cercano por minuto
Recibir daño: -2 por cada corazón perdido
Morir: -20 de golpe
Estar solo (sin NPCs ni otros jugadores cerca): -1 cada 5 minutos
Permanecer bajo tierra mucho tiempo: -1 cada 10 minutos
Escuchar sonidos ambientales tétricos: -2 por evento
No dormir: -5 por día sin dormir (acumulativo)
Estar en el Nether: -2 por minuto
Estar en el End: -3 por minuto
Luchar contra jefes: -5 por minuto mientras dure el combate

Actividades que suben la cordura:

Dormir en una cama: +10 por noche completa
Comer alimentos variados: +2 por tipo de alimento diferente
Estar cerca de animales domésticos: +1 por minuto
Estar cerca de aldeanos amistosos: +1 por minuto
Cultivar plantas: +1 por planta cosechada
Completar un logro: +5 por logro
Escuchar música (discos): +5 mientras suena
Estar en áreas iluminadas y seguras: +1 cada 5 minutos
Derrotar a un jefe: +15 de inmediato
Construir estructuras elaboradas: +1 por cada 10 bloques colocados
Pescar: +2 por pez capturado

Efectos posibles según el nivel de cordura:

100-80: Estado normal, sin efectos
79-60: Visión ligeramente borrosa, sonidos ocasionales
59-40: Alucinaciones visuales menores, sonidos inquietantes frecuentes
39-20: Monstruos falsos apareciendo brevemente, efecto de náusea ocasional
19-1: Daño aleatorio, visión muy distorsionada, efectos de lentitud y debilidad
0: Muerte por locura o transformación en una versión hostil del jugador
     */
}
