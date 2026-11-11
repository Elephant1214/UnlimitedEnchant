---
title: Commands & Permissions
---

# Commands & Permissions

[← Home](index.md)

## Commands

The root command is `/unlimitedenchant` (aliases: `/ue`). All commands require `unlimitedenchant.admin`.

### `/ue set`

```
/ue set <enchantment> <level>
```

Sets the maximum level of an enchantment and saves it to [`enchantment-levels`](config.md#enchantment-levels).

| Argument        | Description                                            |
|-----------------|--------------------------------------------------------|
| `<enchantment>` | Any registered enchantment, e.g. `minecraft:sharpness` |
| `<level>`       | `0` – `255`. `0` disables the enchantment.             |

```
/ue set minecraft:sharpness 10
/ue set minecraft:mending 0
```

> The change is saved immediately but only takes effect after a **server restart**.

## Permissions

| Permission                                 | Description                                                                                     |
|--------------------------------------------|-------------------------------------------------------------------------------------------------|
| `unlimitedenchant.admin`                   | Use all `/ue` commands.                                                                         |
| `unlimitedenchant.anvil.free`              | All anvil operations cost 0 levels. The anvil title shows **(Free)**.                           |
| `unlimitedenchant.anvil.ignore-cost-limit` | No "Too Expensive!" limit at all, ignoring [`anvil.maximum-cost`](config.md#anvilmaximum-cost). |

Anvil permissions have no effect on players in Creative mode.
