package me.elephant1214.unlimitedenchant.bootstrap;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.event.RegistryEvents;
import me.elephant1214.unlimitedenchant.UEConfig;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class UEBootstrap implements PluginBootstrap {
    private UEConfig config;

    @Override
    public void bootstrap(@NotNull BootstrapContext ctx) {
        try {
            config = new UEConfig(ctx.getDataDirectory());
        } catch (IOException e) {
            UEConfig.LOGGER.error("Could not create config", e);
            return;
        }

        ctx.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd().newHandler(event -> {
            int maxLevel = config.getOrSet(event.key().key(), event.builder().maxLevel());
            event.builder().maxLevel(maxLevel);
        }));
    }
}
