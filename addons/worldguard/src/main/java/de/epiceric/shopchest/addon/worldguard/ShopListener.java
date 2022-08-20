package de.epiceric.shopchest.addon.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.DoubleChestInventory;

import de.epiceric.shopchest.api.event.ShopCreateEvent;
import de.epiceric.shopchest.api.event.ShopExtendEvent;
import de.epiceric.shopchest.api.event.ShopUseEvent;
import de.epiceric.shopchest.api.exceptions.ChestNotFoundException;
import de.epiceric.shopchest.api.shop.Shop;

public class ShopListener implements Listener {
    private final WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();

    @EventHandler(ignoreCancelled = true)
    public void onCreateShop(ShopCreateEvent e) {
        Shop shop = e.getShop();
        Player player = e.getPlayer().getBukkitPlayer();

        if (shop.isDoubleChest()) {
            try {
                Location left = ((DoubleChestInventory) shop.getInventory()).getLeftSide().getLocation();
                Location right = ((DoubleChestInventory) shop.getInventory()).getRightSide().getLocation();
                handle(player, left, e);
                handle(player, right, e);
            } catch (ChestNotFoundException ignored) {
                // Should not be possible since Shop#isDoubleChest()
                // requires chest to exist
            }
        } else {
            handle(player, shop.getLocation(), e);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExtendShop(ShopExtendEvent e) {
        handle(e.getPlayer().getBukkitPlayer(), e.getNewChestLocation(), e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onUseShop(ShopUseEvent e) {
        // Ignore double chests potentially being in two regions

        RegionQuery query = platform.getRegionContainer().createQuery();

        if (!query.testState(BukkitAdapter.adapt(e.getShop().getLocation()),
                WorldGuardPlugin.inst().wrapPlayer(e.getPlayer().getBukkitPlayer()),
                AddonMain.SHOP_USE_FLAG)) {
            e.setCancelled(true);
        }
    }

    private void handle(Player player, Location loc, Cancellable e) {
        RegionQuery query = platform.getRegionContainer().createQuery();

        if (!query.testState(BukkitAdapter.adapt(loc),
                WorldGuardPlugin.inst().wrapPlayer(player),
                AddonMain.SHOP_CREATE_FLAG)) {
            e.setCancelled(true);
        }
    }
}