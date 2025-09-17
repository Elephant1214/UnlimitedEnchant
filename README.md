[![Discord](https://discordapp.com/api/guilds/959153592869224579/widget.png?style=banner2)](https://discord.gg/qpc69BUeDe)\
\
![Open Issues](https://img.shields.io/github/issues/Elephant1214/UnlimitedEnchant?style=for-the-badge)
![Stars](https://img.shields.io/github/stars/Elephant1214/UnlimitedEnchant?style=for-the-badge)

# UnlimitedEnchant

Allows you to modify the maximum level for all enchantments registered on your server, whether from vanilla, plugins, or
data packs, without making a data pack.
This allows balancing enchantments as you see fit for your server by completely disabling them or increasing and
decreasing the maximum level of any enchantment.\
\
The name is a play on the "UNLIMITED POWER" meme.\
\
Some notes:

- The server must be restarted to apply config changes. This will also add any new enchantments from plugins and data
  packs to the config.
- Just because you *can* change the maximum level for another plugin's enchantments does not mean you *should*. A plugin
  may not support levels above what exists by default.

## ⚠ WARNING ⚠

<ins>**DO NOT ATTEMPT TO USE `/reload` OR PLUGMAN TO APPLY CHANGES.**</ins>\
I am not responsible for any problems you may cause by ignoring this warning, and any issues related to this will be
closed.

## Usage

1. Add Unlimited Enchant and restart your server.
2. The config should now be under the folder for the plugin, disable any enchantments and make any level changes you
   want.
3. Optionally enable the enchantment blacklist, this will automatically remove the blacklisted enchantment from any
   items.
4. Restart your server.
   When it starts again, the enchantment levels will be what you set in the config,
   and any disabled enchantments will no longer be obtainable.\
   Note: Any enchantments in village trades, previously opened but unlooted chests, etc., will still remain.
   Blacklist the disabled enchantments if you want them to be removed during gameplay.
5. Have fun with your modified enchantment levels!

