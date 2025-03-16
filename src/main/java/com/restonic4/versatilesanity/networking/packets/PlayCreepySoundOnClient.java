package com.restonic4.versatilesanity.networking.packets;

import com.chaotic_loom.under_control.UnderControl;
import com.chaotic_loom.under_control.core.annotations.Packet;
import com.chaotic_loom.under_control.core.annotations.PacketDirection;
import com.restonic4.versatilesanity.modules.hallucinations.CreepySoundManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

@Packet(direction = PacketDirection.SERVER_TO_CLIENT)
public class PlayCreepySoundOnClient {
    public static ResourceLocation getId() {
        return new ResourceLocation(UnderControl.MOD_ID, "play_creepy_sound_on_client");
    }

    public static void receive(Minecraft minecraft, ClientPacketListener clientPacketListener, FriendlyByteBuf friendlyByteBuf, PacketSender packetSender) {
        String sound = friendlyByteBuf.readUtf();

        System.out.println("Sound received from server: " + sound);

        minecraft.execute(() -> {
            switch (sound) {
                case "footsteps" -> CreepySoundManager.playFootstepSequence();
                case "creeper" -> CreepySoundManager.playCreeperSoundBehindPlayer();
                case "cave" -> CreepySoundManager.playCaveSound();
            }
        });
    }

    public static void sendToClient(ServerPlayer serverPlayer, String sound) {
        FriendlyByteBuf friendlyByteBuf = PacketByteBufs.create();

        friendlyByteBuf.writeUtf(sound);

        ServerPlayNetworking.send(serverPlayer, getId(), friendlyByteBuf);
    }
}
