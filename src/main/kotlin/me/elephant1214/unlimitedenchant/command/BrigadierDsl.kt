package me.elephant1214.unlimitedenchant.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.elephant1214.unlimitedenchant.MESSAGE_SERIALIZER
import me.elephant1214.unlimitedenchant.MINI_MESSAGE
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

typealias Source = CommandSourceStack
typealias Ctx = CommandContext<Source>

inline fun ArgumentBuilder<Source, *>.literal(
    name: String,
    block: LiteralArgumentBuilder<Source>.() -> Unit,
) {
    then(Commands.literal(name).apply(block))
}

inline fun <T : Any> ArgumentBuilder<Source, *>.argument(
    name: String,
    type: ArgumentType<T>,
    block: RequiredArgumentBuilder<Source, T>.() -> Unit,
) {
    then(Commands.argument(name, type).apply(block))
}

inline fun ArgumentBuilder<Source, *>.exec(crossinline block: (Ctx) -> Unit) {
    executes {
        block(it)
        Command.SINGLE_SUCCESS
    }
}

inline fun <reified T : Any> Ctx.arg(name: String): T = getArgument(name, T::class.java)

fun fail(message: String, vararg resolvers: TagResolver): Nothing =
    throw SimpleCommandExceptionType(
        MESSAGE_SERIALIZER.serialize(MINI_MESSAGE.deserialize(message, *resolvers))
    ).create()
