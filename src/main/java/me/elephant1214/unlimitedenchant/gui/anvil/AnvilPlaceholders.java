package me.elephant1214.unlimitedenchant.gui.anvil;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.elephant1214.unlimitedenchant.bootstrap.UeBootstrap;
import me.elephant1214.unlimitedenchant.util.ItemUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

final class AnvilPlaceholders {
    static final ItemStack FILLER_STACK = makeFillerStack(Material.GRAY_STAINED_GLASS_PANE);
    static final ItemStack WAITING = makeSeparator();
    static final ItemStack INVALID = makeFillerStack(Material.BARRIER);

    static ItemStack makeFillerStack(@NotNull Material material) {
        ItemStack renameStack = new ItemStack(material, 1);

        ItemMeta meta = renameStack.getItemMeta();
        meta.customName(Component.text(""));
        renameStack.setItemMeta(meta);

        ItemUtils.makeImmovable(renameStack);
        return renameStack;
    }

    static ItemStack makeSeparator() {
        ItemStack head = ItemStack.of(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        PlayerProfile profile = Bukkit.createProfile(new UUID(0, 0), "");
        ProfileProperty textures = new ProfileProperty("textures", UeBootstrap.config.getAnvilHeadTexture());
        profile.setProperty(textures);

        meta.setPlayerProfile(profile);
        meta.displayName(Component.text(""));
        head.setItemMeta(meta);

        ItemUtils.makeImmovable(head);
        return head;
    }
}
