package me.elephant1214.unlimitedenchant.config

import me.elephant1214.unlimitedenchant.LOGGER
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.NodePath
import org.spongepowered.configurate.transformation.ConfigurationTransformation
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class ConfigLoader(private val dataDir: Path) {
    private val loader =
        YamlConfigurationLoader.builder()
            .path(dataDir.resolve("config.yml"))
            .nodeStyle(NodeStyle.BLOCK)
            .defaultOptions { opts ->
                opts.header(HEADER)
            }.build()
    private lateinit var node: CommentedConfigurationNode

    fun load(): UeConfig {
        node = loader.load()
        if (!node.empty()) {
            // Applies the new `config-version` node name before attempting migration
            val oldVersion = node.node("version")
            if (!oldVersion.virtual()) {
                node.node(VERSION_KEY).from(oldVersion)
                oldVersion.set(null)
            }

            val version = MIGRATIONS.version(node)
            if (version < LATEST_VERSION) {
                backup(version)
            }

            MIGRATIONS.apply(node)
        }
        return node.get(UeConfig::class.java) ?: UeConfig()
    }

    private fun backup(version: Int) {
        val name = if (version < 0) "config-unversioned.yml" else "config-v$version.yml"
        Files.copy(dataDir.resolve("config.yml"), dataDir.resolve(name), StandardCopyOption.REPLACE_EXISTING)
        LOGGER.info("Migrating config from version $version to $LATEST_VERSION, backup saved as $name")
    }

    fun save(config: UeConfig) {
        node.set(UeConfig::class.java, config)
        loader.save(node)
    }

    companion object {
        private val HEADER = """
            Unlimited Enchant configuration
            Documentation for every option at: https://elephant1214.github.io/UnlimitedEnchant/config
        """.trimIndent()

        private const val VERSION_KEY = "config-version"

        private val MIGRATIONS = ConfigurationTransformation.versionedBuilder()
            .versionKey(VERSION_KEY)
            .addVersion(1, ConfigurationTransformation.empty())
            .addVersion(
                2, ConfigurationTransformation.builder()
                    .addAction(NodePath.path("customMenus")) { _, menus ->
                        val old = menus.node("anvil")
                        if (!old.virtual()) {
                            val anvil = menus.parent()!!.node("anvil")
                            anvil.node("allow-expensive-repairs").from(old.node("enabled"))
                            anvil.node("maximum-cost").from(old.node("maxLevel"))
                        }
                        menus.set(null)
                        null
                    }.addAction(NodePath.path("levels")) { _, levels ->
                        levels.parent()!!.node("enchantment-levels").from(levels)
                        levels.set(null)
                        null
                    }.addAction(NodePath.path("blacklist")) { _, blacklist ->
                        blacklist.set(null)
                        null
                    }
                    .build()
            )
            .build()

        val LATEST_VERSION = MIGRATIONS.latestVersion()
    }
}