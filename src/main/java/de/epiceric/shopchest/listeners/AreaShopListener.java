package de.epiceric.shopchest.listeners;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import me.wiefferink.areashop.events.notify.DeletedRegionEvent;
import me.wiefferink.areashop.events.notify.ResoldRegionEvent;
import me.wiefferink.areashop.events.notify.SoldRegionEvent;
import me.wiefferink.areashop.events.notify.UnrentedRegionEvent;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AreaShopListener implements Listener {

    private ShopChest plugin;
    private Config config;

    public AreaShopListener(ShopChest plugin) {
        this.plugin = plugin;
        this.config = plugin.getShopChestConfig();
    }

    @EventHandler
    public void onRegionDeleted(DeletedRegionEvent e) {
        if (config.enable_areashop_integration && config.areashop_remove_shop_events.contains("DELETE")) {
            removeShopsInRegion(e.getRegion());
        }
    }

    @EventHandler
    public void onRegionUnrented(UnrentedRegionEvent e) {
        if (config.enable_areashop_integration && config.areashop_remove_shop_events.contains("UNRENT")) {
            removeShopsInRegion(e.getRegion());
        }
    }

    @EventHandler
    public void onRegionResold(ResoldRegionEvent e) {
        if (config.enable_areashop_integration && config.areashop_remove_shop_events.contains("RESELL")) {
            removeShopsInRegion(e.getRegion());
        }
    }

    @EventHandler
    public void onRegionSold(SoldRegionEvent e) {
        if (config.enable_areashop_integration && config.areashop_remove_shop_events.contains("SELL")) {
            removeShopsInRegion(e.getRegion());
        }
    }

    private void removeShopsInRegion(GeneralRegion generalRegion) {
        if (!plugin.hasWorldGuard()) return;

        RegionManager regionManager = plugin.getWorldGuard().getRegionManager(generalRegion.getWorld());

        for (Shop shop : plugin.getShopUtils().getShops()) {
            if (!shop.getLocation().getWorld().getName().equals(generalRegion.getWorldName())) continue;

            for (ProtectedRegion r : regionManager.getApplicableRegions(shop.getLocation())) {
                if (generalRegion.getName().equals(r.getId())) {
                    plugin.getShopUtils().removeShop(shop, true);
                    break;
                }
            }
        }

    }

}
