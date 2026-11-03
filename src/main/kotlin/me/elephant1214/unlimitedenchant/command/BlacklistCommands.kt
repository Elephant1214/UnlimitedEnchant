package me.elephant1214.unlimitedenchant.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.registry.RegistryKey
import me.elephant1214.unlimitedenchant.PLUGIN_PREFIX
import me.elephant1214.unlimitedenchant.config.ConfigHolder
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.enchantments.Enchantment

fun LiteralArgumentBuilder<Source>.blacklistTree(configHolder: ConfigHolder) =
    literal("blacklist") {
        literal("enable") { blacklistToggle(configHolder, true) }
        literal("disable") { blacklistToggle(configHolder, false) }
        blacklistAdd(configHolder)
        blacklistRemove(configHolder)
    }

private fun LiteralArgumentBuilder<Source>.blacklistToggle(
    configHolder: ConfigHolder, enable: Boolean
) = exec { ctx ->
    val status = if (enable) "enabled" else "disabled"

    // Manager handles whether it's enabled once I write it after this
    if (enable == configHolder.config.blacklist.enabled) {
        fail("<prefix> <red>The enchantment blacklist is already $status", PLUGIN_PREFIX)
    }

    // Same thing with the manager here
    // if (enable) manager.enable() else manager.disable()

    ctx.source.sendRichReply("<prefix> <#CCCCCC>The enchantment blacklist is now $status", PLUGIN_PREFIX)
}

private fun LiteralArgumentBuilder<Source>.blacklistAdd(configHolder: ConfigHolder) =
    literal("add") {
        argument("enchantment", ArgumentTypes.resource(RegistryKey.ENCHANTMENT)) {
            exec { ctx ->
                val enchantment = ctx.arg<Enchantment>("enchantment")
                val resolvers = arrayOf(
                    PLUGIN_PREFIX,
                    Placeholder.component("enchantment", enchantment.description()),
                )

                if (!configHolder.config.blacklist.add(enchantment)) {
                    fail("<prefix> <red><enchantment> is already blacklisted", *resolvers)
                }

                configHolder.save()
                ctx.source.sendRichReply(
                    "<prefix> <#CCCCCC>Added <enchantment> to the enchantment blacklist",
                    *resolvers,
                )
            }
        }
    }

private fun LiteralArgumentBuilder<Source>.blacklistRemove(configHolder: ConfigHolder) =
    literal("remove") {
        argument("enchantment", ArgumentTypes.resource(RegistryKey.ENCHANTMENT)) {
            exec { ctx ->
                val enchantment = ctx.arg<Enchantment>("enchantment")
                val resolvers = arrayOf(
                    PLUGIN_PREFIX,
                    Placeholder.component("enchantment", enchantment.description()),
                )

                if (!configHolder.config.blacklist.remove(enchantment)) {
                    fail("<prefix> <red><enchantment> is not blacklisted", *resolvers)
                }

                configHolder.save()
                ctx.source.sendRichReply(
                    "<prefix> <#CCCCCC>Removed <enchantment> from the enchantment blacklist",
                    *resolvers,
                )
            }
        }
    }
