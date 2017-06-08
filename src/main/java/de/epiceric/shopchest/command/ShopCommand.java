package de.epiceric.shopchest.command;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ShopCommand {

    private static boolean commandCreated = false;

    private ShopChest plugin;
    private String name;
    private PluginCommand pluginCommand;

    private List<ShopSubCommand> subCommands = new ArrayList<>();

    public ShopCommand(final ShopChest plugin) {
        if (commandCreated) {
            IllegalStateException e = new IllegalStateException("Command has already been registered");
            plugin.debug(e);
            throw e;
        }

        this.plugin = plugin;
        this.name = plugin.getShopChestConfig().main_command_name;
        this.pluginCommand = createPluginCommand();

        ShopCommandExecutor executor = new ShopCommandExecutor(plugin);
        ShopTabCompleter tabCompleter = new ShopTabCompleter(plugin);

        final LocalizedMessage.ReplacedPlaceholder cmdPlaceholder = new LocalizedMessage.ReplacedPlaceholder(
                Placeholder.COMMAND, plugin.getShopChestConfig().main_command_name);

        addSubCommand(new ShopSubCommand("create", true, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
                boolean receiveCreateMessage = sender.hasPermission(Permissions.CREATE);
                if (!receiveCreateMessage) {
                    for (PermissionAttachmentInfo permInfo : sender.getEffectivePermissions()) {
                        String perm = permInfo.getPermission();
                        if (perm.startsWith(Permissions.CREATE) && sender.hasPermission(perm)) {
                            receiveCreateMessage = true;
                            break;
                        }
                    }
                }

                if (sender.hasPermission(Permissions.CREATE_ADMIN)) {
                    return LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_CREATE_ADMIN, cmdPlaceholder);
                } else if (receiveCreateMessage) {
                    return LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_CREATE, cmdPlaceholder);
                }

                return "";
            }
        });

        addSubCommand(new ShopSubCommand("remove", true, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
                return LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_REMOVE, cmdPlaceholder);
            }
        });

        addSubCommand(new ShopSubCommand("info", true, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
                return LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_INFO, cmdPlaceholder);
            }
        });

        addSubCommand(new ShopSubCommand("limits", true, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
                return LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_LIMITS, cmdPlaceholder);
            }
        });

        addSubCommand(new ShopSubCommand("open", true, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
                return LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_OPEN, cmdPlaceholder);
            }
        });

        addSubCommand(new ShopSubCommand("removeall", false, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
                if (sender.hasPermission(Permissions.REMOVE_OTHER)) {
                    return LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_REMOVEALL, cmdPlaceholder);
                } else {
                    return "";
                }
            }
        });

        addSubCommand(new ShopSubCommand("reload", false, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
                if (sender.hasPermission(Permissions.RELOAD)) {
                    return LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_RELOAD, cmdPlaceholder);
                } else {
                    return "";
                }
            }
        });

        addSubCommand(new ShopSubCommand("update", false, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
                if (sender.hasPermission(Permissions.UPDATE)) {
                    return LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_UPDATE, cmdPlaceholder);
                } else {
                    return "";
                }
            }
        });

        addSubCommand(new ShopSubCommand("config", false, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
                if (sender.hasPermission(Permissions.CONFIG)) {
                    return LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_CONFIG, cmdPlaceholder);
                } else {
                    return "";
                }
            }
        });

        register();
        commandCreated = true;
    }

    public PluginCommand getCommand() {
        return pluginCommand;
    }

    private PluginCommand createPluginCommand() {
        plugin.debug("Creating plugin command");
        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);

            PluginCommand cmd = c.newInstance(name, plugin);
            cmd.setDescription("Manage players' shops or this plugin.");
            cmd.setUsage("/" + name);
            cmd.setExecutor(new ShopBaseCommandExecutor());
            cmd.setTabCompleter(new ShopBaseTabCompleter());

            return cmd;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            plugin.getLogger().severe("Failed to create command");
            plugin.debug("Failed to create plugin command");
            plugin.debug(e);
        }

        return null;
    }

    public void addSubCommand(ShopSubCommand subCommand) {
        plugin.debug("Adding sub command \"" + subCommand.getName() + "\"");
        this.subCommands.add(subCommand);
    }

    public List<ShopSubCommand> getSubCommands() {
        return new ArrayList<>(subCommands);
    }

    private void register() {
        if (pluginCommand == null) return;

        plugin.debug("Registering command " + name);

        try {
            Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);

            Object commandMapObject = f.get(Bukkit.getPluginManager());
            if (commandMapObject instanceof CommandMap) {
                CommandMap commandMap = (CommandMap) commandMapObject;
                commandMap.register(plugin.getName(), pluginCommand);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            plugin.getLogger().severe("Failed to register command");
            plugin.debug("Failed to register plugin command");
            plugin.debug(e);
        }
    }

    /**
     * Sends the basic help message
     * @param sender {@link CommandSender} who will receive the message
     */
    private void sendBasicHelpMessage(CommandSender sender) {
        plugin.debug("Sending basic help message to " + sender.getName());

        sender.sendMessage(" ");

        for (ShopSubCommand subCommand : subCommands) {
            String msg = subCommand.getHelpMessage(sender);
            if (msg == null || msg.isEmpty()) {
                continue;
            }

            sender.sendMessage(msg);
        }

        sender.sendMessage(" ");
    }

    private class ShopBaseCommandExecutor implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length > 0) {
                for (ShopSubCommand subCommand : subCommands) {
                    if (subCommand.getName().equalsIgnoreCase(args[0])) {
                        if (!(sender instanceof Player) && subCommand.isPlayerCommand()) {
                            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                            return true;
                        }

                        if (!subCommand.execute(sender, command, label, args)) {
                            sendBasicHelpMessage(sender);
                        }

                        return true;
                    }
                }

                sendBasicHelpMessage(sender);
            } else {
                sendBasicHelpMessage(sender);
            }

            return true;
        }
    }

    private class ShopBaseTabCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            List<String> subCommandNames = new ArrayList<>();

            List<String> tabCompletions = new ArrayList<>();

            for (ShopSubCommand subCommand : subCommands) {
                subCommandNames.add(subCommand.getName());
            }

            if (args.length == 1) {
                if (!args[0].isEmpty()) {
                    for (String s : subCommandNames) {
                        if (s.startsWith(args[0])) {
                            tabCompletions.add(s);
                        }
                    }
                    return tabCompletions;
                } else {
                    return subCommandNames;
                }
            } else if (args.length > 1) {
                for (ShopSubCommand subCmd : subCommands) {
                    if (subCmd.getName().equalsIgnoreCase(args[0])) {
                        return subCmd.getTabCompletions(sender, command, label, args);
                    }
                }
            }

            return new ArrayList<>();
        }

    }

}
