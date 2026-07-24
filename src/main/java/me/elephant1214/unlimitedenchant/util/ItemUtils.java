package me.elephant1214.unlimitedenchant.util;

import me.elephant1214.unlimitedenchant.UeConstants;
import me.elephant1214.unlimitedenchant.UnlimitedEnchant;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class ItemUtils {
    /**
     * Unmodifiable set containing all anvil material types.
     */
    private static final Set<Material> ANVIL_TYPES = Set.of(Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL);

    /**
     * Checks whether the specified block is any variant of an anvil.
     *
     * @param block the block to evaluate
     * @return {@code true} if the block is an anvil variant, {@code false} otherwise
     */
    public static boolean isAnvil(@NotNull Block block) {
        return ANVIL_TYPES.contains(block.getType());
    }

    /**
     * Key used to mark an item stack as immovable in its {@link PersistentDataContainer}.
     */
    public static final NamespacedKey IMMOVABLE_KEY = new NamespacedKey(UnlimitedEnchant.getInstance(), UeConstants.Misc.IMMOVABLE_VALUE);

    /**
     * Marks the given {@link ItemStack} as immovable by setting a flag in its
     * {@link PersistentDataContainer}.
     *
     * @param stack the item stack to modify
     * @return the mutated item stack
     */
    public static @NotNull ItemStack makeImmovable(@NotNull ItemStack stack) {
        stack.editPersistentDataContainer(pdc -> pdc.set(IMMOVABLE_KEY, PersistentDataType.BOOLEAN, true));
        return stack;
    }

    /**
     * Checks whether the given {@link ItemStack} is marked as immovable.
     *
     * @param stack the item stack to check
     * @return {@code true} if the stack cannot be moved or taken, {@code false} otherwise
     */
    public static boolean isImmovable(@NotNull ItemStack stack) {
        return Boolean.TRUE.equals(
                stack.getPersistentDataContainer().get(IMMOVABLE_KEY, PersistentDataType.BOOLEAN)
        );
    }
}
