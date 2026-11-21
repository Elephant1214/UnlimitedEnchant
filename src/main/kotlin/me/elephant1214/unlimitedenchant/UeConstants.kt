package me.elephant1214.unlimitedenchant

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
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

val GITHUB_URL = Placeholder.component(
    "github",
    Component.text("GitHub", NamedTextColor.AQUA)
        .decorate(TextDecoration.UNDERLINED)
        .clickEvent(ClickEvent.openUrl("https://github.com/Elephant1214/UnlimitedEnchant"))
        .hoverEvent(HoverEvent.showText { Component.text("Click to open the GitHub page") })
)

val DISCORD_INVITE = Placeholder.component(
    "discord",
    Component.text("Discord server", NamedTextColor.AQUA)
        .decorate(TextDecoration.UNDERLINED)
        .clickEvent(ClickEvent.openUrl("https://discord.gg/BKfEdgjMf9"))
        .hoverEvent(HoverEvent.showText { Component.text("Click to join the Discord server") })
)
