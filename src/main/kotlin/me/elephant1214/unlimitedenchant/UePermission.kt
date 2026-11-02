package me.elephant1214.unlimitedenchant

import org.bukkit.permissions.Permissible

enum class UePermission(path: String) {
    ADMIN("admin"),
    ANVIL_FREE("anvil.free"),
    ANVIL_IGNORE_COST_LIMIT("anvil.ignore-cost-limit");

    val node: String = "unlimitedenchant.$path"
}

fun Permissible.hasPermission(perm: UePermission): Boolean =
    this.hasPermission(perm.node)
