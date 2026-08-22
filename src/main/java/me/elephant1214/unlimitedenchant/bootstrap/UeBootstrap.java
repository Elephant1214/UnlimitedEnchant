package me.elephant1214.unlimitedenchant.bootstrap;

import com.google.common.collect.Sets;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import me.elephant1214.unlimitedenchant.UeConstants;
import me.elephant1214.unlimitedenchant.config.UeConfig;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public final class UeBootstrap implements PluginBootstrap {
    public static UeConfig config;
    private final Set<TypedKey<@NotNull Enchantment>> toDisable = Sets.newHashSet();

    @Override
    public void bootstrap(@NotNull BootstrapContext ctx) {
        if (!initConfig(ctx)) return;

        registerEnchantHandlers(ctx);
    }

    private boolean initConfig(@NotNull BootstrapContext ctx) {
        try {
            config = new UeConfig(ctx.getDataDirectory());
            return true;
        } catch (IOException e) {
            UeConstants.LOGGER.error("Could not create config", e);
            return false;
        }
    }

    private void registerEnchantHandlers(@NotNull BootstrapContext ctx) {
        ctx.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd().newHandler(event -> {
            UeConstants.LOGGER.info("Enchantment entry add called");
            // key().key() for 1.21.4 compatability
            int maxLevel = config.getOrComputeMaxLevel(event.key().key(), event.builder().maxLevel());

            if (maxLevel > 0) {
                event.builder().maxLevel(maxLevel);
            } else {
                this.toDisable.add(event.key());
            }
        }));

        ctx.getLifecycleManager()
                .registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT), event -> {
                    UeConstants.LOGGER.info("postFlatten called");
                    final PostFlattenTagRegistrar<@NotNull Enchantment> registrar = event.registrar();

                    for (var entry : registrar.getAllTags().entrySet()) {
                        Set<TypedKey<@NotNull Enchantment>> values = new HashSet<>(entry.getValue());
                        if (values.removeAll(this.toDisable)) {
                            registrar.setTag(entry.getKey(), values);
                        }
                    }

                    this.toDisable.clear();
                    UeConstants.LOGGER.info("toDisable reset");
                });
    }
}
