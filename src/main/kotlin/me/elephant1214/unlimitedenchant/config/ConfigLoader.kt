package me.elephant1214.unlimitedenchant.config

import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.NodePath
import org.spongepowered.configurate.transformation.ConfigurationTransformation
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.Path

class ConfigLoader(dataDir: Path) {
    private val loader =
        YamlConfigurationLoader.builder().path(dataDir.resolve("config.yml")).nodeStyle(NodeStyle.BLOCK)
            .defaultOptions { opts ->
                opts.header(HEADER)
            }.build()
    private lateinit var node: CommentedConfigurationNode

    fun load(): UeConfig {
        node = loader.load()
        if (!node.empty()) MIGRATIONS.apply(node)
        return node.get(UeConfig::class.java) ?: UeConfig()
    }

    fun save(config: UeConfig) {
        node.set(UeConfig::class.java, config)
        loader.save(node)
    }

    companion object {
        private val MIGRATIONS = ConfigurationTransformation.versionedBuilder()
            .versionKey("version")
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
                        levels.node("levels").from(levels.node("enchantment-levels"))
                        levels.set(null)
                        null
                    }
                    .build()
            )
            .build()
        val LATEST_VERSION = MIGRATIONS.latestVersion()
        private val HEADER = """
            This configuration allows you to modify the maximum levels of all registered enchantments.
            Note that the effects of some enchantments scale very little with higher levels, and a few have no changes.
            For a complete list of vanilla enchantments and their resource locations, visit:
            https://minecraft.wiki/w/Java_Edition_data_values#Enchantments
            The effects of Channeling, Curse of Binding, Curse of Vanishing, Flame, Infinity, Mending, and Silk Touch do not change with increased levels.
        """.trimIndent()
    }
}