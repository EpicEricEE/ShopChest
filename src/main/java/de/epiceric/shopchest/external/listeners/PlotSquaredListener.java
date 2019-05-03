package de.epiceric.shopchest.external.listeners;

import java.util.Set;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.ShopCreateEvent;
import de.epiceric.shopchest.event.ShopExtendEvent;
import de.epiceric.shopchest.external.PlotSquaredShopFlag;
import de.epiceric.shopchest.utils.Utils;

public class PlotSquaredListener implements Listener {
    private final ShopChest plugin;

    public PlotSquaredListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreateShop(ShopCreateEvent e) {
        if (!Config.enablePlotsquaredIntegration)
            return;

        Set<org.bukkit.Location> chestLocations = Utils.getChestLocations(e.getShop());
        for (org.bukkit.Location loc : chestLocations) {
            if (handleForLocation(e.getPlayer(), loc, e))
                return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExtendShop(ShopExtendEvent e) {
        if (!Config.enablePlotsquaredIntegration)
            return;

        handleForLocation(e.getPlayer(), e.getNewChestLocation(), e);
    }

    // TODO: Outsource shop use external permission

    // @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    // public void onBuySell(ShopBuySellEvent e) {
    //     if (!Config.enablePlotsquaredIntegration)
    //         return;
            
    //     ShopType shopType = e.getShop().getShopType();
    //     GroupFlag flag = shopType == ShopType.ADMIN ? PlotSquaredShopFlag.USE_ADMIN_SHOP : PlotSquaredShopFlag.USE_SHOP;

    //     Set<org.bukkit.Location> chestLocations = Utils.getChestLocations(e.getShop());
    //     for (org.bukkit.Location loc : chestLocations) {
    //         Location plotLocation = new Location(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    //         Plot plot = plotLocation.getOwnedPlot();
    //         if (!isFlagAllowed(plot, flag, e.getPlayer())) {
    //             e.setCancelled(true);
    //             plugin.debug("Cancel Reason: PlotSquared");
    //             return;
    //         }
    //     }
    // }

    private boolean handleForLocation(Player player, org.bukkit.Location loc, Cancellable e) {
        Location plotLocation = new Location(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        Plot plot = plotLocation.getOwnedPlot();
        if (!PlotSquaredShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredShopFlag.CREATE_SHOP, player)) {
            e.setCancelled(true);
            plugin.debug("Cancel Reason: PlotSquared");
            return true;
        }
        return false;
    }
}