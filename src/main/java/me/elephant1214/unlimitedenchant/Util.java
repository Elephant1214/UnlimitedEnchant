package me.elephant1214.unlimitedenchant;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class Util {
    private static final Set<Material> ANVIL_TYPES = Set.of(Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL);

    public static boolean isAnvil(@NotNull Block block) {
        return ANVIL_TYPES.contains(block.getType());
    }

    public static final NamespacedKey IMMOVABLE_KEY = new NamespacedKey(UnlimitedEnchant.getInstance(), UEConstants.Misc.IMMOVABLE_VALUE);

    public static ItemStack makeImmovable(@NotNull ItemStack stack) {
        stack.editPersistentDataContainer(pdc -> pdc.set(Util.IMMOVABLE_KEY, PersistentDataType.BOOLEAN, true));

        return stack;
    }

    public static boolean isImmovable(@NotNull ItemStack stack) {
        return Boolean.TRUE.equals(
                stack.getPersistentDataContainer().get(Util.IMMOVABLE_KEY, PersistentDataType.BOOLEAN)
        );
    }

    public static String asKey(@NotNull Enchantment enchantment) {
        return enchantment.key().key().asMinimalString();
    }
}
