package me.elephant1214.unlimitedenchant.gui.anvil;

import me.elephant1214.unlimitedenchant.UnlimitedEnchantOld;
import me.elephant1214.unlimitedenchant.util.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class AnvilListenersOld implements Listener {
    private final UnlimitedEnchantOld plugin;

    public AnvilListenersOld(UnlimitedEnchantOld plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onOpenAnvil(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || !ItemUtils.isAnvil(event.getClickedBlock())) return;

        AnvilGui menu = new AnvilGui(event.getPlayer(), event.getClickedBlock());
        menu.openInventory();
        event.setCancelled(true);
    }

    @EventHandler
    public void onClickInv(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof AnvilGui holder)) return;

        final ItemStack stack = event.getCurrentItem();
        final boolean immovable = stack != null && ItemUtils.isImmovable(stack);
        if (immovable) {
            event.setCancelled(true);
            if (event.getSlot() == AnvilGui.ANVIL_SLOT) {
                holder.openRealAnvil();
            }
        } else if (event.getSlot() == AnvilGui.RESULT_SLOT && holder.canTakeResult()) {
            holder.onTake();
        }

        Bukkit.getScheduler().runTaskLater(this.plugin, holder::update, 1L);
    }

    @EventHandler
    public void onInvDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof AnvilGui holder)) return;

        for (int slot : event.getRawSlots()) {
            ItemStack stack = event.getView().getItem(slot);
            if (stack != null && ItemUtils.isImmovable(stack)) {
                event.setCancelled(true);
                if (slot == AnvilGui.ANVIL_SLOT) {
                    holder.openRealAnvil();
                }
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
        if (!(event.getInventory().getHolder() instanceof AnvilGui holder)) return;

        holder.onClose();
    }
}
