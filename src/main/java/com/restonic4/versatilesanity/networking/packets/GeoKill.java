package com.restonic4.versatilesanity.networking.packets;

import com.chaotic_loom.under_control.UnderControl;
import com.chaotic_loom.under_control.core.annotations.Packet;
import com.chaotic_loom.under_control.core.annotations.PacketDirection;
import com.restonic4.versatilesanity.modules.SanityEventHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

@Packet(direction = PacketDirection.CLIENT_TO_SERVER)
public class GeoKill {
    public static ResourceLocation getId() {
        return new ResourceLocation(UnderControl.MOD_ID, "geo_kill");
    }

    public static void receive(MinecraftServer server, Player player, ServerPacketListener serverPacketListener, FriendlyByteBuf friendlyByteBuf, PacketSender packetSender) {
        if (player instanceof ServerPlayer serverPlayer) {
            SanityEventHandler.geoKill(player);
        }
    }

    public static void sendToServer() {
        FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create();

        ClientPlayNetworking.send(getId(), friendlyByteBuf);
    }
}
