package me.elephant1214.unlimitedenchant.bootstrap;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import me.elephant1214.unlimitedenchant.UEConstants;
import me.elephant1214.unlimitedenchant.config.UEConfig;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public final class UEBootstrap implements PluginBootstrap {
    public static UEConfig config;
    private Set<TypedKey<@NotNull Enchantment>> toDisable = new HashSet<>();

    @Override
    public void bootstrap(@NotNull BootstrapContext ctx) {
        if (!initConfig(ctx)) return;

        registerEnchantHandlers(ctx);
    }

    private boolean initConfig(@NotNull BootstrapContext ctx) {
        try {
            config = new UEConfig(ctx.getDataDirectory());
            return true;
        } catch (IOException e) {
            UEConstants.LOGGER.error("Could not create config", e);
            return false;
        }
    }

    private void registerEnchantHandlers(@NotNull BootstrapContext ctx) {
        ctx.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd().newHandler(event -> {
            int maxLevel = config.getOrSetMaxLevel(event.key().key(), event.builder().maxLevel());

            if (maxLevel > 0) {
                event.builder().maxLevel(maxLevel);
            } else {
                this.toDisable.add(event.key());
            }
        }));

        ctx.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT), event -> {
            final PostFlattenTagRegistrar<@NotNull Enchantment> registrar = event.registrar();

            for (Map.Entry<TagKey<@NotNull Enchantment>, Collection<TypedKey<@NotNull Enchantment>>> entry : registrar.getAllTags().entrySet()) {
                Set<TypedKey<@NotNull Enchantment>> values = new HashSet<>(entry.getValue());
                if (values.removeAll(this.toDisable)) {
                    registrar.setTag(entry.getKey(), values);
                }
            }

            this.toDisable = null; // Set to null after no longer in use so that the memory gets freed
        });
    }
}
