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
    private final ConfigurationManager cfgManager;

    public UeConfig(@NotNull Path folder) throws IOException {
        this.cfgManager = new ConfigurationManager(folder, "config.yml");
        this.setDefaults();
    }

    private void setDefaults() {
        YamlConfiguration config = this.cfgManager.get();

        config.addDefault(UeConstants.Config.CONFIG_VERSION, 1);
        config.addDefault(UeConstants.Config.ANVIL_ENABLED, false);
        config.addDefault(UeConstants.Config.ANVIL_MAX_LVL, 120);
        config.addDefault(UeConstants.Config.ANVIL_SEPARATOR, UeConstants.Gui.DEFAULT_SEPERATOR);
        config.addDefault(UeConstants.Config.BLACKLIST_ENABLED, false);
        config.addDefault(UeConstants.Config.BLACKLISTED_ENCHANTS, List.of("aqua_affinity", "bane_of_arthropods"));

        config.options().copyDefaults(true);
        config.options().setHeader(List.of(
                "This configuration allows you to modify the maximum levels of all registered enchantments.",
                "Note that the effects of some enchantments scale very little with higher levels, and a few have no changes.",
                "For a complete list of vanilla enchantments and their resource locations, visit:",
                "https://minecraft.wiki/w/Java_Edition_data_values#Enchantments",
                "The effects of Channeling, Curse of Binding, Curse of Vanishing, Flame, Infinity, Mending, and Silk Touch do not change with increased levels.")
        );

        this.cfgManager.save();
    }

    public boolean isAnvilEnabled() {
        return this.cfgManager.get().getBoolean(UeConstants.Config.ANVIL_ENABLED);
    }

    public void setAnvilEnabled(boolean enabled) {
        this.cfgManager.get().set(UeConstants.Config.ANVIL_ENABLED, enabled);
    }

    public int getAnvilMaxLevel() {
        return this.cfgManager.get().getInt(UeConstants.Config.ANVIL_MAX_LVL);
    }

    public void setAnvilMaxLevel(int level) {
        this.cfgManager.get().set(UeConstants.Config.ANVIL_MAX_LVL, level);
    }

    /**
     * Loads and parses the base64 head texture for the input/output seperator in the anvil. If the configured base64
     * is invalid, an error will be logged and the plugin will use the default.
     *
     * @return the texture string for the input/output seperator slot or the default
     */
    public @NotNull String getAnvilHeadTexture() {
        @NotNull String base64 = this.cfgManager.get()
                .getString(UeConstants.Config.ANVIL_SEPARATOR, UeConstants.Gui.DEFAULT_SEPERATOR);

        try {
            Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            UeConstants.LOGGER.error("{} had invalid base64; using the default head icon.", UeConstants.Config.ANVIL_SEPARATOR);
            base64 = UeConstants.Gui.DEFAULT_SEPERATOR;
        }

        return base64;
    }

    public boolean isBlacklistEnabled() {
        return this.cfgManager.get().getBoolean(UeConstants.Config.BLACKLIST_ENABLED);
    }

    public void setBlacklistEnabled(boolean enabled) {
        this.cfgManager.get().set(UeConstants.Config.BLACKLIST_ENABLED, enabled);
    }

    @SuppressWarnings("PatternValidation")
    public Set<TypedKey<@NotNull Enchantment>> getBlacklistedKeys() {
        final Set<TypedKey<@NotNull Enchantment>> blacklist = Sets.newHashSet();
        if (!this.isBlacklistEnabled()) return blacklist;

        final List<String> unparsedStrs = this.cfgManager.get().getStringList(UeConstants.Config.BLACKLISTED_ENCHANTS);

        for (String unparsed : unparsedStrs) {
            if (!Key.parseable(unparsed)) {
                UeConstants.LOGGER.error("Unable to parse {} as a registry key, skipping it", unparsed);
                continue;
            }

            blacklist.add(TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(unparsed)));
        }

        return blacklist;
    }

    public Set<@NotNull Enchantment> getBlacklistedEnchantments() {
        Registry<@NonNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return this.getBlacklistedKeys().stream().map(registry::get).filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void addBlacklistedEnchantment(Enchantment enchantment) {
        YamlConfiguration config = this.cfgManager.get();

        List<String> blacklist = config.getStringList(UeConstants.Config.BLACKLISTED_ENCHANTS);
        blacklist.add(Helpers.asKey(enchantment));

        config.set(UeConstants.Config.BLACKLISTED_ENCHANTS, blacklist);
        this.save();
    }

    public int getMaxLevel(@NotNull Enchantment enchantment) {
        return getOrComputeMaxLevel(enchantment.key().key(), enchantment.getMaxLevel());
    }

    public void setMaxLevel(@NotNull Key enchantment, int value) {
        final String path = UeConstants.Config.ENCHANT_LEVEL_RANGES + "." + enchantment.namespace() + "." + enchantment.value();
        this.cfgManager.get().set(path, value);
        this.save();
    }

    public int getOrComputeMaxLevel(@NotNull Key enchantment, int def) {
        YamlConfiguration config = this.cfgManager.get();
        final String path = UeConstants.Config.ENCHANT_LEVEL_RANGES + "." + enchantment.namespace() + "." + enchantment.value();

        Object levelObj = config.get(path);
        if (levelObj instanceof Integer level && level >= 0) return level;

        config.set(path, def);
        return def;
    }

    public void reload() {
        this.cfgManager.reload();
    }

    public void save() {
        this.cfgManager.save();
    }
}
