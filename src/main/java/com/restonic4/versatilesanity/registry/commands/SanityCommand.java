package com.restonic4.versatilesanity.registry.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import com.restonic4.versatilesanity.networking.packets.PlayCreepySoundOnClient;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class SanityCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("sanity")
                        .then(createSubcommandWithValue("set"))
                        .then(createSubcommandWithValue("add"))
                        .then(createSubcommandWithValue("decrease"))
                        .then(
                                literal("reset")
                                        .then(argument("targets", EntityArgument.players())
                                                .executes(context -> {
                                                    var targets = EntityArgument.getPlayers(context, "targets");
                                                    targets.forEach(player -> {
                                                        SanityStatusComponents.SANITY_STATUS.get(player)
                                                                .setSanityStatus(VersatileSanity.getConfig().getMaxSanity());
                                                    });
                                                    return 1;
                                                })
                                        )
                        )
                        .then(createSoundSubcommand())
        );
    }

    // Subcomandos para "set", "add" y "decrease" que usan un valor entero.
    private static LiteralArgumentBuilder<CommandSourceStack> createSubcommandWithValue(String action) {
        return literal(action)
                .then(
                        argument("targets", EntityArgument.players())
                                .then(
                                        argument("value", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    var targets = EntityArgument.getPlayers(context, "targets");
                                                    int value = IntegerArgumentType.getInteger(context, "value");

                                                    switch (action) {
                                                        case "set":
                                                            targets.forEach(player -> {
                                                                SanityStatusComponents.SANITY_STATUS.get(player)
                                                                        .setSanityStatus(value);
                                                            });
                                                            break;
                                                        case "add":
                                                            targets.forEach(player -> {
                                                                SanityStatusComponents.SANITY_STATUS.get(player)
                                                                        .incrementSanityStatus(value);
                                                            });
                                                            break;
                                                        case "decrease":
                                                            targets.forEach(player -> {
                                                                SanityStatusComponents.SANITY_STATUS.get(player)
                                                                        .decrementSanityStatus(value);
                                                            });
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                    return 1;
                                                })
                                )
                );
    }

    // Subcomando "sound" que utiliza un argumento de tipo String con sugerencias.
    private static LiteralArgumentBuilder<CommandSourceStack> createSoundSubcommand() {
        return literal("sound")
                .then(
                        argument("targets", EntityArgument.players())
                                .then(
                                        argument("sound", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    builder.suggest("creeper");
                                                    builder.suggest("footsteps");
                                                    builder.suggest("cave");
                                                    return builder.buildFuture();
                                                })
                                                .executes(context -> {
                                                    var targets = EntityArgument.getPlayers(context, "targets");
                                                    String sound = StringArgumentType.getString(context, "sound");
                                                    targets.forEach(player -> {
                                                        PlayCreepySoundOnClient.sendToClient(player, sound);
                                                    });
                                                    return 1;
                                                })
                                )
                );
    }
}
