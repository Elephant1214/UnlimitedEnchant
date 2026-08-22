package me.elephant1214.unlimitedenchant.gui.anvil;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import io.papermc.paper.datacomponent.DataComponentTypes;
import me.elephant1214.unlimitedenchant.UeConstants;
import me.elephant1214.unlimitedenchant.UnlimitedEnchant;
import me.elephant1214.unlimitedenchant.bootstrap.UeBootstrap;
import me.elephant1214.unlimitedenchant.util.ItemUtils;
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
import org.bukkit.enchantments.Enchantment;
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

@SuppressWarnings("UnstableApiUsage")
public final class AnvilGui implements InventoryHolder {
    public static final int ANVIL_SLOT = 8;
    public static final int INPUT1_SLOT = 10;
    public static final int INPUT2_SLOT = 12;
    public static final int SEPARATOR_SLOT = 14;
    public static final int RESULT_SLOT = 16;
    public static final int COST_SLOT = 25;
    private static final int INV_SIZE = 9 * 3;

    private final @NotNull Player player;
    private final @NotNull AnvilView anvilView;
    private final @NotNull Block anvil;
    private final @NotNull Inventory inv;
    private boolean canTake = false;

    public AnvilGui(@NotNull Player player, @NotNull Block anvil) {
        this.player = player;
        this.anvilView = buildAnvilView(player, anvil);
        this.anvil = anvil;
        this.inv = Bukkit.createInventory(this, INV_SIZE, Component.translatable("container.repair"));
    }

    private static AnvilView buildAnvilView(@NotNull Player player, @NotNull Block anvil) {
        return MenuType.ANVIL.builder()
                .location(anvil.getLocation())
                .build(player);
    }

    public void openInventory() {
        update();
        addAnvilButton();
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
            this.setSlots(AnvilPlaceholders.WAITING, AnvilPlaceholders.FILLER_STACK, AnvilPlaceholders.FILLER_STACK);
            this.canTake = false;
            return;
        }

        if (has1 ^ has2) {
            this.setSlots(AnvilPlaceholders.INVALID, AnvilPlaceholders.FILLER_STACK, AnvilPlaceholders.FILLER_STACK);
            this.canTake = false;
            return;
        }

        final @Nullable AnvilOutput result = getRecipe(input1.clone(), input2.clone());
        if (result == null) {
            this.setSlots(AnvilPlaceholders.INVALID, AnvilPlaceholders.FILLER_STACK, AnvilPlaceholders.FILLER_STACK);
            this.canTake = false;
            return;
        }

        this.canTake = player.getLevel() >= result.cost() || this.creativeMode();

        this.setSlots(
                this.canTake ? AnvilPlaceholders.WAITING : AnvilPlaceholders.INVALID,
                this.canTake ? result.stack() : ItemUtils.makeImmovable(result.stack()),
                makeCostStack(result.cost())
        );
    }

    public boolean canTakeResult() {
        return this.canTake;
    }

    /**
     * Consumes the input items and the player's experience, placing the output in the
     * result slot.
     */
    public void onTake() {
        this.inv.setItem(INPUT1_SLOT, null);
        this.inv.setItem(INPUT2_SLOT, null);

        AnvilMenu menu = (AnvilMenu) ((CraftAnvilView) this.anvilView).getHandle();
        menu.slots.get(menu.getResultSlot())
                .onTake(((CraftPlayer) this.player).getHandle(), menu.getItems().get(menu.getResultSlot()));
    }

    /**
     * Handles returning a player's items when they close the GUI.
     */
    private void returnItems() {
        ItemStack input1 = this.inv.getItem(INPUT1_SLOT);
        ItemStack input2 = this.inv.getItem(INPUT2_SLOT);

        List<ItemStack> items = Lists.newArrayList();
        if (input1 != null) items.add(input1);
        if (input2 != null) items.add(input2);

        if (!items.isEmpty()) {
            this.player.give(items, true);
        }
    }

    /**
     * Starts a task to run {@link AnvilGui#returnItems()} to run one tick after the GUI
     * is closed.
     */
    public void onClose() {
        Bukkit.getScheduler().runTaskLater(UnlimitedEnchant.getInstance(), AnvilGui.this::returnItems, 1L);
    }

    /**
     * Updates the underlying {@link AnvilMenu} with item changes and forces it to
     * recalculate the result.
     */
    private @Nullable ItemStack updateAnvil(@Nullable ItemStack input1, @Nullable ItemStack input2) {
        AnvilMenu menu = (AnvilMenu) ((CraftAnvilView) this.anvilView).getHandle();
        AnvilInventory anvil = this.anvilView.getTopInventory();
        anvil.setFirstItem(input1);
        anvil.setSecondItem(input2);
        menu.createResult();

        return anvil.getResult();
    }

    /**
     * Uses the {@link AnvilView} to calculate the result and cost of a repair, then
     * returns both.
     *
     * @return null if nothing was produced, otherwise an {@link AnvilOutput} with the
     * result stack and cost.
     */
    private @Nullable AnvilOutput getRecipe(ItemStack input1, ItemStack input2) {
        this.anvilView.setMaximumRepairCost(
                this.bypassLimitPerm()
                        ? 8192 // What are you even doing if you make something cost this much?
                        : UeBootstrap.config.getAnvilMaxLevel()
        );

        ItemStack result = this.updateAnvil(input1, input2);
        if (result == null) return null;

        final Map<Enchantment, Integer> enchantments = result.getEnchantments();
        final ItemMeta input1Meta = input1.getItemMeta().clone();

        // For some reason the anvil doesn't transfer meta on its own
        result.setItemMeta(input1Meta);
        result.addEnchantments(enchantments);

        return new AnvilOutput(result, this.anvilView.getRepairCost());
    }

    /**
     * Creates the experience bottle stack that displays the total cost for the repair.
     */
    private ItemStack makeCostStack(int cost) {
        ItemStack stack = ItemStack.of(Material.EXPERIENCE_BOTTLE);
        stack.setData(DataComponentTypes.MAX_STACK_SIZE, 99);
        stack.setAmount(cost);

        stack.editMeta(meta -> {
            if (cost > UeBootstrap.config.getAnvilMaxLevel() && !this.bypassLimitPerm() && !this.creativeMode()) {
                meta.customName(
                        Component.translatable("container.repair.expensive")
                                .style(Style.style(NamedTextColor.RED))
                );
            } else {
                meta.customName(
                        Component.translatable("container.repair.cost", Component.text(cost))
                                .style(Style.style(this.canTake ? NamedTextColor.GREEN : NamedTextColor.RED))
                );
            }
        });

        ItemUtils.makeImmovable(stack);
        return stack;
    }

    /**
     * Opens a normal vanilla anvil gui using the same anvil that this was already
     * created for.
     */
    public void openRealAnvil() {
        this.player.openInventory(buildAnvilView(this.player, this.anvil));
    }

    private void addAnvilButton() {
        this.inv.setItem(ANVIL_SLOT, ItemUtils.makeImmovable(ItemStack.of(Material.ANVIL)));
    }

    /**
     * The slots where a player is allowed to either place or take items. Used to quickly
     * fill all other slots with the filler item.
     */
    private static final Set<Integer> INTERACT_SLOTS = ImmutableSet.of(10, 12, 16);

    private void addFiller() {
        for (int slot = 0; slot < INV_SIZE; slot++) {
            if (this.inv.getItem(slot) == null && !INTERACT_SLOTS.contains(slot)) {
                this.inv.setItem(slot, AnvilPlaceholders.FILLER_STACK);
            }
        }
    }

    private boolean bypassLimitPerm() {
        return this.player.hasPermission(UeConstants.Permissions.BYPASS_ANVIL_LIMIT);
    }

    private boolean creativeMode() {
        return this.player.getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public @NonNull Inventory getInventory() {
        return this.inv;
    }
}
