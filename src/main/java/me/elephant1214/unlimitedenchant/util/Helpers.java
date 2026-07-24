package me.elephant1214.unlimitedenchant.util;

import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

public final class Helpers {
    public static @NotNull String asKey(@NotNull Enchantment enchantment) {
        return enchantment.key().key().asMinimalString();
    }
}
