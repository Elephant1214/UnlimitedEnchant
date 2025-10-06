package me.elephant1214.unlimitedenchant.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.registry.RegistryKey;
import me.elephant1214.unlimitedenchant.UEConstants;
import me.elephant1214.unlimitedenchant.blacklist.BlacklistManager;
import me.elephant1214.unlimitedenchant.bootstrap.UEBootstrap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.enchantments.Enchantment;

public final class BlacklistCommand {
    public static void addBlacklistTree(BlacklistManager manager, LiteralArgumentBuilder<CommandSourceStack> root) {
        LiteralArgumentBuilder<CommandSourceStack> blacklist = Commands.literal("blacklist");

        blacklist.then(Commands.literal("add")
                .then(Commands.argument("enchantment", ArgumentTypes.resource(RegistryKey.ENCHANTMENT))
                        .executes(ctx -> {
                            if (!manager.isEnabled()) {
                                final Message message = MessageComponentSerializer.message().serialize(Component.text("The enchantment blacklist is disabled"));
                                throw new SimpleCommandExceptionType(message).create();
                            }

                            final Enchantment enchantment = ctx.getArgument("enchantment", Enchantment.class);

                            if (manager.contains(enchantment)) {
                                ctx.getSource().getSender().sendRichMessage(
                                        "<prefix> <enchantment> was already blacklisted.",
                                        UEConstants.Misc.PLUGIN_PREFIX,
                                        Placeholder.component("enchantment", enchantment.description())
                                );
                                return Command.SINGLE_SUCCESS;
                            }

                            UEBootstrap.config.blacklistEnchantment(enchantment);
                            manager.updateBlacklist();

                            ctx.getSource().getSender().sendRichMessage(
                                    "<prefix> Added <enchantment> to the enchantment blacklist.",
                                    UEConstants.Misc.PLUGIN_PREFIX,
                                    Placeholder.component("enchantment", enchantment.description())
                            );
                            return Command.SINGLE_SUCCESS;
                        })));

        blacklist.then(Commands.literal("enable").executes(ctx -> toggleBlacklist(manager, ctx, true)));
        blacklist.then(Commands.literal("disable").executes(ctx -> toggleBlacklist(manager, ctx, false)));

        root.then(blacklist);
    }

    private static int toggleBlacklist(BlacklistManager manager, CommandContext<CommandSourceStack> ctx, final boolean enable) throws CommandSyntaxException {
        final String status = enable ? "enabled" : "disabled";

        if (enable == UEBootstrap.config.blacklistEnabled()) {
            final Message message = MessageComponentSerializer.message().serialize(Component.text("The enchantment blacklist is already " + status));
            throw new SimpleCommandExceptionType(message).create();
        }

        UEBootstrap.config.setBlacklist(enable);
        if (enable) {
            manager.enableBlacklist();
        } else {
            manager.disableBlacklist();
        }

        ctx.getSource().getSender().sendRichMessage("<prefix> The enchantment blacklist is now " + status, UEConstants.Misc.PLUGIN_PREFIX);
        return Command.SINGLE_SUCCESS;
    }
}
