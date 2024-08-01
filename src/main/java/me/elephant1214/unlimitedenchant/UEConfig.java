package me.elephant1214.unlimitedenchant;

import net.kyori.adventure.key.Key;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Handling for the config because it has to be loaded before the main plugin class is
 * actually initialized.
 */
public final class UEConfig {
    public static final Logger LOGGER = LoggerFactory.getLogger("UnlimitedEnchant");
    private final Path configPath;
    private final YamlConfiguration config;

    public UEConfig(Path path) throws IOException {
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
            saveConfig();
        }
    }

    public int getOrSet(Key enchantment, int def) {
        final String path = enchantment.namespace() + "." + enchantment.value();

        Object levelObj = config.get(path);
        if (levelObj == null) {
            config.set(path, def);
            saveConfig();
        } else if (levelObj instanceof Integer level && level >= 1) {
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
