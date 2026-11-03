package me.elephant1214.unlimitedenchant

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import me.elephant1214.unlimitedenchant.command.blacklistTree
import me.elephant1214.unlimitedenchant.command.levelSet
import me.elephant1214.unlimitedenchant.config.ConfigHolder
import me.elephant1214.unlimitedenchant.config.ConfigLoader
import me.elephant1214.unlimitedenchant.config.UeConfig
import me.elephant1214.unlimitedenchant.listeners.AnvilListener
import org.bukkit.plugin.java.JavaPlugin

class UnlimitedEnchant(
    private val configLoader: ConfigLoader,
    override var config: UeConfig
) : JavaPlugin(), ConfigHolder {
    //private val blacklistListeners = BlacklistListeners(this)

    override fun onEnable() {
        registerCommands()
        registerEvents()
    }

    fun registerEvents() {
        server.pluginManager.registerEvents(AnvilListener { config }, this)
    }

    fun registerCommands() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            val root = Commands.literal("unlimitedenchant").apply {
                requires { it.sender.hasPermission(UePermission.ADMIN) }

                levelSet(this@UnlimitedEnchant)
                blacklistTree(this@UnlimitedEnchant)
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
    }

    override fun saveConfig() {
        configLoader.save(config)
    }

    override fun save() {
        saveConfig()
    }
}
