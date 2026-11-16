@file:Suppress("UnstableApiUsage")

package me.elephant1214.unlimitedenchant.config

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import me.elephant1214.unlimitedenchant.LOGGER
import me.elephant1214.unlimitedenchant.bootstrap.VanillaValuesMap
import net.kyori.adventure.key.Key
import org.bukkit.Registry

/**
 * Logs every problem found in the config
 */
fun UeConfig.validate(vanilla: VanillaValuesMap) {
    warnUnknownKeys()
    warnBlacklisted()
    warnUnobtainable(vanilla)
}

private fun UeConfig.warnUnknownKeys() {
    val access = RegistryAccess.registryAccess()
    val enchantmentRegistry = access.getRegistry(RegistryKey.ENCHANTMENT)
    val itemRegistry = access.getRegistry(RegistryKey.ITEM)

    warn("enchantments", enchantments.keys, enchantmentRegistry, "enchantment")
    warn("blacklist.blacklist", blacklist.blacklist.keys, enchantmentRegistry, "enchantment")
    warn("blacklist.per-item", blacklist.perItem.keys, itemRegistry, "item")
    blacklist.perItem.forEach { (item, caps) ->
        warn("blacklist.per-item.${item.asString()}", caps.keys, enchantmentRegistry, "enchantment")
    }
}

private fun warn(section: String, keys: Iterable<Key>, registry: Registry<*>, kind: String) =
    keys.filter { registry.get(it) == null }
        .forEach { LOGGER.warn("Unknown $kind `${it.asString()}` in $section, it will be ignored") }

/**
 * Warns when all levels of an enchantment are blacklisted but the enchantment is still enabled.
 */
private fun UeConfig.warnBlacklisted() {
    if (!blacklist.enabled) return
    blacklist.blacklist.filterValues { it == 0 }.keys
        .filter { (enchantments[it]?.maxLevel ?: 1) != 0 }
        .forEach {
            LOGGER.info(
                "`${it.asString()}` is fully blacklisted but not disabled. " +
                    "Set its max-level to 0 to stop it from being obtained."
            )
        }
}

/**
 * The highest enchanting power an enchantment table can reach. The maximum level with bookshelves (30) plus gold
 * armor's enchantability (25), and the maximum random bonus.
 *
 * `(30 + 1 + 6 + 6) * 1.15`.
 */
private const val TABLE_POWER_LIMIT = 49

/**
 * Warns about enchantment levels that can never be rolled or won't appear in enchantment tables using the configured
 * values with vanilla as a fallback so that problems show up on reload and after a restart.
 */
private fun UeConfig.warnUnobtainable(vanilla: VanillaValuesMap) {
    val treasure = RegistryAccess.registryAccess()
        .getRegistry(RegistryKey.ENCHANTMENT)
        .getTag(EnchantmentTagKeys.TREASURE)

    for ((key, options) in enchantments) {
        val base = vanilla[key] ?: continue // unknown keys are already reported by warnUnknownKeys
        val maxLevel = options.maxLevel ?: base.maxLevel
        if (maxLevel < 1) continue // disabled

        val min = options.minCost ?: base.minCost
        val max = options.maxCost ?: base.maxCost
        val name = key.asString()

        // Both costs are linear, so they meet at most once and the levels where max-cost < min-cost are always a
        // single continuous range. Depending on whether min or max per-level is larger, the range will be at the top
        // or the bottom.
        val costsChanged = options.minCost != null || options.maxCost != null
        val neverRolled = (1..maxLevel).filter { level ->
            (costsChanged || level > base.maxLevel) && max.at(level) < min.at(level)
        }
        if (neverRolled.isNotEmpty()) {
            LOGGER.warn(
                "`$name` ${levels(neverRolled.first(), neverRolled.last())} can never be rolled because max-cost " +
                    "is below min-cost"
            )
        }

        // Treasure enchantments never come from enchantment tables anyway
        // Otherwise, only check levels the config changes so that vanilla's own unreachable levels aren't reported.
        if (treasure.contains(TypedKey.create(RegistryKey.ENCHANTMENT, key))) continue
        val firstOutOfReach = (1..maxLevel).firstOrNull { level ->
            (options.minCost != null || level > base.maxLevel) && min.at(level) > TABLE_POWER_LIMIT
        } ?: continue

        // Min/max per-level is never negative, so the minimum only increases and every level above is also out of reach
        LOGGER.info(
            "`$name` ${levels(firstOutOfReach, maxLevel)} need${if (firstOutOfReach == maxLevel) "s" else ""} " +
                "enchanting power ${min.at(firstOutOfReach)}+ and won't appear in enchanting tables"
        )
    }
}

private fun levels(from: Int, to: Int): String = if (from == to) "level $from" else "levels $from-$to"
