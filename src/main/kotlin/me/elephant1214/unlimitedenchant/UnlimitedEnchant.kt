package me.elephant1214.unlimitedenchant

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import me.elephant1214.unlimitedenchant.config.ConfigLoader
import me.elephant1214.unlimitedenchant.config.UeConfig
import me.elephant1214.unlimitedenchant.listeners.AnvilListener
import org.bukkit.plugin.java.JavaPlugin

class UnlimitedEnchant(
    private val configLoader: ConfigLoader,
    var ueConfig: UeConfig
) : JavaPlugin() {
    //private val blacklistListeners = BlacklistListeners(this)

    override fun onEnable() {
        registerCommands()
        registerEvents()
    }

    fun registerEvents() {
        server.pluginManager.registerEvents(AnvilListener { ueConfig }, this)
    }

    fun registerCommands() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            val root = Commands.literal("unlimitedenchant")
                .requires { it.sender.hasPermission(UePermission.ADMIN) }

            //LevelSetCommand.addLevelSet(root);
            //BlacklistCommand.addBlacklistTree(black)
        }
    }

    override fun reloadConfig() {
        ueConfig = configLoader.load()
    }

    override fun saveConfig() {
        configLoader.save(ueConfig)
    }
}
