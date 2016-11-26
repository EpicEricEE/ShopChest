package de.epiceric.shopchest.listeners;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.Permissions;
import de.epiceric.shopchest.worldguard.ShopFlag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class WorldGuardListener implements Listener {

    private ShopChest plugin;
    private WorldGuardPlugin worldGuard;

    public WorldGuardListener(ShopChest plugin) {
        this.plugin = plugin;
        this.worldGuard = plugin.getWorldGuard();
    }


    private boolean isAllowed(UseBlockEvent event, Location location, Action action) {
        Player p = event.getCause().getFirstPlayer();

        LocalPlayer localPlayer = worldGuard.wrapPlayer(p);
        RegionContainer container = worldGuard.getRegionContainer();
        RegionQuery query = container.createQuery();

        if (action == Action.RIGHT_CLICK_BLOCK) {

            if (ClickType.getPlayerClickType(p) != null) {

                switch (ClickType.getPlayerClickType(p).getClickType()) {

                    case CREATE:
                        return query.testState(location, localPlayer, ShopFlag.CREATE_SHOP);
                    case REMOVE:
                    case INFO:
                        return true;
                }
            } else {
                if (plugin.getShopUtils().isShop(location)) {
                    Shop shop = plugin.getShopUtils().getShop(location);

                    if (shop.getVendor().getUniqueId().equals(p.getUniqueId()) && shop.getShopType() != Shop.ShopType.ADMIN) {
                        return true;
                    }

                    if (!shop.getVendor().getUniqueId().equals(p.getUniqueId()) && p.isSneaking()) {
                        return p.hasPermission(Permissions.OPEN_OTHER);
                    }

                    StateFlag flag = (shop.getShopType() == Shop.ShopType.NORMAL ? ShopFlag.USE_SHOP : ShopFlag.USE_ADMIN_SHOP);

                    return query.testState(location, localPlayer, flag);
                }
            }
        } else if (action == Action.LEFT_CLICK_BLOCK) {
            if (plugin.getShopUtils().isShop(location)) {
                Shop shop = plugin.getShopUtils().getShop(location);

                StateFlag flag = (shop.getShopType() == Shop.ShopType.NORMAL ? ShopFlag.USE_SHOP : ShopFlag.USE_ADMIN_SHOP);

                return query.testState(location, localPlayer, flag);
            }
        }

        return false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUseBlock(UseBlockEvent event) {
        if (plugin.getShopChestConfig().enable_worldguard_integration) {
            if (event.getCause().getFirstPlayer() == null) return;

            if (event.getOriginalEvent() instanceof PlayerInteractEvent) {
                PlayerInteractEvent orig = (PlayerInteractEvent) event.getOriginalEvent();

                if (orig.hasBlock()) {
                    Material type = orig.getClickedBlock().getType();
                    if (type == Material.CHEST || type == Material.TRAPPED_CHEST) {
                        if (isAllowed(event, orig.getClickedBlock().getLocation(), orig.getAction())) {
                            event.setAllowed(true);
                            orig.setCancelled(false);
                        }
                    }
                }
            } else if (event.getOriginalEvent() instanceof InventoryOpenEvent) {
                InventoryOpenEvent orig = (InventoryOpenEvent) event.getOriginalEvent();

                if (orig.getInventory().getHolder() instanceof Chest) {
                    if (isAllowed(event, ((Chest)orig.getInventory().getHolder()).getLocation(), Action.RIGHT_CLICK_BLOCK)) {
                        event.setAllowed(true);
                        orig.setCancelled(false);
                    }
                }
            }
        }
    }

}
