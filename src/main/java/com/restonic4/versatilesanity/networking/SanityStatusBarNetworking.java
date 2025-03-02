package com.restonic4.versatilesanity.networking;

import com.restonic4.versatilesanity.VersatileSanity;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class SanityStatusBarNetworking {
    public static final ResourceLocation SYNC_SANITY_STATUS = VersatileSanity.id("sync_sanity_status");

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_SANITY_STATUS, (client, handler, buf, responseSender) -> {
            int sanity = buf.readInt();

            client.execute(() -> {
                if (client.player != null) {
                    ClientSanityManager.setSanity(sanity);
                }
            });
        });
    }

    public static void syncCustomStatus(Player player, int sanity) {
        if (player instanceof ServerPlayer serverPlayer) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeInt(sanity);

            ServerPlayNetworking.send(serverPlayer, SYNC_SANITY_STATUS, buf);
        }
    }
}
