package me.elephant1214.unlimitedenchant

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryEntryAddEvent
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

val ItemStack?.isNullOrEmpty
    get() = this == null || this.isEmpty || this.type == Material.AIR || this.amount < 1

val ItemStack.isEmptyBook
    get() = (itemMeta as? EnchantmentStorageMeta)?.hasStoredEnchants() == false

/**
 * Gets the enchantments on this item stack. If it is an enchanted book, it gets stored enchantments.
 */
val ItemStack.allEnchantments: Map<Enchantment, Int>
    get() = (itemMeta as? EnchantmentStorageMeta)?.storedEnchants ?: enchantments

/**
 * Gets the Adventure identifier for the enchantment in the current registry event.
 */
@Suppress("UnstableApiUsage")
val RegistryEntryAddEvent<Enchantment, EnchantmentRegistryEntry.Builder>.adventureKey: Key
    get() = this.key().key()
