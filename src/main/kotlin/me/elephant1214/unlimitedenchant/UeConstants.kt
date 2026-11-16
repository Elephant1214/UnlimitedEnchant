package me.elephant1214.unlimitedenchant

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val LOGGER: Logger = LoggerFactory.getLogger("UnlimitedEnchant")

val MINI_MESSAGE = MiniMessage.miniMessage()

val PLUGIN_PREFIX = Placeholder.component(
    "prefix",
    MINI_MESSAGE.deserialize(
        "<gradient:#2E5D87:#6C3483>[Unlimited Enchant]</gradient>"
    )
)
