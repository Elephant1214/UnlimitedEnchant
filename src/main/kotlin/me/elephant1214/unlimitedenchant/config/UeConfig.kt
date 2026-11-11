package me.elephant1214.unlimitedenchant.config

import me.elephant1214.unlimitedenchant.config.ConfigLoader.Companion.LATEST_VERSION
import org.bukkit.enchantments.Enchantment
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@Suppress("unused")
@ConfigSerializable
class UeConfig {
    /**
     * The version reference for the current config so that plugin updates won't break them.
     */
    var configVersion: Int = LATEST_VERSION
    var anvil = Anvil()

    /**
     * namespace -> enchantment -> max level
     */
    var enchantmentLevels: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()

    fun getMaximumLevel(enchantment: Enchantment): Int =
        enchantmentLevels[enchantment.key().key().namespace()]?.get(enchantment.key().key().value())
            ?: enchantment.maxLevel

    fun setMaximumLevel(enchantment: Enchantment, level: Int) {
        enchantmentLevels.getOrPut(enchantment.key().key().namespace()) {
            mutableMapOf()
        }[enchantment.key().key().value()] = level
    }

    @ConfigSerializable
    class Anvil {
        /**
         * Increases the maximum cost of an anvil repair to the value of [maximumCost].
         */
        var allowExpensiveRepairs: Boolean = false

        /**
         * The maximum cost (inclusive) of an anvil repair when [allowExpensiveRepairs] is true.
         */
        var maximumCost: Int = 39
        var rename: Rename = Rename()
    }

    @ConfigSerializable
    class Rename {
        /**
         * When true, all renames will always cost the value set at [cost].
         */
        var fixedCost: Boolean = false
        var cost: Int = 1
    }
}
