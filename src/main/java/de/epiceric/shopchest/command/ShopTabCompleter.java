package de.epiceric.shopchest.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;

class ShopTabCompleter implements TabCompleter {
    private ShopChest plugin;

    ShopTabCompleter(ShopChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(Config.mainCommandName)) {

            List<String> createSubCommands = Arrays.asList("admin");
            List<String> configSubCommands = Arrays.asList("add", "remove", "set");
            List<String> areaShopRemoveEvents = Arrays.asList("DELETE", "RESELL", "SELL", "UNRENT");
            List<String> townyShopPlots = Arrays.asList("ARENA", "COMMERCIAL", "EMBASSY", "FARM", "INN", "JAIL", "RESIDENTIAL", "SPLEEF", "WILDS");

            Set<String> configValues = plugin.getConfig().getKeys(true);
            List<String> playerNames = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());

            ArrayList<String> returnCompletions = new ArrayList<>();

            if (args.length == 2) {
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
                } else if (args[0].equals("removeall")) {
                    if (!args[1].equals("")) {
                        for (String name : playerNames) {
                            if (name.startsWith(args[1])) {
                                returnCompletions.add(name);
                            }
                        }

                        return returnCompletions;
                    } else {
                        return playerNames;
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
            } else if (args.length == 4) {
                if (args[0].equals("config")) {
                    if (args[2].equals("towny-shop-plots")) {
                        if (!args[3].equals("")) {
                            for (String s : townyShopPlots) {
                                if (s.startsWith(args[3])) {
                                    returnCompletions.add(s);
                                }
                            }

                            return returnCompletions;
                        } else {
                            return townyShopPlots;
                        }
                    } else if (args[2].equals("areashop-remove-shops")) {
                        if (!args[3].equals("")) {
                            for (String s : areaShopRemoveEvents) {
                                if (s.startsWith(args[3])) {
                                    returnCompletions.add(s);
                                }
                            }

                            return returnCompletions;
                        } else {
                            return areaShopRemoveEvents;
                        }
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
