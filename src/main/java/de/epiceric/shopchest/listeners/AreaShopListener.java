package de.epiceric.shopchest.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import me.wiefferink.areashop.events.notify.DeletedRegionEvent;
import me.wiefferink.areashop.events.notify.ResoldRegionEvent;
import me.wiefferink.areashop.events.notify.SoldRegionEvent;
import me.wiefferink.areashop.events.notify.UnrentedRegionEvent;
import me.wiefferink.areashop.regions.GeneralRegion;

public class AreaShopListener implements Listener {

    private ShopChest plugin;

    public AreaShopListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRegionDeleted(DeletedRegionEvent e) {
        if (Config.enableAreaShopIntegration && Config.areashopRemoveShopEvents.contains("DELETE")) {
            removeShopsInRegion(e.getRegion());
        }
    }

    @EventHandler
    public void onRegionUnrented(UnrentedRegionEvent e) {
        if (Config.enableAreaShopIntegration && Config.areashopRemoveShopEvents.contains("UNRENT")) {
            removeShopsInRegion(e.getRegion());
        }
    }

    @EventHandler
    public void onRegionResold(ResoldRegionEvent e) {
        if (Config.enableAreaShopIntegration && Config.areashopRemoveShopEvents.contains("RESELL")) {
            removeShopsInRegion(e.getRegion());
        }
    }

    @EventHandler
    public void onRegionSold(SoldRegionEvent e) {
        if (Config.enableAreaShopIntegration && Config.areashopRemoveShopEvents.contains("SELL")) {
            removeShopsInRegion(e.getRegion());
        }
    }

    private void removeShopsInRegion(GeneralRegion generalRegion) {
        if (!plugin.hasWorldGuard()) return;

        for (Shop shop : plugin.getShopUtils().getShops()) {
            if (!shop.getLocation().getWorld().getName().equals(generalRegion.getWorldName())) continue;

            for (IWrappedRegion r : WorldGuardWrapper.getInstance().getRegions(shop.getLocation())) {
                if (generalRegion.getLowerCaseName().equals(r.getId())) {
                    plugin.getShopUtils().removeShopById(shop.getID(), true);
                    break;
                }
            }
        }

    }

}
