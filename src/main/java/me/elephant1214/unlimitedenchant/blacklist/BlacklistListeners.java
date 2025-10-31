package me.elephant1214.unlimitedenchant.blacklist;

import com.google.common.collect.ImmutableList;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftInventoryMerchant;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Listeners for the enchantment blacklist.
 */
@SuppressWarnings("DataFlowIssue") // If useBlacklist is true, getBlacklist cannot be null
public final class BlacklistListeners implements Listener {
    private final BlacklistManager manager;

    BlacklistListeners(BlacklistManager manager) {
        this.manager = manager;
    }

    private Set<Enchantment> filterBlacklisted(@NotNull Set<Enchantment> enchants) {
        return enchants.stream()
                .filter(enchant -> this.manager.blacklistedEnchants.contains(enchant))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @return true if any enchantments were removed
     */
    private boolean removeBlacklisted(@NotNull Set<Enchantment> enchants, @NotNull Consumer<Enchantment> remover) {
        final Set<Enchantment> filtered = filterBlacklisted(enchants);
        if (filtered.isEmpty()) {
            return false;
        } else {
            filtered.forEach(remover);
            return true;
        }
    }

    /**
     * @return true if the book no longer contains enchantments
     */
    private boolean handleBook(@NotNull ItemStack stack) {
        if (!(stack.getItemMeta() instanceof EnchantmentStorageMeta storage) || !storage.hasStoredEnchants()) {
            return false;
        }

        boolean modified = removeBlacklisted(storage.getStoredEnchants().keySet(), storage::removeStoredEnchant);
        if (storage.getStoredEnchants().isEmpty())
            return true; // the copy of the meta can be thrown away, the item should be deleted

        if (modified) stack.setItemMeta(storage);
        return false;
    }

    /**
     * @return true if any enchantments were removed.
     */
    private boolean removeBlacklisted(@NotNull ItemStack stack) {
        return removeBlacklisted(stack.getEnchantments().keySet(), stack::removeEnchantment);
    }

    /**
     * @return true if the stack is legal (doesn't contain any blacklisted enchantments).
     */
    private boolean isStackLegal(@NotNull ItemStack stack) {
        Set<Enchantment> onItem;
        if (stack.getItemMeta() instanceof EnchantmentStorageMeta storage && storage.hasStoredEnchants()) {
            onItem = storage.getStoredEnchants().keySet();
        } else {
            onItem = stack.getEnchantments().keySet();
        }

        return filterBlacklisted(onItem).isEmpty();
    }

    /**
     * @return true if any illegal trades were found and removed
     */
    private boolean checkTrades(Merchant merchant) {
        final List<MerchantRecipe> original = merchant.getRecipes();
        final int originalSize = original.size();
        MerchantRecipe[] recipes = new MerchantRecipe[original.size()];

        boolean anyRemoved = false;
        for (int idx = 0; idx < original.size(); idx++) {
            MerchantRecipe recipe = original.get(idx);

            if (isStackLegal(recipe.getResult())) {
                recipes[idx] = recipe;
            } else {
                anyRemoved = true;
            }
        }

        if (anyRemoved && merchant instanceof Villager villager) {
            villager.resetOffers();
            if (originalSize > 2)
                villager.addTrades(originalSize - 2);

            final List<MerchantRecipe> newRecipes = villager.getRecipes();
            for (int idx = 0; idx < originalSize; idx++) {
                if (recipes[idx] == null) {
                    recipes[idx] = newRecipes.get(idx);
                }
            }

            merchant.setRecipes(ImmutableList.copyOf(recipes));
        }

        return anyRemoved;
    }

    @SuppressWarnings("WhileLoopReplaceableByForEach")
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemClicked(InventoryClickEvent event) {
        if (!this.manager.isEnabled()) return;

        ItemStack currentItem = event.getCurrentItem();
        Inventory clicked = event.getClickedInventory();
        if (currentItem == null || clicked == null) return;

        if (clicked instanceof CraftInventoryMerchant merchantInv) {
            if (checkTrades(merchantInv.getMerchant())) {
                event.setCancelled(true);
                Iterator<HumanEntity> viewers = event.getViewers().iterator();
                while (viewers.hasNext()) { // concurrent modification exception with foreach
                    HumanEntity human = viewers.next();
                    human.closeInventory(); // inventory must be closed for all viewers, forcing an update does nothing
                }
                return;
            }
        }

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
