package me.elephant1214.unlimitedenchant

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

val ItemStack?.isNullOrEmpty
    get() = this == null || this.isEmpty || this.type == Material.AIR || this.amount < 1
