package de.epiceric.shopchest.listeners;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.nms.Hologram;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.Permissions;
import de.epiceric.shopchest.external.WorldGuardShopFlag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class WorldGuardListener implements Listener {

    private ShopChest plugin;
    private WorldGuardPlugin worldGuard;

    public WorldGuardListener(ShopChest plugin) {
        this.plugin = plugin;
        this.worldGuard = plugin.getWorldGuard();
    }


    private boolean isAllowed(Player player, Location location, Action action) {
        LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
        RegionContainer container = worldGuard.getRegionContainer();
        RegionQuery query = container.createQuery();
        Shop shop = plugin.getShopUtils().getShop(location);

        if (action == Action.RIGHT_CLICK_BLOCK && shop != null) {
            if (shop.getVendor().getUniqueId().equals(player.getUniqueId()) && shop.getShopType() != Shop.ShopType.ADMIN) {
                return true;
            }

            if (!shop.getVendor().getUniqueId().equals(player.getUniqueId()) && player.isSneaking()) {
                return player.hasPermission(Permissions.OPEN_OTHER);
            }
        }

        if (ClickType.getPlayerClickType(player) != null) {
            switch (ClickType.getPlayerClickType(player).getClickType()) {
                case CREATE:
                    return query.testState(location, localPlayer, WorldGuardShopFlag.CREATE_SHOP);
                case REMOVE:
                case INFO:
                    return true;
            }
        } else {
            if (shop != null) {
                StateFlag flag = (shop.getShopType() == Shop.ShopType.NORMAL ? WorldGuardShopFlag.USE_SHOP : WorldGuardShopFlag.USE_ADMIN_SHOP);

                return query.testState(location, localPlayer, flag);
            }
        }

        return false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUseEntity(UseEntityEvent event) {
        if (plugin.getShopChestConfig().enable_worldguard_integration) {
            Player player = event.getCause().getFirstPlayer();
            if (player == null) return;

            if (event.getOriginalEvent() instanceof PlayerInteractAtEntityEvent) {
                PlayerInteractAtEntityEvent orig = (PlayerInteractAtEntityEvent) event.getOriginalEvent();
                Entity e = orig.getRightClicked();

                if (e.getType() == EntityType.ARMOR_STAND) {
                    if (!Hologram.isPartOfHologram((ArmorStand) e))
                        return;

                    for (Shop shop : plugin.getShopUtils().getShops()) {
                        if (shop.getHologram() != null && shop.getHologram().contains((ArmorStand) e)) {
                            if (isAllowed(player, shop.getLocation(), Action.RIGHT_CLICK_BLOCK)) {
                                event.setAllowed(true);
                                orig.setCancelled(false);
                            }

                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamageEntity(DamageEntityEvent event) {
        if (plugin.getShopChestConfig().enable_worldguard_integration) {
            Player player = event.getCause().getFirstPlayer();
            if (player == null) return;

            if (event.getOriginalEvent() instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent orig = (EntityDamageByEntityEvent) event.getOriginalEvent();
                Entity e = orig.getEntity();

                if (e.getType() == EntityType.ARMOR_STAND) {
                    if (!Hologram.isPartOfHologram((ArmorStand) e))
                        return;

                    for (Shop shop : plugin.getShopUtils().getShops()) {
                        if (shop.getHologram() != null && shop.getHologram().contains((ArmorStand) e)) {
                            if (isAllowed(player, shop.getLocation(), Action.LEFT_CLICK_BLOCK)) {
                                event.setAllowed(true);
                                orig.setCancelled(false);
                            }

                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUseBlock(UseBlockEvent event) {
        if (plugin.getShopChestConfig().enable_worldguard_integration) {
            Player player = event.getCause().getFirstPlayer();
            if (player == null) return;

            if (event.getOriginalEvent() instanceof PlayerInteractEvent) {
                PlayerInteractEvent orig = (PlayerInteractEvent) event.getOriginalEvent();

                if (orig.hasBlock()) {
                    Material type = orig.getClickedBlock().getType();
                    if (type == Material.CHEST || type == Material.TRAPPED_CHEST) {
                        if (isAllowed(player, orig.getClickedBlock().getLocation(), orig.getAction())) {
                            event.setAllowed(true);
                            orig.setCancelled(false);
                        }
                    }
                }
            } else if (event.getOriginalEvent() instanceof InventoryOpenEvent) {
                InventoryOpenEvent orig = (InventoryOpenEvent) event.getOriginalEvent();

                if (orig.getInventory().getHolder() instanceof Chest) {
                    if (isAllowed(player, ((Chest)orig.getInventory().getHolder()).getLocation(), Action.RIGHT_CLICK_BLOCK)) {
                        event.setAllowed(true);
                        orig.setCancelled(false);
                    }
                }
            }
        }
    }

}
