package me.elephant1214.unlimitedenchant.bootstrap

import io.papermc.paper.registry.data.EnchantmentRegistryEntry

/**
 * A copy of the vanilla settings for an enchantment in the game.
 */
@Suppress("UnstableApiUsage")
data class VanillaValues(
    val weight: Int,
    val maxLevel: Int,
    val anvilCost: Int,
    val minCost: EnchantmentRegistryEntry.EnchantmentCost,
    val maxCost: EnchantmentRegistryEntry.EnchantmentCost,
)
