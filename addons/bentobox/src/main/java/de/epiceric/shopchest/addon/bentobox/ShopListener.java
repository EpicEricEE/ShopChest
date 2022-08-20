package de.epiceric.shopchest.addon.bentobox;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.DoubleChestInventory;

import de.epiceric.shopchest.api.event.ShopCreateEvent;
import de.epiceric.shopchest.api.event.ShopExtendEvent;
import de.epiceric.shopchest.api.exceptions.ChestNotFoundException;
import de.epiceric.shopchest.api.shop.Shop;
import world.bentobox.bentobox.api.flags.FlagListener;

public class ShopListener extends FlagListener {
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

    private void handle(Player player, Location loc, Cancellable e) {
        if (!checkIsland((Event) e, player, loc, AddonMain.SHOP_FLAG)) {
            e.setCancelled(true);
        }
    }
}