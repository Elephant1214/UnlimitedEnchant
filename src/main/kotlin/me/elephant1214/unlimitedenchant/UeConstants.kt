package me.elephant1214.unlimitedenchant

import io.papermc.paper.ServerBuildInfo
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.slf4j.LoggerFactory

val LOGGER = LoggerFactory.getLogger("UnlimitedEnchant")

val MINI_MESSAGE = MiniMessage.miniMessage()

val MESSAGE_SERIALIZER = MessageComponentSerializer.message()

val PLUGIN_PREFIX = Placeholder.component(
    "prefix",
    MINI_MESSAGE.deserialize(
        "<gradient:#2E5D87:#6C3483>[Unlimited Enchant]</gradient>"
    )
)

/**
 * On versions < 26.x, [org.bukkit.entity.AbstractVillager.resetOffers] does not look at the villager's level when
 * repopulating their trades and must be done manually.
 */
val USE_VILLAGE_TRADE_COUNT_FIX: Boolean = ServerBuildInfo.buildInfo()
    .minecraftVersionId()
    .substringBefore('.')
    .toIntOrNull()
    ?.let { it < 26 }
    ?: true.also {
        LOGGER.warn(
            "Unable to parse Minecraft version, applying villager trade fix. " +
                "Please report this on GitHub if you are using a stable release of the game."
        )
    }
