package me.elephant1214.unlimitedenchant.bootstrap

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryEvents
import me.elephant1214.unlimitedenchant.UnlimitedEnchant
import me.elephant1214.unlimitedenchant.adventureKey
import me.elephant1214.unlimitedenchant.config.ConfigLoader
import me.elephant1214.unlimitedenchant.config.LevelCost
import me.elephant1214.unlimitedenchant.config.UeConfig
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.bukkit.plugin.java.JavaPlugin

typealias DisabledEnchantments = Set<Key>

@Suppress("UnstableApiUsage", "unused")
class UeBootstrap : PluginBootstrap {
    private lateinit var loader: ConfigLoader
    private lateinit var config: UeConfig
    private val vanillaValues = mutableMapOf<Key, VanillaValues>()
    private val disabled = mutableSetOf<TypedKey<Enchantment>>()

    override fun bootstrap(ctx: BootstrapContext) {
        loader = ConfigLoader(ctx.dataDirectory)
        config = loader.load()

        registerEnchantmentHandlers(ctx)
    }

    override fun createPlugin(ctx: PluginProviderContext): JavaPlugin {
        loader.save(config)

        return UnlimitedEnchant(
            loader,
            config,
            ImmutableMap.copyOf(vanillaValues),
            ImmutableSet.copyOf(disabled.map { it.key() }),
        )
    }

    private fun registerEnchantmentHandlers(ctx: BootstrapContext) {
        ctx.lifecycleManager.registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd().newHandler { event ->
            vanillaValues[event.adventureKey] = VanillaValues(
                weight = event.builder().weight(),
                maxLevel = event.builder().maxLevel(),
                anvilCost = event.builder().anvilCost(),
                minCost = LevelCost.of(event.builder().minimumCost()),
                maxCost = LevelCost.of(event.builder().maximumCost()),
            )

            val settings = computeSettings(event.adventureKey, event.builder())

            // `maxLevel` is never null after `computeSettings`
            if (settings.maxLevel!! > 0) {
                event.builder().maxLevel(settings.maxLevel!!)
            } else {
                disabled.add(event.key())
            }

            settings.weight?.let { event.builder().weight(it) }
            settings.anvilCost?.let { event.builder().anvilCost(it) }
            settings.minCost?.let {
                event.builder().minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(it.base, it.perLevel))
            }
            settings.maxCost?.let {
                event.builder().maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(it.base, it.perLevel))
            }
        })

        ctx.lifecycleManager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT)) { event ->
            if (disabled.isEmpty()) return@registerEventHandler // Skip entirely if there's nothing to disable

            val registrar = event.registrar()

            registrar.allTags.forEach { (key, enchantments) ->
                val filtered = enchantments.filterNot { it in disabled }
                if (filtered.size != enchantments.size) {
                    registrar.setTag(key, filtered)
                }
            }
        }
    }

    /**
     * Fills the [UeConfig.EnchantmentOptions.maxLevel] if it's currently missing.
     *
     * @param key The namespaced key for the enchantment
     * @param builder The registry entry builder for the enchantment
     */
    private fun computeSettings(key: Key, builder: EnchantmentRegistryEntry.Builder): UeConfig.EnchantmentOptions {
        val configured = config.enchantments.getOrPut(key) { UeConfig.EnchantmentOptions() }
        if (configured.maxLevel == null) configured.maxLevel = builder.maxLevel()
        return configured
    }
}