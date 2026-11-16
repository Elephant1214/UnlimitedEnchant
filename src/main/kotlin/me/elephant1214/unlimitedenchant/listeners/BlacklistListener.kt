package me.elephant1214.unlimitedenchant.listeners

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys
import me.elephant1214.unlimitedenchant.allEnchantments
import me.elephant1214.unlimitedenchant.config.UeConfig
import me.elephant1214.unlimitedenchant.isEmptyBook
import me.elephant1214.unlimitedenchant.isNullOrEmpty
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Merchant
import org.bukkit.inventory.MerchantInventory
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import kotlin.random.Random

@Suppress("UnstableApiUsage")
class BlacklistListener(private val config: () -> UeConfig) : Listener {
    /**
     * Read settings through the [config] supplier every call so that they're always latest
     */
    private val settings: UeConfig.Blacklist
        get() = config().blacklist

    private fun enabled(group: UeConfig.Blacklist.Enforcement.() -> Boolean): Boolean =
        settings.active && settings.enforcement.group()

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onItemClicked(event: InventoryClickEvent) {
        if (!enabled { inventory }) return
        event.currentItem?.enforce { event.currentItem = null }
    }

    /**
     * Prevents anvils from producing items with blacklisted enchantment levels.
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        if (!settings.active) return
        val result = event.result ?: return
        if (!result.isLegal()) event.result = null
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onMerchantOpen(event: InventoryOpenEvent) {
        if (!enabled { trades }) return
        val inventory = event.inventory as? MerchantInventory ?: return
        checkTrades(inventory.merchant)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onItemPickup(event: EntityPickupItemEvent) {
        if (!enabled { inventory }) return

        val stack = event.item.itemStack
        stack.enforce {
            event.isCancelled = true
            event.item.remove()
        }
        event.item.itemStack = stack
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerAttack(event: PrePlayerAttackEntityEvent) {
        if (!enabled { heldItems }) return

        checkHands(event, event.player)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!enabled { heldItems }) return

        checkHands(event, event.player)
    }

    /**
     * Enforces the blacklist on both of the [player]'s hands, cancelling the supplied [cancellable] if either item
     * was changed or removed.
     */
    private fun checkHands(cancellable: Cancellable, player: Player) {
        val inventory = player.inventory
        val main = checkUse(inventory.itemInMainHand) { inventory.setItemInMainHand(null) }
        val off = checkUse(inventory.itemInOffHand) { inventory.setItemInOffHand(null) }

        if (main || off) {
            cancellable.isCancelled = true
        }
    }

    private fun checkUse(stack: ItemStack, remove: () -> Unit): Boolean =
        !stack.isNullOrEmpty && stack.enforce(remove)

    /**
     * Caps any of the [merchant]'s trades that sell items with blacklisted enchantments. Trades with no blacklisted
     * enchantments are never touched.
     *
     * Enchanted books are lowered to their cap and repriced for the new level. Books whose enchantment can't stay are
     * rerolled to a different enchantment if [UeConfig.Blacklist.rerollTrades] is enabled, and removed otherwise.
     * Other items are lowered to their cap and keep their price.
     *
     * @return `true` if any trades were changed
     */
    private fun checkTrades(merchant: Merchant): Boolean {
        val recipes = merchant.recipes
        if (recipes.all { it.result.isLegal() }) return false

        merchant.recipes = recipes.mapNotNull { recipe ->
            when {
                recipe.result.isLegal() -> recipe
                recipe.result.itemMeta is EnchantmentStorageMeta ->
                    recipe.cappedBook() ?: if (settings.rerollTrades) recipe.rerolledBook() else null

                else -> recipe.capped()
            }
        }
        return true
    }

    /**
     * Lowers an enchanted book trade to its cap and reprices it for the new level, keeping the original price's
     * relative position in vanilla's price range so cheap trades stay cheap.
     *
     * @return the new trade or `null` if the enchantment can't stay on the book
     */
    private fun MerchantRecipe.cappedBook(): MerchantRecipe? {
        val (enchantment, oldLevel) = (result.itemMeta as? EnchantmentStorageMeta)
            ?.storedEnchants?.entries?.singleOrNull()?.toPair()
            ?: return capped() // not a vanilla-style single enchantment book
        val itemKey = result.type.key()
        if (settings.removeEnchantment || !enchantment.isAllowedOn(itemKey)) return null

        val newLevel = minOf(oldLevel, enchantment.maxAllowedOn(itemKey))
        val doubled = enchantment.hasDoubleTradePrice()

        val book = ItemStack(result.type).apply {
            editMeta(EnchantmentStorageMeta::class.java) { it.addStoredEnchant(enchantment, newLevel, true) }
        }
        val newIngredients = ingredients.map { ingredient ->
            if (ingredient.type != Material.EMERALD) ingredient
            else ingredient.clone().apply { amount = scaledBookPrice(amount, oldLevel, newLevel, doubled) }
        }
        return withResult(book, newIngredients)
    }

    /**
     * Scales a vanilla book price from [oldLevel] to [newLevel], keeping the original roll's relative position in the
     * price range. Vanilla prices books at `2 + roll + 3 * level` where `roll` is in `0 until 5 + 10 * level`, doubled
     * for `#double_trade_price` enchantments and capped at 64.
     */
    private fun scaledBookPrice(oldPrice: Int, oldLevel: Int, newLevel: Int, doubled: Boolean): Int {
        val base = if (doubled) oldPrice / 2 else oldPrice
        val oldRange = 5 + oldLevel * 10
        val roll = (base - 2 - 3 * oldLevel).coerceIn(0, oldRange - 1)
        val newRange = 5 + newLevel * 10

        var price = 2 + roll * newRange / oldRange + 3 * newLevel
        if (doubled) price *= 2
        return price.coerceIn(1, 64)
    }

    /**
     * Replaces a book trade whose enchantment is blacklisted with a different tradeable enchantment. The new
     * enchantment is rolled like a villager would.
     *
     * @return the new trade or `null` if no other tradeable enchantment is allowed
     */
    private fun MerchantRecipe.rerolledBook(): MerchantRecipe? {
        val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
        val itemKey = result.type.key()
        val current = (result.itemMeta as? EnchantmentStorageMeta)?.storedEnchants?.keys?.singleOrNull()

        // Only called when the current enchantment is blacklisted, so always pick a different one
        val enchantment = registry.getTag(EnchantmentTagKeys.TRADEABLE).values()
            .mapNotNull { registry.get(it.key()) }
            .filter { it != current && it.isAllowedOn(itemKey) }
            .randomOrNull()
            ?: return null

        // Matches vanilla's EnchantBookForEmeralds#getOffer
        val level = enchantment.randomAllowedLevel(itemKey)
        var price = 2 + Random.nextInt(5 + level * 10) + 3 * level
        if (enchantment.hasDoubleTradePrice()) price *= 2
        price = price.coerceAtMost(64)

        val book = ItemStack(result.type).apply {
            editMeta(EnchantmentStorageMeta::class.java) { it.addStoredEnchant(enchantment, level, true) }
        }
        return withResult(book, listOf(ItemStack(Material.EMERALD, price), ItemStack(Material.BOOK)))
    }

    /**
     * Whether villagers charge double for books with this enchantment (`#minecraft:double_trade_price`).
     */
    private fun Enchantment.hasDoubleTradePrice(): Boolean =
        RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
            .getTag(EnchantmentTagKeys.DOUBLE_TRADE_PRICE)
            .contains(TypedKey.create(RegistryKey.ENCHANTMENT, key()))

    /**
     * The highest level of this enchantment allowed on this [item] type.
     *
     * @return the maximum allowed level or `0` if it's fully blacklisted
     */
    private fun Enchantment.maxAllowedOn(item: Key): Int {
        val cap = settings.capFor(item, key()) ?: return maxLevel
        return minOf(cap, maxLevel)
    }

    private fun Enchantment.isAllowedOn(item: Key): Boolean = maxAllowedOn(item) >= startLevel

    private fun Enchantment.randomAllowedLevel(item: Key): Int = Random.nextInt(startLevel, maxAllowedOn(item) + 1)

    /**
     * A copy of this trade with its result capped or `null` if nothing would be left to sell.
     */
    private fun MerchantRecipe.capped(): MerchantRecipe? {
        val result = result.clone()
        result.enforceCaps()
        if (result.isEmptyBook) return null

        return withResult(result)
    }

    /**
     * A copy of this trade selling [result] instead. Uses, max uses, and experience are kept from the original so that
     * rerolling or capping can't refresh a trade that needs to be restocked.
     */
    private fun MerchantRecipe.withResult(
        result: ItemStack,
        ingredients: List<ItemStack> = this.ingredients,
    ): MerchantRecipe = MerchantRecipe(
        result, uses, maxUses, hasExperienceReward(), villagerExperience,
        priceMultiplier, demand, specialPrice, shouldIgnoreDiscounts(),
    ).also { it.ingredients = ingredients }

    /**
     * Caps enchantments, calling [remove] if a book ends up empty.
     *
     * @return `true` if anything changed
     */
    private fun ItemStack.enforce(remove: () -> Unit): Boolean {
        if (!enforceCaps()) return false
        if (isEmptyBook) remove()
        return true
    }

    /**
     * Lowers or removes enchantments that are over their level cap.
     *
     * @return `true` if anything changed
     */
    private fun ItemStack.enforceCaps(): Boolean {
        val overCap = overCap(type.key(), allEnchantments)
        if (overCap.isEmpty()) return false

        val removeAll = settings.removeEnchantment
        editMeta { meta ->
            val book = meta as? EnchantmentStorageMeta // the meta actually being edited
            for ((enchantment, cap) in overCap) {
                if (book != null) book.removeStoredEnchant(enchantment) else meta.removeEnchant(enchantment)
                if (cap > 0 && !removeAll) {
                    if (book != null) book.addStoredEnchant(enchantment, cap, false)
                    else meta.addEnchant(enchantment, cap, false)
                }
            }
        }
        return true
    }

    /**
     * @return `true` if the stack contains no blacklisted enchantments
     */
    private fun ItemStack.isLegal(): Boolean = overCap(type.key(), allEnchantments).isEmpty()

    /**
     * Computes all enchantments that are disabled or over-leveled on this [item] type.
     *
     * @return a map of the corrected enchantments
     */
    private fun overCap(item: Key, enchantments: Map<Enchantment, Int>): Map<Enchantment, Int> {
        val settings = settings
        return buildMap {
            for ((enchantment, level) in enchantments) {
                val cap = settings.capFor(item, enchantment.key()) ?: continue
                if (level > cap) put(enchantment, cap)
            }
        }
    }

    /**
     * Gets the configured level cap for the [enchantment] on this [item] type.
     */
    private fun UeConfig.Blacklist.capFor(item: Key, enchantment: Key): Int? {
        if (enabled) blacklist[enchantment]?.let { return it }
        if (perItemEnabled) return perItem[item]?.get(enchantment)
        return null
    }
}
