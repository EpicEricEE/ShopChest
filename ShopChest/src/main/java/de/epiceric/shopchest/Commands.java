package de.epiceric.shopchest;

import de.epiceric.shopchest.config.Regex;
import de.epiceric.shopchest.event.ShopPreCreateEvent;
import de.epiceric.shopchest.event.ShopPreInfoEvent;
import de.epiceric.shopchest.event.ShopPreRemoveEvent;
import de.epiceric.shopchest.event.ShopReloadEvent;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.nms.IJsonBuilder;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.ClickType.EnumClickType;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.UpdateChecker;
import de.epiceric.shopchest.utils.UpdateChecker.UpdateCheckerResult;
import de.epiceric.shopchest.utils.Utils;
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
    private Permission perm;
    private ShopUtils shopUtils;

    public Commands(ShopChest plugin, String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.plugin = plugin;
        this.perm = plugin.getPermission();
        this.shopUtils = plugin.getShopUtils();
    }

    /**
     * Register a command to ShopChest
     *
     * @param command Command to register
     * @param plugin  Instance of ShopChest
     * @throws ReflectiveOperationException
     */
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
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_CREATE_ADMIN));
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
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_CREATE));
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
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_RELOAD));
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("update")) {
                    if (perm.has(p, "shopchest.update")) {
                        checkUpdates(p);
                        return true;
                    } else {
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_UPDATE));
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("limits")) {
                    if (perm.has(p, "shopchest.limits")) {
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.OCCUPIED_SHOP_SLOTS,
                                new LocalizedMessage.ReplacedRegex(Regex.LIMIT, String.valueOf(shopUtils.getShopLimit(p))),
                                new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(shopUtils.getShopAmount(p)))));

                        return true;
                    } else {
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_LIMITS));
                    }
                } else if (args[0].equalsIgnoreCase("config")) {
                    if (perm.has(p, "shopchest.config")) {
                        if (args.length >= 4) {
                            String property = args[2];
                            String value = args[3];

                            if (args[1].equalsIgnoreCase("set")) {
                                plugin.getShopChestConfig().set(property, value);
                                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHANGED_CONFIG_SET, new LocalizedMessage.ReplacedRegex(Regex.PROPERTY, property), new LocalizedMessage.ReplacedRegex(Regex.VALUE, value)));
                                return true;
                            } else if (args[1].equalsIgnoreCase("add")) {
                                plugin.getShopChestConfig().add(property, value);
                                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHANGED_CONFIG_ADDED, new LocalizedMessage.ReplacedRegex(Regex.PROPERTY, property), new LocalizedMessage.ReplacedRegex(Regex.VALUE, value)));
                                return true;
                            } else if (args[1].equalsIgnoreCase("remove")) {
                                plugin.getShopChestConfig().remove(property, value);
                                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHANGED_CONFIG_REMOVED, new LocalizedMessage.ReplacedRegex(Regex.PROPERTY, property), new LocalizedMessage.ReplacedRegex(Regex.VALUE, value)));
                                return true;
                            } else {
                                sendBasicHelpMessage(p);
                                return true;
                            }
                        } else {
                            sendBasicHelpMessage(p);
                            return true;
                        }
                    } else {
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_CONFIG));
                        return true;
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

    /**
     * A given player checks for updates
     * @param player The command executor
     */
    private void checkUpdates(Player player) {
        player.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CHECKING));

        UpdateChecker uc = new UpdateChecker(ShopChest.getInstance());
        UpdateCheckerResult result = uc.check();

        if (result == UpdateCheckerResult.TRUE) {
            plugin.setLatestVersion(uc.getVersion());
            plugin.setDownloadLink(uc.getLink());
            plugin.setUpdateNeeded(true);

            IJsonBuilder jb;
            switch (Utils.getServerVersion()) {
                case "v1_8_R1":
                    jb = new de.epiceric.shopchest.nms.v1_8_R1.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, uc.getVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), uc.getLink());
                    break;
                case "v1_8_R2":
                    jb = new de.epiceric.shopchest.nms.v1_8_R2.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, uc.getVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), uc.getLink());
                    break;
                case "v1_8_R3":
                    jb = new de.epiceric.shopchest.nms.v1_8_R3.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, uc.getVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), uc.getLink());
                    break;
                case "v1_9_R1":
                    jb = new de.epiceric.shopchest.nms.v1_9_R1.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, uc.getVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), uc.getLink());
                    break;
                case "v1_9_R2":
                    jb = new de.epiceric.shopchest.nms.v1_9_R2.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, uc.getVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), uc.getLink());
                    break;
                case "v1_10_R1":
                    jb = new de.epiceric.shopchest.nms.v1_10_R1.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, uc.getVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), uc.getLink());
                    break;
                default:
                    return;
            }
            jb.sendJson(player);

        } else if (result == UpdateCheckerResult.FALSE) {
            plugin.setLatestVersion("");
            plugin.setDownloadLink("");
            plugin.setUpdateNeeded(false);
            player.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_NO_UPDATE));
        } else {
            plugin.setLatestVersion("");
            plugin.setDownloadLink("");
            plugin.setUpdateNeeded(false);
            player.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_ERROR));
        }
    }

    /**
     * A given player reloads the shops
     * @param player The command executor
     */
    private void reload(Player player) {
        ShopReloadEvent event = new ShopReloadEvent(player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        player.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.RELOADED_SHOPS, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(shopUtils.reloadShops(true)))));
    }

    /**
     * A given player creates a shop
     * @param args Arguments of the entered command
     * @param shopType The {@link ShopType}, the shop will have
     * @param p The command executor
     */
    private void create(String[] args, ShopType shopType, Player p) {
        int amount;
        double buyPrice, sellPrice;

        int limit = shopUtils.getShopLimit(p);

        if (limit != -1) {
            if (shopUtils.getShopAmount(p) >= limit) {
                if (shopType != ShopType.ADMIN || !plugin.getShopChestConfig().exclude_admin_shops) {
                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_LIMIT_REACHED, new LocalizedMessage.ReplacedRegex(Regex.LIMIT, String.valueOf(limit))));
                    return;
                }
            }
        }

        try {
            amount = Integer.parseInt(args[1]);
            buyPrice = Double.parseDouble(args[2]);
            sellPrice = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.AMOUNT_PRICE_NOT_NUMBER));
            return;
        }

        boolean buyEnabled = !(buyPrice <= 0), sellEnabled = !(sellPrice <= 0);

        if (!buyEnabled && !sellEnabled) {
            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_SELL_DISABLED));
            return;
        }

        if (p.getItemInHand().getType().equals(Material.AIR)) {
            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_ITEM_IN_HAND));
            return;
        }

        for (String item : plugin.getShopChestConfig().blacklist) {

            ItemStack itemStack;

            if (item.contains(":")) {
                itemStack = new ItemStack(Material.getMaterial(item.split(":")[0]), 1, Short.parseShort(item.split(":")[1]));
            } else {
                itemStack = new ItemStack(Material.getMaterial(item), 1);
            }

            if (itemStack.getType().equals(p.getItemInHand().getType()) && itemStack.getDurability() == p.getItemInHand().getDurability()) {
                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CANNOT_SELL_ITEM));
                return;
            }
        }

        for (String key : plugin.getShopChestConfig().minimum_prices) {

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
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_PRICE_TOO_LOW, new LocalizedMessage.ReplacedRegex(Regex.MIN_PRICE, String.valueOf(amount * price))));
                        return;
                    }
                }

                if (sellEnabled) {
                    if ((sellPrice <= amount * price) && (sellPrice > 0)) {
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SELL_PRICE_TOO_LOW, new LocalizedMessage.ReplacedRegex(Regex.MIN_PRICE, String.valueOf(amount * price))));
                        return;
                    }
                }
            }
        }

        if (sellEnabled && buyEnabled) {
            if (plugin.getShopChestConfig().buy_greater_or_equal_sell) {
                if (buyPrice < sellPrice) {
                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_PRICE_TOO_LOW, new LocalizedMessage.ReplacedRegex(Regex.MIN_PRICE, String.valueOf(sellPrice))));
                    return;
                }
            }
        }

        ItemStack itemStack = new ItemStack(p.getItemInHand().getType(), amount, p.getItemInHand().getDurability());
        itemStack.setItemMeta(p.getItemInHand().getItemMeta());

        if (Enchantment.DURABILITY.canEnchantItem(itemStack)) {
            if (itemStack.getDurability() > 0 && !plugin.getShopChestConfig().allow_broken_items) {
                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CANNOT_SELL_BROKEN_ITEM));
                return;
            }
        }

        double creationPrice = (shopType == ShopType.NORMAL) ? plugin.getShopChestConfig().shop_creation_price_normal : plugin.getShopChestConfig().shop_creation_price_admin;
        if (creationPrice > 0) {
            if (plugin.getEconomy().getBalance(p) < creationPrice) {
                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_CREATE_NOT_ENOUGH_MONEY, new LocalizedMessage.ReplacedRegex(Regex.CREATION_PRICE, String.valueOf(creationPrice))));
                return;
            }
        }

        ShopPreCreateEvent event = new ShopPreCreateEvent(p, Shop.createImaginaryShop(p, itemStack, buyPrice, sellPrice, shopType));
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            ClickType.setPlayerClickType(p, new ClickType(EnumClickType.CREATE, itemStack, buyPrice, sellPrice, shopType));
            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_CHEST_CREATE));
        }
    }

    /**
     * A given player removes a shop
     * @param p The command executor
     */
    private void remove(Player p) {
        ShopPreRemoveEvent event = new ShopPreRemoveEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_CHEST_REMOVE));
        ClickType.setPlayerClickType(p, new ClickType(EnumClickType.REMOVE));
    }

    /**
     * A given player retrieves information about a shop
     * @param p The command executor
     */
    private void info(Player p) {
        ShopPreInfoEvent event = new ShopPreInfoEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_CHEST_INFO));
        ClickType.setPlayerClickType(p, new ClickType(EnumClickType.INFO));
    }

    /**
     * Sends the basic help message to a given player
     * @param player Player who will receive the message
     */
    private void sendBasicHelpMessage(Player player) {
        player.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " create <amount> <buy-price> <sell-price> [normal|admin] - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_CREATE));
        player.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " remove - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_REMOVE));
        player.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " info - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_INFO));
        player.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " reload - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_RELOAD));
        player.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " update - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_UPDATE));
        player.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " limits - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_LIMITS));
        player.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " config <set|add|remove> <property> <value> - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_CONFIG));
    }

}
