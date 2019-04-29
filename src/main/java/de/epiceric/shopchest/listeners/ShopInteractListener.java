package de.epiceric.shopchest.listeners;

import com.google.gson.JsonPrimitive;
import com.intellectualcrafters.plot.object.Plot;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
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
import de.epiceric.shopchest.external.PlotSquaredShopFlag.GroupFlag;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;
import de.epiceric.shopchest.nms.Hologram;
import de.epiceric.shopchest.nms.JsonBuilder;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.ShopProduct;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.ItemUtils;
import de.epiceric.shopchest.utils.Permissions;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.utils.Utils;
import fr.xephi.authme.api.v3.AuthMeApi;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import pl.islandworld.api.IslandWorldApi;
import us.talabrek.ultimateskyblock.api.IslandInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopInteractListener implements Listener {

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile(".*([§]([a-fA-F0-9]))");
    private static final Pattern FORMAT_CODE_PATTERN = Pattern.compile(".*([§]([l-oL-OkK]))");

    private ShopChest plugin;
    private Economy econ;
    private Database database;
    private ShopUtils shopUtils;

    public ShopInteractListener(ShopChest plugin) {
        this.plugin = plugin;
        this.econ = plugin.getEconomy();
        this.database = plugin.getShopDatabase();
        this.shopUtils = plugin.getShopUtils();
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

        if (Config.enableAuthMeIntegration && plugin.hasAuthMe() && !AuthMeApi.getInstance().isAuthenticated(p)) return;

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

                                if (plugin.hasWorldGuard() && Config.enableWorldGuardIntegration) {
                                    plugin.debug("Checking if WorldGuard allows shop creation...");
                                    WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
                                    Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag("create-shop", WrappedState.class);
                                    if (flag.isPresent()) {
                                      for (Location loc : chestLocations) {
                                            if (loc != null) {
                                                WrappedState state = wgWrapper.queryFlag(p, loc, flag.get()).orElse(WrappedState.DENY);
                                                externalPluginsAllowed = state == WrappedState.ALLOW;
                                            }
                                        }
                                    } else {
                                        plugin.debug("WorldGuard flag 'create-shop' is not present!");
                                    }

                                    if (!externalPluginsAllowed) denyReason = "WorldGuard";
                                }

                                if (externalPluginsAllowed && plugin.hasTowny() && Config.enableTownyIntegration) {
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
                                                                externalPluginsAllowed &= (Config.townyShopPlotsMayor.contains(townBlock.getType().name()));
                                                            } else if (resident.isKing()) {
                                                                plugin.debug(p.getName() + " is king of town");
                                                                externalPluginsAllowed &= (Config.townyShopPlotsKing.contains(townBlock.getType().name()));
                                                            } else {
                                                                plugin.debug(p.getName() + " is resident in town");
                                                                externalPluginsAllowed &= (Config.townyShopPlotsResidents.contains(townBlock.getType().name()));
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

                                if (externalPluginsAllowed && plugin.hasPlotSquared() && Config.enablePlotsquaredIntegration) {
                                    plugin.debug("Checking if PlotSquared allows shop creation...");
                                    for (Location loc : chestLocations) {
                                        if (loc != null) {
                                            com.intellectualcrafters.plot.object.Location plotLocation = new com.intellectualcrafters.plot.object.Location(
                                                    loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

                                            Plot plot = plotLocation.getOwnedPlot();
                                            externalPluginsAllowed &= PlotSquaredShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredShopFlag.CREATE_SHOP, p);
                                        }
                                    }

                                    if (!externalPluginsAllowed) denyReason = "PlotSquared";
                                }

                                if (externalPluginsAllowed && plugin.hasUSkyBlock() && Config.enableUSkyblockIntegration) {
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

                                if (externalPluginsAllowed && plugin.hasASkyBlock() && Config.enableASkyblockIntegration) {
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

                                if (externalPluginsAllowed && plugin.hasIslandWorld() && Config.enableIslandWorldIntegration && IslandWorldApi.isInitialized()) {
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

                                if (externalPluginsAllowed && plugin.hasGriefPrevention() && Config.enableGriefPreventionIntegration) {
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
                                    p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_CREATE_PROTECTED));
                                    ClickType.removePlayerClickType(p);
                                    plugin.debug(p.getName() + " is not allowed to create a shop on the selected chest because " + denyReason);
                                    e.setCancelled(true);
                                    return;
                                }

                                e.setCancelled(true);

                                if (ItemUtils.isAir(b.getRelative(BlockFace.UP).getType())) {
                                    ClickType clickType = ClickType.getPlayerClickType(p);
                                    ShopProduct product = clickType.getProduct();
                                    double buyPrice = clickType.getBuyPrice();
                                    double sellPrice = clickType.getSellPrice();
                                    ShopType shopType = clickType.getShopType();

                                    create(p, b.getLocation(), product, buyPrice, sellPrice, shopType);
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(Message.CHEST_BLOCKED));
                                    plugin.debug("Chest is blocked");
                                }
                            } else {
                                e.setCancelled(true);
                                p.sendMessage(LanguageUtils.getMessage(Message.CHEST_ALREADY_SHOP));
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
        boolean inverted = Config.invertMouseButtons;

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
                                case CREATE:
                                    break;
                                    
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
                                            p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_REMOVE_ADMIN));
                                            plugin.debug(p.getName() + " is not permitted to remove an admin shop");
                                        }
                                    } else {
                                        if (shop.getVendor().getUniqueId().equals(p.getUniqueId()) || p.hasPermission(Permissions.REMOVE_OTHER)) {
                                            remove(p, shop);
                                        } else {
                                            p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_REMOVE_OTHERS));
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
                                        p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_OPEN_OTHERS));
                                        plugin.debug(p.getName() + " is not permitted to open another player's shop");
                                    }

                                    ClickType.removePlayerClickType(p);
                                    break;
                            }
                        } else {
                            p.sendMessage(LanguageUtils.getMessage(Message.CHEST_NO_SHOP));
                            plugin.debug("Chest is not a shop");
                        }
                    }
                } else {
                    Shop shop = shopUtils.getShop(b.getLocation());

                    if (shop != null) {
                        boolean confirmed = needsConfirmation.containsKey(p.getUniqueId()) && needsConfirmation.get(p.getUniqueId()).contains(shop.getID());
                        
                        if (e.getAction() == Action.LEFT_CLICK_BLOCK && p.isSneaking() && Utils.hasAxeInHand(p)) {
                            return;
                        }

                        ItemStack infoItem = Config.shopInfoItem;
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
                            p.sendMessage(LanguageUtils.getMessage(Message.USE_IN_CREATIVE));
                            return;
                        }

                        if ((e.getAction() == Action.RIGHT_CLICK_BLOCK && !inverted) || (e.getAction() == Action.LEFT_CLICK_BLOCK && inverted)) {
                            e.setCancelled(true);

                            if (shop.getShopType() == ShopType.ADMIN || !shop.getVendor().getUniqueId().equals(p.getUniqueId())) {
                                plugin.debug(p.getName() + " wants to buy");

                                if (shop.getBuyPrice() > 0) {
                                    if (p.hasPermission(Permissions.BUY)) {
                                        boolean externalPluginsAllowed = true;

                                        if (plugin.hasPlotSquared() && Config.enablePlotsquaredIntegration) {
                                            com.intellectualcrafters.plot.object.Location plotLocation =
                                                    new com.intellectualcrafters.plot.object.Location(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());

                                            Plot plot = plotLocation.getOwnedPlot();
                                            GroupFlag flag = shop.getShopType() == Shop.ShopType.ADMIN ? PlotSquaredShopFlag.USE_ADMIN_SHOP : PlotSquaredShopFlag.USE_SHOP;

                                            externalPluginsAllowed = PlotSquaredShopFlag.isFlagAllowedOnPlot(plot, flag, p);
                                        }

                                        if (externalPluginsAllowed && plugin.hasWorldGuard() && Config.enableWorldGuardIntegration) {
                                            String flagName = (shop.getShopType() == ShopType.ADMIN ? "use-admin-shop" : "use-shop");
                                            WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
                                            Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag(flagName, WrappedState.class);
                                            if (!flag.isPresent()) plugin.debug("WorldGuard flag '" + flagName + "' is not present!");
                                            WrappedState state = flag.map(f -> wgWrapper.queryFlag(p, b.getLocation(), f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
                                            externalPluginsAllowed = state == WrappedState.ALLOW;
                                        }

                                        if (shop.getShopType() == ShopType.ADMIN) {
                                            if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                                if (confirmed || !Config.confirmShopping) {
                                                    buy(p, shop, p.isSneaking());
                                                    if (Config.confirmShopping) {
                                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                        ids.remove(shop.getID());
                                                        if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                        else needsConfirmation.put(p.getUniqueId(), ids);
                                                    }
                                                } else {
                                                    plugin.debug("Needs confirmation");
                                                    p.sendMessage(LanguageUtils.getMessage(Message.CLICK_TO_CONFIRM));
                                                    Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                    ids.add(shop.getID());
                                                    needsConfirmation.put(p.getUniqueId(), ids);
                                                }
                                            } else {
                                                plugin.debug(p.getName() + " doesn't have external plugin's permission");
                                                p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_BUY_HERE));
                                            }
                                        } else {
                                            if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                                Chest c = (Chest) b.getState();
                                                ItemStack itemStack = shop.getProduct().getItemStack();
                                                int amount = (p.isSneaking() ? itemStack.getMaxStackSize() : shop.getProduct().getAmount());

                                                if (Utils.getAmount(c.getInventory(), itemStack) >= amount) {
                                                    if (confirmed || !Config.confirmShopping) {
                                                        buy(p, shop, p.isSneaking());
                                                        if (Config.confirmShopping) {
                                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                            ids.remove(shop.getID());
                                                            if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                            else needsConfirmation.put(p.getUniqueId(), ids);
                                                        }
                                                    } else {
                                                        plugin.debug("Needs confirmation");
                                                        p.sendMessage(LanguageUtils.getMessage(Message.CLICK_TO_CONFIRM));
                                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                        ids.add(shop.getID());
                                                        needsConfirmation.put(p.getUniqueId(), ids);
                                                    }
                                                } else {
                                                    if (Config.autoCalculateItemAmount && Utils.getAmount(c.getInventory(), itemStack) > 0) {
                                                        if (confirmed || !Config.confirmShopping) {
                                                            buy(p, shop, p.isSneaking());
                                                            if (Config.confirmShopping) {
                                                                Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                                ids.remove(shop.getID());
                                                                if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                                else needsConfirmation.put(p.getUniqueId(), ids);
                                                            }
                                                        } else {
                                                            plugin.debug("Needs confirmation");
                                                            p.sendMessage(LanguageUtils.getMessage(Message.CLICK_TO_CONFIRM));
                                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                            ids.add(shop.getID());
                                                            needsConfirmation.put(p.getUniqueId(), ids);
                                                        }
                                                    } else {
                                                        p.sendMessage(LanguageUtils.getMessage(Message.OUT_OF_STOCK));
                                                        if (shop.getVendor().isOnline() && Config.enableVendorMessages) {
                                                            shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(Message.VENDOR_OUT_OF_STOCK,
                                                                    new Replacement(Placeholder.AMOUNT, String.valueOf(shop.getProduct().getAmount())),
                                                                            new Replacement(Placeholder.ITEM_NAME, LanguageUtils.getItemName(itemStack))));
                                                        }
                                                        plugin.debug("Shop is out of stock");
                                                    }
                                                }
                                            } else {
                                                plugin.debug(p.getName() + " doesn't have external plugin's permission");
                                                p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_BUY_HERE));
                                            }
                                        }
                                    } else {
                                        p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_BUY));
                                        plugin.debug(p.getName() + " is not permitted to buy");
                                    }
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(Message.BUYING_DISABLED));
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

                                        if (plugin.hasPlotSquared() && Config.enablePlotsquaredIntegration) {
                                            com.intellectualcrafters.plot.object.Location plotLocation =
                                                    new com.intellectualcrafters.plot.object.Location(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());

                                            Plot plot = plotLocation.getOwnedPlot();
                                            GroupFlag flag = shop.getShopType() == Shop.ShopType.ADMIN ? PlotSquaredShopFlag.USE_ADMIN_SHOP : PlotSquaredShopFlag.USE_SHOP;
                                            
                                            externalPluginsAllowed = PlotSquaredShopFlag.isFlagAllowedOnPlot(plot, flag, p);
                                        }

                                        if (externalPluginsAllowed && plugin.hasWorldGuard() && Config.enableWorldGuardIntegration) {
                                            String flagName = (shop.getShopType() == ShopType.ADMIN ? "use-admin-shop" : "use-shop");
                                            WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
                                            Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag(flagName, WrappedState.class);
                                            if (!flag.isPresent()) plugin.debug("WorldGuard flag '" + flagName + "' is not present!");
                                            WrappedState state = flag.map(f -> wgWrapper.queryFlag(p, b.getLocation(), f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
                                            externalPluginsAllowed = state == WrappedState.ALLOW;
                                        }

                                        ItemStack itemStack = shop.getProduct().getItemStack();

                                        if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                            boolean stack = p.isSneaking() && !Utils.hasAxeInHand(p);
                                            int amount = stack ? itemStack.getMaxStackSize() : shop.getProduct().getAmount();

                                            if (Utils.getAmount(p.getInventory(), itemStack) >= amount) {
                                                if (confirmed || !Config.confirmShopping) {
                                                    sell(p, shop, stack);
                                                    if (Config.confirmShopping) {
                                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                        ids.remove(shop.getID());
                                                        if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                        else needsConfirmation.put(p.getUniqueId(), ids);
                                                    }
                                                } else {
                                                    plugin.debug("Needs confirmation");
                                                    p.sendMessage(LanguageUtils.getMessage(Message.CLICK_TO_CONFIRM));
                                                    Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                    ids.add(shop.getID());
                                                    needsConfirmation.put(p.getUniqueId(), ids);
                                                }
                                            } else {
                                                if (Config.autoCalculateItemAmount && Utils.getAmount(p.getInventory(), itemStack) > 0) {
                                                    if (confirmed || !Config.confirmShopping) {
                                                        sell(p, shop, stack);
                                                        if (Config.confirmShopping) {
                                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                            ids.remove(shop.getID());
                                                            if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                            else needsConfirmation.put(p.getUniqueId(), ids);
                                                        }
                                                    } else {
                                                        plugin.debug("Needs confirmation");
                                                        p.sendMessage(LanguageUtils.getMessage(Message.CLICK_TO_CONFIRM));
                                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                        ids.add(shop.getID());
                                                        needsConfirmation.put(p.getUniqueId(), ids);
                                                    }
                                                } else {
                                                    p.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_ITEMS));
                                                    plugin.debug(p.getName() + " doesn't have enough items");
                                                }
                                            }
                                        } else {
                                            plugin.debug(p.getName() + " doesn't have external plugin's permission");
                                            p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_SELL_HERE));
                                        }
                                    } else {
                                        p.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_SELL));
                                        plugin.debug(p.getName() + " is not permitted to sell");
                                    }
                                } else {
                                    p.sendMessage(LanguageUtils.getMessage(Message.SELLING_DISABLED));
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
        if (Config.enableAuthMeIntegration && plugin.hasAuthMe() && !AuthMeApi.getInstance().isAuthenticated(e.getPlayer())) return;
        handleInteractEvent(e);
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (!Config.enableHologramInteraction) return;

        Entity entity = e.getRightClicked();
        Player p = e.getPlayer();
        if (Config.enableAuthMeIntegration && plugin.hasAuthMe() && !AuthMeApi.getInstance().isAuthenticated(p)) return;

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
        if (!Config.enableHologramInteraction) return;

        Entity entity = e.getEntity();
        Entity damager = e.getDamager();

        if (!(damager instanceof Player)) return;
        Player p = (Player) damager;
        if (Config.enableAuthMeIntegration && plugin.hasAuthMe() && !AuthMeApi.getInstance().isAuthenticated(p)) return;

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
    private void create(final Player executor, final Location location, final ShopProduct product, final double buyPrice, final double sellPrice, final ShopType shopType) {
        plugin.debug(executor.getName() + " is creating new shop...");

        if (!executor.hasPermission(Permissions.CREATE)) {
            executor.sendMessage(LanguageUtils.getMessage(Message.NO_PERMISSION_CREATE));
            plugin.debug(executor.getName() + " is not permitted to create the shop");
            return;
        }

        double creationPrice = (shopType == ShopType.NORMAL) ? Config.shopCreationPriceNormal : Config.shopCreationPriceAdmin;
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
            executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED,
                    new Replacement(Placeholder.ERROR, r.errorMessage)));
            return;
        }

        shop.create(true);

        plugin.debug("Shop created");
        shopUtils.addShop(shop, true);

        Replacement placeholder = new Replacement(
                Placeholder.CREATION_PRICE, String.valueOf(creationPrice));

        if (shopType == ShopType.ADMIN) {
            executor.sendMessage(LanguageUtils.getMessage(Message.ADMIN_SHOP_CREATED, placeholder));
        } else {
            executor.sendMessage(LanguageUtils.getMessage(Message.SHOP_CREATED, placeholder));
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

        if (executor.getUniqueId().equals(shop.getVendor().getUniqueId())) {
            double creationPrice = shop.getShopType() == ShopType.ADMIN ? Config.shopCreationPriceAdmin : Config.shopCreationPriceNormal;
            EconomyResponse r = plugin.getEconomy().depositPlayer(executor, shop.getLocation().getWorld().getName(), creationPrice);
            if (!r.transactionSuccess()) {
                plugin.debug("Economy transaction failed: " + r.errorMessage);
                executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED,
                        new Replacement(Placeholder.ERROR, r.errorMessage)));
            }
        }

        shopUtils.removeShop(shop, true);
        plugin.debug("Removed shop (#" + shop.getID() + ")");
        executor.sendMessage(LanguageUtils.getMessage(Message.SHOP_REMOVED));
    }

    /**
     * Open a shop
     * @param executor Player, who executed the command and will receive the message
     * @param shop Shop to be opened
     * @param message Whether the player should receive the {@link Message#OPENED_SHOP} message
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
        if (message) executor.sendMessage(LanguageUtils.getMessage(Message.OPENED_SHOP,
                new Replacement(Placeholder.VENDOR, shop.getVendor().getName())));
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
        ItemStack itemStack = shop.getProduct().getItemStack();
        int amount = Utils.getAmount(c.getInventory(), itemStack);

        String vendorName = (shop.getVendor().getName() == null ?
                shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());

        String vendorString = LanguageUtils.getMessage(Message.SHOP_INFO_VENDOR,
                new Replacement(Placeholder.VENDOR, vendorName));

        // Make JSON message with item preview
        JsonBuilder jb = getProductJson(shop.getProduct());

        String disabled = LanguageUtils.getMessage(Message.SHOP_INFO_DISABLED);

        String priceString = LanguageUtils.getMessage(Message.SHOP_INFO_PRICE,
                new Replacement(Placeholder.BUY_PRICE, (shop.getBuyPrice() > 0 ? String.valueOf(shop.getBuyPrice()) : disabled)),
                new Replacement(Placeholder.SELL_PRICE, (shop.getSellPrice() > 0 ? String.valueOf(shop.getSellPrice()) : disabled)));

        String shopType = LanguageUtils.getMessage(shop.getShopType() == ShopType.NORMAL ?
                Message.SHOP_INFO_NORMAL : Message.SHOP_INFO_ADMIN);

        String stock = LanguageUtils.getMessage(Message.SHOP_INFO_STOCK,
                new Replacement(Placeholder.STOCK, String.valueOf(amount)));

        executor.sendMessage(" ");
        if (shop.getShopType() != ShopType.ADMIN) executor.sendMessage(vendorString);
        jb.sendJson(executor);
        if (shop.getShopType() != ShopType.ADMIN) executor.sendMessage(stock);
        executor.sendMessage(priceString);
        executor.sendMessage(shopType);
        executor.sendMessage(" ");
    }

    /**
     * Create a {@link JsonBuilder} containing the shop info message for the product
     * in which you can hover the item name to get a preview.
     * @param product The product of the shop
     * @return A {@link JsonBuilder} that can send the message via {@link JsonBuilder#sendJson(Player)}
     */
    private JsonBuilder getProductJson(ShopProduct product) {
        // Add spaces at start and end, so there will always be a part before and after
        // the item name after splitting at Placeholder.ITEM_NAME
        String productString = " " + LanguageUtils.getMessage(Message.SHOP_INFO_PRODUCT,
                new Replacement(Placeholder.AMOUNT, String.valueOf(product.getAmount()))) + " ";

        String[] parts = productString.split(Placeholder.ITEM_NAME.toString());
        String productName = LanguageUtils.getItemName(product.getItemStack());
        String jsonItem = "";
        JsonBuilder jb = new JsonBuilder(plugin);
        JsonBuilder.PartArray rootArray = new JsonBuilder.PartArray();
        
        try {
            Class<?> craftItemStackClass = Utils.getCraftClass("inventory.CraftItemStack");	
            Object nmsStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, product.getItemStack());	
            Class<?> nbtTagCompoundClass = Utils.getNMSClass("NBTTagCompound");
            Object nbtTagCompound = nbtTagCompoundClass.getConstructor().newInstance();
            nmsStack.getClass().getMethod("save", nbtTagCompoundClass).invoke(nmsStack, nbtTagCompound);
            jsonItem = new JsonPrimitive(nbtTagCompound.toString()).toString();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create JSON from item. Product preview will not be available.");
            plugin.debug("Failed to create JSON from item:");
            plugin.debug(e);
            jb.setRootPart(new JsonBuilder.Part(productString.replace(Placeholder.ITEM_NAME.toString(), productName)));
            return jb;
        }

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            // Remove spaces at start and end that were added before
            if (i == 0 && part.startsWith(" ")) {
                part = part.substring(1);
            } else if (i == parts.length - 1 && part.endsWith(" ")) {
                part = part.substring(0, part.length() - 1);
            }

            String formatPrefix = "";

            // A color code resets all format codes, so only format codes
            // after the last color code have to be found.
            int lastColorGroupEndIndex = 0;

            Matcher colorMatcher = COLOR_CODE_PATTERN.matcher(part);
            if (colorMatcher.find()) {
                formatPrefix = colorMatcher.group(1);
                lastColorGroupEndIndex = colorMatcher.end();
            }
            
            Matcher formatMatcher = FORMAT_CODE_PATTERN.matcher(part);
            while (formatMatcher.find(lastColorGroupEndIndex)) {
                formatPrefix += formatMatcher.group(1);
            }

            rootArray.addPart(new JsonBuilder.Part(part));

            if (i < parts.length - 1) {
                JsonBuilder.PartMap hoverEvent = new JsonBuilder.PartMap();
                hoverEvent.setValue("action", new JsonBuilder.Part("show_item"));
                hoverEvent.setValue("value", new JsonBuilder.Part(jsonItem, false));

                JsonBuilder.PartMap itemNameMap = JsonBuilder.parse(formatPrefix + productName).toMap();
                itemNameMap.setValue("hoverEvent", hoverEvent);

                rootArray.addPart(itemNameMap);
            }
        }

        jb.setRootPart(rootArray);
        return jb;
    }

    /**
     * A player buys from a shop
     * @param executor Player, who executed the command and will buy the product
     * @param shop Shop, from which the player buys
     * @param stack Whether a whole stack should be bought
     */
    private void buy(Player executor, final Shop shop, boolean stack) {
        plugin.debug(executor.getName() + " is buying (#" + shop.getID() + ")");

        ItemStack itemStack = shop.getProduct().getItemStack();
        int amount = shop.getProduct().getAmount();
        if (stack) amount = itemStack.getMaxStackSize();

        String worldName = shop.getLocation().getWorld().getName();

        double price = shop.getBuyPrice();
        if (stack) price = (price / shop.getProduct().getAmount()) * amount;

        if (econ.getBalance(executor, worldName) >= price || Config.autoCalculateItemAmount) {

            int amountForMoney = (int) (amount / price * econ.getBalance(executor, worldName));

            if (amountForMoney == 0 && Config.autoCalculateItemAmount) {
                executor.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_MONEY));
                return;
            }

            plugin.debug(executor.getName() + " has enough money for " + amountForMoney + " item(s) (#" + shop.getID() + ")");

            Block b = shop.getLocation().getBlock();
            Chest c = (Chest) b.getState();

            int amountForChestItems = Utils.getAmount(c.getInventory(), itemStack);

            if (amountForChestItems == 0 && shop.getShopType() != ShopType.ADMIN) {
                executor.sendMessage(LanguageUtils.getMessage(Message.OUT_OF_STOCK));
                return;
            }

            ItemStack product = new ItemStack(itemStack);
            if (stack) product.setAmount(amount);

            Inventory inventory = executor.getInventory();

            int freeSpace = Utils.getFreeSpaceForItem(inventory, product);

            if (freeSpace == 0) {
                executor.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_INVENTORY_SPACE));
                return;
            }

            int newAmount = amount;

            if (Config.autoCalculateItemAmount) {
                if (shop.getShopType() == ShopType.ADMIN)
                    newAmount = Math.min(amountForMoney, freeSpace);
                else
                    newAmount = Math.min(Math.min(amountForMoney, amountForChestItems), freeSpace);
            }

            if (newAmount > amount) newAmount = amount;

            ShopProduct newProduct = new ShopProduct(product, newAmount);
            double newPrice = (price / amount) * newAmount;

            if (freeSpace >= newAmount) {
                plugin.debug(executor.getName() + " has enough inventory space for " + freeSpace + " items (#" + shop.getID() + ")");

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

                            database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.BUY, null);

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
                            executor.sendMessage(LanguageUtils.getMessage(Message.BUY_SUCCESS, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                    new Replacement(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                                    new Replacement(Placeholder.VENDOR, vendorName)));

                            plugin.debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");

                            if (shop.getVendor().isOnline() && Config.enableVendorMessages) {
                                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(Message.SOMEONE_BOUGHT, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                        new Replacement(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                                        new Replacement(Placeholder.PLAYER, executor.getName())));
                            }

                        } else {
                            plugin.debug("Economy transaction failed (r2): " + r2.errorMessage + " (#" + shop.getID() + ")");
                            executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r2.errorMessage)));
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

                        database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.BUY, null);

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

                        executor.sendMessage(LanguageUtils.getMessage(Message.BUY_SUCCESS_ADMIN, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                new Replacement(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice))));

                        plugin.debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");
                    }
                } else {
                    plugin.debug("Economy transaction failed (r): " + r.errorMessage + " (#" + shop.getID() + ")");
                    executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r.errorMessage)));
                    econ.depositPlayer(executor, worldName, newPrice);
                }
            } else {
                executor.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_INVENTORY_SPACE));
            }
        } else {
            executor.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_MONEY));
        }
    }

    /**
     * A player sells to a shop
     * @param executor Player, who executed the command and will sell the product
     * @param shop Shop, to which the player sells
     */
    private void sell(Player executor, final Shop shop, boolean stack) {
        plugin.debug(executor.getName() + " is selling (#" + shop.getID() + ")");

        ItemStack itemStack = shop.getProduct().getItemStack();
        int amount = shop.getProduct().getAmount();
        if (stack) amount = itemStack.getMaxStackSize();

        double price = shop.getSellPrice();
        if (stack) price = (price / shop.getProduct().getAmount()) * amount;

        String worldName = shop.getLocation().getWorld().getName();

        if (shop.getShopType() == ShopType.ADMIN || econ.getBalance(shop.getVendor(), worldName) >= price || Config.autoCalculateItemAmount) {
            int amountForMoney = 1;
            
            if (shop.getShopType() != ShopType.ADMIN) {
                 amountForMoney = (int) (amount / price * econ.getBalance(shop.getVendor(), worldName));
            }

            plugin.debug("Vendor has enough money for " + amountForMoney + " item(s) (#" + shop.getID() + ")");

            if (amountForMoney == 0 && Config.autoCalculateItemAmount && shop.getShopType() != ShopType.ADMIN) {
                executor.sendMessage(LanguageUtils.getMessage(Message.VENDOR_NOT_ENOUGH_MONEY));
                return;
            }

            Block block = shop.getLocation().getBlock();
            Chest chest = (Chest) block.getState();

            int amountForItemCount = Utils.getAmount(executor.getInventory(), itemStack);

            if (amountForItemCount == 0) {
                executor.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_ITEMS));
                return;
            }

            ItemStack product = new ItemStack(itemStack);
            if (stack) product.setAmount(amount);

            Inventory inventory = chest.getInventory();

            int freeSpace = Utils.getFreeSpaceForItem(inventory, product);

            if (freeSpace == 0 && shop.getShopType() != ShopType.ADMIN) {
                executor.sendMessage(LanguageUtils.getMessage(Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
                return;
            }

            int newAmount = amount;

            if (Config.autoCalculateItemAmount) {
                if (shop.getShopType() == ShopType.ADMIN)
                    newAmount = amountForItemCount;
                else
                    newAmount = Math.min(Math.min(amountForMoney, amountForItemCount), freeSpace);
            }

            if (newAmount > amount) newAmount = amount;

            ShopProduct newProduct = new ShopProduct(product, newAmount);
            double newPrice = (price / amount) * newAmount;

            if (freeSpace >= newAmount || shop.getShopType() == ShopType.ADMIN) {
                plugin.debug("Chest has enough inventory space for " + freeSpace + " items (#" + shop.getID() + ")");

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

                            database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.SELL, null);

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
                            executor.sendMessage(LanguageUtils.getMessage(Message.SELL_SUCCESS, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                    new Replacement(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                                    new Replacement(Placeholder.VENDOR, vendorName)));

                            plugin.debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");

                            if (shop.getVendor().isOnline() && Config.enableVendorMessages) {
                                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(Message.SOMEONE_SOLD, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                        new Replacement(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                                        new Replacement(Placeholder.PLAYER, executor.getName())));
                            }

                        } else {
                            plugin.debug("Economy transaction failed (r2): " + r2.errorMessage + " (#" + shop.getID() + ")");
                            executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r2.errorMessage)));
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

                        database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.SELL, null);

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

                        executor.sendMessage(LanguageUtils.getMessage(Message.SELL_SUCCESS_ADMIN, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                new Replacement(Placeholder.ITEM_NAME, LanguageUtils.getItemName(product)), new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice))));

                        plugin.debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");
                    }

                } else {
                    plugin.debug("Economy transaction failed (r): " + r.errorMessage + " (#" + shop.getID() + ")");
                    executor.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r.errorMessage)));
                    econ.withdrawPlayer(executor, worldName, newPrice);
                }

            } else {
                executor.sendMessage(LanguageUtils.getMessage(Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
            }

        } else {
            executor.sendMessage(LanguageUtils.getMessage(Message.VENDOR_NOT_ENOUGH_MONEY));
        }
    }

    /**
     * Adds items to an inventory
     * @param inventory The inventory, to which the items will be added
     * @param itemStack Items to add
     * @return Whether all items were added to the inventory
     */
    private boolean addToInventory(Inventory inventory, ShopProduct product) {
        plugin.debug("Adding items to inventory...");

        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        ItemStack itemStack = product.getItemStack();
        int amount = product.getAmount();
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
    private boolean removeFromInventory(Inventory inventory, ShopProduct product) {
        plugin.debug("Removing items from inventory...");

        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        ItemStack itemStack = product.getItemStack();
        int amount = product.getAmount();
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
