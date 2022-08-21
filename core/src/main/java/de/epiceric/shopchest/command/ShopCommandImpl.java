package de.epiceric.shopchest.command;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.command.ShopCommand;
import de.epiceric.shopchest.api.config.Config;
import de.epiceric.shopchest.util.Logger;

public class ShopCommandImpl extends ShopCommand {
    private ShopChest plugin;
    private PluginCommand pluginCommand;

    public ShopCommandImpl(ShopChest plugin) {
        this.plugin = plugin;

        addSubCommand(new CreateSubCommand(plugin));
        addSubCommand(new EditSubCommand(plugin));
        addSubCommand(new RemoveSubCommand(plugin));
        addSubCommand(new InfoSubCommand(plugin));
    }

    @Override
    public String getName() {
        return Config.CORE_MAIN_COMMAND_NAME.get().toLowerCase(Locale.ENGLISH).trim();
    }

    /**
     * Gets the {@link PluginCommand plugin command} for registering the command
     * 
     * @return the plugin command
     * @since 2.0
     */
    public PluginCommand getPluginCommand() {
        if (pluginCommand == null) {
            try {
                Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                c.setAccessible(true);

                pluginCommand = c.newInstance(getName(), plugin);
                ShopCommandExecutor executor = new ShopCommandExecutor(this); 
                pluginCommand.setDescription("Create and manage your player shops");
                pluginCommand.setExecutor(executor);
                pluginCommand.setTabCompleter(executor);
            } catch (ReflectiveOperationException e) {
                Logger.severe("Failed to register shop command");
                Logger.severe(e);
            }
        }
        return pluginCommand;
    }

    private static final class ShopCommandExecutor implements CommandExecutor, TabCompleter {
        private final ShopCommand shopCommand;

        private ShopCommandExecutor(ShopCommand shopCommand) {
            this.shopCommand = shopCommand;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            return shopCommand.onTabComplete(sender, args);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            shopCommand.onExecute(sender, args);
            return true;
        }
    }
}