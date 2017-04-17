package de.epiceric.shopchest.listeners;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.Regex;
import de.epiceric.shopchest.event.*;
import de.epiceric.shopchest.external.PlotSquaredShopFlag;
import de.epiceric.shopchest.external.WorldGuardShopFlag;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.nms.CustomBookMeta;
import de.epiceric.shopchest.nms.Hologram;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.Permissions;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.Utils;
import fr.xephi.authme.AuthMe;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import us.talabrek.ultimateskyblock.api.IslandInfo;

import java.util.HashMap;
import java.util.Map;

public class ShopInteractListener implements Listener {

    private ShopChest plugin;
    private Economy econ;
    private Database database;
    private ShopUtils shopUtils;
    private Config config;
    private WorldGuardPlugin worldGuard;

    public ShopInteractListener(ShopChest plugin) {
        this.plugin = plugin;
        this.econ = plugin.getEconomy();
        this.database = plugin.getShopDatabase();
        this.shopUtils = plugin.getShopUtils();
        this.config = plugin.getShopChestConfig();
        this.worldGuard = plugin.getWorldGuard();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerManipulateArmorStand(PlayerArmorStandManipulateEvent e) {
        // When clicking an armor stand with an armor item, the armor stand will take it.
        // As a hologram consists of armor stands, they would also take the item.
        ArmorStand armorStand = e.getRightClicked();
        if (Hologram.isPartOfHologram(armorStand)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractCreate(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();

        if (config.enable_authme_integration && plugin.hasAuthMe() && !AuthMe.getApi().isAuthenticated(p)) return;

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (ClickType.getPlayerClickType(p) != null) {
                        if (ClickType.getPlayerClickType(p).getClickType() == ClickType.EnumClickType.CREATE) {
                            if (!shopUtils.isShop(b.getLocation())) {

                                boolean externalPluginsAllowed = true;

                                Location[] chestLocations = {b.getLocation(), null};

                                InventoryHolder ih = ((Chest) b.getState()).getInventory().getHolder();
                                if (ih instanceof DoubleChest) {
                                    DoubleChest dc = (DoubleChest) ih;
                                    chestLocations[0] = ((Chest) dc.getLeftSide()).getLocation();
                                    chestLocations[1] = ((Chest) dc.getRightSide()).getLocation();
                                }

                                if (plugin.hasWorldGuard() && config.enable_worldguard_integration) {
                                    RegionContainer container = worldGuard.getRegionContainer();
                                    RegionQuery query = container.createQuery();

                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            externalPluginsAllowed &= query.testState(loc, p, WorldGuardShopFlag.CREATE_SHOP);
                                        }
                                    }
                                }

                                if (plugin.hasTowny() && config.enable_towny_integration) {
                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            TownBlock townBlock = TownyUniverse.getTownBlock(loc);
                                            if (townBlock != null) {
                                                try {
                                                    Town town = townBlock.getTown();
                                                    for (Resident resident : town.getResidents()) {
                                                        if (resident.getName().equals(p.getName())) {
                                                            if (resident.isMayor()) {
                                                                externalPluginsAllowed &= (config.towny_shop_plots_mayor.contains(townBlock.getType().name()));
                                                            } else if (resident.isKing()) {
                                                                externalPluginsAllowed &= (config.towny_shop_plots_king.contains(townBlock.getType().name()));
                                                            } else {
                                                                externalPluginsAllowed &= (config.towny_shop_plots_residents.contains(townBlock.getType().name()));
                                                            }
                                                        }
                                                    }
                                                } catch (Exception ex) {
                                                    plugin.debug(ex);
                                                }
                                            }
                                        }
                                    }
                                }

                                if (plugin.hasPlotSquared() && config.enable_plotsquared_integration) {
                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            com.intellectualcrafters.plot.object.Location plotLocation = new com.intellectualcrafters.plot.object.Location(
                                                    loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

                                            Plot plot = plotLocation.getOwnedPlot();
                                            externalPluginsAllowed &= Utils.isFlagAllowedOnPlot(plot, PlotSquaredShopFlag.CREATE_SHOP, p);
                                        }
                                    }
                                }

                                if (plugin.hasUSkyBlock() && config.enable_uskyblock_integration) {
                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            IslandInfo islandInfo = plugin.getUSkyBlock().getIslandInfo(loc);
                                            externalPluginsAllowed &= islandInfo.getMembers().contains(p.getName()) || islandInfo.getLeader().equals(p.getName());
                                        }
                                    }
                                }

                                if (plugin.hasASkyBlock() && config.enable_askyblock_integration) {
                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            Island island = ASkyBlockAPI.getInstance().getIslandAt(loc);
                                            externalPluginsAllowed &= island.getMembers().contains(p.getUniqueId()) || island.getOwner().equals(p.getUniqueId());
                                        }
                                    }
                                }

                                if ((e.isCancelled() || !externalPluginsAllowed) && !p.hasPermission(Permissions.CREATE_PROTECTED)) {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_CREATE_PROTECTED));
                                    ClickType.removePlayerClickType(p);
                                    plugin.debug(p.getName() + " is not allowed to create a shop on the selected chest");
                                    e.setCancelled(true);
                                    return;
                                }

                                e.setCancelled(true);

                                if (b.getRelative(BlockFace.UP).getType() == Material.AIR) {
                                    ClickType clickType = ClickType.getPlayerClickType(p);
                                    ItemStack product = clickType.getProduct();
                                    double buyPrice = clickType.getBuyPrice();
                                    double sellPrice = clickType.getSellPrice();
                                    ShopType shopType = clickType.getShopType();

                                    create(p, b.getLocation(), product, buyPrice, sellPrice, shopType);
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_BLOCKED));
                                    plugin.debug("Chest is blocked");
                                }
                            } else {
                                e.setCancelled(true);
                                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_ALREADY_SHOP));
                                plugin.debug("Chest is already a shop");
                            }

                            ClickType.removePlayerClickType(p);
                        }
                    }
                }
            }
        }
    }

    private void handleInteractEvent(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        Player p = e.getPlayer();
        boolean inverted = config.invert_mouse_buttons;

        if (Utils.getMajorVersion() >= 9) {
            if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
                if (ClickType.getPlayerClickType(p) != null) {
                    if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                        switch (ClickType.getPlayerClickType(p).getClickType()) {
                            case INFO:
                                e.setCancelled(true);

                                if (shopUtils.isShop(b.getLocation())) {
                                    Shop shop = shopUtils.getShop(b.getLocation());
                                    info(p, shop);
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_NO_SHOP));
                                    plugin.debug("Chest is not a shop");
                                }

                                ClickType.removePlayerClickType(p);
                                break;

                            case REMOVE:
                                e.setCancelled(true);

                                if (shopUtils.isShop(b.getLocation())) {
                                    Shop shop = shopUtils.getShop(b.getLocation());

                                    if (shop.getShopType() == ShopType.ADMIN) {
                                        if (p.hasPermission(Permissions.REMOVE_ADMIN)) {
                                            remove(p, shop);
                                        } else {
                                            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_REMOVE_ADMIN));
                                            plugin.debug(p.getName() + " is not permitted to remove an admin shop");
                                        }
                                    } else {
                                        if (shop.getVendor().getUniqueId().equals(p.getUniqueId()) || p.hasPermission(Permissions.REMOVE_OTHER)) {
                                            remove(p, shop);
                                        } else {
                                            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_REMOVE_OTHERS));
                                            plugin.debug(p.getName() + " is not permitted to remove another player's shop");
                                        }
                                    }
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_NO_SHOP));
                                    plugin.debug("Chest is not a shop");
                                }

                                ClickType.removePlayerClickType(p);
                                break;

                            case OPEN:
                                e.setCancelled(true);

                                if (shopUtils.isShop(b.getLocation())) {
                                    Shop shop = shopUtils.getShop(b.getLocation());
                                    if (p.getUniqueId().equals(shop.getVendor().getUniqueId()) || p.hasPermission(Permissions.OPEN_OTHER)) {
                                        open(p, shop, true);
                                    } else {
                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_OPEN_OTHERS));
                                        plugin.debug(p.getName() + " is not permitted to open another player's shop");
                                    }
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_NO_SHOP));
                                    plugin.debug("Chest is not a shop");
                                }

                                ClickType.removePlayerClickType(p);
                                break;
                        }
                    }
                } else {
                    if (shopUtils.isShop(b.getLocation())) {
                        Shop shop = shopUtils.getShop(b.getLocation());

                        if (e.getAction() == Action.LEFT_CLICK_BLOCK && p.isSneaking() && Utils.hasAxeInHand(p)) {
                            return;
                        }

                        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && p.getUniqueId().equals(shop.getVendor().getUniqueId()) && shop.getShopType() != ShopType.ADMIN) {
                            return;
                        }

                        if ((e.getAction() == Action.RIGHT_CLICK_BLOCK && !inverted) || (e.getAction() == Action.LEFT_CLICK_BLOCK && inverted)) {
                            e.setCancelled(true);

                            if (shop.getShopType() == ShopType.ADMIN || !shop.getVendor().getUniqueId().equals(p.getUniqueId())) {
                                plugin.debug(p.getName() + " wants to buy");

                                if (shop.getBuyPrice() > 0) {
                                    if (p.hasPermission(Permissions.BUY)) {
                                        boolean externalPluginsAllowed = true;

                                        if (plugin.hasPlotSquared() && config.enable_plotsquared_integration) {
                                            com.intellectualcrafters.plot.object.Location plotLocation =
                                                    new com.intellectualcrafters.plot.object.Location(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());

                                            Plot plot = plotLocation.getOwnedPlot();
                                            Flag flag = (shop.getShopType() == Shop.ShopType.ADMIN ? PlotSquaredShopFlag.USE_ADMIN_SHOP : PlotSquaredShopFlag.USE_SHOP);

                                            externalPluginsAllowed &= Utils.isFlagAllowedOnPlot(plot, flag, p);
                                        }

                                        if (plugin.hasWorldGuard() && config.enable_worldguard_integration) {
                                            StateFlag flag = (shop.getShopType() == ShopType.ADMIN ? WorldGuardShopFlag.USE_ADMIN_SHOP : WorldGuardShopFlag.USE_SHOP);
                                            RegionContainer container = worldGuard.getRegionContainer();
                                            RegionQuery query = container.createQuery();
                                            externalPluginsAllowed &= query.testState(b.getLocation(), p, flag);
                                        }

                                        if (shop.getShopType() == ShopType.ADMIN) {
                                            if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                                buy(p, shop, p.isSneaking());
                                            } else {
                                                plugin.debug(p.getName() + " doesn't have external plugin's permission");
                                                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_BUY_HERE));
                                            }
                                        } else {
                                            if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                                Chest c = (Chest) b.getState();
                                                int amount = (p.isSneaking() ? shop.getProduct().getMaxStackSize() : shop.getProduct().getAmount());

                                                if (Utils.getAmount(c.getInventory(), shop.getProduct()) >= amount) {
                                                    buy(p, shop, p.isSneaking());
                                                } else {
                                                    if (config.auto_calculate_item_amount && Utils.getAmount(c.getInventory(), shop.getProduct()) > 0) {
                                                        buy(p, shop, p.isSneaking());
                                                    } else {
                                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.OUT_OF_STOCK));
                                                        if (shop.getVendor().isOnline() && config.enable_vendor_messages) {
                                                            shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.VENDOR_OUT_OF_STOCK,
                                                                    new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(shop.getProduct().getAmount())),
                                                                            new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(shop.getProduct()))));
                                                        }
                                                        plugin.debug("Shop is out of stock");
                                                    }
                                                }
                                            } else {
                                                plugin.debug(p.getName() + " doesn't have external plugin's permission");
                                                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_BUY_HERE));
                                            }
                                        }
                                    } else {
                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_BUY));
                                        plugin.debug(p.getName() + " is not permitted to buy");
                                    }
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUYING_DISABLED));
                                    plugin.debug("Buying is disabled");
                                }
                            }

                        } else if ((e.getAction() == Action.LEFT_CLICK_BLOCK && !inverted) || (e.getAction() == Action.RIGHT_CLICK_BLOCK && inverted)) {
                            e.setCancelled(true);

                            if ((shop.getShopType() == ShopType.ADMIN) || (!shop.getVendor().getUniqueId().equals(p.getUniqueId()))) {
                                plugin.debug(p.getName() + " wants to sell");

                                if (shop.getSellPrice() > 0) {
                                    if (p.hasPermission(Permissions.SELL)) {
                                        boolean externalPluginsAllowed = true;

                                        if (plugin.hasPlotSquared() && config.enable_plotsquared_integration) {
                                            com.intellectualcrafters.plot.object.Location plotLocation =
                                                    new com.intellectualcrafters.plot.object.Location(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());

                                            Plot plot = plotLocation.getOwnedPlot();
                                            Flag flag = (shop.getShopType() == Shop.ShopType.ADMIN ? PlotSquaredShopFlag.USE_ADMIN_SHOP : PlotSquaredShopFlag.USE_SHOP);

                                            externalPluginsAllowed &= Utils.isFlagAllowedOnPlot(plot, flag, p);
                                        }

                                        if (plugin.hasWorldGuard() && config.enable_worldguard_integration) {
                                            RegionContainer container = worldGuard.getRegionContainer();
                                            RegionQuery query = container.createQuery();

                                            StateFlag flag = (shop.getShopType() == ShopType.ADMIN ? WorldGuardShopFlag.USE_ADMIN_SHOP : WorldGuardShopFlag.USE_SHOP);
                                            externalPluginsAllowed &= query.testState(b.getLocation(), p, flag);
                                        }

                                        if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                            boolean stack = p.isSneaking() && !Utils.hasAxeInHand(p);
                                            int amount = stack ? shop.getProduct().getMaxStackSize() : shop.getProduct().getAmount();

                                            if (Utils.getAmount(p.getInventory(), shop.getProduct()) >= amount) {
                                                sell(p, shop, stack);
                                            } else {
                                                if (config.auto_calculate_item_amount && Utils.getAmount(p.getInventory(), shop.getProduct()) > 0) {
                                                    sell(p, shop, stack);
                                                } else {
                                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NOT_ENOUGH_ITEMS));
                                                    plugin.debug(p.getName() + " doesn't have enough items");
                                                }
                                            }
                                        } else {
                                            plugin.debug(p.getName() + " doesn't have external plugin's permission");
                                            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_SELL_HERE));
                                        }
                                    } else {
                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_SELL));
                                        plugin.debug(p.getName() + " is not permitted to sell");
                                    }
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SELLING_DISABLED));
                                    plugin.debug("Selling is disabled");
                                }
                            } else {
                                e.setCancelled(false);
                            }
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (config.enable_authme_integration && plugin.hasAuthMe() && !AuthMe.getApi().isAuthenticated(e.getPlayer())) return;
        handleInteractEvent(e);
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (!plugin.getShopChestConfig().enable_hologram_interaction) return;

        Entity entity = e.getRightClicked();
        Player p = e.getPlayer();
        if (config.enable_authme_integration && plugin.hasAuthMe() && !AuthMe.getApi().isAuthenticated(p)) return;

        if (Utils.getMajorVersion() == 8 || e.getHand() == EquipmentSlot.HAND) {
            if (entity instanceof ArmorStand) {
                ArmorStand armorStand = (ArmorStand) entity;
                if (Hologram.isPartOfHologram(armorStand)) {
                    Hologram hologram = Hologram.getHologram(armorStand);
                    if (hologram != null) {
                        Block b = null;
                        for (Shop shop : plugin.getShopUtils().getShops()) {
                            if (shop.getHologram() != null && shop.getHologram().equals(hologram)) {
                                b = shop.getLocation().getBlock();
                            }
                        }

                        if (b != null) {
                            PlayerInteractEvent interactEvent = new PlayerInteractEvent(p, Action.RIGHT_CLICK_BLOCK, Utils.getPreferredItemInHand(p), b, null);
                            handleInteractEvent(interactEvent);
                        }

                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
        if (!plugin.getShopChestConfig().enable_hologram_interaction) return;

        Entity entity = e.getEntity();
        Entity damager = e.getDamager();

        if (!(damager instanceof Player)) return;
        Player p = (Player) damager;
        if (config.enable_authme_integration && plugin.hasAuthMe() && !AuthMe.getApi().isAuthenticated(p)) return;

        if (entity instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) entity;
            if (Hologram.isPartOfHologram(armorStand)) {
                Hologram hologram = Hologram.getHologram(armorStand);
                if (hologram != null) {
                    Block b = null;
                    for (Shop shop : plugin.getShopUtils().getShops()) {
                        if (shop.getHologram() != null && shop.getHologram().equals(hologram)) {
                            b = shop.getLocation().getBlock();
                        }
                    }

                    if (b != null) {
                        PlayerInteractEvent interactEvent = new PlayerInteractEvent(p, Action.LEFT_CLICK_BLOCK, Utils.getPreferredItemInHand(p), b, null);
                        handleInteractEvent(interactEvent);
                        e.setCancelled(true);
                    }

                }
            }
        }
    }

    /**
     * Create a new shop
     *
     * @param executor  Player, who executed the command, will receive the message and become the vendor of the shop
     * @param location  Where the shop will be located
     * @param product   Product of the Shop
     * @param buyPrice  Buy price
     * @param sellPrice Sell price
     * @param shopType  Type of the shop
     */
    private void create(final Player executor, final Location location, final ItemStack product, final double buyPrice, final double sellPrice, final ShopType shopType) {
        plugin.debug(executor.getName() + " is creating new shop...");

        double creationPrice = (shopType == ShopType.NORMAL) ? config.shop_creation_price_normal : config.shop_creation_price_admin;
        Shop shop = new Shop(plugin, executor, product, location, buyPrice, sellPrice, shopType);

        ShopCreateEvent event = new ShopCreateEvent(executor, shop, creationPrice);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            plugin.debug("Create event cancelled");
            return;
        }

        EconomyResponse r = plugin.getEconomy().withdrawPlayer(executor, location.getWorld().getName(), creationPrice);
        if (!r.transactionSuccess()) {
            plugin.debug("Economy transaction failed: " + r.errorMessage);
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedRegex(Regex.ERROR, r.errorMessage)));
            return;
        }

        shop.create(true);

        plugin.debug("Shop created");
        shopUtils.addShop(shop, true);

        executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_CREATED));

        for (Player p : location.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(location) <= Math.pow(config.maximal_distance, 2)) {
                if (shop.getHologram() != null) {
                    shop.getHologram().showPlayer(p);
                }
            }
            if (p.getLocation().distanceSquared(location) <= Math.pow(config.maximal_item_distance, 2)) {
                if (shop.getItem() != null) {
                    shop.getItem().setVisible(p, true);
                }
            }
        }
    }

    /**
     * Remove a shop
     * @param executor Player, who executed the command and will receive the message
     * @param shop Shop to be removed
     */
    private void remove(Player executor, Shop shop) {
        plugin.debug(executor.getName() + " is removing " + shop.getVendor().getName() + "'s shop (#" + shop.getID() + ")");
        ShopRemoveEvent event = new ShopRemoveEvent(executor, shop);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Remove event cancelled (#" + shop.getID() + ")");
            return;
        }

        shopUtils.removeShop(shop, true);
        plugin.debug("Removed shop (#" + shop.getID() + ")");
        executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_REMOVED));
    }

    /**
     * Open a shop
     * @param executor Player, who executed the command and will receive the message
     * @param shop Shop to be opened
     * @param message Whether the player should receive the {@link LocalizedMessage.Message#OPENED_SHOP} message
     */
    private void open(Player executor, Shop shop, boolean message) {
        plugin.debug(executor.getName() + " is opening " + shop.getVendor().getName() + "'s shop (#" + shop.getID() + ")");
        ShopOpenEvent event = new ShopOpenEvent(executor, shop);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Open event cancelled (#" + shop.getID() + ")");
            return;
        }

        executor.openInventory(shop.getInventoryHolder().getInventory());
        plugin.debug("Opened shop (#" + shop.getID() + ")");
        if (message) executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.OPENED_SHOP, new LocalizedMessage.ReplacedRegex(Regex.VENDOR, shop.getVendor().getName())));
    }

    /**
     *
     * @param executor Player, who executed the command and will retrieve the information
     * @param shop Shop from which the information will be retrieved
     */
    private void info(Player executor, Shop shop) {
        plugin.debug(executor.getName() + " is retrieving shop info (#" + shop.getID() + ")");
        ShopInfoEvent event = new ShopInfoEvent(executor, shop);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            plugin.debug("Info event cancelled (#" + shop.getID() + ")");
            return;
        }

        Chest c = (Chest) shop.getLocation().getBlock().getState();

        int amount = Utils.getAmount(c.getInventory(), shop.getProduct());
        Material type = shop.getProduct().getType();

        String vendorName = (shop.getVendor().getName() == null ?
                shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());

        String vendorString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_VENDOR,
                new LocalizedMessage.ReplacedRegex(Regex.VENDOR, vendorName));

        String productString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_PRODUCT,
                new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(shop.getProduct().getAmount())),
                new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(shop.getProduct())));

        String enchantmentString = "";
        String potionEffectString = "";
        String bookGenerationString = "";
        String musicDiscTitleString = "";

        String disabled = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_DISABLED);

        String priceString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_PRICE,
                new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, (shop.getBuyPrice() > 0 ? String.valueOf(shop.getBuyPrice()) : disabled)),
                new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, (shop.getSellPrice() > 0 ? String.valueOf(shop.getSellPrice()) : disabled)));

        String shopType = LanguageUtils.getMessage(shop.getShopType() == ShopType.NORMAL ?
                LocalizedMessage.Message.SHOP_INFO_NORMAL : LocalizedMessage.Message.SHOP_INFO_ADMIN);

        String stock = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_STOCK,
                new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(amount)));

        boolean potionExtended = false;

        Map<Enchantment, Integer> enchantmentMap;

        String potionEffectName = "";
        if (Utils.getMajorVersion() >= 9) {
            if (type == Material.TIPPED_ARROW || type == Material.LINGERING_POTION || type == Material.SPLASH_POTION) {
                potionEffectName = LanguageUtils.getPotionEffectName(shop.getProduct());
                PotionMeta potionMeta = (PotionMeta) shop.getProduct().getItemMeta();
                potionExtended = potionMeta.getBasePotionData().isExtended();

                if (potionEffectName.trim().isEmpty())
                    potionEffectName = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_NONE);
            }
        }

        if (type == Material.POTION) {
            potionEffectName = LanguageUtils.getPotionEffectName(shop.getProduct());
            if (Utils.getMajorVersion() < 9) {
                Potion potion = Potion.fromItemStack(shop.getProduct());
                potionExtended = potion.hasExtendedDuration();
            } else {
                PotionMeta potionMeta = (PotionMeta) shop.getProduct().getItemMeta();
                potionExtended = potionMeta.getBasePotionData().isExtended();
            }

            if (potionEffectName.trim().isEmpty())
                potionEffectName = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_NONE);
        }

        if (potionEffectName.length() > 0) {
            String extended = potionExtended ? LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_EXTENDED) : "";
            potionEffectString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_POTION_EFFECT,
                    new LocalizedMessage.ReplacedRegex(Regex.POTION_EFFECT, potionEffectName),
                    new LocalizedMessage.ReplacedRegex(Regex.EXTENDED, extended));
        }

        if (type == Material.WRITTEN_BOOK) {
            BookMeta meta = (BookMeta) shop.getProduct().getItemMeta();
            CustomBookMeta.Generation generation = CustomBookMeta.Generation.TATTERED;

            if ((Utils.getMajorVersion() == 9 && Utils.getRevision() == 1) || Utils.getMajorVersion() == 8) {
                CustomBookMeta.Generation gen = CustomBookMeta.getGeneration(shop.getProduct());
                generation = (gen == null ? CustomBookMeta.Generation.ORIGINAL : gen);
            } else if (Utils.getMajorVersion() >= 10) {
                if (meta.hasGeneration()) {
                    generation = CustomBookMeta.Generation.valueOf(meta.getGeneration().toString());
                } else {
                    generation = CustomBookMeta.Generation.ORIGINAL;
                }
            }

            bookGenerationString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_BOOK_GENERATION,
                    new LocalizedMessage.ReplacedRegex(Regex.GENERATION, LanguageUtils.getBookGenerationName(generation)));
        }

        String musicDiscName = LanguageUtils.getMusicDiscName(type);
        if (musicDiscName.length() > 0) {
            musicDiscTitleString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_MUSIC_TITLE,
                    new LocalizedMessage.ReplacedRegex(Regex.MUSIC_TITLE, musicDiscName));
        }

        String enchantmentList = "";
        if (shop.getProduct().getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) shop.getProduct().getItemMeta();
            enchantmentMap = esm.getStoredEnchants();
        } else {
            enchantmentMap = shop.getProduct().getEnchantments();
        }

        Enchantment[] enchantments = enchantmentMap.keySet().toArray(new Enchantment[enchantmentMap.size()]);

        for (int i = 0; i < enchantments.length; i++) {
            Enchantment enchantment = enchantments[i];

            if (i == enchantments.length - 1) {
                enchantmentList += LanguageUtils.getEnchantmentName(enchantment, enchantmentMap.get(enchantment));
            } else {
                enchantmentList += LanguageUtils.getEnchantmentName(enchantment, enchantmentMap.get(enchantment)) + ", ";
            }
        }

        if (enchantmentList.length() > 0) {
            enchantmentString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_ENCHANTMENTS,
                    new LocalizedMessage.ReplacedRegex(Regex.ENCHANTMENT, enchantmentList));
        }

        executor.sendMessage(" ");
        if (shop.getShopType() != ShopType.ADMIN) executor.sendMessage(vendorString);
        executor.sendMessage(productString);
        if (shop.getShopType() != ShopType.ADMIN) executor.sendMessage(stock);
        if (enchantmentString.length() > 0) executor.sendMessage(enchantmentString);
        if (potionEffectString.length() > 0) executor.sendMessage(potionEffectString);
        if (musicDiscTitleString.length() > 0) executor.sendMessage(musicDiscTitleString);
        if (bookGenerationString.length() > 0) executor.sendMessage(bookGenerationString);
        executor.sendMessage(priceString);
        executor.sendMessage(shopType);
        executor.sendMessage(" ");
    }

    /**
     * A player buys from a shop
     * @param executor Player, who executed the command and will buy the product
     * @param shop Shop, from which the player buys
     * @param stack Whether a whole stack should be bought
     */
    private void buy(Player executor, Shop shop, boolean stack) {
        plugin.debug(executor.getName() + " is buying (#" + shop.getID() + ")");

        int amount = shop.getProduct().getAmount();
        if (stack) amount = shop.getProduct().getMaxStackSize();

        double price = shop.getBuyPrice();
        if (stack) price = (price / shop.getProduct().getAmount()) * amount;

        if (econ.getBalance(executor) >= price || config.auto_calculate_item_amount) {

            int amountForMoney = (int) (amount / price * econ.getBalance(executor));

            if (amountForMoney == 0 && config.auto_calculate_item_amount) {
                executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NOT_ENOUGH_MONEY));
                return;
            }

            plugin.debug(executor.getName() + " has enough money for " + amountForMoney + " item(s) (#" + shop.getID() + ")");

            Block b = shop.getLocation().getBlock();
            Chest c = (Chest) b.getState();

            int amountForChestItems = Utils.getAmount(c.getInventory(), shop.getProduct());

            if (amountForChestItems == 0 && shop.getShopType() != ShopType.ADMIN) {
                executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.OUT_OF_STOCK));
                return;
            }

            ItemStack product = new ItemStack(shop.getProduct());
            if (stack) product.setAmount(amount);

            Inventory inventory = executor.getInventory();

            int freeSpace = Utils.getFreeSpaceForItem(inventory, product);

            if (freeSpace == 0) {
                executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NOT_ENOUGH_INVENTORY_SPACE));
                return;
            }

            int newAmount = amount;

            if (config.auto_calculate_item_amount) {
                if (shop.getShopType() == ShopType.ADMIN)
                    newAmount = Math.min(amountForMoney, freeSpace);
                else
                    newAmount = Math.min(Math.min(amountForMoney, amountForChestItems), freeSpace);
            }

            if (newAmount > amount) newAmount = amount;

            double newPrice = (price / amount) * newAmount;

            if (freeSpace >= newAmount) {
                plugin.debug(executor.getName() + " has enough inventory space for " + freeSpace + " items (#" + shop.getID() + ")");

                ItemStack newProduct = new ItemStack(product);
                newProduct.setAmount(newAmount);

                String worldName = shop.getLocation().getWorld().getName();

                EconomyResponse r = econ.withdrawPlayer(executor, worldName, newPrice);

                if (r.transactionSuccess()) {
                    EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.depositPlayer(shop.getVendor(), worldName, newPrice) : null;

                    if (r2 != null) {
                        if (r2.transactionSuccess()) {
                            ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.BUY, newAmount, newPrice);
                            Bukkit.getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                econ.depositPlayer(executor, newPrice);
                                econ.withdrawPlayer(shop.getVendor(), newPrice);
                                plugin.debug("Buy event cancelled (#" + shop.getID() + ")");
                                return;
                            }

                            database.logEconomy(executor, newProduct, shop.getVendor(), shop.getShopType(), shop.getLocation(), newPrice, ShopBuySellEvent.Type.BUY, null);

                            addToInventory(inventory, newProduct);
                            removeFromInventory(c.getInventory(), newProduct);
                            executor.updateInventory();

                            String vendorName = (shop.getVendor().getName() == null ? shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_SUCCESS, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(newAmount)),
                                    new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, String.valueOf(newPrice)),
                                    new LocalizedMessage.ReplacedRegex(Regex.VENDOR, vendorName)));

                            plugin.debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");

                            if (shop.getVendor().isOnline() && config.enable_vendor_messages) {
                                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SOMEONE_BOUGHT, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(newAmount)),
                                        new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, String.valueOf(newPrice)),
                                        new LocalizedMessage.ReplacedRegex(Regex.PLAYER, executor.getName())));
                            }

                        } else {
                            plugin.debug("Economy transaction failed (r2): " + r2.errorMessage + " (#" + shop.getID() + ")");
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedRegex(Regex.ERROR, r2.errorMessage)));
                            econ.depositPlayer(executor, newPrice);
                        }
                    } else {
                        ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.BUY, newAmount, newPrice);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            econ.depositPlayer(executor, newPrice);
                            plugin.debug("Buy event cancelled (#" + shop.getID() + ")");
                            return;
                        }

                        database.logEconomy(executor, newProduct, shop.getVendor(), shop.getShopType(), shop.getLocation(), newPrice, ShopBuySellEvent.Type.BUY, null);

                        addToInventory(inventory, newProduct);
                        executor.updateInventory();
                        executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_SUCESS_ADMIN, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(newAmount)),
                                new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, String.valueOf(newPrice))));

                        plugin.debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");
                    }
                } else {
                    plugin.debug("Economy transaction failed (r): " + r.errorMessage + " (#" + shop.getID() + ")");
                    executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedRegex(Regex.ERROR, r.errorMessage)));
                }
            } else {
                executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NOT_ENOUGH_INVENTORY_SPACE));
            }
        } else {
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NOT_ENOUGH_MONEY));
        }
    }

    /**
     * A player sells to a shop
     * @param executor Player, who executed the command and will sell the product
     * @param shop Shop, to which the player sells
     */
    private void sell(Player executor, Shop shop, boolean stack) {
        plugin.debug(executor.getName() + " is selling (#" + shop.getID() + ")");

        int amount = shop.getProduct().getAmount();
        if (stack) amount = shop.getProduct().getMaxStackSize();

        double price = shop.getSellPrice();
        if (stack) price = (price / shop.getProduct().getAmount()) * amount;

        if (econ.getBalance(shop.getVendor()) >= price || shop.getShopType() == ShopType.ADMIN || config.auto_calculate_item_amount) {
            int amountForMoney = (int) (amount / price * econ.getBalance(shop.getVendor()));

            plugin.debug("Vendor has enough money for " + amountForMoney + " item(s) (#" + shop.getID() + ")");

            if (amountForMoney == 0 && config.auto_calculate_item_amount && shop.getShopType() != ShopType.ADMIN) {
                executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.VENDOR_NOT_ENOUGH_MONEY));
                return;
            }

            Block block = shop.getLocation().getBlock();
            Chest chest = (Chest) block.getState();

            int amountForItemCount = Utils.getAmount(executor.getInventory(), shop.getProduct());

            if (amountForItemCount == 0) {
                executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NOT_ENOUGH_ITEMS));
                return;
            }

            ItemStack product = new ItemStack(shop.getProduct());
            if (stack) product.setAmount(amount);

            Inventory inventory = chest.getInventory();

            int freeSpace = Utils.getFreeSpaceForItem(inventory, product);

            if (freeSpace == 0 && shop.getShopType() != ShopType.ADMIN) {
                executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
                return;
            }

            int newAmount = amount;

            if (config.auto_calculate_item_amount) {
                if (shop.getShopType() == ShopType.ADMIN)
                    newAmount = amountForItemCount;
                else
                    newAmount = Math.min(Math.min(amountForMoney, amountForItemCount), freeSpace);
            }

            if (newAmount > amount) newAmount = amount;

            double newPrice = (price / amount) * newAmount;

            if (freeSpace >= newAmount || shop.getShopType() == ShopType.ADMIN) {
                plugin.debug("Chest has enough inventory space for " + freeSpace + " items (#" + shop.getID() + ")");

                ItemStack newProduct = new ItemStack(product);
                newProduct.setAmount(newAmount);

                String worldName = shop.getLocation().getWorld().getName();

                EconomyResponse r = econ.depositPlayer(executor, worldName, newPrice);

                if (r.transactionSuccess()) {
                    EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.withdrawPlayer(shop.getVendor(), worldName, newPrice) : null;

                    if (r2 != null) {
                        if (r2.transactionSuccess()) {
                            ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.SELL, newAmount, newPrice);
                            Bukkit.getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                econ.withdrawPlayer(executor, newPrice);
                                econ.depositPlayer(shop.getVendor(), newPrice);
                                plugin.debug("Sell event cancelled (#" + shop.getID() + ")");
                                return;
                            }

                            database.logEconomy(executor, newProduct, shop.getVendor(), shop.getShopType(), shop.getLocation(), newPrice, ShopBuySellEvent.Type.SELL, null);

                            addToInventory(inventory, newProduct);
                            removeFromInventory(executor.getInventory(), newProduct);
                            executor.updateInventory();

                            String vendorName = (shop.getVendor().getName() == null ? shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SELL_SUCESS, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(newAmount)),
                                    new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, String.valueOf(newPrice)),
                                    new LocalizedMessage.ReplacedRegex(Regex.VENDOR, vendorName)));

                            plugin.debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");

                            if (shop.getVendor().isOnline() && config.enable_vendor_messages) {
                                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SOMEONE_SOLD, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(newAmount)),
                                        new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, String.valueOf(newPrice)),
                                        new LocalizedMessage.ReplacedRegex(Regex.PLAYER, executor.getName())));
                            }

                        } else {
                            plugin.debug("Economy transaction failed (r2): " + r2.errorMessage + " (#" + shop.getID() + ")");
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedRegex(Regex.ERROR, r2.errorMessage)));
                            econ.withdrawPlayer(executor, newPrice);
                        }

                    } else {
                        ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.SELL, newAmount, newPrice);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            econ.withdrawPlayer(executor, newPrice);
                            plugin.debug("Sell event cancelled (#" + shop.getID() + ")");
                            return;
                        }

                        database.logEconomy(executor, newProduct, shop.getVendor(), shop.getShopType(), shop.getLocation(), newPrice, ShopBuySellEvent.Type.SELL, null);

                        removeFromInventory(executor.getInventory(), newProduct);
                        executor.updateInventory();
                        executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SELL_SUCESS_ADMIN, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(newAmount)),
                                new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, String.valueOf(newPrice))));

                        plugin.debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");
                    }

                } else {
                    plugin.debug("Economy transaction failed (r): " + r.errorMessage + " (#" + shop.getID() + ")");
                    executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedRegex(Regex.ERROR, r.errorMessage)));
                }

            } else {
                executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
            }

        } else {
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.VENDOR_NOT_ENOUGH_MONEY));
        }
    }

    /**
     * Adds items to an inventory
     * @param inventory The inventory, to which the items will be added
     * @param itemStack Items to add
     * @return Whether all items were added to the inventory
     */
    private boolean addToInventory(Inventory inventory, ItemStack itemStack) {
        plugin.debug("Adding items to inventory...");

        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        int amount = itemStack.getAmount();
        int added = 0;

        if (inventory instanceof PlayerInventory) {
            if (Utils.getMajorVersion() >= 9) {
                inventoryItems.put(40, inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }
        }

        slotLoop:
        for (int slot : inventoryItems.keySet()) {
            while (added < amount) {
                ItemStack item = inventory.getItem(slot);

                if (item != null && item.getType() != Material.AIR) {
                    if (Utils.isItemSimilar(item, itemStack)) {
                        if (item.getAmount() != item.getMaxStackSize()) {
                            ItemStack newItemStack = new ItemStack(item);
                            newItemStack.setAmount(item.getAmount() + 1);
                            inventory.setItem(slot, newItemStack);
                            added++;
                        } else {
                            continue slotLoop;
                        }
                    } else {
                        continue slotLoop;
                    }
                } else {
                    ItemStack newItemStack = new ItemStack(itemStack);
                    newItemStack.setAmount(1);
                    inventory.setItem(slot, newItemStack);
                    added++;
                }
            }
        }

        return (added == amount);
    }

    /**
     * Removes items to from an inventory
     * @param inventory The inventory, from which the items will be removed
     * @param itemStack Items to remove
     * @return Whether all items were removed from the inventory
     */
    private boolean removeFromInventory(Inventory inventory, ItemStack itemStack) {
        plugin.debug("Removing items from inventory...");

        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        int amount = itemStack.getAmount();
        int removed = 0;

        if (inventory instanceof PlayerInventory) {
            if (Utils.getMajorVersion() >= 9) {
                inventoryItems.put(40, inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }
        }

        slotLoop:
        for (int slot : inventoryItems.keySet()) {
            while (removed < amount) {
                ItemStack item = inventory.getItem(slot);

                if (item != null && item.getType() != Material.AIR) {
                    if (Utils.isItemSimilar(item, itemStack)) {
                        if (item.getAmount() > 0) {
                            int newAmount = item.getAmount() - 1;

                            ItemStack newItemStack = new ItemStack(item);
                            newItemStack.setAmount(newAmount);

                            if (newAmount == 0)
                                inventory.setItem(slot, null);
                            else
                                inventory.setItem(slot, newItemStack);

                            removed++;
                        } else {
                            continue slotLoop;
                        }
                    } else {
                        continue slotLoop;
                    }
                } else {
                    continue slotLoop;
                }

            }
        }

        return (removed == amount);
    }

}
