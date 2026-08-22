package me.elephant1214.unlimitedenchant.blacklist;

import me.elephant1214.unlimitedenchant.UeConstants;
import me.elephant1214.unlimitedenchant.UnlimitedEnchant;
import me.elephant1214.unlimitedenchant.bootstrap.UeBootstrap;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class BlacklistManager {
    private final UnlimitedEnchant plugin;
    private boolean listenersRegistered = false;
    @Nullable Set<Enchantment> blacklistedEnchants = null;

    public BlacklistManager(UnlimitedEnchant plugin) {
        this.plugin = plugin;

        if (UeBootstrap.config.isBlacklistEnabled()) {
            this.enableBlacklist();
        }
    }

    public void updateBlacklist() {
        this.blacklistedEnchants = UeBootstrap.config.getBlacklistedEnchantments();
    }

    public boolean contains(@NotNull Enchantment enchantment) {
        return this.blacklistedEnchants != null && this.blacklistedEnchants.contains(enchantment);
    }

    public void enableBlacklist() {
        UeConstants.LOGGER.info("Enchantment blacklist is now enabled");

        this.updateBlacklist();
        if (!this.listenersRegistered) {
            this.plugin.getServer().getPluginManager().registerEvents(new BlacklistListeners(this), this.plugin);
            this.listenersRegistered = true;
        }
        UeBootstrap.config.setBlacklistEnabled(true);
    }

    public void disableBlacklist() {
        UeConstants.LOGGER.info("Enchantment blacklist is now disabled");

        UeBootstrap.config.setBlacklistEnabled(false);
        this.blacklistedEnchants = null;
    }
}
