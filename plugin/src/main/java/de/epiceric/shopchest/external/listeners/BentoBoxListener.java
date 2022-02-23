package de.epiceric.shopchest.external.listeners;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.ShopCreateEvent;
import de.epiceric.shopchest.event.ShopExtendEvent;
import de.epiceric.shopchest.external.BentoBoxShopFlag;
import de.epiceric.shopchest.utils.Utils;
import world.bentobox.bentobox.api.flags.FlagListener;

public class BentoBoxListener extends FlagListener {
    private ShopChest plugin;

    public BentoBoxListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreateShop(ShopCreateEvent e) {
        if (!Config.enableBentoBoxIntegration)
            return;

        Set<Location> chestLocations = Utils.getChestLocations(e.getShop());
        for (Location loc : chestLocations) {
            if (handleForLocation(e.getPlayer(), loc, e))
                return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExtendShop(ShopExtendEvent e) {
        if (!Config.enableBentoBoxIntegration)
            return;

        handleForLocation(e.getPlayer(), e.getNewChestLocation(), e);
    }

    private boolean handleForLocation(Player player, Location loc, Cancellable e) {
        boolean allowed = checkIsland((Event) e, player, loc, BentoBoxShopFlag.SHOP_FLAG);
        if (!allowed) {
            e.setCancelled(true);
            plugin.debug("Cancel Reason: BentoBox");
            return true;
        }

        return false;
    }
}