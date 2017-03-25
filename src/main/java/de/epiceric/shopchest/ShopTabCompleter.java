package de.epiceric.shopchest;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

class ShopTabCompleter implements TabCompleter {

    private ShopChest plugin;

    ShopTabCompleter(ShopChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(plugin.getShopChestConfig().main_command_name)) {

            List<String> subCommands = Arrays.asList("config", "create", "info", "limits", "open", "reload", "remove", "update");
            List<String> createSubCommands = Arrays.asList("admin", "normal");
            List<String> configSubCommands = Arrays.asList("add", "remove", "set");

            Set<String> configValues = plugin.getConfig().getKeys(true);

            ArrayList<String> returnCompletions = new ArrayList<>();

            if (args.length == 1) {
                if (!args[0].equals("")) {
                    for (String s : subCommands) {
                        if (s.startsWith(args[0])) {
                            returnCompletions.add(s);
                        }
                    }

                    return returnCompletions;
                } else {
                    return subCommands;
                }
            } else if (args.length == 2) {
                if (args[0].equals("config")) {
                    if (!args[1].equals("")) {
                        for (String s : configSubCommands) {
                            if (s.startsWith(args[1])) {
                                returnCompletions.add(s);
                            }
                        }

                        return returnCompletions;
                    } else {
                        return configSubCommands;
                    }
                }
            } else if (args.length == 3) {
                if (args[0].equals("config")) {
                    if (!args[2].equals("")) {
                        for (String s : configValues) {
                            if (s.startsWith(args[2])) {
                                returnCompletions.add(s);
                            }
                        }

                        return returnCompletions;
                    } else {
                        return new ArrayList<>(configValues);
                    }
                }
            } else if (args.length == 5) {
                if (args[0].equals("create")) {
                    if (!args[4].equals("")) {
                        for (String s : createSubCommands) {
                            if (s.startsWith(args[4])) {
                                returnCompletions.add(s);
                            }
                        }

                        return returnCompletions;
                    } else {
                        return createSubCommands;
                    }
                }
            }
        }

        return new ArrayList<>();
    }
}
