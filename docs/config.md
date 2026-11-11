---
title: Configuration
---

# Configuration

[← Home](index.md)

The config is at `plugins/UnlimitedEnchant/config.yml`. It is created the first time the server starts. **Restart the
server after modifying it.**

## Overview

```yaml
config-version: 2
anvil:
  allow-expensive-repairs: false
  maximum-cost: 39
  rename:
    fixed-cost: false
    cost: 1
enchantment-levels:
  minecraft:
    sharpness: 5
    efficiency: 5
    # ...every registered enchantment
```

| Option                                                           | Type    | Default        |
|------------------------------------------------------------------|---------|----------------|
| [`config-version`](#config-version)                              | number  | `2`            |
| [`anvil.allow-expensive-repairs`](#anvilallow-expensive-repairs) | boolean | `false`        |
| [`anvil.maximum-cost`](#anvilmaximum-cost)                       | number  | `39`           |
| [`anvil.rename.fixed-cost`](#anvilrenamefixed-cost)              | boolean | `false`        |
| [`anvil.rename.cost`](#anvilrenamecost)                          | number  | `1`            |
| [`enchantment-levels`](#enchantment-levels)                      | map     | vanilla levels |

---

## `config-version`

Used to update older config files automatically. **Do not change this.**

---

## Anvil

### `anvil.allow-expensive-repairs`

Normally, the anvil refuses any operation costing 40 levels or more and shows **"Too Expensive!"**. When this is
`true`, operations are allowed up to [`maximum-cost`](#anvilmaximum-cost) instead.

Useful when you've increased enchantment levels since combining high-level items can get expensive.

### `anvil.maximum-cost`

The highest cost (in levels) a repair can have while `allow-expensive-repairs` is enabled.
The vanilla limit is `39`. This option has no effect if `allow-expensive-repairs` is `false`.

### `anvil.rename.fixed-cost`

When `true`, renaming an item (with nothing in the second slot) always costs [`rename.cost`](#anvilrenamecost)
no matter how many times the item has already been modified in an anvil.

### `anvil.rename.cost`

The cost in levels for renaming while `fixed-cost` is enabled.

> Players with the `unlimitedenchant.anvil.free` or `unlimitedenchant.anvil.ignore-cost-limit` permission bypass these
> settings. See [Commands & Permissions](commands.md). Creative-mode players are never affected.

---

## `enchantment-levels`

The maximum level of each enchantment. Always grouped by **namespace** and then **enchantment name**:

```yaml
enchantment-levels:
  minecraft: # vanilla enchantments
    sharpness: 10
    mending: 0        # disabled
  someplugin: # enchantments added by a plugin or data pack
    lifesteal: 3
```

Every enchantment on the server is added automatically on startup with its normal maximum level, including enchantments
from plugins and data packs.

| Value       | Effect                                                                                       |
|-------------|----------------------------------------------------------------------------------------------|
| `1` – `255` | Sets the maximum level. Vanilla enforces an absolute maximum level of 255 in newer versions. |
| `0`         | **Disables** the enchantment. It can no longer be obtained from new source.                  |
| Negative    | Invalid. The normal maximum level is used and a warning is logged.                           |

> **Disabling doesn't remove existing copies.** Enchantments already on items, in villager trades, or in unopened loot
> chests will not change.

For the full list of vanilla enchantment names, see the
[Minecraft Wiki](https://minecraft.wiki/w/Java_Edition_data_values#Enchantments).

You can also change levels in-game with [`/ue set`](commands.md#ue-set). A restart is still required.
