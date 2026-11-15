package me.elephant1214.unlimitedenchant

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryEntryAddEvent
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

val ItemStack?.isNullOrEmpty
    get() = this == null || this.isEmpty || this.type == Material.AIR || this.amount < 1

@Suppress("UnstableApiUsage")
val RegistryEntryAddEvent<Enchantment, EnchantmentRegistryEntry.Builder>.adventureKey: Key
    get() = this.key().key()
