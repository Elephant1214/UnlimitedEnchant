package me.elephant1214.unlimitedenchant.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.registry.RegistryKey
import me.elephant1214.unlimitedenchant.PLUGIN_PREFIX
import me.elephant1214.unlimitedenchant.config.ConfigHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.enchantments.Enchantment

fun LiteralArgumentBuilder<Source>.levelSet(configHolder: ConfigHolder) = literal("set") {
    argument("enchantment", ArgumentTypes.resource(RegistryKey.ENCHANTMENT)) {
        argument("level", IntegerArgumentType.integer(0, 255)) {
            exec { ctx ->
                val enchantment = ctx.arg<Enchantment>("enchantment")
                val level = ctx.arg<Int>("level")

                val resolvers = arrayOf(
                    PLUGIN_PREFIX,
                    Placeholder.component("enchantment", enchantment.description()),
                    Placeholder.component("level", Component.text(level)),
                )

                if (configHolder.config.getMaximumLevel(enchantment) == level) {
                    fail(
                        "<prefix> <red>The maximum level for <enchantment> was already set to <level>!",
                        *resolvers,
                    )
                }

                configHolder.config.setMaximumLevel(enchantment, level)
                configHolder.save()
                ctx.source.sendRichReply(
                    "<prefix> <red>Set the maximum level for <enchantment> to <level>.\n<u>You must restart your server for this change to take effect.</u>",
                    *resolvers,
                )
            }
        }
    }
}
