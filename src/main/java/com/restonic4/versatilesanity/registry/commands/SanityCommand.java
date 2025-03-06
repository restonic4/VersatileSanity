package com.restonic4.versatilesanity.registry.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.restonic4.versatilesanity.VersatileSanity;
import com.restonic4.versatilesanity.components.SanityStatusComponents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

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
                                                        SanityStatusComponents.SANITY_STATUS.get(player).setSanityStatus(VersatileSanity.getConfig().getMaxSanity());
                                                    });
                                                    return 1;
                                                })
                                        )
                        )
        );
    }

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
                                                                SanityStatusComponents.SANITY_STATUS.get(player).setSanityStatus(value);
                                                            });
                                                            break;
                                                        case "add":
                                                            targets.forEach(player -> {
                                                                SanityStatusComponents.SANITY_STATUS.get(player).incrementSanityStatus(value);
                                                            });
                                                            break;
                                                        case "decrease":
                                                            targets.forEach(player -> {
                                                                SanityStatusComponents.SANITY_STATUS.get(player).decrementSanityStatus(value);
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
}