package me.elephant1214.unlimitedenchant.listeners

import me.elephant1214.unlimitedenchant.UePermission
import me.elephant1214.unlimitedenchant.config.UeConfig
import me.elephant1214.unlimitedenchant.hasPermission
import me.elephant1214.unlimitedenchant.isNullOrEmpty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket
import net.minecraft.world.entity.player.Abilities
import org.bukkit.GameMode
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.view.AnvilView

private const val VANILLA_TOO_EXPENSIVE: Int = 40

class AnvilListener(private val config: () -> UeConfig) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onClose(event: InventoryCloseEvent) {
        if (event.inventory !is AnvilInventory) return
        val player = event.view.takePlayer() ?: return

        player.restoreAbilities()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.inventory !is AnvilInventory) return
        val view = event.view as? AnvilView ?: return
        val player = view.takePlayer() ?: return

        if (player.hasPermission(UePermission.ANVIL_FREE)) {
            event.titleOverride(Component.translatable { builder ->
                builder.key("container.repair")
                    .append(Component.text(" (Free)", NamedTextColor.GREEN))
            })
        }

        if (player.hasPermission(UePermission.ANVIL_IGNORE_COST_LIMIT)) {
            view.maximumRepairCost = Int.MAX_VALUE
            return
        }

        val config = config()
        if (config.anvil.allowExpensiveRepairs) {
            view.maximumRepairCost = config.anvil.maximumCost + 1
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        val view = event.view
        val player = view.takePlayer() ?: return
        if (event.result.isNullOrEmpty) return

        if (player.hasPermission(UePermission.ANVIL_FREE)) {
            view.repairCost = 0
            return
        }

        val config = config()
        if (config.anvil.rename.fixedCost && view.topInventory.secondItem.isNullOrEmpty) {
            view.repairCost = config.anvil.rename.cost
        }

        if (view.maximumRepairCost >= VANILLA_TOO_EXPENSIVE && view.repairCost >= VANILLA_TOO_EXPENSIVE) {
            player.sendFakeInstabuild()
        }
    }
}

private fun InventoryView.takePlayer(): Player? =
    (player as? Player)?.takeIf { it.gameMode != GameMode.CREATIVE }

@Suppress("UsePropertyAccessSyntax")
private fun Player.sendFakeInstabuild() {
    val handle = (this as CraftPlayer).handle
    val fake = Abilities().apply {
        val real = handle.abilities

        invulnerable = real.invulnerable
        flying = real.flying
        mayfly = real.mayfly
        instabuild = true
        mayBuild = real.mayBuild
        setFlyingSpeed(real.getFlyingSpeed())
        setWalkingSpeed(real.getWalkingSpeed())
    }

    handle.connection.send(ClientboundPlayerAbilitiesPacket(fake))
}

private fun Player.restoreAbilities() {
    (this as CraftPlayer).handle.onUpdateAbilities()
}
