package de.epiceric.shopchest.listeners;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.BoundingBox;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandBanEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandDeleteChunksEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandDeletedEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandResettedEvent;
import world.bentobox.bentobox.api.events.team.TeamEvent.TeamKickEvent;
import world.bentobox.bentobox.api.events.team.TeamEvent.TeamLeaveEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.database.objects.IslandDeletion;

public class BentoBoxListener implements Listener {
    private ShopChest plugin;

    public BentoBoxListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandDeleted(IslandDeletedEvent e) {
        deleteShops(e.getDeletedIslandInfo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDeleteChunks(IslandDeleteChunksEvent e) {
        deleteShops(e.getDeletedIslandInfo());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onIslandResetted(IslandResettedEvent e) {
        deleteShops(e.getIsland(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandBan(IslandBanEvent e) {
        deleteShops(e.getIsland(), e.getPlayerUUID());
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeamKick(TeamKickEvent e) {
        deleteShops(e.getIsland(), e.getPlayerUUID());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeamLeave(TeamLeaveEvent e) {
        deleteShops(e.getIsland(), e.getPlayerUUID());
    }

    private void deleteShops(IslandDeletion deletedIsland) {
        deleteShops(deletedIsland.getWorld(), deletedIsland.getBox(), null);
    }

    private void deleteShops(Island island, UUID vendorUuid) {
        deleteShops(island.getWorld(), island.getBoundingBox(), vendorUuid);
    }

    private void deleteShops(World world, BoundingBox box, UUID vendorUuid) {
        if (!Config.enableBentoBoxIntegration)
            return;
            
        Collection<Shop> shops = plugin.getShopUtils().getShops();
        for (Shop shop : shops) {
            if (!shop.getLocation().getWorld().getName().equals(world.getName())) {
                continue;
            }

            if (vendorUuid != null && !shop.getVendor().getUniqueId().equals(vendorUuid)) {
                continue;
            }

            int x = shop.getLocation().getBlockX();
            int z = shop.getLocation().getBlockZ();
            if (box.contains(x, 0, z)) {
                plugin.getShopUtils().removeShop(shop, true);
            }
        }
    }
    
}