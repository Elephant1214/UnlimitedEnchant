package me.elephant1214.unlimitedenchant.gui.anvil;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record AnvilOutput(@NotNull ItemStack stack, int cost) {
}
