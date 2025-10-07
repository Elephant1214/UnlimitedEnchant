package me.elephant1214.unlimitedenchant;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UEConstants {
    public static final Logger LOGGER = LoggerFactory.getLogger("UnlimitedEnchant");

    private UEConstants() {
    }

    public static final class Config {
        public static final String CUSTOM_ANVIL_ENABLED = "customMenus.anvil.enabled";
        public static final String CUSTOM_ANVIL_MAX_LVL = "customMenus.anvil.maxLevel";
        public static final String CUSTOM_ANVIL_SEPARATOR = "customMenus.anvil.separator";
        public static final String BLACKLIST_ENABLED = "blacklist.enabled";
        public static final String BLACKLIST_ENCHANTS = "blacklist.enchantments";
        public static final String ENCHANTMENT_LEVELS = "levels";
    }

    public static final class Permissions {
        public static final String ADMIN = "unlimitedenchant.admin";
        public static final String BYPASS_ANVIL_LIMIT = "unlimitedenchant.anvil.bypass";
    }

    public static final class Misc {
        public static final TagResolver PLUGIN_PREFIX = Placeholder.component(
                "prefix",
                MiniMessage.miniMessage().deserialize(
                        "<gradient:#2E5D87:#6C3483>[Unlimited Enchant]</gradient><#CCCCCC>"
                )
        );
        public static final String IMMOVABLE_VALUE = "immovable";
    }
}
