package de.epiceric.shopchest.external.listeners;

import com.github.intellectualsites.plotsquared.bukkit.events.PlotClearEvent;
import com.github.intellectualsites.plotsquared.bukkit.events.PlotDeleteEvent;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.google.common.eventbus.Subscribe;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.ShopCreateEvent;
import de.epiceric.shopchest.event.ShopExtendEvent;
import de.epiceric.shopchest.external.PlotSquaredOldShopFlag;
import de.epiceric.shopchest.external.PlotSquaredShopFlag;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
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

    @Subscribe
    public void onDeletePlot(PlotDeleteEvent event) {
        for (Shop shop: plugin.getShopUtils().getShops()) {
            org.bukkit.Location l = shop.getLocation();
            Location location = new Location(l.getWorld().getName(),l.getBlockX(), l.getBlockY(), l.getBlockZ());
            if (!location.isPlotArea()) {
                continue;
            }
            if (!event.getPlot().getArea().contains(location)) {
                return;
            }

            plugin.getShopUtils().removeShop(shop, true);
        }
    }

    @Subscribe
    public void onClearPlot(PlotClearEvent event) {
        for (Shop shop: plugin.getShopUtils().getShops()) {
            org.bukkit.Location l = shop.getLocation();
            Location location = new Location(l.getWorld().getName(),l.getBlockX(), l.getBlockY(), l.getBlockZ());
            if (!location.isPlotArea()) {
                continue;
            }
            if (!event.getPlot().getArea().contains(location)) {
                return;
            }

            plugin.getShopUtils().removeShop(shop, true);
        }
    }

    // TODO: Outsource shop use external permission

    // @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    // public void onBuySell(ShopBuySellEvent e) {
    //     if (!Config.enablePlotsquaredIntegration)
    //         return;

    //     Set<org.bukkit.Location> chestLocations = Utils.getChestLocations(e.getShop());
    //     for (org.bukkit.Location loc : chestLocations) {
    //         Location plotLocation = new Location(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    //         Plot plot = plotLocation.getOwnedPlot();
    //         if (!isFlagAllowed(plot, PlotSquaredShopFlag.USE_SHOP, e.getPlayer())) {
    //             e.setCancelled(true);
    //             plugin.debug("Cancel Reason: PlotSquared");
    //             return;
    //         }
    //     }
    // }

    private boolean handleForLocation(Player player, org.bukkit.Location loc, Cancellable e) {
        boolean isAllowed = false;

        try {
            Class.forName("com.plotsquared.core.PlotSquared");
            com.plotsquared.core.location.Location plotLocation = new com.plotsquared.core.location.Location(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            com.plotsquared.core.plot.Plot plot = plotLocation.getOwnedPlot();
            isAllowed = PlotSquaredShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredShopFlag.CREATE_SHOP, player);
        } catch (ClassNotFoundException ex) {
            com.github.intellectualsites.plotsquared.plot.object.Location plotLocation = new com.github.intellectualsites.plotsquared.plot.object.Location(
                    loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            com.github.intellectualsites.plotsquared.plot.object.Plot plot = plotLocation.getOwnedPlot();
            isAllowed = PlotSquaredOldShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredOldShopFlag.CREATE_SHOP, player);
        }

        if (!isAllowed) {
            e.setCancelled(true);
            plugin.debug("Cancel Reason: PlotSquared");
            return true;
        }
        return false;
    }
}
