package me.elephant1214.unlimitedenchant

import io.papermc.paper.ServerBuildInfo
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.slf4j.LoggerFactory

val LOGGER = LoggerFactory.getLogger("UnlimitedEnchant")

val MINI_MESSAGE = MiniMessage.miniMessage()

val PLUGIN_PREFIX = Placeholder.component(
    "prefix",
    MINI_MESSAGE.deserialize(
        "<gradient:#2E5D87:#6C3483>[Unlimited Enchant]</gradient>"
    )
)
