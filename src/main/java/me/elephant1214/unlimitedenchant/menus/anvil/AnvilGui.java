package me.elephant1214.unlimitedenchant.menus.anvil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.papermc.paper.datacomponent.DataComponentTypes;
import me.elephant1214.unlimitedenchant.UEConstants;
import me.elephant1214.unlimitedenchant.UnlimitedEnchant;
import me.elephant1214.unlimitedenchant.Util;
import me.elephant1214.unlimitedenchant.bootstrap.UEBootstrap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minecraft.world.inventory.AnvilMenu;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.view.CraftAnvilView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public final class AnvilGui implements InventoryHolder {
    public static final int INPUT1_SLOT = 10;
    public static final int INPUT2_SLOT = 12;
    public static final int SEPARATOR_SLOT = 14;
    public static final int RESULT_SLOT = 16;
    public static final int COST_SLOT = 25;
    private static final int INV_SIZE = 9 * 3;
    private static final Set<Integer> INTERACT_SLOTS = Set.of(10, 12, 16);
    private static final Map<UUID, AnvilGui> ANVILS = Maps.newHashMap();
    private static final ItemStack FILLER_STACK = getFillerStack();
    private static final ItemStack WAITING = Util.makeImmovable(ItemStack.of(Material.ARROW));
    private static final ItemStack INVALID = Util.makeImmovable(ItemStack.of(Material.BARRIER));
    private final @NotNull Player player;
    private final @NotNull AnvilView anvilView;
    private final @NotNull Inventory inv;
    private boolean canTake = false;

    public AnvilGui(@NotNull Player player, @NotNull Block anvil) {
        this.player = player;
        this.anvilView = MenuType.ANVIL.builder()
                .location(anvil.getLocation())
                .build(player);
        this.inv = Bukkit.createInventory(player, INV_SIZE, Component.translatable("container.repair"));
    }

    public static @NotNull AnvilGui makeMenu(@NotNull Player player, @NotNull Block anvil) {
        return ANVILS.computeIfAbsent(player.getPlayerProfile().getId(), uuid -> new AnvilGui(player, anvil));
    }

    public static @Nullable AnvilGui getMenu(@NotNull Player player) {
        return ANVILS.get(player.getPlayerProfile().getId());
    }

    private static ItemStack getFillerStack() {
        ItemStack renameStack = new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1);

        ItemMeta meta = renameStack.getItemMeta();
        meta.customName(Component.text(""));
        renameStack.setItemMeta(meta);

        Util.makeImmovable(renameStack);
        return renameStack;
    }

    public void openInventory() {
        update();
        addFiller();
        this.player.openInventory(this.inv);
    }

    private void setSlots(
            @NotNull final ItemStack separator,
            @NotNull final ItemStack result,
            @NotNull final ItemStack cost
    ) {
        this.inv.setItem(SEPARATOR_SLOT, separator);
        this.inv.setItem(RESULT_SLOT, result);
        this.inv.setItem(COST_SLOT, cost);
    }

    public void update() {
        ItemStack input1 = this.inv.getItem(INPUT1_SLOT);
        ItemStack input2 = this.inv.getItem(INPUT2_SLOT);

        boolean has1 = input1 != null;
        boolean has2 = input2 != null;

        if (!has1 && !has2) {
            this.setSlots(WAITING, FILLER_STACK, FILLER_STACK);
            this.canTake = false;
            return;
        }

        if (has1 ^ has2) {
            this.setSlots(INVALID, FILLER_STACK, FILLER_STACK);
            this.canTake = false;
            return;
        }

        final @Nullable AnvilOutput result = getRecipe(input1.clone(), input2.clone());
        if (result == null) {
            this.setSlots(INVALID, FILLER_STACK, FILLER_STACK);
            this.canTake = false;
            return;
        }

        this.canTake = player.getLevel() >= result.cost() || this.creativeMode();

        this.setSlots(
                this.canTake ? WAITING : INVALID,
                makeCostStack(result.cost()),
                this.canTake ? result.stack() : Util.makeImmovable(result.stack())
        );
    }

    public boolean canTakeResult() {
        return this.canTake;
    }

    public void onTake() {
        this.inv.setItem(INPUT1_SLOT, null);
        this.inv.setItem(INPUT2_SLOT, null);

        AnvilMenu menu = (AnvilMenu) ((CraftAnvilView) this.anvilView).getHandle();
        menu.slots.get(menu.getResultSlot()).onTake(((CraftPlayer) this.player).getHandle(), menu.getItems().get(menu.getResultSlot()));
    }

    public void onClose() {
        ANVILS.remove(player.getPlayerProfile().getId());

        Bukkit.getScheduler().runTaskLater(UnlimitedEnchant.getInstance(), () -> {
            ItemStack input1 = this.inv.getItem(INPUT1_SLOT);
            ItemStack input2 = this.inv.getItem(INPUT2_SLOT);

            List<ItemStack> items = Lists.newArrayList();
            if (input1 != null) items.add(input1);
            if (input2 != null) items.add(input2);

            if (!items.isEmpty()) {
                player.give(items, true);
            }
        }, 1L);
    }

    private @Nullable AnvilOutput getRecipe(ItemStack input1, ItemStack input2) {
        this.anvilView.setMaximumRepairCost(
                this.player.hasPermission(UEConstants.Permissions.BYPASS_ANVIL_LIMIT)
                        ? 8192 // What are you doing to make something cost this much and how can you even afford it?
                        : UEBootstrap.config.customAnvilMaxLevel()
        );

        AnvilMenu menu = (AnvilMenu) ((CraftAnvilView) this.anvilView).getHandle();
        AnvilInventory anvil = this.anvilView.getTopInventory();
        anvil.setFirstItem(input1);
        anvil.setSecondItem(input2);
        menu.createResult();

        if (anvil.getResult() == null) return null;
        return new AnvilOutput(anvil.getResult(), this.anvilView.getRepairCost());
    }

    private ItemStack makeCostStack(int cost) {
        ItemStack stack = ItemStack.of(Material.EXPERIENCE_BOTTLE);
        stack.setData(DataComponentTypes.MAX_STACK_SIZE, 99);
        stack.setAmount(cost);

        ItemMeta meta = stack.getItemMeta();
        meta.customName(
                Component.translatable(
                        "container.repair.cost",
                        "Enchantment Cost: %1$s",
                        Component.text(cost)
                ).style(Style.style(this.canTake ? NamedTextColor.GREEN : NamedTextColor.RED))
        );
        stack.setItemMeta(meta);

        Util.makeImmovable(stack);
        return stack;
    }

    private void addFiller() {
        for (int slot = 0; slot < INV_SIZE; slot++) {
            if (this.inv.getItem(slot) == null && !INTERACT_SLOTS.contains(slot)) {
                this.inv.setItem(slot, FILLER_STACK);
            }
        }
    }

    private boolean creativeMode() {
        return this.player.getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public @NonNull Inventory getInventory() {
        return this.inv;
    }
}
