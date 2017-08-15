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
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.event.ShopBuySellEvent;
import de.epiceric.shopchest.event.ShopCreateEvent;
import de.epiceric.shopchest.event.ShopInfoEvent;
import de.epiceric.shopchest.event.ShopOpenEvent;
import de.epiceric.shopchest.event.ShopRemoveEvent;
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
import de.epiceric.shopchest.utils.ItemUtils;
import de.epiceric.shopchest.utils.Permissions;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.Utils;
import fr.xephi.authme.AuthMe;
import me.ryanhamshire.GriefPrevention.Claim;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;
import pl.islandworld.api.IslandWorldApi;
import us.talabrek.ultimateskyblock.api.IslandInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!plugin.getHologramFormat().isDynamic()) return;

        Inventory chestInv = e.getInventory();

        if (!(chestInv.getHolder() instanceof Chest || chestInv.getHolder() instanceof DoubleChest)) {
            return;
        }

        Location loc = null;
        if (chestInv.getHolder() instanceof Chest) {
            loc = ((Chest) chestInv.getHolder()).getLocation();
        } else if (chestInv.getHolder() instanceof DoubleChest) {
            loc = ((DoubleChest) chestInv.getHolder()).getLocation();
        }

        final Shop shop = plugin.getShopUtils().getShop(loc);
        if (shop == null) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                shop.updateHologramText();
            }
        }.runTaskLater(plugin, 1L);
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

                                String denyReason = "Event Cancelled";

                                if (plugin.hasWorldGuard() && config.enable_worldguard_integration) {
                                    plugin.debug("Checking if WorldGuard allows shop creation...");
                                    RegionContainer container = worldGuard.getRegionContainer();
                                    RegionQuery query = container.createQuery();

                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            externalPluginsAllowed &= query.testState(loc, p, WorldGuardShopFlag.CREATE_SHOP);
                                        }
                                    }

                                    if (!externalPluginsAllowed) denyReason = "WorldGuard";
                                }

                                if (externalPluginsAllowed && plugin.hasTowny() && config.enable_towny_integration) {
                                    plugin.debug("Checking if Towny allows shop creation...");
                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            TownBlock townBlock = TownyUniverse.getTownBlock(loc);
                                            if (townBlock != null) {
                                                plugin.debug("Plot Type is " + townBlock.getType().name());
                                                try {
                                                    Town town = townBlock.getTown();
                                                    boolean residentFound = false;
                                                    for (Resident resident : town.getResidents()) {
                                                        if (resident.getName().equals(p.getName())) {
                                                            residentFound = true;
                                                            if (resident.isMayor()) {
                                                                plugin.debug(p.getName() + " is mayor of town");
                                                                externalPluginsAllowed &= (config.towny_shop_plots_mayor.contains(townBlock.getType().name()));
                                                            } else if (resident.isKing()) {
                                                                plugin.debug(p.getName() + " is king of town");
                                                                externalPluginsAllowed &= (config.towny_shop_plots_king.contains(townBlock.getType().name()));
                                                            } else {
                                                                plugin.debug(p.getName() + " is resident in town");
                                                                externalPluginsAllowed &= (config.towny_shop_plots_residents.contains(townBlock.getType().name()));
                                                            }
                                                            break;
                                                        }
                                                    }
                                                    if (!residentFound) {
                                                        plugin.debug(p.getName() + " is not resident in town");
                                                        externalPluginsAllowed = false;
                                                    }
                                                } catch (Exception ex) {
                                                    plugin.debug(ex);
                                                }
                                            }
                                        }
                                    }

                                    if (!externalPluginsAllowed) denyReason = "Towny";
                                }

                                if (externalPluginsAllowed && plugin.hasPlotSquared() && config.enable_plotsquared_integration) {
                                    plugin.debug("Checking if PlotSquared allows shop creation...");
                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            com.intellectualcrafters.plot.object.Location plotLocation = new com.intellectualcrafters.plot.object.Location(
                                                    loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

                                            Plot plot = plotLocation.getOwnedPlot();
                                            externalPluginsAllowed &= Utils.isFlagAllowedOnPlot(plot, PlotSquaredShopFlag.CREATE_SHOP, p);
                                        }
                                    }

                                    if (!externalPluginsAllowed) denyReason = "PlotSquared";
                                }

                                if (externalPluginsAllowed && plugin.hasUSkyBlock() && config.enable_uskyblock_integration) {
                                    plugin.debug("Checking if uSkyBlock allows shop creation...");
                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            IslandInfo islandInfo = plugin.getUSkyBlock().getIslandInfo(loc);
                                            if (islandInfo != null) {
                                                plugin.debug("Chest is on island of " + islandInfo.getLeader());
                                                externalPluginsAllowed &= islandInfo.getMembers().contains(p.getName()) || islandInfo.getLeader().equals(p.getName());
                                            }
                                        }
                                    }

                                    if (!externalPluginsAllowed) denyReason = "uSkyBlock";
                                }

                                if (externalPluginsAllowed && plugin.hasASkyBlock() && config.enable_askyblock_integration) {
                                    plugin.debug("Checking if ASkyBlock allows shop creation...");
                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            Island island = ASkyBlockAPI.getInstance().getIslandAt(loc);
                                            if (island != null) {
                                                if (island.getOwner() == null) {
                                                    plugin.debug("Chest is on an unowned island.");
                                                    externalPluginsAllowed &= island.getMembers().contains(p.getUniqueId());
                                                } else {
                                                    plugin.debug("Chest is on island of " + Bukkit.getOfflinePlayer(island.getOwner()).getName());
                                                    externalPluginsAllowed &= island.getMembers().contains(p.getUniqueId()) || island.getOwner().equals(p.getUniqueId());
                                                }
                                            }
                                        }
                                    }

                                    if (!externalPluginsAllowed) denyReason = "ASkyBlock";
                                }

                                if (externalPluginsAllowed && plugin.hasIslandWorld() && config.enable_islandworld_integration && IslandWorldApi.isInitialized()) {
                                    plugin.debug("Checking if IslandWorld allows shop creation...");
                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            if (loc.getWorld().getName().equals(IslandWorldApi.getIslandWorld().getName())) {
                                                plugin.debug("Chest is in island world");
                                                externalPluginsAllowed &= IslandWorldApi.canBuildOnLocation(p, loc, true);
                                            }
                                        }
                                    }

                                    if (!externalPluginsAllowed) denyReason = "IslandWorld";
                                }

                                if (externalPluginsAllowed && plugin.hasGriefPrevention() && config.enable_griefprevention_integration) {
                                    plugin.debug("Checking if GriefPrevention allows shop creation...");
                                    String gpDenyReason = null;
                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            Claim claim = plugin.getGriefPrevention().dataStore.getClaimAt(loc, false, null);
                                            if (claim != null) {
                                                plugin.debug("Checking if claim allows container access");
                                                gpDenyReason = claim.allowContainers(p);
                                                externalPluginsAllowed &= gpDenyReason == null;
                                            }
                                        }
                                    }

                                    if (!externalPluginsAllowed) denyReason = "GriefPrevention (" + gpDenyReason + ")";
                                }

                                if ((e.isCancelled() || !externalPluginsAllowed) && !p.hasPermission(Permissions.CREATE_PROTECTED)) {
                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_CREATE_PROTECTED));
                                    ClickType.removePlayerClickType(p);
                                    plugin.debug(p.getName() + " is not allowed to create a shop on the selected chest because " + denyReason);
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

    private Map<UUID, Set<Integer>> needsConfirmation = new HashMap<>();

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
                        Shop shop = shopUtils.getShop(b.getLocation());
                        if (shop != null || ClickType.getPlayerClickType(p).getClickType() == ClickType.EnumClickType.CREATE) {
                            switch (ClickType.getPlayerClickType(p).getClickType()) {
                                case INFO:
                                    e.setCancelled(true);

                                    info(p, shop);

                                    ClickType.removePlayerClickType(p);
                                    break;

                                case REMOVE:
                                    e.setCancelled(true);

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

                                    ClickType.removePlayerClickType(p);
                                    break;

                                case OPEN:
                                    e.setCancelled(true);

                                    if (p.getUniqueId().equals(shop.getVendor().getUniqueId()) || p.hasPermission(Permissions.OPEN_OTHER)) {
                                        open(p, shop, true);
                                    } else {
                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_OPEN_OTHERS));
                                        plugin.debug(p.getName() + " is not permitted to open another player's shop");
                                    }

                                    ClickType.removePlayerClickType(p);
                                    break;
                            }
                        } else {
                            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CHEST_NO_SHOP));
                            plugin.debug("Chest is not a shop");
                        }
                    }
                } else {
                    Shop shop = shopUtils.getShop(b.getLocation());

                    boolean confirmed = needsConfirmation.containsKey(p.getUniqueId()) && needsConfirmation.get(p.getUniqueId()).contains(shop.getID());

                    if (shop != null) {
                        if (e.getAction() == Action.LEFT_CLICK_BLOCK && p.isSneaking() && Utils.hasAxeInHand(p)) {
                            return;
                        }

                        ItemStack infoItem = config.shop_info_item;
                        if (infoItem != null) {
                            if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                                ItemStack item = Utils.getItemInMainHand(p);

                                if (item == null || !(infoItem.getType() == item.getType() && infoItem.getDurability() == item.getDurability())) {
                                    item = Utils.getItemInOffHand(p);

                                    if (item != null && infoItem.getType() == item.getType() && infoItem.getDurability() == item.getDurability()) {
                                        e.setCancelled(true);
                                        info(p, shop);
                                        return;
                                    }
                                } else {
                                    e.setCancelled(true);
                                    info(p, shop);
                                    return;
                                }
                            }
                        }

                        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && p.getUniqueId().equals(shop.getVendor().getUniqueId()) && shop.getShopType() != ShopType.ADMIN) {
                            return;
                        }

                        if (p.getGameMode() == GameMode.CREATIVE) {
                            e.setCancelled(true);
                            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.USE_IN_CREATIVE));
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

                                            externalPluginsAllowed = Utils.isFlagAllowedOnPlot(plot, flag, p);
                                        }

                                        if (externalPluginsAllowed && plugin.hasWorldGuard() && config.enable_worldguard_integration) {
                                            StateFlag flag = (shop.getShopType() == ShopType.ADMIN ? WorldGuardShopFlag.USE_ADMIN_SHOP : WorldGuardShopFlag.USE_SHOP);
                                            RegionContainer container = worldGuard.getRegionContainer();
                                            RegionQuery query = container.createQuery();
                                            externalPluginsAllowed = query.testState(b.getLocation(), p, flag);
                                        }

                                        if (shop.getShopType() == ShopType.ADMIN) {
                                            if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                                if (confirmed || !config.confirm_shopping) {
                                                    buy(p, shop, p.isSneaking());
                                                    if (config.confirm_shopping) {
                                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                        ids.remove(shop.getID());
                                                        if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                        else needsConfirmation.put(p.getUniqueId(), ids);
                                                    }
                                                } else {
                                                    plugin.debug("Needs confirmation");
                                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_TO_CONFIRM));
                                                    Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                    ids.add(shop.getID());
                                                    needsConfirmation.put(p.getUniqueId(), ids);
                                                }
                                            } else {
                                                plugin.debug(p.getName() + " doesn't have external plugin's permission");
                                                p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.NO_PERMISSION_BUY_HERE));
                                            }
                                        } else {
                                            if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                                Chest c = (Chest) b.getState();
                                                int amount = (p.isSneaking() ? shop.getProduct().getMaxStackSize() : shop.getProduct().getAmount());

                                                if (Utils.getAmount(c.getInventory(), shop.getProduct()) >= amount) {
                                                    if (confirmed || !config.confirm_shopping) {
                                                        buy(p, shop, p.isSneaking());
                                                        if (config.confirm_shopping) {
                                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                            ids.remove(shop.getID());
                                                            if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                            else needsConfirmation.put(p.getUniqueId(), ids);
                                                        }
                                                    } else {
                                                        plugin.debug("Needs confirmation");
                                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_TO_CONFIRM));
                                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                        ids.add(shop.getID());
                                                        needsConfirmation.put(p.getUniqueId(), ids);
                                                    }
                                                } else {
                                                    if (config.auto_calculate_item_amount && Utils.getAmount(c.getInventory(), shop.getProduct()) > 0) {
                                                        if (confirmed || !config.confirm_shopping) {
                                                            buy(p, shop, p.isSneaking());
                                                            if (config.confirm_shopping) {
                                                                Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                                ids.remove(shop.getID());
                                                                if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                                else needsConfirmation.put(p.getUniqueId(), ids);
                                                            }
                                                        } else {
                                                            plugin.debug("Needs confirmation");
                                                            p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_TO_CONFIRM));
                                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                            ids.add(shop.getID());
                                                            needsConfirmation.put(p.getUniqueId(), ids);
                                                        }
                                                    } else {
                                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.OUT_OF_STOCK));
                                                        if (shop.getVendor().isOnline() && config.enable_vendor_messages) {
                                                            shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.VENDOR_OUT_OF_STOCK,
                                                                    new LocalizedMessage.ReplacedPlaceholder(Placeholder.AMOUNT, String.valueOf(shop.getProduct().getAmount())),
                                                                            new LocalizedMessage.ReplacedPlaceholder(Placeholder.ITEM_NAME, LanguageUtils.getItemName(shop.getProduct()))));
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

                                            externalPluginsAllowed = Utils.isFlagAllowedOnPlot(plot, flag, p);
                                        }

                                        if (externalPluginsAllowed && plugin.hasWorldGuard() && config.enable_worldguard_integration) {
                                            RegionContainer container = worldGuard.getRegionContainer();
                                            RegionQuery query = container.createQuery();

                                            StateFlag flag = (shop.getShopType() == ShopType.ADMIN ? WorldGuardShopFlag.USE_ADMIN_SHOP : WorldGuardShopFlag.USE_SHOP);
                                            externalPluginsAllowed = query.testState(b.getLocation(), p, flag);
                                        }

                                        if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                            boolean stack = p.isSneaking() && !Utils.hasAxeInHand(p);
                                            int amount = stack ? shop.getProduct().getMaxStackSize() : shop.getProduct().getAmount();

                                            if (Utils.getAmount(p.getInventory(), shop.getProduct()) >= amount) {
                                                if (confirmed || !config.confirm_shopping) {
                                                    sell(p, shop, stack);
                                                    if (config.confirm_shopping) {
                                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                        ids.remove(shop.getID());
                                                        if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                        else needsConfirmation.put(p.getUniqueId(), ids);
                                                    }
                                                } else {
                                                    plugin.debug("Needs confirmation");
                                                    p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_TO_CONFIRM));
                                                    Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                    ids.add(shop.getID());
                                                    needsConfirmation.put(p.getUniqueId(), ids);
                                                }
                                            } else {
                                                if (config.auto_calculate_item_amount && Utils.getAmount(p.getInventory(), shop.getProduct()) > 0) {
                                                    if (confirmed || !config.confirm_shopping) {
                                                        sell(p, shop, stack);
                                                        if (config.confirm_shopping) {
                                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                            ids.remove(shop.getID());
                                                            if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                            else needsConfirmation.put(p.getUniqueId(), ids);
                                                        }
                                                    } else {
                                                        plugin.debug("Needs confirmation");
                                                        p.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.CLICK_TO_CONFIRM));
                                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                        ids.add(shop.getID());
                                                        needsConfirmation.put(p.getUniqueId(), ids);
                                                    }
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
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED,
                    new LocalizedMessage.ReplacedPlaceholder(Placeholder.ERROR, r.errorMessage)));
            return;
        }

        shop.create(true);

        plugin.debug("Shop created");
        shopUtils.addShop(shop, true);

        LocalizedMessage.ReplacedPlaceholder placeholder = new LocalizedMessage.ReplacedPlaceholder(
                Placeholder.CREATION_PRICE, String.valueOf(creationPrice));

        if (shopType == ShopType.ADMIN) {
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ADMIN_SHOP_CREATED, placeholder));
        } else {
            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_CREATED, placeholder));
        }

        // next update will display the new shop
        for (Player player : location.getWorld().getPlayers()) {
            plugin.getShopUtils().resetPlayerLocation(player);
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
        if (message) executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.OPENED_SHOP,
                new LocalizedMessage.ReplacedPlaceholder(Placeholder.VENDOR, shop.getVendor().getName())));
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
                new LocalizedMessage.ReplacedPlaceholder(Placeholder.VENDOR, vendorName));

        String productString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_PRODUCT,
                new LocalizedMessage.ReplacedPlaceholder(Placeholder.AMOUNT, String.valueOf(shop.getProduct().getAmount())),
                new LocalizedMessage.ReplacedPlaceholder(Placeholder.ITEM_NAME, LanguageUtils.getItemName(shop.getProduct())));

        String enchantmentString = "";
        String potionEffectString = "";
        String bookGenerationString = "";
        String musicDiscTitleString = "";

        String disabled = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_DISABLED);

        String priceString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_PRICE,
                new LocalizedMessage.ReplacedPlaceholder(Placeholder.BUY_PRICE, (shop.getBuyPrice() > 0 ? String.valueOf(shop.getBuyPrice()) : disabled)),
                new LocalizedMessage.ReplacedPlaceholder(Placeholder.SELL_PRICE, (shop.getSellPrice() > 0 ? String.valueOf(shop.getSellPrice()) : disabled)));

        String shopType = LanguageUtils.getMessage(shop.getShopType() == ShopType.NORMAL ?
                LocalizedMessage.Message.SHOP_INFO_NORMAL : LocalizedMessage.Message.SHOP_INFO_ADMIN);

        String stock = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_STOCK,
                new LocalizedMessage.ReplacedPlaceholder(Placeholder.STOCK, String.valueOf(amount)));

        String potionEffectName = LanguageUtils.getPotionEffectName(shop.getProduct());

        if (potionEffectName.length() > 0) {
            boolean potionExtended = ItemUtils.isExtendedPotion(shop.getProduct());

            String extended = potionExtended ? LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_EXTENDED) : "";
            potionEffectString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_POTION_EFFECT,
                    new LocalizedMessage.ReplacedPlaceholder(Placeholder.POTION_EFFECT, potionEffectName),
                    new LocalizedMessage.ReplacedPlaceholder(Placeholder.EXTENDED, extended));
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
                    new LocalizedMessage.ReplacedPlaceholder(Placeholder.GENERATION, LanguageUtils.getBookGenerationName(generation)));
        }

        String musicDiscName = LanguageUtils.getMusicDiscName(type);
        if (musicDiscName.length() > 0) {
            musicDiscTitleString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_MUSIC_TITLE,
                    new LocalizedMessage.ReplacedPlaceholder(Placeholder.MUSIC_TITLE, musicDiscName));
        }

        Map<Enchantment, Integer> enchantmentMap = ItemUtils.getEnchantments(shop.getProduct());
        String enchantmentList = LanguageUtils.getEnchantmentString(enchantmentMap);

        if (enchantmentList.length() > 0) {
            enchantmentString = LanguageUtils.getMessage(LocalizedMessage.Message.SHOP_INFO_ENCHANTMENTS,
                    new LocalizedMessage.ReplacedPlaceholder(Placeholder.ENCHANTMENT, enchantmentList));
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
    private void buy(Player executor, final Shop shop, boolean stack) {
        plugin.debug(executor.getName() + " is buying (#" + shop.getID() + ")");

        int amount = shop.getProduct().getAmount();
        if (stack) amount = shop.getProduct().getMaxStackSize();

        String worldName = shop.getLocation().getWorld().getName();

        double price = shop.getBuyPrice();
        if (stack) price = (price / shop.getProduct().getAmount()) * amount;

        if (econ.getBalance(executor, worldName) >= price || config.auto_calculate_item_amount) {

            int amountForMoney = (int) (amount / price * econ.getBalance(executor, worldName));

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

                EconomyResponse r = econ.withdrawPlayer(executor, worldName, newPrice);

                if (r.transactionSuccess()) {
                    EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.depositPlayer(shop.getVendor(), worldName, newPrice) : null;

                    if (r2 != null) {
                        if (r2.transactionSuccess()) {
                            ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.BUY, newAmount, newPrice);
                            Bukkit.getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                econ.depositPlayer(executor, worldName, newPrice);
                                econ.withdrawPlayer(shop.getVendor(), worldName, newPrice);
                                plugin.debug("Buy event cancelled (#" + shop.getID() + ")");
                                return;
                            }

                            database.logEconomy(executor, newProduct, shop.getVendor(), shop.getShopType(), shop.getLocation(), newPrice, ShopBuySellEvent.Type.BUY, null);

                            addToInventory(inventory, newProduct);
                            removeFromInventory(c.getInventory(), newProduct);
                            executor.updateInventory();

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (plugin.getHologramFormat().isDynamic()) {
                                        shop.updateHologramText();
                                    }
                                }
                            }.runTaskLater(plugin, 1L);

                            String vendorName = (shop.getVendor().getName() == null ? shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_SUCCESS, new LocalizedMessage.ReplacedPlaceholder(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                    new LocalizedMessage.ReplacedPlaceholder(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedPlaceholder(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                                    new LocalizedMessage.ReplacedPlaceholder(Placeholder.VENDOR, vendorName)));

                            plugin.debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");

                            if (shop.getVendor().isOnline() && config.enable_vendor_messages) {
                                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SOMEONE_BOUGHT, new LocalizedMessage.ReplacedPlaceholder(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                        new LocalizedMessage.ReplacedPlaceholder(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedPlaceholder(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                                        new LocalizedMessage.ReplacedPlaceholder(Placeholder.PLAYER, executor.getName())));
                            }

                        } else {
                            plugin.debug("Economy transaction failed (r2): " + r2.errorMessage + " (#" + shop.getID() + ")");
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedPlaceholder(Placeholder.ERROR, r2.errorMessage)));
                            econ.withdrawPlayer(shop.getVendor(), worldName, newPrice);
                            econ.depositPlayer(executor, worldName, newPrice);
                        }
                    } else {
                        ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.BUY, newAmount, newPrice);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            econ.depositPlayer(executor, worldName, newPrice);
                            plugin.debug("Buy event cancelled (#" + shop.getID() + ")");
                            return;
                        }

                        database.logEconomy(executor, newProduct, shop.getVendor(), shop.getShopType(), shop.getLocation(), newPrice, ShopBuySellEvent.Type.BUY, null);

                        addToInventory(inventory, newProduct);
                        executor.updateInventory();

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (plugin.getHologramFormat().isDynamic()) {
                                    shop.updateHologramText();
                                }
                            }
                        }.runTaskLater(plugin, 1L);

                        executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.BUY_SUCCESS_ADMIN, new LocalizedMessage.ReplacedPlaceholder(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                new LocalizedMessage.ReplacedPlaceholder(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedPlaceholder(Placeholder.BUY_PRICE, String.valueOf(newPrice))));

                        plugin.debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");
                    }
                } else {
                    plugin.debug("Economy transaction failed (r): " + r.errorMessage + " (#" + shop.getID() + ")");
                    executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedPlaceholder(Placeholder.ERROR, r.errorMessage)));
                    econ.depositPlayer(executor, worldName, newPrice);
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
    private void sell(Player executor, final Shop shop, boolean stack) {
        plugin.debug(executor.getName() + " is selling (#" + shop.getID() + ")");

        int amount = shop.getProduct().getAmount();
        if (stack) amount = shop.getProduct().getMaxStackSize();

        double price = shop.getSellPrice();
        if (stack) price = (price / shop.getProduct().getAmount()) * amount;

        String worldName = shop.getLocation().getWorld().getName();

        if (econ.getBalance(shop.getVendor(), worldName) >= price || shop.getShopType() == ShopType.ADMIN || config.auto_calculate_item_amount) {
            int amountForMoney = (int) (amount / price * econ.getBalance(shop.getVendor(), worldName));

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

                EconomyResponse r = econ.depositPlayer(executor, worldName, newPrice);

                if (r.transactionSuccess()) {
                    EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.withdrawPlayer(shop.getVendor(), worldName, newPrice) : null;

                    if (r2 != null) {
                        if (r2.transactionSuccess()) {
                            ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.SELL, newAmount, newPrice);
                            Bukkit.getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                econ.withdrawPlayer(executor, worldName, newPrice);
                                econ.depositPlayer(shop.getVendor(), worldName, newPrice);
                                plugin.debug("Sell event cancelled (#" + shop.getID() + ")");
                                return;
                            }

                            database.logEconomy(executor, newProduct, shop.getVendor(), shop.getShopType(), shop.getLocation(), newPrice, ShopBuySellEvent.Type.SELL, null);

                            addToInventory(inventory, newProduct);
                            removeFromInventory(executor.getInventory(), newProduct);
                            executor.updateInventory();

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (plugin.getHologramFormat().isDynamic()) {
                                        shop.updateHologramText();
                                    }
                                }
                            }.runTaskLater(plugin, 1L);

                            String vendorName = (shop.getVendor().getName() == null ? shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SELL_SUCCESS, new LocalizedMessage.ReplacedPlaceholder(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                    new LocalizedMessage.ReplacedPlaceholder(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedPlaceholder(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                                    new LocalizedMessage.ReplacedPlaceholder(Placeholder.VENDOR, vendorName)));

                            plugin.debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");

                            if (shop.getVendor().isOnline() && config.enable_vendor_messages) {
                                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SOMEONE_SOLD, new LocalizedMessage.ReplacedPlaceholder(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                        new LocalizedMessage.ReplacedPlaceholder(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedPlaceholder(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                                        new LocalizedMessage.ReplacedPlaceholder(Placeholder.PLAYER, executor.getName())));
                            }

                        } else {
                            plugin.debug("Economy transaction failed (r2): " + r2.errorMessage + " (#" + shop.getID() + ")");
                            executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedPlaceholder(Placeholder.ERROR, r2.errorMessage)));
                            econ.withdrawPlayer(executor, worldName, newPrice);
                            econ.depositPlayer(shop.getVendor(), worldName, newPrice);
                        }

                    } else {
                        ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.SELL, newAmount, newPrice);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            econ.withdrawPlayer(executor, worldName, newPrice);
                            plugin.debug("Sell event cancelled (#" + shop.getID() + ")");
                            return;
                        }

                        database.logEconomy(executor, newProduct, shop.getVendor(), shop.getShopType(), shop.getLocation(), newPrice, ShopBuySellEvent.Type.SELL, null);

                        removeFromInventory(executor.getInventory(), newProduct);
                        executor.updateInventory();

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (plugin.getHologramFormat().isDynamic()) {
                                    shop.updateHologramText();
                                }
                            }
                        }.runTaskLater(plugin, 1L);

                        executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.SELL_SUCCESS_ADMIN, new LocalizedMessage.ReplacedPlaceholder(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                new LocalizedMessage.ReplacedPlaceholder(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new LocalizedMessage.ReplacedPlaceholder(Placeholder.SELL_PRICE, String.valueOf(newPrice))));

                        plugin.debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");
                    }

                } else {
                    plugin.debug("Economy transaction failed (r): " + r.errorMessage + " (#" + shop.getID() + ")");
                    executor.sendMessage(LanguageUtils.getMessage(LocalizedMessage.Message.ERROR_OCCURRED, new LocalizedMessage.ReplacedPlaceholder(Placeholder.ERROR, r.errorMessage)));
                    econ.withdrawPlayer(executor, worldName, newPrice);
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
