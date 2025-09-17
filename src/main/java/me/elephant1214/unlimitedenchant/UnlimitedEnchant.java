package me.elephant1214.unlimitedenchant;

import me.elephant1214.unlimitedenchant.bootstrap.UEBootstrap;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class UnlimitedEnchant extends JavaPlugin implements Listener {
    private Set<Key> blacklistedEnchants;

    @Override
    public void onEnable() {
        if (UEBootstrap.config.blacklistEnabled()) {
            UEConfig.LOGGER.info("Enchantment blacklist is enabled. Blacklisted enchantments will be removed from any items that have them.");

            this.blacklistedEnchants = UEBootstrap.config.blacklistedEnchantments();
            getServer().getPluginManager().registerEvents(this, this);
        }
    }

    private boolean handleBook(ItemStack stack) {
        if (stack.getItemMeta() instanceof EnchantmentStorageMeta storage && storage.hasStoredEnchants()) {
            Map<Enchantment, Integer> enchants = new HashMap<>(storage.getStoredEnchants());

            Iterator<Enchantment> iter = enchants.keySet().iterator();
            while (iter.hasNext()) {
                Enchantment enchantment = iter.next();
                if (this.blacklistedEnchants.contains(enchantment.key())) {
                    storage.removeStoredEnchant(enchantment);
                    iter.remove();
                }
            }

            if (enchants.isEmpty()) {
                return true;
            } else {
                stack.setItemMeta(storage);
            }
        }
        return false;
    }

    private void removeBlacklisted(ItemStack stack) {
        for (Enchantment enchantment : stack.getEnchantments().keySet()) {
            if (this.blacklistedEnchants.contains(enchantment.key())) {
                stack.removeEnchantment(enchantment);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemClicked(InventoryClickEvent event) {
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
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
}
