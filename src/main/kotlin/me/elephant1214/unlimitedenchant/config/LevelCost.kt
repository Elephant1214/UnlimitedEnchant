package me.elephant1214.unlimitedenchant.config

import io.papermc.paper.registry.data.EnchantmentRegistryEntry

data class LevelCost(val base: Int, val perLevel: Int) {
    fun at(level: Int): Int = base + perLevel * (level - 1)

    companion object {
        @Suppress("UnstableApiUsage")
        fun of(cost: EnchantmentRegistryEntry.EnchantmentCost) =
            LevelCost(cost.baseCost(), cost.additionalPerLevelCost())
    }
}
