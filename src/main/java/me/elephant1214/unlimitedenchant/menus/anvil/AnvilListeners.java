package me.elephant1214.unlimitedenchant.menus.anvil;

import me.elephant1214.unlimitedenchant.UnlimitedEnchant;
import me.elephant1214.unlimitedenchant.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class AnvilListeners implements Listener {
    private final UnlimitedEnchant plugin;

    public AnvilListeners(UnlimitedEnchant plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("DataFlowIssue") // getClickedBlock cannot be null if hasBlock is true
    @EventHandler
    public void onOpenAnvil(PlayerInteractEvent event) {
        if (event.hasBlock() && Util.isAnvil(event.getClickedBlock())) {
            AnvilGui menu = AnvilGui.makeMenu(event.getPlayer(), event.getClickedBlock());
            menu.openInventory();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClickInv(InventoryClickEvent event) {
        AnvilGui holder = AnvilGui.getMenu((Player) event.getView().getPlayer());
        if (holder == null || event.getInventory() != holder.getInventory()) return;

        final ItemStack stack = event.getCurrentItem();
        final boolean immovable = stack != null && Util.isImmovable(stack);
        if (immovable) {
            event.setCancelled(true);
        } else if (event.getSlot() == AnvilGui.RESULT_SLOT && holder.canTakeResult()) {
            holder.onTake();
        }

        Bukkit.getScheduler().runTaskLater(this.plugin, holder::update, 1L);
    }

    @EventHandler
    public void onInvDrag(InventoryDragEvent event) {
        AnvilGui holder = AnvilGui.getMenu((Player) event.getView().getPlayer());
        if (holder == null || event.getView().getTopInventory() != holder.getInventory()) return;

        for (int slot : event.getRawSlots()) {
            ItemStack stack = event.getView().getItem(slot);
            if (stack != null && Util.isImmovable(stack)) {
                event.setCancelled(true);
                break;
            }
        }

        if (!event.isCancelled() && event.getRawSlots().contains(AnvilGui.RESULT_SLOT) && holder.canTakeResult()) {
            holder.onTake();
        }

        Bukkit.getScheduler().runTaskLater(this.plugin, holder::update, 1L);
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        AnvilGui holder = AnvilGui.getMenu((Player) event.getView().getPlayer());
        if (holder == null || holder.getInventory() != event.getInventory()) return;

        holder.onClose();
    }
}
