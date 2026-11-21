package me.elephant1214.unlimitedenchant.config.serializers

import me.elephant1214.unlimitedenchant.LOGGER
import me.elephant1214.unlimitedenchant.config.LevelCost
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

object LevelCostSerializer : TypeSerializer<LevelCost> {
    const val MIN = 0
    const val MAX = 1024

    override fun deserialize(type: Type, node: ConfigurationNode): LevelCost = LevelCost(
        base = node.node("base").readClamped(),
        perLevel = node.node("per-level").readClamped(),
    )

    override fun serialize(type: Type, obj: LevelCost?, node: ConfigurationNode) {
        if (obj == null) {
            node.raw(null)
            return
        }
        node.node("base").set(obj.base)
        node.node("per-level").set(obj.perLevel)
    }

    private fun ConfigurationNode.readClamped(): Int {
        val value = getInt(MIN)
        val clamped = value.coerceIn(MIN, MAX)
        if (clamped != value) LOGGER.warn("${path()} = $value is out of range, using $clamped")
        return clamped
    }
}
