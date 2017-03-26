package de.epiceric.shopchest;

import com.intellectualcrafters.plot.object.PlotPlayer;
import de.epiceric.shopchest.config.Regex;
import de.epiceric.shopchest.event.*;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.nms.JsonBuilder;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.utils.*;
import de.epiceric.shopchest.utils.ClickType.EnumClickType;
import de.epiceric.shopchest.utils.UpdateChecker.UpdateCheckerResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

class ShopCommand implements CommandExecutor {

    private ShopChest plugin;
    private String name;
    private ShopUtils shopUtils;
    private PluginCommand pluginCommand;

    ShopCommand(ShopChest plugin) {
        this.plugin = plugin;
        this.name = plugin.getShopChestConfig().main_command_name;
        this.shopUtils = plugin.getShopUtils();
        this.pluginCommand = createPluginCommand();

        register();
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
            cmd.setExecutor(this);
            cmd.setPermission(Permissions.BUY);
            cmd.setTabCompleter(new ShopTabCompleter(plugin));

            return cmd;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            plugin.getLogger().severe("Failed to create command");
            plugin.debug("Failed to create plugin command");
            plugin.debug(e);
        }

        return null;
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean needsHelp = true;

        if (args.length > 0) {
            if (!(sender instanceof Player)) {
                switch (args[0].toUpperCase(Locale.US)) {
                    case "CREATE":
                    case "REMOVE":
                    case "INFO":
                    case "LIMITS":
                    case "OPEN":
                        sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                        return true;
                }
            } else {
                Player p = (Player) sender;

                if (args[0].equalsIgnoreCase("create")) {

                    if (p.hasPermission(Permissions.CREATE)) {
                        if (args.length == 4) {
                            needsHelp = false;
                            create(args, ShopType.NORMAL, p);
                        } else if (args.length == 5) {
                            if (args[4].equalsIgnoreCase("normal")) {
                                needsHelp = false;
                                create(args, ShopType.NORMAL, p);
                            } else if (args[4].equalsIgnoreCase("admin")) {
                                needsHelp = false;
                                if (p.hasPermission(Permissions.CREATE_ADMIN)) {
                                    create(args, ShopType.ADMIN, p);
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_CREATE_ADMIN));
                                }
                            }
                        }
                    } else {
                        needsHelp = false;
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_CREATE));
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    needsHelp = false;
                    remove(p);
                } else if (args[0].equalsIgnoreCase("info")) {
                    needsHelp = false;
                    info(p);
                } else if (args[0].equalsIgnoreCase("limits")) {
                    needsHelp = false;
                    plugin.debug(p.getName() + " is viewing his shop limits: " + shopUtils.getShopAmount(p) + "/" + shopUtils.getShopLimit(p));
                    int limit = shopUtils.getShopLimit(p);
                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.OCCUPIED_SHOP_SLOTS,
                            new LocalizedMessage.ReplacedRegex(Regex.LIMIT, (limit < 0 ? "∞" : String.valueOf(limit))),
                            new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(shopUtils.getShopAmount(p)))));
                } else if (args[0].equalsIgnoreCase("open")) {
                    needsHelp = false;
                    open(p);
                }
            }

            if (args[0].equalsIgnoreCase("reload")) {
                needsHelp = false;
                if (sender.hasPermission(Permissions.RELOAD)) {
                    reload(sender);
                } else {
                    sender.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_RELOAD));
                }
            } else if (args[0].equalsIgnoreCase("update")) {
                needsHelp = false;
                if (sender.hasPermission(Permissions.UPDATE)) {
                    checkUpdates(sender);
                } else {
                    sender.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_UPDATE));
                }
            } else if (args[0].equalsIgnoreCase("config")) {
                if (sender.hasPermission(Permissions.CONFIG)) {
                    if (args.length >= 4) {
                        needsHelp = false;
                        changeConfig(sender, args);
                    }
                } else {
                    needsHelp = false;
                    sender.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_CONFIG));
                }
            }
        }


        if (needsHelp) sendBasicHelpMessage(sender);
        return true;
    }

    private void changeConfig(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is changing the configuration");

        String property = args[2];
        String value = args[3];

        if (args[1].equalsIgnoreCase("set")) {
            plugin.getShopChestConfig().set(property, value);
            sender.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHANGED_CONFIG_SET, new LocalizedMessage.ReplacedRegex(Regex.PROPERTY, property), new LocalizedMessage.ReplacedRegex(Regex.VALUE, value)));
        } else if (args[1].equalsIgnoreCase("add")) {
            plugin.getShopChestConfig().add(property, value);
            sender.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHANGED_CONFIG_ADDED, new LocalizedMessage.ReplacedRegex(Regex.PROPERTY, property), new LocalizedMessage.ReplacedRegex(Regex.VALUE, value)));
        } else if (args[1].equalsIgnoreCase("remove")) {
            plugin.getShopChestConfig().remove(property, value);
            sender.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHANGED_CONFIG_REMOVED, new LocalizedMessage.ReplacedRegex(Regex.PROPERTY, property), new LocalizedMessage.ReplacedRegex(Regex.VALUE, value)));
        } else {
            sendBasicHelpMessage(sender);
        }
    }

    /**
     * A given player checks for updates
     *
     * @param sender The command executor
     */
    private void checkUpdates(CommandSender sender) {
        plugin.debug(sender.getName() + " is checking for updates");

        sender.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CHECKING));

        UpdateChecker uc = new UpdateChecker(ShopChest.getInstance());
        UpdateCheckerResult result = uc.check();

        if (result == UpdateCheckerResult.TRUE) {
            plugin.setLatestVersion(uc.getVersion());
            plugin.setDownloadLink(uc.getLink());
            plugin.setUpdateNeeded(true);

            if (sender instanceof Player) {
                JsonBuilder jb = new JsonBuilder(plugin, LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, uc.getVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), uc.getLink());
                jb.sendJson((Player) sender);
            } else {
                sender.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, uc.getVersion())));
            }

        } else if (result == UpdateCheckerResult.FALSE) {
            plugin.setLatestVersion("");
            plugin.setDownloadLink("");
            plugin.setUpdateNeeded(false);
            sender.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_NO_UPDATE));
        } else {
            plugin.setLatestVersion("");
            plugin.setDownloadLink("");
            plugin.setUpdateNeeded(false);
            sender.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_ERROR));
        }
    }

    /**
     * A given player reloads the shops
     *
     * @param sender The command executor
     */
    private void reload(final CommandSender sender) {
        plugin.debug(sender.getName() + " is reloading the shops");

        ShopReloadEvent event = new ShopReloadEvent(sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Reload event cancelled");
            return;
        }

        shopUtils.reloadShops(true, true, new Callback(plugin) {
            @Override
            public void onResult(Object result) {
                if (result instanceof Integer) {
                    int count = (int) result;
                    sender.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.RELOADED_SHOPS, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(count))));
                    plugin.debug(sender.getName() + " has reloaded " + count + " shops");
                }
            }
        });

    }

    /**
     * A given player creates a shop
     *
     * @param args     Arguments of the entered command
     * @param shopType The {@link ShopType}, the shop will have
     * @param p   The command executor
     */
    private void create(String[] args, ShopType shopType, Player p) {
        plugin.debug(p.getName() + " wants to create a shop");

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

        plugin.debug(p.getName() + " has not reached the limit");

        try {
            amount = Integer.parseInt(args[1]);
            buyPrice = Double.parseDouble(args[2]);
            sellPrice = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.AMOUNT_PRICE_NOT_NUMBER));
            return;
        }

        if (amount <= 0) {
            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.AMOUNT_IS_ZERO));
            return;
        }

        plugin.debug(p.getName() + " has entered numbers as prices and amount");

        if (!plugin.getShopChestConfig().allow_decimals_in_price && (buyPrice != (int) buyPrice || sellPrice != (int) sellPrice)) {
            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.PRICES_CONTAIN_DECIMALS));
            return;
        }

        plugin.debug(p.getName() + " has entered the numbers correctly (according to allow-decimals configuration)");

        boolean buyEnabled = buyPrice > 0;
        boolean sellEnabled = sellPrice > 0;

        if (!buyEnabled && !sellEnabled) {
            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_SELL_DISABLED));
            return;
        }

        plugin.debug(p.getName() + " has enabled buying, selling or both");

        if (Utils.getPreferredItemInHand(p) == null) {
            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_ITEM_IN_HAND));
            return;
        }

        plugin.debug(p.getName() + " has an item in his hand");

        for (String item : plugin.getShopChestConfig().blacklist) {
            ItemStack itemStack;

            if (item.contains(":")) {
                Material mat = Material.getMaterial(item.split(":")[0]);
                if (mat == null) {
                    plugin.getLogger().warning("Invalid item found in blacklist: " + item);
                    plugin.debug("Invalid item in blacklist: " + item);
                    continue;
                }
                itemStack = new ItemStack(mat, 1, Short.parseShort(item.split(":")[1]));
            } else {
                Material mat = Material.getMaterial(item);
                if (mat == null) {
                    plugin.getLogger().warning("Invalid item found in blacklist: " + item);
                    plugin.debug("Invalid item in blacklist: " + item);
                    continue;
                }
                itemStack = new ItemStack(mat, 1);
            }

            if (itemStack.getType().equals(Utils.getPreferredItemInHand(p).getType()) && itemStack.getDurability() == Utils.getPreferredItemInHand(p).getDurability()) {
                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CANNOT_SELL_ITEM));
                return;
            }
        }

        plugin.debug(p.getName() + "'s item is not on the blacklist");

        for (String key : plugin.getShopChestConfig().minimum_prices) {
            ItemStack itemStack;
            double minPrice = plugin.getConfig().getDouble("minimum-prices." + key);

            if (key.contains(":")) {
                Material mat = Material.getMaterial(key.split(":")[0]);
                if (mat == null) {
                    plugin.getLogger().warning("Invalid item found in minimum-prices: " + key);
                    plugin.debug("Invalid item in minimum-prices: " + key);
                    continue;
                }
                itemStack = new ItemStack(mat, 1, Short.parseShort(key.split(":")[1]));
            } else {
                Material mat = Material.getMaterial(key);
                if (mat == null) {
                    plugin.getLogger().warning("Invalid item found in minimum-prices: " + key);
                    plugin.debug("Invalid item in minimum-prices: " + key);
                    continue;
                }
                itemStack = new ItemStack(mat, 1);
            }

            if (itemStack.getType().equals(Utils.getPreferredItemInHand(p).getType()) && itemStack.getDurability() == Utils.getPreferredItemInHand(p).getDurability()) {
                if (buyEnabled) {
                    if ((buyPrice < amount * minPrice) && (buyPrice > 0)) {
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_PRICE_TOO_LOW, new LocalizedMessage.ReplacedRegex(Regex.MIN_PRICE, String.valueOf(amount * minPrice))));
                        return;
                    }
                }

                if (sellEnabled) {
                    if ((sellPrice < amount * minPrice) && (sellPrice > 0)) {
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SELL_PRICE_TOO_LOW, new LocalizedMessage.ReplacedRegex(Regex.MIN_PRICE, String.valueOf(amount * minPrice))));
                        return;
                    }
                }
            }
        }

        plugin.debug(p.getName() + "'s prices are higher than the minimum");

        for (String key : plugin.getShopChestConfig().maximum_prices) {
            ItemStack itemStack;
            double maxPrice = plugin.getConfig().getDouble("maximum-prices." + key);

            if (key.contains(":")) {
                Material mat = Material.getMaterial(key.split(":")[0]);
                if (mat == null) {
                    plugin.getLogger().warning("Invalid item found in maximum-prices: " + key);
                    plugin.debug("Invalid item in maximum-prices: " + key);
                    continue;
                }
                itemStack = new ItemStack(mat, 1, Short.parseShort(key.split(":")[1]));
            } else {
                Material mat = Material.getMaterial(key);
                if (mat == null) {
                    plugin.getLogger().warning("Invalid item found in maximum-prices: " + key);
                    plugin.debug("Invalid item in maximum-prices: " + key);
                    continue;
                }
                itemStack = new ItemStack(mat, 1);
            }

            if (itemStack.getType().equals(Utils.getPreferredItemInHand(p).getType()) && itemStack.getDurability() == Utils.getPreferredItemInHand(p).getDurability()) {
                if (buyEnabled) {
                    if ((buyPrice > amount * maxPrice) && (buyPrice > 0)) {
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_PRICE_TOO_HIGH, new LocalizedMessage.ReplacedRegex(Regex.MAX_PRICE, String.valueOf(amount * maxPrice))));
                        return;
                    }
                }

                if (sellEnabled) {
                    if ((sellPrice > amount * maxPrice) && (sellPrice > 0)) {
                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SELL_PRICE_TOO_HIGH, new LocalizedMessage.ReplacedRegex(Regex.MAX_PRICE, String.valueOf(amount * maxPrice))));
                        return;
                    }
                }
            }
        }

        plugin.debug(p.getName() + "'s prices are higher than the minimum");

        if (sellEnabled && buyEnabled) {
            if (plugin.getShopChestConfig().buy_greater_or_equal_sell) {
                if (buyPrice < sellPrice) {
                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_PRICE_TOO_LOW, new LocalizedMessage.ReplacedRegex(Regex.MIN_PRICE, String.valueOf(sellPrice))));
                    return;
                }
            }
        }

        plugin.debug(p.getName() + "'s buy price is high enough");

        ItemStack itemStack = new ItemStack(Utils.getPreferredItemInHand(p).getType(), amount, Utils.getPreferredItemInHand(p).getDurability());
        itemStack.setItemMeta(Utils.getPreferredItemInHand(p).getItemMeta());

        if (Enchantment.DURABILITY.canEnchantItem(itemStack)) {
            if (itemStack.getDurability() > 0 && !plugin.getShopChestConfig().allow_broken_items) {
                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CANNOT_SELL_BROKEN_ITEM));
                return;
            }
        }

        plugin.debug(p.getName() + "'s item is not broken (or broken items are allowed through config)");

        double creationPrice = (shopType == ShopType.NORMAL) ? plugin.getShopChestConfig().shop_creation_price_normal : plugin.getShopChestConfig().shop_creation_price_admin;
        if (creationPrice > 0) {
            if (plugin.getEconomy().getBalance(p) < creationPrice) {
                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_CREATE_NOT_ENOUGH_MONEY, new LocalizedMessage.ReplacedRegex(Regex.CREATION_PRICE, String.valueOf(creationPrice))));
                return;
            }
        }

        plugin.debug(p.getName() + " can pay the creation price");

        ShopPreCreateEvent event = new ShopPreCreateEvent(p, new Shop(plugin, p, itemStack, null, buyPrice, sellPrice, shopType));
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            ClickType.setPlayerClickType(p, new ClickType(EnumClickType.CREATE, itemStack, buyPrice, sellPrice, shopType));
            plugin.debug(p.getName() + " can now click a chest");
            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_CHEST_CREATE));
        } else {
            plugin.debug("Shop pre create event cancelled");
        }
    }

    /**
     * A given player removes a shop
     *
     * @param p The command executor
     */
    private void remove(Player p) {
        plugin.debug(p.getName() + " wants to remove a shop");

        ShopPreRemoveEvent event = new ShopPreRemoveEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Shop pre remove event cancelled");
            return;
        }

        plugin.debug(p.getName() + " can now click a chest");
        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_CHEST_REMOVE));
        ClickType.setPlayerClickType(p, new ClickType(EnumClickType.REMOVE));
    }

    /**
     * A given player retrieves information about a shop
     *
     * @param p The command executor
     */
    private void info(Player p) {
        plugin.debug(p.getName() + " wants to retrieve information");

        ShopPreInfoEvent event = new ShopPreInfoEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Shop pre info event cancelled");
            return;
        }

        plugin.debug(p.getName() + " can now click a chest");
        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_CHEST_INFO));
        ClickType.setPlayerClickType(p, new ClickType(EnumClickType.INFO));
    }

    /**
     * A given player opens a shop
     *
     * @param p The command executor
     */
    private void open(Player p) {
        plugin.debug(p.getName() + " wants to open a shop");

        ShopPreOpenEvent event = new ShopPreOpenEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Shop pre open event cancelled");
            return;
        }

        plugin.debug(p.getName() + " can now click a chest");
        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_CHEST_OPEN));
        ClickType.setPlayerClickType(p, new ClickType(EnumClickType.OPEN));
    }

    /**
     * Sends the basic help message
     *
     * @param sender {@link CommandSender} who will receive the message
     */
    private void sendBasicHelpMessage(CommandSender sender) {
        plugin.debug("Sending basic help message to " + sender.getName());

        if (sender instanceof Player) {
            if (sender.hasPermission(Permissions.CREATE_ADMIN)) {
                sender.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " create <amount> <buy-price> <sell-price> [normal|admin] - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_CREATE));
            } else {
                sender.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " create <amount> <buy-price> <sell-price> - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_CREATE));
            }

            sender.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " remove - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_REMOVE));
            sender.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " info - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_INFO));
            sender.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " limits - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_LIMITS));
            sender.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " open - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_OPEN));
        }

        if (sender.hasPermission(Permissions.RELOAD)) {
            sender.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " reload - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_RELOAD));
        }

        if (sender.hasPermission(Permissions.UPDATE)) {
            sender.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " update - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_UPDATE));
        }

        if (sender.hasPermission(Permissions.CONFIG)) {
            sender.sendMessage(ChatColor.GREEN + "/" + plugin.getShopChestConfig().main_command_name + " config <set|add|remove> <property> <value> - " + LanguageUtils.getMessage(LocalizedMessage.Message.COMMAND_DESC_CONFIG));
        }
    }

}
