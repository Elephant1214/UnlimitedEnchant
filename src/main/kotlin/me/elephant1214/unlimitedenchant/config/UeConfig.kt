package me.elephant1214.unlimitedenchant.config

import me.elephant1214.unlimitedenchant.config.ConfigLoader.Companion.LATEST_VERSION
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
class UeConfig {
    @Comment("Do not change")
    var configVersion: Int = LATEST_VERSION
    var anvil = Anvil()
    var blacklist = Blacklist()

    @Comment("namespace -> enchantment -> max level")
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
        @Comment("Allow repairs past vanilla's level 39 limit")
        var allowExpensiveRepairs: Boolean = false

        @Comment("The maximum repair cost allowed while allow-expensive-repairs is enabled. Vanilla is 39.")
        var maximumCost: Int = 39
        var rename: Rename = Rename()
    }

    @ConfigSerializable
    class Rename {
        @Comment("Changes the cost to rename an item to the configured cost regardless of how many times the item has been in an anvil.")
        var fixedCost: Boolean = false
        var cost: Int = 1
    }

    @ConfigSerializable
    class Blacklist {
        var enabled: Boolean = false

        @Comment("namespace -> blacklisted enchantments")
        var enchantments: MutableMap<String, MutableSet<String>> = defaultEnchantments()

        fun isBlacklisted(enchantment: Enchantment): Boolean =
            enchantments[enchantment.key().key().namespace()]?.contains(enchantment.key().key().value()) == true

        /**
         * @return false if it was already blacklisted
         */
        fun add(enchantment: Enchantment): Boolean =
            enchantments.getOrPut(enchantment.key().key().namespace()) { mutableSetOf() }.add(enchantment.key.key)

        /**
         * @return false if it wasn't blacklisted
         */
        fun remove(enchantment: Enchantment): Boolean {
            val namespace = enchantments[enchantment.key().key().namespace()] ?: return false
            return namespace.remove(enchantment.key().key().value())
        }

        companion object {
            private fun defaultEnchantments(): MutableMap<String, MutableSet<String>> = mutableMapOf(
                Key.MINECRAFT_NAMESPACE to mutableSetOf(
                    Enchantment.AQUA_AFFINITY.key().key().value(),
                    Enchantment.BANE_OF_ARTHROPODS.key().key().value()
                ),
            )
        }
    }
}
