package me.elephant1214.unlimitedenchant;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.elephant1214.unlimitedenchant.blacklist.BlacklistManager;
import me.elephant1214.unlimitedenchant.commands.BlacklistCommand;
import me.elephant1214.unlimitedenchant.commands.LevelSetCommand;
import me.elephant1214.unlimitedenchant.gui.anvil.AnvilListenersOld;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class UnlimitedEnchantOld extends JavaPlugin {
    private BlacklistManager blacklist;

    private static UnlimitedEnchantOld INSTANCE;

    public static UnlimitedEnchantOld getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        UeBootstrapOld.config.save();

        this.blacklist = new BlacklistManager(this);
        this.registerEvents();
        this.registerCommands();
    }

    @Override
    public void onDisable() {
        UeBootstrapOld.config.reload();
    }

    private void registerEvents() {
        // this.getServer().getPluginManager().registerEvents(new EnchantmentMenu(), this);
        if (UeBootstrapOld.config.isAnvilEnabled()) {
            this.getServer().getPluginManager().registerEvents(new AnvilListenersOld(this), this);
        }
    }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ue")
                    .requires(sender -> sender.getSender().hasPermission(UeConstants.Permissions.ADMIN));

            LevelSetCommand.addLevelSet(root);
            BlacklistCommand.addBlacklistTree(this.blacklist, root);

            commands.registrar().register(root.build(), null, List.of("unlimitedenchant"));
        });
    }
}
