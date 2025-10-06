package me.elephant1214.unlimitedenchant.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.registry.RegistryKey;
import me.elephant1214.unlimitedenchant.UEConstants;
import me.elephant1214.unlimitedenchant.bootstrap.UEBootstrap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.enchantments.Enchantment;

public final class LevelSetCommand {
    public static void addLevelSet(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("set").then(Commands.argument("enchantment", ArgumentTypes.resource(RegistryKey.ENCHANTMENT))
                .then(Commands.argument("maximum level", IntegerArgumentType.integer(1, 255)).executes(ctx -> {
                    final Enchantment enchantment = ctx.getArgument("enchantment", Enchantment.class);
                    final int level = IntegerArgumentType.getInteger(ctx, "maximum level");

                    final int curLevel = UEBootstrap.config.getMaxLevel(enchantment);
                    if (curLevel == level) {
                        ctx.getSource().getSender().sendRichMessage(
                                "<prefix> The maximum level for <enchantment> was already set to <level>!",
                                UEConstants.Misc.PLUGIN_PREFIX,
                                Placeholder.component("enchantment", enchantment.description()),
                                Placeholder.component("level", Component.text(level))
                        );
                        return Command.SINGLE_SUCCESS;
                    }

                    UEBootstrap.config.setMaxLevel(enchantment.key().key(), level);
                    ctx.getSource().getSender().sendRichMessage(
                            "<prefix> Set the maximum level for <enchantment> to <level>.\n<u>You must restart your server for this change to take effect.</u>",
                            UEConstants.Misc.PLUGIN_PREFIX,
                            Placeholder.component("enchantment", enchantment.description()),
                            Placeholder.component("level", Component.text(level))
                    );
                    return Command.SINGLE_SUCCESS;
                }))
        ));
    }
}
