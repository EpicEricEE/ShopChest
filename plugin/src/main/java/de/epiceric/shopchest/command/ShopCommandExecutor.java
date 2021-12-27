package de.epiceric.shopchest.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.event.ShopPreCreateEvent;
import de.epiceric.shopchest.event.ShopPreInfoEvent;
import de.epiceric.shopchest.event.ShopPreOpenEvent;
import de.epiceric.shopchest.event.ShopPreRemoveEvent;
import de.epiceric.shopchest.event.ShopReloadEvent;
import de.epiceric.shopchest.event.ShopRemoveAllEvent;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.shop.ShopProduct;
import de.epiceric.shopchest.utils.Callback;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.ClickType.CreateClickType;
import de.epiceric.shopchest.utils.ClickType.SelectClickType;
import de.epiceric.shopchest.utils.ItemUtils;
import de.epiceric.shopchest.utils.Permissions;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.UpdateChecker;
import de.epiceric.shopchest.utils.Utils;

class ShopCommandExecutor implements CommandExecutor {

    private ShopChest plugin;
    private ShopUtils shopUtils;

    ShopCommandExecutor(ShopChest plugin) {
        this.plugin = plugin;
        this.shopUtils = plugin.getShopUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        List<ShopSubCommand> subCommands = plugin.getShopCommand().getSubCommands();

        if (args.length > 0) {
            String _subCommand = args[0];
            ShopSubCommand subCommand = null;

            for (ShopSubCommand shopSubCommand : subCommands) {
                if (shopSubCommand.getName().equalsIgnoreCase(_subCommand)) {
                    subCommand = shopSubCommand;
                    break;
                }
            }

            if (subCommand == null) {
                return false;
            }

            if (subCommand.getName().equalsIgnoreCase("reload")) {
                if (sender.hasPermission(Permissions.RELOAD)) {
                    reload(sender);
                } else {
                    sender.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_RELOAD));
                }
            } else if (subCommand.getName().equalsIgnoreCase("update")) {
                if (sender.hasPermission(Permissions.UPDATE)) {
                    checkUpdates(sender);
                } else {
                    sender.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_UPDATE));
                }
            } else if (subCommand.getName().equalsIgnoreCase("config")) {
                if (sender.hasPermission(Permissions.CONFIG)) {
                    return args.length >= 4 && changeConfig(sender, args);
                } else {
                    sender.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_CONFIG));
                }
            } else if (subCommand.getName().equalsIgnoreCase("removeall")) {
                if (sender.hasPermission(Permissions.REMOVE_OTHER)) {
                    if (args.length >= 2) {
                        removeAll(sender, args);
                    } else {
                        return false;
                    }
                } else {
                    sender.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_REMOVE_OTHERS));
                }
            } else {
                if (sender instanceof Player) {
                    Player p = (Player) sender;

                    if (subCommand.getName().equalsIgnoreCase("create")) {
                        if (args.length == 4) {
                            create(args, Shop.ShopType.NORMAL, p);
                        } else if (args.length == 5) {
                            if (args[4].equalsIgnoreCase("normal")) {
                                create(args, Shop.ShopType.NORMAL, p);
                            } else if (args[4].equalsIgnoreCase("admin")) {
                                if (p.hasPermission(Permissions.CREATE_ADMIN)) {
                                    create(args, Shop.ShopType.ADMIN, p);
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_CREATE_ADMIN));
                                }
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } else if (subCommand.getName().equalsIgnoreCase("remove")) {
                        remove(p);
                    } else if (subCommand.getName().equalsIgnoreCase("info")) {
                        info(p);
                    } else if (subCommand.getName().equalsIgnoreCase("limits")) {
                        plugin.debug(p.getName() + " is viewing his shop limits: " + shopUtils.getShopAmount(p) + "/" + shopUtils.getShopLimit(p));
                        int limit = shopUtils.getShopLimit(p);
                        p.sendMessage(LanguageUtils.getMessage(Message.OCCUPIED_SHOP_SLOTS,
                                new Replacement(Placeholder.LIMIT, (limit < 0 ? "∞" : String.valueOf(limit))),
                                new Replacement(Placeholder.AMOUNT, String.valueOf(shopUtils.getShopAmount(p)))));
                    } else if (subCommand.getName().equalsIgnoreCase("open")) {
                        open(p);
                    } else {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    /**
     * A given player checks for updates
     * @param sender The command executor
     */
    private void checkUpdates(CommandSender sender) {
        plugin.debug(sender.getName() + " is checking for updates");

        sender.sendMessage(LanguageUtils.getMessage(Message.UPDATE_CHECKING));

        UpdateChecker uc = new UpdateChecker(ShopChest.getInstance());
        UpdateChecker.UpdateCheckerResult result = uc.check();

        if (result == UpdateChecker.UpdateCheckerResult.TRUE) {
            plugin.setLatestVersion(uc.getVersion());
            plugin.setDownloadLink(uc.getLink());
            plugin.setUpdateNeeded(true);

            if (sender instanceof Player) {
                Utils.sendUpdateMessage(plugin, (Player) sender);
            } else {
                sender.sendMessage(LanguageUtils.getMessage(Message.UPDATE_AVAILABLE, new Replacement(Placeholder.VERSION, uc.getVersion())));
            }

        } else if (result == UpdateChecker.UpdateCheckerResult.FALSE) {
            plugin.setLatestVersion("");
            plugin.setDownloadLink("");
            plugin.setUpdateNeeded(false);
            sender.sendMessage(LanguageUtils.getMessage(Message.UPDATE_NO_UPDATE));
        } else {
            plugin.setLatestVersion("");
            plugin.setDownloadLink("");
            plugin.setUpdateNeeded(false);
            sender.sendMessage(LanguageUtils.getMessage(Message.UPDATE_ERROR));
        }
    }

    /**
     * A given player reloads the shops
     * @param sender The command executor
     */
    private void reload(final CommandSender sender) {
        plugin.debug(sender.getName() + " is reloading the shops");

        ShopReloadEvent event = new ShopReloadEvent(sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()){
            plugin.debug("Reload event cancelled");
            return;
        }

        // Reload configurations
        plugin.getShopChestConfig().reload(false, true, true);
        plugin.getHologramFormat().reload();
        plugin.getUpdater().restart();

        // Remove all shops
        for (Shop shop : shopUtils.getShops()) {
            shopUtils.removeShop(shop, false);
        }

        Chunk[] loadedChunks = Bukkit.getWorlds().stream().map(World::getLoadedChunks)
                .flatMap(Stream::of).toArray(Chunk[]::new);

        // Reconnect to the database and re-load shops in loaded chunks
        plugin.getShopDatabase().connect(new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                shopUtils.loadShops(loadedChunks, new Callback<Integer>(plugin) {
                    @Override
                    public void onResult(Integer result) {
                        sender.sendMessage(LanguageUtils.getMessage(Message.RELOADED_SHOPS,
                                new Replacement(Placeholder.AMOUNT, String.valueOf(result))));
                        plugin.debug(sender.getName() + " has reloaded " + result + " shops");
                    }
        
                    @Override
                    public void onError(Throwable throwable) {
                        sender.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, 
                                new Replacement(Placeholder.ERROR, "Failed to load shops from database")));
                        plugin.getLogger().severe("Failed to load shops");
                        if (throwable != null) plugin.getLogger().severe(throwable.getMessage());
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                // Database connection probably failed => disable plugin to prevent more errors
                sender.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, 
                        new Replacement(Placeholder.ERROR, "No database access: Disabling ShopChest")));
                plugin.getLogger().severe("No database access: Disabling ShopChest");
                if (throwable != null) plugin.getLogger().severe(throwable.getMessage());
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            }
        });
    }

    /**
     * A given player creates a shop
     * @param args Arguments of the entered command
     * @param shopType The {@link Shop.ShopType}, the shop will have
     * @param p The command executor
     */
    private void create(String[] args, Shop.ShopType shopType, final Player p) {
        plugin.debug(p.getName() + " wants to create a shop");

        int amount;
        double buyPrice, sellPrice;

        // Check if amount and prices are valid
        try {
            amount = Integer.parseInt(args[1]);
            buyPrice = Double.parseDouble(args[2]);
            sellPrice = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            p.sendMessage(LanguageUtils.getMessage(Message.AMOUNT_PRICE_NOT_NUMBER));
            plugin.debug(p.getName() + " has entered an invalid amount and/or prices");
            return;
        }

        if (!Utils.hasPermissionToCreateShop(p, Utils.getPreferredItemInHand(p), buyPrice > 0, sellPrice > 0)) {
            p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_CREATE));
            plugin.debug(p.getName() + " is not permitted to create the shop");
            return;
        }

        // Check for limits
        int limit = shopUtils.getShopLimit(p);
        if (limit != -1) {
            if (shopUtils.getShopAmount(p) >= limit) {
                if (shopType != Shop.ShopType.ADMIN) {
                    p.sendMessage(LanguageUtils.getMessage(Message.SHOP_LIMIT_REACHED, new Replacement(Placeholder.LIMIT, String.valueOf(limit))));
                    plugin.debug(p.getName() + " has reached the limit");
                    return;
                }
            }
        }

        if (amount <= 0) {
            p.sendMessage(LanguageUtils.getMessage(Message.AMOUNT_IS_ZERO));
            plugin.debug(p.getName() + " has entered an invalid amount");
            return;
        }

        if (!Config.allowDecimalsInPrice && (buyPrice != (int) buyPrice || sellPrice != (int) sellPrice)) {
            p.sendMessage(LanguageUtils.getMessage(Message.PRICES_CONTAIN_DECIMALS));
            plugin.debug(p.getName() + " has entered an invalid price");
            return;
        }

        boolean buyEnabled = buyPrice > 0;
        boolean sellEnabled = sellPrice > 0;

        if (!buyEnabled && !sellEnabled) {
            p.sendMessage(LanguageUtils.getMessage(Message.BUY_SELL_DISABLED));
            plugin.debug(p.getName() + " has disabled buying and selling");
            return;
        }

        ItemStack inHand = Utils.getPreferredItemInHand(p);

        // Check if item in hand
        if (inHand == null) {
            plugin.debug(p.getName() + " does not have an item in his hand");

            if (!Config.creativeSelectItem) {
                p.sendMessage(LanguageUtils.getMessage(Message.NO_ITEM_IN_HAND));
                return;
            }

            if (!(ClickType.getPlayerClickType(p) instanceof SelectClickType)) {
                // Don't set previous game mode to creative if player already has select click type
                ClickType.setPlayerClickType(p, new SelectClickType(p.getGameMode(), amount, buyPrice, sellPrice, shopType));
                p.setGameMode(GameMode.CREATIVE);
            }

            p.sendMessage(LanguageUtils.getMessage(Message.SELECT_ITEM));
        } else {
            SelectClickType ct = new SelectClickType(null, amount, buyPrice, sellPrice, shopType);
            ct.setItem(inHand);
            create2(p, ct);
        }
    }

    /**
     * <b>SHALL ONLY BE CALLED VIA {@link ShopCommand#createShopAfterSelected()}</b>
     */
    protected void create2(Player p, SelectClickType selectClickType) {
        ItemStack itemStack = selectClickType.getItem();
        int amount = selectClickType.getAmount();
        double buyPrice = selectClickType.getBuyPrice();
        double sellPrice = selectClickType.getSellPrice();
        boolean buyEnabled = buyPrice > 0;
        boolean sellEnabled = sellPrice > 0;
        ShopType shopType = selectClickType.getShopType();

        // Check if item on blacklist
        for (String item :Config.blacklist) {
            ItemStack is = ItemUtils.getItemStack(item);

            if (is == null) {
                plugin.getLogger().warning("Invalid item found in blacklist: " + item);
                plugin.debug("Invalid item in blacklist: " + item);
                continue;
            }

            if (is.getType().equals(itemStack.getType()) && is.getDurability() == itemStack.getDurability()) {
                p.sendMessage(LanguageUtils.getMessage(Message.CANNOT_SELL_ITEM));
                plugin.debug(p.getName() + "'s item is on the blacklist");
                return;
            }
        }

        // Check if prices lower than minimum price
        for (String key :Config.minimumPrices) {
            ItemStack is = ItemUtils.getItemStack(key);
            double minPrice = plugin.getConfig().getDouble("minimum-prices." + key);

            if (is == null) {
                plugin.getLogger().warning("Invalid item found in minimum-prices: " + key);
                plugin.debug("Invalid item in minimum-prices: " + key);
                continue;
            }

            if (is.getType().equals(itemStack.getType()) && is.getDurability() == itemStack.getDurability()) {
                if (buyEnabled) {
                    if ((buyPrice < amount * minPrice) && (buyPrice > 0)) {
                        p.sendMessage(LanguageUtils.getMessage(Message.BUY_PRICE_TOO_LOW, new Replacement(Placeholder.MIN_PRICE, String.valueOf(amount * minPrice))));
                        plugin.debug(p.getName() + "'s buy price is lower than the minimum");
                        return;
                    }
                }

                if (sellEnabled) {
                    if ((sellPrice < amount * minPrice) && (sellPrice > 0)) {
                        p.sendMessage(LanguageUtils.getMessage(Message.SELL_PRICE_TOO_LOW, new Replacement(Placeholder.MIN_PRICE, String.valueOf(amount * minPrice))));
                        plugin.debug(p.getName() + "'s sell price is lower than the minimum");
                        return;
                    }
                }
            }
        }

        // Check if prices higher than maximum price
        for (String key :Config.maximumPrices) {
            ItemStack is = ItemUtils.getItemStack(key);
            double maxPrice = plugin.getConfig().getDouble("maximum-prices." + key);

            if (is == null) {
                plugin.getLogger().warning("Invalid item found in maximum-prices: " + key);
                plugin.debug("Invalid item in maximum-prices: " + key);
                continue;
            }

            if (is.getType().equals(itemStack.getType()) && is.getDurability() == itemStack.getDurability()) {
                if (buyEnabled) {
                    if ((buyPrice > amount * maxPrice) && (buyPrice > 0)) {
                        p.sendMessage(LanguageUtils.getMessage(Message.BUY_PRICE_TOO_HIGH, new Replacement(Placeholder.MAX_PRICE, String.valueOf(amount * maxPrice))));
                        plugin.debug(p.getName() + "'s buy price is higher than the maximum");
                        return;
                    }
                }

                if (sellEnabled) {
                    if ((sellPrice > amount * maxPrice) && (sellPrice > 0)) {
                        p.sendMessage(LanguageUtils.getMessage(Message.SELL_PRICE_TOO_HIGH, new Replacement(Placeholder.MAX_PRICE, String.valueOf(amount * maxPrice))));
                        plugin.debug(p.getName() + "'s sell price is higher than the maximum");
                        return;
                    }
                }
            }
        }


        if (sellEnabled && buyEnabled) {
            if (Config.buyGreaterOrEqualSell) {
                if (buyPrice < sellPrice) {
                    p.sendMessage(LanguageUtils.getMessage(Message.BUY_PRICE_TOO_LOW, new Replacement(Placeholder.MIN_PRICE, String.valueOf(sellPrice))));
                    plugin.debug(p.getName() + "'s buy price is lower than the sell price");
                    return;
                }
            }
        }

        if (Enchantment.DURABILITY.canEnchantItem(itemStack)) {
            if (itemStack.getDurability() > 0 && !Config.allowBrokenItems) {
                p.sendMessage(LanguageUtils.getMessage(Message.CANNOT_SELL_BROKEN_ITEM));
                plugin.debug(p.getName() + "'s item is broken");
                return;
            }
        }

        double creationPrice = (shopType == Shop.ShopType.NORMAL) ?Config.shopCreationPriceNormal :Config.shopCreationPriceAdmin;
        if (creationPrice > 0) {
            if (plugin.getEconomy().getBalance(p, p.getWorld().getName()) < creationPrice) {
                p.sendMessage(LanguageUtils.getMessage(Message.SHOP_CREATE_NOT_ENOUGH_MONEY, new Replacement(Placeholder.CREATION_PRICE, String.valueOf(creationPrice))));
                plugin.debug(p.getName() + " can not pay the creation price");
                return;
            }
        }

        ShopProduct product = new ShopProduct(itemStack, amount);
        ShopPreCreateEvent event = new ShopPreCreateEvent(p, new Shop(plugin, p, product, null, buyPrice, sellPrice, shopType));
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            ClickType.setPlayerClickType(p, new CreateClickType(product, buyPrice, sellPrice, shopType));
            plugin.debug(p.getName() + " can now click a chest");
            p.sendMessage(LanguageUtils.getMessage(Message.CLICK_CHEST_CREATE));
        } else {
            plugin.debug("Shop pre create event cancelled");
        }
    }

    /**
     * A given player removes a shop
     * @param p The command executor
     */
    private void remove(final Player p) {
        plugin.debug(p.getName() + " wants to remove a shop");

        ShopPreRemoveEvent event = new ShopPreRemoveEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Shop pre remove event cancelled");
            return;
        }

        plugin.debug(p.getName() + " can now click a chest");
        p.sendMessage(LanguageUtils.getMessage(Message.CLICK_CHEST_REMOVE));
        ClickType.setPlayerClickType(p, new ClickType(ClickType.EnumClickType.REMOVE));
    }

    /**
     * A given player retrieves information about a shop
     * @param p The command executor
     */
    private void info(final Player p) {
        plugin.debug(p.getName() + " wants to retrieve information");

        ShopPreInfoEvent event = new ShopPreInfoEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Shop pre info event cancelled");
            return;
        }

        plugin.debug(p.getName() + " can now click a chest");
        p.sendMessage(LanguageUtils.getMessage(Message.CLICK_CHEST_INFO));
        ClickType.setPlayerClickType(p, new ClickType(ClickType.EnumClickType.INFO));
    }

    /**
     * A given player opens a shop
     * @param p The command executor
     */
    private void open(final Player p) {
        plugin.debug(p.getName() + " wants to open a shop");

        ShopPreOpenEvent event = new ShopPreOpenEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Shop pre open event cancelled");
            return;
        }

        plugin.debug(p.getName() + " can now click a chest");
        p.sendMessage(LanguageUtils.getMessage(Message.CLICK_CHEST_OPEN));
        ClickType.setPlayerClickType(p, new ClickType(ClickType.EnumClickType.OPEN));
    }

    private boolean changeConfig(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is changing the configuration");

        String property = args[2];
        String value = args[3];

        if (args[1].equalsIgnoreCase("set")) {
            plugin.getShopChestConfig().set(property, value);
            sender.sendMessage(LanguageUtils.getMessage(Message.CHANGED_CONFIG_SET, new Replacement(Placeholder.PROPERTY, property), new Replacement(Placeholder.VALUE, value)));
        } else if (args[1].equalsIgnoreCase("add")) {
            plugin.getShopChestConfig().add(property, value);
            sender.sendMessage(LanguageUtils.getMessage(Message.CHANGED_CONFIG_ADDED, new Replacement(Placeholder.PROPERTY, property), new Replacement(Placeholder.VALUE, value)));
        } else if (args[1].equalsIgnoreCase("remove")) {
            plugin.getShopChestConfig().remove(property, value);
            sender.sendMessage(LanguageUtils.getMessage(Message.CHANGED_CONFIG_REMOVED, new Replacement(Placeholder.PROPERTY, property), new Replacement(Placeholder.VALUE, value)));
        } else {
            return false;
        }

        return true;
    }

    private void removeAll(CommandSender sender, String[] args) {
        OfflinePlayer vendor = Bukkit.getOfflinePlayer(args[1]);

        plugin.debug(sender.getName() + " is removing all shops of " + vendor.getName());

        plugin.getShopUtils().getShops(vendor, new Callback<Collection<Shop>>(plugin) {
            @Override
            public void onResult(Collection<Shop> result) {
                List<Shop> shops = new ArrayList<>(result);

                ShopRemoveAllEvent event = new ShopRemoveAllEvent(sender, vendor, shops);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()){
                    plugin.debug("Remove all event cancelled");
                    return;
                }
        
                for (Shop shop : shops) {
                    shopUtils.removeShop(shop, true);
                }
        
                sender.sendMessage(LanguageUtils.getMessage(Message.ALL_SHOPS_REMOVED,
                        new Replacement(Placeholder.AMOUNT, String.valueOf(shops.size())),
                        new Replacement(Placeholder.VENDOR, vendor.getName())));
            }

            @Override
            public void onError(Throwable throwable) {
                sender.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED,
                        new Replacement(Placeholder.ERROR, "Failed to get player's shops")));
            }
        });

        
    }
}
