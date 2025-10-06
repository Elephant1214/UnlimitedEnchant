package me.elephant1214.unlimitedenchant;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.elephant1214.unlimitedenchant.blacklist.BlacklistManager;
import me.elephant1214.unlimitedenchant.bootstrap.UEBootstrap;
import me.elephant1214.unlimitedenchant.commands.BlacklistCommand;
import me.elephant1214.unlimitedenchant.commands.LevelSetCommand;
import me.elephant1214.unlimitedenchant.menus.anvil.AnvilListeners;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class UnlimitedEnchant extends JavaPlugin {
    private final BlacklistManager blacklist = new BlacklistManager(this);

    private static UnlimitedEnchant INSTANCE;

    public static UnlimitedEnchant getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        UEBootstrap.config.save();

        this.registerEvents();
        this.registerCommands();
    }

    private void registerEvents() {
        // this.getServer().getPluginManager().registerEvents(new EnchantmentMenu(), this);
        if (UEBootstrap.config.customAnvilEnabled()) {
            this.getServer().getPluginManager().registerEvents(new AnvilListeners(this), this);
        }
    }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ue")
                    .requires(sender -> sender.getSender().hasPermission(UEConstants.Permissions.ADMIN));

            LevelSetCommand.addLevelSet(root);
            BlacklistCommand.addBlacklistTree(this.blacklist, root);

            commands.registrar().register(root.build(), null, List.of("unlimitedenchant"));
        });
    }
}
