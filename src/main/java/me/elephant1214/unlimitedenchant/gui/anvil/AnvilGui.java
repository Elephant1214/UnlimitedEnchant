package me.elephant1214.unlimitedenchant.gui.anvil;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
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
import org.bukkit.inventory.meta.SkullMeta;
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
    public static final int ANVIL_SLOT = 8;
    public static final int INPUT1_SLOT = 10;
    public static final int INPUT2_SLOT = 12;
    public static final int SEPARATOR_SLOT = 14;
    public static final int RESULT_SLOT = 16;
    public static final int COST_SLOT = 25;
    private static final int INV_SIZE = 9 * 3;
    private static final Set<Integer> INTERACT_SLOTS = Set.of(10, 12, 16);
    private static final Map<UUID, AnvilGui> ANVILS = Maps.newHashMap();
    private static final ItemStack FILLER_STACK = makeGuiPart(Material.GRAY_STAINED_GLASS_PANE);
    private static final ItemStack WAITING = makeSeparator();
    private static final ItemStack INVALID = makeGuiPart(Material.BARRIER);
    private final @NotNull Player player;
    private final @NotNull AnvilView anvilView;
    private final @NotNull Block anvil;
    private final @NotNull Inventory inv;
    private boolean canTake = false;

    public AnvilGui(@NotNull Player player, @NotNull Block anvil) {
        this.player = player;
        this.anvilView = buildAnvilView(player, anvil);
        this.anvil = anvil;
        this.inv = Bukkit.createInventory(player, INV_SIZE, Component.translatable("container.repair"));
    }

    public static @NotNull AnvilGui makeMenu(@NotNull Player player, @NotNull Block anvil) {
        return ANVILS.computeIfAbsent(player.getPlayerProfile().getId(), uuid -> new AnvilGui(player, anvil));
    }

    public static @Nullable AnvilGui getMenu(@NotNull Player player) {
        return ANVILS.get(player.getPlayerProfile().getId());
    }

    private static AnvilView buildAnvilView(@NotNull Player player, @NotNull Block anvil) {
        return MenuType.ANVIL.builder()
                .location(anvil.getLocation())
                .build(player);
    }

    private static ItemStack makeGuiPart(@NotNull Material material) {
        ItemStack renameStack = new ItemStack(material, 1);

        ItemMeta meta = renameStack.getItemMeta();
        meta.customName(Component.text(""));
        renameStack.setItemMeta(meta);

        Util.makeImmovable(renameStack);
        return renameStack;
    }

    private static ItemStack makeSeparator() {
        ItemStack head = ItemStack.of(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        PlayerProfile profile = Bukkit.createProfile(new UUID(0, 0), "");
        ProfileProperty textures = new ProfileProperty("textures", UEBootstrap.config.customAnvilSeparator());
        profile.setProperty(textures);

        meta.setPlayerProfile(profile);
        meta.displayName(Component.text(""));
        head.setItemMeta(meta);

        Util.makeImmovable(head);
        return head;
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
                this.canTake ? result.stack() : Util.makeImmovable(result.stack()),
                makeCostStack(result.cost())
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

    public void onClose() {
        ANVILS.remove(this.player.getPlayerProfile().getId());

        Bukkit.getScheduler().runTaskLater(UnlimitedEnchant.getInstance(), AnvilGui.this::returnItems, 1L);
    }

    private @Nullable ItemStack updateAnvil(@Nullable ItemStack input1, @Nullable ItemStack input2) {
        AnvilMenu menu = (AnvilMenu) ((CraftAnvilView) this.anvilView).getHandle();
        AnvilInventory anvil = this.anvilView.getTopInventory();
        anvil.setFirstItem(input1);
        anvil.setSecondItem(input2);
        menu.createResult();

        return anvil.getResult();
    }

    private @Nullable AnvilOutput getRecipe(ItemStack input1, ItemStack input2) {
        this.anvilView.setMaximumRepairCost(
                this.bypassLimitPerm()
                        ? 8192 // What are you doing to make something cost this much and how can you even afford it?
                        : UEBootstrap.config.customAnvilMaxLevel()
        );

        ItemStack result = this.updateAnvil(input1, input2);
        if (result == null) return null;

        if (input1.getItemMeta().hasDisplayName()) {
            ItemMeta meta = result.getItemMeta();
            meta.displayName(input1.getItemMeta().displayName());
            result.setItemMeta(meta);
        }

        return new AnvilOutput(result, this.anvilView.getRepairCost());
    }

    private ItemStack makeCostStack(int cost) {
        ItemStack stack = ItemStack.of(Material.EXPERIENCE_BOTTLE);
        stack.setData(DataComponentTypes.MAX_STACK_SIZE, 99);
        stack.setAmount(cost);

        ItemMeta meta = stack.getItemMeta();
        if (cost > UEBootstrap.config.customAnvilMaxLevel() && !this.bypassLimitPerm() && !this.creativeMode()) {
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
        stack.setItemMeta(meta);

        Util.makeImmovable(stack);
        return stack;
    }

    public void anvilClicked() {
        this.player.openInventory(buildAnvilView(this.player, this.anvil));
    }

    private void addAnvilButton() {
        this.inv.setItem(ANVIL_SLOT, Util.makeImmovable(ItemStack.of(Material.ANVIL)));
    }

    private void addFiller() {
        for (int slot = 0; slot < INV_SIZE; slot++) {
            if (this.inv.getItem(slot) == null && !INTERACT_SLOTS.contains(slot)) {
                this.inv.setItem(slot, FILLER_STACK);
            }
        }
    }

    private boolean bypassLimitPerm() {
        return this.player.hasPermission(UEConstants.Permissions.BYPASS_ANVIL_LIMIT);
    }

    private boolean creativeMode() {
        return this.player.getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public @NonNull Inventory getInventory() {
        return this.inv;
    }
}
