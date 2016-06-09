package de.epiceric.shopchest;

import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.interfaces.JsonBuilder;
import de.epiceric.shopchest.interfaces.jsonbuilder.*;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.ClickType.EnumClickType;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.UpdateChecker;
import de.epiceric.shopchest.utils.UpdateChecker.UpdateCheckerResult;
import de.epiceric.shopchest.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.List;

public class Commands extends BukkitCommand {

    private ShopChest plugin;

    private Permission perm = ShopChest.perm;

    public Commands(ShopChest plugin, String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.plugin = plugin;
    }

    public static void registerCommand(Command command, ShopChest plugin) throws ReflectiveOperationException {
        Method commandMap = plugin.getServer().getClass().getMethod("getCommandMap");
        Object cmdmap = commandMap.invoke(plugin.getServer());
        Method register = cmdmap.getClass().getMethod("register", String.class, Command.class);
        register.invoke(cmdmap, command.getName(), command);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (args.length == 0) {
                sendBasicHelpMessage(p);
                return true;
            } else {
                if (args[0].equalsIgnoreCase("create")) {
                    if (perm.has(p, "shopchest.create")) {
                        if (args.length == 4) {
                            create(args, ShopType.NORMAL, p);
                            return true;
                        } else if (args.length == 5) {
                            if (args[4].equalsIgnoreCase("normal")) {
                                create(args, ShopType.NORMAL, p);
                                return true;
                            } else if (args[4].equalsIgnoreCase("admin")) {
                                if (perm.has(p, "shopchest.create.admin")) {
                                    create(args, ShopType.ADMIN, p);
                                    return true;
                                } else {
                                    p.sendMessage(Config.noPermission_createAdmin());
                                    return true;
                                }
                            } else {
                                sendBasicHelpMessage(p);
                                return true;
                            }
                        } else {
                            sendBasicHelpMessage(p);
                            return true;
                        }
                    } else {
                        p.sendMessage(Config.noPermission_create());
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    remove(p);
                    return true;
                } else if (args[0].equalsIgnoreCase("info")) {
                    info(p);
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (perm.has(p, "shopchest.reload")) {
                        reload(p);
                        return true;
                    } else {
                        p.sendMessage(Config.noPermission_reload());
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("update")) {
                    if (perm.has(p, "shopchest.update")) {
                        checkUpdates(p);
                        return true;
                    } else {
                        p.sendMessage(Config.noPermission_update());
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("limits")) {
                    if (perm.has(p, "shopchest.limits")) {
                        p.sendMessage(Config.occupied_shop_slots(ShopUtils.getShopLimit(p), ShopUtils.getShopAmount(p)));
                        return true;
                    } else {
                        p.sendMessage(Config.noPermission_limits());
                    }
                } else {
                    sendBasicHelpMessage(p);
                    return true;
                }

                return true;
            }

        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

    }

    private void checkUpdates(Player player) {
        player.sendMessage(Config.checking_update());

        UpdateChecker uc = new UpdateChecker(ShopChest.getInstance(), ShopChest.getInstance().getDescription().getWebsite());
        UpdateCheckerResult result = uc.updateNeeded();

        if (result == UpdateCheckerResult.TRUE) {
            ShopChest.latestVersion = uc.getVersion();
            ShopChest.downloadLink = uc.getLink();
            ShopChest.isUpdateNeeded = true;

            JsonBuilder jb;
            switch (Utils.getVersion(plugin.getServer())) {
                case "v1_8_R1":
                    jb = new JsonBuilder_1_8_R1(Config.update_available(ShopChest.latestVersion));
                    break;
                case "v1_8_R2":
                    jb = new JsonBuilder_1_8_R2(Config.update_available(ShopChest.latestVersion));
                    break;
                case "v1_8_R3":
                    jb = new JsonBuilder_1_8_R3(Config.update_available(ShopChest.latestVersion));
                    break;
                case "v1_9_R1":
                    jb = new JsonBuilder_1_9_R1(Config.update_available(ShopChest.latestVersion));
                    break;
                case "v1_9_R2":
                    jb = new JsonBuilder_1_9_R2(Config.update_available(ShopChest.latestVersion));
                    break;
                case "v1_10_R1":
                    jb = new JsonBuilder_1_10_R1(Config.update_available(ShopChest.latestVersion));
                    break;
                default:
                    return;
            }
            jb.sendJson(player);

        } else if (result == UpdateCheckerResult.FALSE) {
            ShopChest.latestVersion = "";
            ShopChest.downloadLink = "";
            ShopChest.isUpdateNeeded = false;
            player.sendMessage(Config.no_new_update());
        } else {
            ShopChest.latestVersion = "";
            ShopChest.downloadLink = "";
            ShopChest.isUpdateNeeded = false;
            player.sendMessage(Config.update_check_error());
        }

        if (perm.has(player, "shopchest.broadcast")) {
            if (Config.enable_broadcast()) ShopChest.broadcast = uc.getBroadcast();
            if (ShopChest.broadcast != null) {
                for (String message : ShopChest.broadcast) {
                    player.sendMessage(message);
                }
            }
        }

    }

    private void reload(Player player) {
        ShopUtils.reloadShops(player);
    }

    private void create(String[] args, ShopType shopType, Player p) {
        int amount;
        double buyPrice, sellPrice;

        int limit = ShopUtils.getShopLimit(p);

        if (limit != -1) {
            if (ShopUtils.getShopAmount(p) >= limit) {
                p.sendMessage(Config.limit_reached(limit));
                return;
            }
        }

        try {
            amount = Integer.parseInt(args[1]);
            buyPrice = Double.parseDouble(args[2]);
            sellPrice = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            p.sendMessage(Config.amount_and_price_not_number());
            return;
        }

        boolean buyEnabled = !(buyPrice <= 0), sellEnabled = !(sellPrice <= 0);

        if (!buyEnabled && !sellEnabled) {
            p.sendMessage(Config.buy_and_sell_disabled());
            return;
        }

        if (p.getItemInHand().getType().equals(Material.AIR)) {
            p.sendMessage(Config.no_item_in_hand());
            return;
        }

        for (String item : Config.blacklist()) {

            ItemStack itemStack;

            if (item.contains(":")) {
                itemStack = new ItemStack(Material.getMaterial(item.split(":")[0]), 1, Short.parseShort(item.split(":")[1]));
            } else {
                itemStack = new ItemStack(Material.getMaterial(item), 1);
            }

            if (itemStack.getType().equals(p.getItemInHand().getType()) && itemStack.getDurability() == p.getItemInHand().getDurability()) {
                p.sendMessage(Config.cannot_sell_item());
                return;
            }
        }

        for (String key : Config.minimum_prices()) {

            ItemStack itemStack;
            double price = plugin.getConfig().getDouble("minimum-prices." + key);

            if (key.contains(":")) {
                itemStack = new ItemStack(Material.getMaterial(key.split(":")[0]), 1, Short.parseShort(key.split(":")[1]));
            } else {
                itemStack = new ItemStack(Material.getMaterial(key), 1);
            }

            if (itemStack.getType().equals(p.getItemInHand().getType()) && itemStack.getDurability() == p.getItemInHand().getDurability()) {
                if (buyEnabled) {
                    if ((buyPrice <= amount * price) && (buyPrice > 0)) {
                        p.sendMessage(Config.buyPrice_too_low(amount * price));
                        return;
                    }
                }

                if (sellEnabled) {
                    if ((sellPrice <= amount * price) && (sellPrice > 0)) {
                        p.sendMessage(Config.sellPrice_too_low(amount * price));
                        return;
                    }
                }
            }
        }

        if (sellEnabled && buyEnabled) {
            if (Config.buy_greater_or_equal_sell()) {
                if (buyPrice < sellPrice) {
                    p.sendMessage(Config.buyPrice_too_low(sellPrice));
                    return;
                }
            }
        }

        ItemStack itemStack = new ItemStack(p.getItemInHand().getType(), amount, p.getItemInHand().getDurability());
        itemStack.setItemMeta(p.getItemInHand().getItemMeta());

        if (Enchantment.DURABILITY.canEnchantItem(itemStack)) {
            if (itemStack.getDurability() > 0) {
                p.sendMessage(Config.cannot_sell_broken_item());
                return;
            }
        }

        double creationPrice = (shopType == ShopType.NORMAL) ? Config.shop_creation_price_normal() : Config.shop_creation_price_admin();
        if (creationPrice > 0) {
            if (ShopChest.econ.getBalance(p) >= creationPrice) {
                EconomyResponse r = ShopChest.econ.withdrawPlayer(p, creationPrice);
                if (!r.transactionSuccess()) {
                    p.sendMessage(Config.error_occurred(r.errorMessage));
                    return;
                }
            } else {
                p.sendMessage(Config.shop_create_not_enough_money(creationPrice));
                return;
            }
        }

        ClickType.addPlayerClickType(p, new ClickType(EnumClickType.CREATE, itemStack, buyPrice, sellPrice, shopType));
        p.sendMessage(Config.click_chest_to_create());

    }

    private void remove(Player p) {
        p.sendMessage(Config.click_chest_to_remove());
        ClickType.addPlayerClickType(p, new ClickType(EnumClickType.REMOVE));
    }

    private void info(Player p) {
        p.sendMessage(Config.click_chest_for_info());
        ClickType.addPlayerClickType(p, new ClickType(EnumClickType.INFO));
    }

    private void sendBasicHelpMessage(Player player) {
        player.sendMessage(ChatColor.GREEN + "/" + Config.main_command_name() + " create <amount> <buy-price> <sell-price> [normal|admin] - " + Config.cmdDesc_create());
        player.sendMessage(ChatColor.GREEN + "/" + Config.main_command_name() + " remove - " + Config.cmdDesc_remove());
        player.sendMessage(ChatColor.GREEN + "/" + Config.main_command_name() + " info - " + Config.cmdDesc_info());
        player.sendMessage(ChatColor.GREEN + "/" + Config.main_command_name() + " reload - " + Config.cmdDesc_reload());
        player.sendMessage(ChatColor.GREEN + "/" + Config.main_command_name() + " update - " + Config.cmdDesc_update());
        player.sendMessage(ChatColor.GREEN + "/" + Config.main_command_name() + " limits - " + Config.cmdDesc_limits());
    }

}
