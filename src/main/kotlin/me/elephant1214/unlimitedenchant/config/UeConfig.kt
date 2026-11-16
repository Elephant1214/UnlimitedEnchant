package me.elephant1214.unlimitedenchant.config

import me.elephant1214.unlimitedenchant.config.ConfigLoader.Companion.LATEST_VERSION
import me.elephant1214.unlimitedenchant.config.annotations.Clamp
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
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
    var blacklist = Blacklist()

    /**
     * namespace:enchantment -> options
     */
    var enchantments: MutableMap<Key, EnchantmentOptions> = mutableMapOf()

    fun getMaximumLevel(enchantment: Enchantment): Int =
        enchantments[enchantment.key()]?.maxLevel ?: enchantment.maxLevel

    fun setMaximumLevel(enchantment: Keyed, level: Int) {
        enchantments.getOrPut(enchantment.key()) { EnchantmentOptions() }.maxLevel = level
    }

    fun setWeight(enchantment: Keyed, weight: Int) {
        enchantments.getOrPut(enchantment.key()) { EnchantmentOptions() }.weight = weight
    }

    fun setAnvilCost(enchantment: Keyed, cost: Int) {
        enchantments.getOrPut(enchantment.key()) { EnchantmentOptions() }.anvilCost = cost
    }

    @ConfigSerializable
    class EnchantmentOptions {
        /**
         * Absolute minimum weight required by vanilla is `1`.
         */
        @field:Clamp(min = 1, max = 1024)
        var weight: Int? = null

        /**
         * `0` = disabled
         */
        @field:Clamp(min = 0, max = 255)
        var maxLevel: Int? = null

        /**
         * The base cost of applying an enchantment with an anvil. Halved when from a book, then multiplied by the
         * enchantment's level.
         *
         * `0` makes it free when combining items, however the cost will still be one per enchantment level when using
         * books.
         */
        @field:Clamp(min = 0, max = 1024)
        var anvilCost: Int? = null

        var minCost: LevelCost? = null

        var maxCost: LevelCost? = null
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
        @field:Clamp(min = 0)
        var maximumCost: Int = 39
        var rename: Rename = Rename()

        @ConfigSerializable
        class Rename {
            /**
             * When true, all renames will always cost the value set at [cost].
             */
            var fixedCost: Boolean = false

            /**
             * `0` = free
             */
            @field:Clamp(min = 0)
            var cost: Int = 1
        }
    }

    @ConfigSerializable
    class Blacklist {
        var enabled: Boolean = false

        /**
         * Takes priority over [perItem]. If an enchantment is blacklisted here, the other list will be
         * ignored for the same enchantment.
         *
         * namespace:enchantment -> maximum level
         * `0` = all levels blacklisted. This only removes the enchantment from existing items; to stop it from being
         * obtained at all, set its `max-level` to `0` under `enchantments`.
         */
        var blacklist: MutableMap<Key, Int> = mutableMapOf(
            Key.key("aqua_affinity") to 0,
            Key.key("bane_of_arthropods") to 3,
        )

        var perItemEnabled: Boolean = false

        /**
         * namespace:item -> { namespace:enchantment -> maximum level }
         * `0` = all levels blacklisted on item
         */
        var perItem: MutableMap<Key, MutableMap<Key, Int>> = mutableMapOf(
            Key.key("diamond_sword") to mutableMapOf(Key.key("mending") to 0),
            Key.key("wooden_shovel") to mutableMapOf(Key.key("efficiency") to 3),
        )

        /**
         * If `true`, any items with a blacklisted enchantment level will have the enchantment removed entirely rather
         * than decreased. Otherwise, the level on the item is decreased to the cap.
         */
        var removeEnchantment: Boolean = false

        /**
         * Trades with a blacklisted enchantment level always have it lowered to the cap (incl. per item caps).
         * Enchanted books are also repriced for the new level.
         *
         * If `true`, enchanted book trades whose enchantment is blacklisted are rerolled to a different tradeable
         * enchantment with vanilla pricing. Otherwise, those trades are removed.
         */
        var rerollTrades: Boolean = true

        var enforcement: Enforcement = Enforcement()

        val active: Boolean
            get() = enabled || perItemEnabled

        fun set(enchantment: Keyed, level: Int) {
            blacklist[enchantment.key()] = level
        }

        fun setPerItem(item: Keyed, enchantment: Keyed, level: Int) {
            perItem.getOrPut(item.key()) { mutableMapOf() }[enchantment.key()] = level
        }

        @ConfigSerializable
        class Enforcement {
            /**
             * Fix/remove items when they're clicked in an inventory or picked up off of the ground.
             */
            var inventory: Boolean = true

            /**
             * Fix/remove villager and wandering trader offers when their menus are opened.
             */
            var trades: Boolean = true

            /**
             * Fix/remove held items when used. This only handles attacking or breaking a block unless someone requests
             * to have more cases covered. I don't see these two even really being required, though.
             */
            var heldItems: Boolean = false
        }
    }
}
