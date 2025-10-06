package me.elephant1214.unlimitedenchant.blacklist;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Listeners for the enchantment blacklist.
 */
@SuppressWarnings("DataFlowIssue") // If useBlacklist is true, getBlacklist cannot be null
public final class BlacklistListeners implements Listener {
    private final BlacklistManager manager;

    BlacklistListeners(BlacklistManager manager) {
        this.manager = manager;
    }

    /**
     * @return true if any enchantments were removed
     */
    private boolean removeBlacklisted(@NotNull Map<Enchantment, Integer> enchants, @NotNull Consumer<Enchantment> remover) {
        boolean anyRemoved = false;

        for (final Enchantment enchantment : enchants.keySet()) {
            if (this.manager.blacklistedEnchants.contains(enchantment)) {
                remover.accept(enchantment);
                anyRemoved = true;
            }
        }
        return anyRemoved;
    }

    /**
     * @return true if the book no longer contains enchantments
     */
    private boolean handleBook(@NotNull ItemStack stack) {
        if (!(stack.getItemMeta() instanceof EnchantmentStorageMeta storage) || !storage.hasStoredEnchants()) {
            return false;
        }

        boolean modified = removeBlacklisted(storage.getStoredEnchants(), storage::removeStoredEnchant);
        if (storage.getStoredEnchants().isEmpty())
            return true; // the copy of the meta can be thrown away, the item should be deleted

        if (modified) stack.setItemMeta(storage);
        return false;
    }

    /**
     * @return true if any enchantments were removed.
     */
    private boolean removeBlacklisted(@NotNull ItemStack stack) {
        return removeBlacklisted(stack.getEnchantments(), stack::removeEnchantment);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemClicked(InventoryClickEvent event) {
        if (!this.manager.isEnabled()) return;

        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || event.getClickedInventory() == null) return;
        if (currentItem.getType() == Material.ENCHANTED_BOOK) {
            if (handleBook(currentItem)) {
                event.getClickedInventory().remove(currentItem);
            }
            return;
        }

        removeBlacklisted(currentItem);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!this.manager.isEnabled()) return;

        ItemStack pickedUp = event.getItem().getItemStack();
        if (pickedUp.getType() == Material.ENCHANTED_BOOK) {
            if (handleBook(pickedUp)) {
                event.getItem().remove();
                event.setCancelled(true);
            }
            return;
        }

        if (pickedUp.getEnchantments().isEmpty()) return;
        removeBlacklisted(pickedUp);
    }

    /**
     * @return true if the event the called this should be canceled.
     */
    private boolean checkUse(PlayerInventory inventory, ItemStack stack) {
        if (stack == null) return false;

        if (stack.getType() == Material.ENCHANTED_BOOK) {
            if (handleBook(stack)) {
                inventory.remove(stack);
                return true;
            }
            return false;
        }

        return removeBlacklisted(stack);
    }

    private void handleSimilar(@NotNull Cancellable event, @NotNull Player player) {
        if (!this.manager.isEnabled()) return;

        PlayerInventory inventory = player.getInventory();
        ItemStack main = inventory.getItemInMainHand();
        ItemStack offhand = inventory.getItemInOffHand();

        if (checkUse(inventory, main) || checkUse(inventory, offhand)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAttack(PrePlayerAttackEntityEvent event) {
        handleSimilar(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        handleSimilar(event, event.getPlayer());
    }
}
