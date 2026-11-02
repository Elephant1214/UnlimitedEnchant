package me.elephant1214.unlimitedenchant.bootstrap

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.event.RegistryEvents
import me.elephant1214.unlimitedenchant.LOGGER
import me.elephant1214.unlimitedenchant.UnlimitedEnchant
import me.elephant1214.unlimitedenchant.config.ConfigLoader
import me.elephant1214.unlimitedenchant.config.UeConfig
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage", "unused")
class UeBootstrap : PluginBootstrap {
    private lateinit var loader: ConfigLoader
    private lateinit var config: UeConfig

    override fun bootstrap(ctx: BootstrapContext) {
        loader = ConfigLoader(ctx.dataDirectory)
        config = loader.load()

        registerEnchantmentHandlers(ctx)
    }

    override fun createPlugin(ctx: PluginProviderContext): JavaPlugin {
        loader.save(config)

        return UnlimitedEnchant(loader, config)
    }

    private fun registerEnchantmentHandlers(ctx: BootstrapContext) {
        val toDisable = mutableSetOf<TypedKey<Enchantment>>()

        ctx.lifecycleManager.registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd().newHandler { event ->
            val level = computeMaximumLevel(event.key().key(), event.builder().maxLevel())

            if (level > 0) {
                event.builder().maxLevel(level)
            } else {
                toDisable.add(event.key())
            }
        })

        ctx.lifecycleManager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT)) { event ->
            if (toDisable.isEmpty()) return@registerEventHandler // Skip entirely if there's nothing to disable

            val registrar = event.registrar()

            registrar.allTags.forEach { (key, enchantments) ->
                val filtered = enchantments.filterNot { it in toDisable }
                if (filtered.size != enchantments.size) {
                    registrar.setTag(key, filtered)
                }
            }
        }
    }

    /**
     * Computes the maximum level that the plugin should set for an enchantment.
     * If zero, the enchantment will be disabled entirely.
     *
     * @param key The namespaced key for the enchantment
     * @param default The vanilla maximum level for the enchantment
     */
    private fun computeMaximumLevel(key: Key, default: Int): Int {
        val keyNamespace = key.namespace()
        val keyValue = key.value()

        val namespace = config.enchantmentLevels.getOrPut(keyNamespace) { mutableMapOf() }
        val level = namespace.getOrPut(keyValue) { default }

        return when {
            level == 0 -> 0
            level < 0 -> default.also { LOGGER.warn("Invalid max level $level for $key, using $default") }
            else -> level.coerceAtMost(255)
        }
    }
}