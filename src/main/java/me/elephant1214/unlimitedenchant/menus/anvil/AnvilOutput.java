package me.elephant1214.unlimitedenchant.menus.anvil;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record AnvilOutput(@NotNull ItemStack stack, int cost) {
}
