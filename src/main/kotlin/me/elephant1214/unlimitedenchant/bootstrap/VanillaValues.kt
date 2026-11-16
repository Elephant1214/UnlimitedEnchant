package me.elephant1214.unlimitedenchant.bootstrap

import me.elephant1214.unlimitedenchant.config.LevelCost
import net.kyori.adventure.key.Key

typealias VanillaValuesMap = Map<Key, VanillaValues>

/**
 * A copy of the vanilla settings for an enchantment in the game.
 */
data class VanillaValues(
    val weight: Int,
    val maxLevel: Int,
    val anvilCost: Int,
    val minCost: LevelCost,
    val maxCost: LevelCost,
)
