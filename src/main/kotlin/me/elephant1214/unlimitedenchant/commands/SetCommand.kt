@file:Suppress("UnstableApiUsage")

package me.elephant1214.unlimitedenchant.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.registry.RegistryKey
import me.elephant1214.unlimitedenchant.PLUGIN_PREFIX
import me.elephant1214.unlimitedenchant.config.ConfigHolder
import me.elephant1214.unlimitedenchant.config.LevelCost
import me.elephant1214.unlimitedenchant.config.UeConfig
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.enchantments.Enchantment

fun LiteralArgumentBuilder<Source>.set(configHolder: ConfigHolder) {
    literal("set") {
        argument("enchantment", ArgumentTypes.resource(RegistryKey.ENCHANTMENT)) {
            argument("level", IntegerArgumentType.integer(0, 255)) {
                exec { ctx ->
                    val level = ctx.arg<Int>("level")
                    update(ctx, configHolder, "maxLevel", level.toString()) { maxLevel = level }
                }
            }

            intField(configHolder, "maxLevel", 0, 255) { maxLevel = it }
            intField(configHolder, "weight", 1, 1024) { weight = it }
            intField(configHolder, "anvilCost", 0, 1024) { anvilCost = it }
            costField(configHolder, "minCost") { minCost = it }
            costField(configHolder, "maxCost") { maxCost = it }
        }
    }
}

private fun ArgumentBuilder<Source, *>.intField(
    configHolder: ConfigHolder,
    name: String,
    min: Int,
    max: Int,
    apply: UeConfig.EnchantmentOptions.(Int) -> Unit,
) {
    literal(name) {
        argument("value", IntegerArgumentType.integer(min, max)) {
            exec { ctx ->
                val value = ctx.arg<Int>("value")
                update(ctx, configHolder, name, value.toString()) { apply(value) }
            }
        }
    }
}

private fun ArgumentBuilder<Source, *>.costField(
    configHolder: ConfigHolder,
    name: String,
    apply: UeConfig.EnchantmentOptions.(LevelCost) -> Unit,
) {
    literal(name) {
        argument("base", IntegerArgumentType.integer(0)) {
            argument("perLevel", IntegerArgumentType.integer(0)) {
                exec { ctx ->
                    val cost = LevelCost(ctx.arg("base"), ctx.arg("perLevel"))
                    update(ctx, configHolder, name, "${cost.base} + ${cost.perLevel}/level") { apply(cost) }
                }
            }
        }
    }
}

private fun update(
    ctx: Ctx,
    configHolder: ConfigHolder,
    field: String,
    shownValue: String,
    change: UeConfig.EnchantmentOptions.() -> Unit,
) {
    val enchantment = ctx.arg<Enchantment>("enchantment")
    configHolder.config.enchantments.getOrPut(enchantment.key()) { UeConfig.EnchantmentOptions() }.change()
    configHolder.save()

    ctx.source.sender.sendRichMessage(
        "<prefix> <#CCCCCC>Set <field> for <enchantment> to <value>.\n" +
            "You must restart your server for this change to take effect.",
        PLUGIN_PREFIX,
        Placeholder.unparsed("field", field),
        Placeholder.component("enchantment", enchantment.description()),
        Placeholder.unparsed("value", shownValue),
    )
}
