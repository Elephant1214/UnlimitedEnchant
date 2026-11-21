package me.elephant1214.unlimitedenchant

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import me.elephant1214.unlimitedenchant.bootstrap.DisabledEnchantments
import me.elephant1214.unlimitedenchant.bootstrap.VanillaValuesMap
import me.elephant1214.unlimitedenchant.commands.get
import me.elephant1214.unlimitedenchant.commands.reload
import me.elephant1214.unlimitedenchant.commands.set
import me.elephant1214.unlimitedenchant.config.ConfigHolder
import me.elephant1214.unlimitedenchant.config.ConfigLoader
import me.elephant1214.unlimitedenchant.config.UeConfig
import me.elephant1214.unlimitedenchant.config.validate
import me.elephant1214.unlimitedenchant.listeners.AnvilListener
import me.elephant1214.unlimitedenchant.listeners.BlacklistListener
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage")
class UnlimitedEnchant(
    private val configLoader: ConfigLoader,
    override var config: UeConfig,
    val vanillaValues: VanillaValuesMap,
    val disabled: DisabledEnchantments,
) : JavaPlugin(), ConfigHolder {

    override fun onEnable() {
        config.validate(vanillaValues)
        registerCommands()
        registerEvents()
    }

    fun registerEvents() {
        server.pluginManager.registerEvents(AnvilListener { config }, this)
        server.pluginManager.registerEvents(BlacklistListener { config }, this)
    }

    fun registerCommands() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            val root = Commands.literal("unlimitedenchant").apply {
                requires { it.sender.hasPermission(UePermission.ADMIN) }

                reload(this@UnlimitedEnchant)
                set(this@UnlimitedEnchant)
                get(this@UnlimitedEnchant)
            }

            commands.registrar().register(
                root.build(),
                "Manage Unlimited Enchant",
                listOf("ue"),
            )
        }
    }

    override fun reloadConfig() {
        config = configLoader.load()
        config.validate(vanillaValues)
    }

    override fun saveConfig() {
        configLoader.save(config)
    }

    override fun save() {
        saveConfig()
    }

    override fun reload() {
        reloadConfig()
    }
}
