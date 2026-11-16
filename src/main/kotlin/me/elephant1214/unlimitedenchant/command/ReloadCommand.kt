package me.elephant1214.unlimitedenchant.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import me.elephant1214.unlimitedenchant.PLUGIN_PREFIX
import me.elephant1214.unlimitedenchant.config.ConfigHolder

@Suppress("UnstableApiUsage")
fun LiteralArgumentBuilder<Source>.reload(configHolder: ConfigHolder) = literal("reload") {
    exec { ctx ->
        configHolder.reload()
        ctx.source.sender.sendRichMessage(
            "<prefix> <#CCCCCC>Reloaded! A restart is still required if you changed any enchantment levels.",
            PLUGIN_PREFIX,
        )
    }
}
