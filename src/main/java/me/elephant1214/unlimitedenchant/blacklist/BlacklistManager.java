package me.elephant1214.unlimitedenchant.blacklist;

import me.elephant1214.unlimitedenchant.UEConstants;
import me.elephant1214.unlimitedenchant.UnlimitedEnchant;
import me.elephant1214.unlimitedenchant.bootstrap.UEBootstrap;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class BlacklistManager {
    private final UnlimitedEnchant plugin;
    private boolean listenersRegistered = false;
    private boolean blacklistEnabled = UEBootstrap.config.blacklistEnabled();
    @Nullable Set<Enchantment> blacklistedEnchants = null;

    public BlacklistManager(UnlimitedEnchant plugin) {
        this.plugin = plugin;

        if (this.blacklistEnabled) {
            this.enableBlacklist();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isEnabled() {
        return this.blacklistEnabled;
    }

    public void updateBlacklist() {
        this.blacklistedEnchants = UEBootstrap.config.blacklistedEnchantments();
    }

    public boolean contains(@NotNull Enchantment enchantment) {
        return this.blacklistedEnchants != null && this.blacklistedEnchants.contains(enchantment);
    }

    public void enableBlacklist() {
        UEConstants.LOGGER.info("Enchantment blacklist is now enabled");

        this.blacklistedEnchants = UEBootstrap.config.blacklistedEnchantments();
        if (!this.listenersRegistered) {
            this.plugin.getServer().getPluginManager().registerEvents(new BlacklistListeners(this), this.plugin);
            this.listenersRegistered = true;
        }
        this.blacklistEnabled = true;
    }

    public void disableBlacklist() {
        UEConstants.LOGGER.info("Enchantment blacklist is now disabled");

        this.blacklistEnabled = false;
        this.blacklistedEnchants = null;
    }
}
