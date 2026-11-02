package me.elephant1214.unlimitedenchant.blacklist;

import me.elephant1214.unlimitedenchant.UeConstants;
import me.elephant1214.unlimitedenchant.UnlimitedEnchantOld;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class BlacklistManager {
    private final UnlimitedEnchantOld plugin;
    private boolean listenersRegistered = false;
    @Nullable Set<Enchantment> blacklistedEnchants = null;

    public BlacklistManager(UnlimitedEnchantOld plugin) {
        this.plugin = plugin;

        if (UeBootstrapOld.config.isBlacklistEnabled()) {
            this.enableBlacklist();
        }
    }

    public void updateBlacklist() {
        this.blacklistedEnchants = UeBootstrapOld.config.getBlacklistedEnchantments();
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
        UeBootstrapOld.config.setBlacklistEnabled(true);
    }

    public void disableBlacklist() {
        UeConstants.LOGGER.info("Enchantment blacklist is now disabled");

        UeBootstrapOld.config.setBlacklistEnabled(false);
        this.blacklistedEnchants = null;
    }
}
