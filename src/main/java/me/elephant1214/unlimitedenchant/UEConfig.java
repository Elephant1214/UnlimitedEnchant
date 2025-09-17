package me.elephant1214.unlimitedenchant;

import net.kyori.adventure.key.Key;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handling for the config because it has to be loaded before the main plugin class exists.
 */
public final class UEConfig {
    public static final Logger LOGGER = LoggerFactory.getLogger("UnlimitedEnchant");
    private final Path configPath;
    private final YamlConfiguration config;

    public UEConfig(@NotNull Path path) throws IOException {
        if (Files.notExists(path)) Files.createDirectory(path);

        configPath = path.resolve("config.yml");

        boolean notExists = Files.notExists(configPath);
        if (notExists) Files.createFile(configPath);
        config = YamlConfiguration.loadConfiguration(configPath.toFile());

        if (notExists) {
            config.options().setHeader(List.of(
                    "This config allows you to modify the maximum levels of all registered enchantments.",
                    "You must restart the server for changes to apply, which is why there is no reload command.",
                    "Be aware that some enchantments don't change with higher levels, don't scale up like you would expect with higher levels, and a few don't change at all."
            ));
            blacklistEnabled();
            this.config.set("blacklist.enchantments", List.<String>of());
            saveConfig();
        }
    }

    public boolean blacklistEnabled() {
        return this.config.getBoolean("blacklist.enabled", false);
    }

    @SuppressWarnings("PatternValidation")
    public Set<Key> blacklistedEnchantments() {
        final Set<Key> blacklist = new HashSet<>();
        if (!this.blacklistEnabled()) return blacklist;

        final List<String> unparsed = this.config.getStringList("blacklist.enchantments");

        for (String enchant : unparsed) {
            if (!Key.parseable(enchant)) {
                LOGGER.error("Unable to parse '{}' as a registry key, skipping it", enchant);
                continue;
            }

            final Key key = Key.key(enchant);
            blacklist.add(key);
        }

        return blacklist;
    }

    public int getOrSet(@NotNull Key enchantment, int def) {
        final String path = "levels." + enchantment.namespace() + "." + enchantment.value();

        Object levelObj = config.get(path);
        if (levelObj == null) {
            config.set(path, def);
            saveConfig();
        } else if (levelObj instanceof Integer level && level >= 0) {
            return level;
        } else {
            LOGGER.error("Invalid value at `{}` in config, using default level of `{}` for `{}`.", path, def, enchantment.asString());
        }
        return def;
    }

    private void saveConfig() {
        try {
            config.save(configPath.toFile());
        } catch (IOException e) {
            LOGGER.error("Could not write to config file", e);
        }
    }
}
