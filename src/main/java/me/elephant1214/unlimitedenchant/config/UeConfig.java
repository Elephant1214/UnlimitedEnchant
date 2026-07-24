package me.elephant1214.unlimitedenchant.config;

import com.google.common.collect.Sets;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import me.elephant1214.unlimitedenchant.UeConstants;
import me.elephant1214.unlimitedenchant.util.Helpers;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handling for the config because it has to be loaded before the main plugin class exists.
 */
public final class UeConfig {
    private final ConfigurationManager manager;

    public UeConfig(@NotNull Path folder) throws IOException {
        this.manager = new ConfigurationManager(folder, "config.yml");
        this.setDefaults();
    }

    private void setDefaults() {
        YamlConfiguration config = this.manager.get();

        config.addDefault(UeConstants.Config.CONFIG_VERSION, 1);
        config.addDefault(UeConstants.Config.CUSTOM_ANVIL_ENABLED, false);
        config.addDefault(UeConstants.Config.CUSTOM_ANVIL_MAX_LVL, 120);
        config.addDefault(UeConstants.Config.CUSTOM_ANVIL_SEPARATOR, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19");
        config.addDefault(UeConstants.Config.BLACKLIST_ENABLED, false);
        config.addDefault(UeConstants.Config.BLACKLIST_ENCHANTS, List.of("aqua_affinity", "bane_of_arthropods"));

        config.options().copyDefaults(true);
        config.options().setHeader(List.of(
                "This configuration allows you to modify the maximum levels of all registered enchantments.",
                "Note that the effects of some enchantments scale very little with higher levels, and a few have no changes.",
                "For a complete list of vanilla enchantments and their resource locations, visit:",
                "https://minecraft.wiki/w/Java_Edition_data_values#Enchantments",
                "The effects of Channeling, Curse of Binding, Curse of Vanishing, Flame, Infinity, Mending, and Silk Touch do not change with increased levels.")
        );

        this.manager.save();
    }

    public boolean customAnvilEnabled() {
        return this.manager.get().getBoolean(UeConstants.Config.CUSTOM_ANVIL_ENABLED);
    }

    public void setCustomAnvil(boolean enabled) {
        this.manager.get().set(UeConstants.Config.CUSTOM_ANVIL_ENABLED, enabled);
    }

    public int customAnvilMaxLevel() {
        return this.manager.get().getInt(UeConstants.Config.CUSTOM_ANVIL_MAX_LVL);
    }

    public void setAnvilMaxLevel(int level) {
        this.manager.get().set(UeConstants.Config.CUSTOM_ANVIL_MAX_LVL, level);
    }

    @SuppressWarnings("DataFlowIssue")
    public String customAnvilSeparator() {
        String base64 = this.manager.get().getString(UeConstants.Config.CUSTOM_ANVIL_SEPARATOR);

        try {
            Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            UeConstants.LOGGER.error("{} had invalid base64; using the default head icon.", UeConstants.Config.CUSTOM_ANVIL_SEPARATOR);
            base64 = this.manager
                    .get()
                    .getDefaults()
                    .getString(UeConstants.Config.CUSTOM_ANVIL_SEPARATOR);
        }

        return base64;
    }

    public boolean blacklistEnabled() {
        return this.manager.get().getBoolean(UeConstants.Config.BLACKLIST_ENABLED);
    }

    public void setBlacklist(boolean enabled) {
        this.manager.get().set(UeConstants.Config.BLACKLIST_ENABLED, enabled);
    }

    @SuppressWarnings("PatternValidation")
    public Set<TypedKey<@NotNull Enchantment>> blacklistedKeys() {
        final Set<TypedKey<@NotNull Enchantment>> blacklist = Sets.newHashSet();
        if (!this.blacklistEnabled()) return blacklist;

        final List<String> unparsedStrs = this.manager.get().getStringList(UeConstants.Config.BLACKLIST_ENCHANTS);

        for (String unparsed : unparsedStrs) {
            if (!Key.parseable(unparsed)) {
                UeConstants.LOGGER.error("Unable to parse {} as a registry key, skipping it", unparsed);
                continue;
            }

            blacklist.add(TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(unparsed)));
        }

        return blacklist;
    }

    public Set<@NotNull Enchantment> blacklistedEnchantments() {
        Registry<@NonNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return this.blacklistedKeys().stream().map(registry::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public void blacklistEnchantment(Enchantment enchantment) {
        YamlConfiguration config = this.manager.get();

        List<String> blacklist = config.getStringList(UeConstants.Config.BLACKLIST_ENCHANTS);
        blacklist.add(Helpers.asKey(enchantment));

        config.set(UeConstants.Config.BLACKLIST_ENCHANTS, blacklist);
        this.save();
    }

    public int getOrSetMaxLevel(@NotNull Key enchantment, int def) {
        YamlConfiguration config = this.manager.get();
        final String path = UeConstants.Config.ENCHANTMENT_LEVELS + "." + enchantment.namespace() + "." + enchantment.value();

        Object levelObj = config.get(path);
        if (levelObj instanceof Integer level && level >= 0) return level;

        config.set(path, def);
        return def;
    }

    public int getMaxLevel(@NotNull Enchantment enchantment) {
        return getOrSetMaxLevel(enchantment.key().key(), enchantment.getMaxLevel());
    }

    public void setMaxLevel(@NotNull Key enchantment, int value) {
        final String path = UeConstants.Config.ENCHANTMENT_LEVELS + "." + enchantment.namespace() + "." + enchantment.value();
        this.manager.get().set(path, value);
        this.save();
    }

    public void reload() {
        this.manager.reload();
    }

    public void save() {
        this.manager.save();
    }
}
