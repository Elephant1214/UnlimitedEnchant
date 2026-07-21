package me.elephant1214.unlimitedenchant.config;

import me.elephant1214.unlimitedenchant.UeConstants;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A basic wrapper for a {@link YamlConfiguration}.
 */
public final class ConfigurationManager {
    private final Path configPath;
    private YamlConfiguration configuration;

    public ConfigurationManager(@NotNull Path folder, @NotNull String name) throws IOException {
        if (Files.notExists(folder)) Files.createDirectory(folder);

        this.configPath = folder.resolve(name);
        if (Files.notExists(this.configPath)) Files.createFile(this.configPath);

        this.configuration = YamlConfiguration.loadConfiguration(this.configPath.toFile());
    }

    /**
     * @return The {@link YamlConfiguration} managed by this instance.
     */
    YamlConfiguration get() {
        return this.configuration;
    }

    /**
     * Saves the configuration to the disk.
     */
    public void save() {
        try {
            this.configuration.save(this.configPath.toFile());
        } catch (IOException e) {
            UeConstants.LOGGER.error("Failed to save configuration", e);
        }
    }

    /**
     * Reloads the configuration from the disk. This will overwrite all options in-memory.
     */
    public void reload() {
        this.configuration = YamlConfiguration.loadConfiguration(this.configPath.toFile());
    }
}
