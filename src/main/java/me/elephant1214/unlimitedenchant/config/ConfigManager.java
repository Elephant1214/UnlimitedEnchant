package me.elephant1214.unlimitedenchant.config;

import me.elephant1214.unlimitedenchant.UEConstants;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private final Path configPath;
    private YamlConfiguration config;

    public ConfigManager(@NotNull Path folder, @NotNull String name) throws IOException {
        if (Files.notExists(folder)) Files.createDirectory(folder);
        this.configPath = folder.resolve(name);
        if (Files.notExists(this.configPath)) Files.createFile(this.configPath);
        this.config = YamlConfiguration.loadConfiguration(this.configPath.toFile());
    }

    YamlConfiguration get() {
        return this.config;
    }

    public void save() {
        try {
            this.config.save(this.configPath.toFile());
        } catch (IOException e) {
            UEConstants.LOGGER.error("Failed to save config", e);
        }
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.configPath.toFile());
    }
}
