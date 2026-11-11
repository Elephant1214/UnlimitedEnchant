---
title: Home
---

# Unlimited Enchant

Change and disable the maximum level of any registered enchantment on your server, whether from vanilla, a plugin,
or a data pack.

## Pages

| Page                                  | What's there                                |
|---------------------------------------|---------------------------------------------|
| [Configuration](config.md)            | Every option in `config.yml`, with defaults |
| [Commands & Permissions](commands.md) | `/ue` commands and permission nodes         |

## Quick start

1. Drop the plugin into your `plugins/` folder and start the server.
2. Open `plugins/UnlimitedEnchant/config.yml`. Every enchantment on the server is listed under `enchantment-levels`
   with its current maximum level.
3. Change the levels you want. Setting an enchantment's level to `0` disables it.
4. **Restart** the server.

> **⚠ Do not use `/reload` or PlugMan.** Enchantment levels are applied while the server starts up. This is due to how
> the game works and a full restart is the only way to apply changes.

## Good to know

- Changes only apply after a restart. A restart also adds new enchantments from plugins and data packs to the config.
- Just because you *can* raise another plugin's enchantment doesn't mean you *should*. A plugin might not handle levels
  above the ones it was designed for.
- Some enchantments barely scale with level, and Channeling, Curse of Binding, Curse of Vanishing, Flame, Infinity,
  Mending, and Silk Touch don't change at all.

[GitHub](https://github.com/Elephant1214/UnlimitedEnchant) ·
[Issues](https://github.com/Elephant1214/UnlimitedEnchant/issues) ·
[Discord](https://discord.gg/qpc69BUeDe)
