package me.elephant1214.unlimitedenchant.config

import me.elephant1214.unlimitedenchant.config.ConfigLoader.Companion.LATEST_VERSION
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
        var enchantments = mutableListOf("aqua_affinity", "band_of_arthropods")
    }
}