package me.elephant1214.unlimitedenchant.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.registry.RegistryKey
import me.elephant1214.unlimitedenchant.*
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment

private val NOT_CACHED_MESSAGE: String = """
    <prefix> <red><key> was never cached. If you are sure this is a real
    enchantment registered by the game, another plugin, or a datapack, please report this on
    <github><red> or my <discord>.
""".trimIndent().replace('\n', ' ')

private val INFO_MESSAGE = """
    <prefix> <white><name></white> <dark_gray>(<key>)</dark_gray>
    <gray> Max level: <level>
    <gray> Weight: <weight>
    <gray> Anvil cost: <anvil>
    <gray> Table cost: <min-base>-<max-base>, +<min-per-level>/+<max-per-level> per level
""".trimIndent()

@Suppress("UnstableApiUsage")
fun LiteralArgumentBuilder<Source>.get(
    plugin: UnlimitedEnchant,
) = literal("get") {
    argument("enchantment", ArgumentTypes.resource(RegistryKey.ENCHANTMENT)) {
        exec { ctx -> plugin.sendGetInfo(ctx.source.sender, ctx.arg("enchantment")) }
    }
}

private fun UnlimitedEnchant.sendGetInfo(sender: CommandSender, enchantment: Enchantment) {
    val key = enchantment.adventureKey
    val name = Placeholder.component("name", enchantment.description())
    val asString = Placeholder.unparsed("key", key.asString())
    val vanilla = vanillaValues[enchantment.adventureKey] ?: fail(
        NOT_CACHED_MESSAGE,
        PLUGIN_PREFIX,
        asString,
        GITHUB_URL,
        DISCORD_INVITE,
    )

    val pending = config.enchantments[key]
    val activeLevel = if (key in disabled) 0 else enchantment.maxLevel
    val min = enchantment.activeMinCost()
    val max = enchantment.activeMaxCost()
    sender.sendRichMessage(
        INFO_MESSAGE,
        PLUGIN_PREFIX,
        name,
        asString,
        valueText("weight", enchantment.weight, vanilla.weight, pending?.weight),
        valueText("level", activeLevel, vanilla.maxLevel, pending?.maxLevel, ::levelText),
        valueText("anvil", enchantment.anvilCost, vanilla.anvilCost, pending?.anvilCost),
        valueText("min-base", min.base, vanilla.minCost.base, pending?.minCost?.base),
        valueText("min-per-level", min.perLevel, vanilla.minCost.perLevel, pending?.minCost?.perLevel),
        valueText("max-base", max.base, vanilla.maxCost.base, pending?.maxCost?.base),
        valueText("max-per-level", max.perLevel, vanilla.maxCost.perLevel, pending?.maxCost?.perLevel),
    )
}

private fun valueText(
    name: String,
    active: Int,
    vanilla: Int,
    pending: Int?,
    format: (Int) -> String = Int::toString,
): TagResolver.Single = Placeholder.parsed(
    name,
    buildString {
        if (active == vanilla) {
            append("<white>${format(active)}</white>")
        } else {
            append("<yellow>${format(active)}</yellow> <dark_gray>(vanilla: ${format(vanilla)})</dark_gray>")
        }
        if (pending != null && pending != active) append(" <gold>→ ${format(pending)} after restart</gold>")
    },
)

private fun levelText(level: Int) = if (level == 0) "disabled" else "$level"
