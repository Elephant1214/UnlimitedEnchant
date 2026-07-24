package me.elephant1214.unlimitedenchant;

import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.AbstractVillager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UeConstants {
    public static final Logger LOGGER = LoggerFactory.getLogger("UnlimitedEnchant");
    /**
     * On versions < 26.x, {@link AbstractVillager#resetOffers()} does not look at the villager's level when
     * repopulating their trades and must be done manually.
     */
    public static final boolean USE_VILLAGE_TRADE_COUNT_FIX = shouldUseVillageTradeFix();

    /**
     * Pretty easy to figure if the plugin is running on 26.x or something earlier because the dot got moved.
     */
    private static boolean shouldUseVillageTradeFix() {
        final String version = ServerBuildInfo.buildInfo().minecraftVersionId().substring(0, 2);
        UeConstants.LOGGER.info(version);

        try {
            final int major = Integer.parseInt(version);
            // Technically not possible anyway since prior versions use a dot at index 1
            return major < 26;
        } catch (NumberFormatException e) {
            UeConstants.LOGGER.info("Unable to parse version number"); // do not forget to remove this lmao
            return true;
        }
    }

    public static final class Config {
        public static final String CONFIG_VERSION = "version";

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

    private UeConstants() {
    }
}
