package de.epiceric.shopchest.addon.bentobox;

import java.util.UUID;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.BoundingBox;

import de.epiceric.shopchest.api.ShopChest;
import world.bentobox.bentobox.api.events.island.IslandBanEvent;
import world.bentobox.bentobox.api.events.island.IslandDeleteChunksEvent;
import world.bentobox.bentobox.api.events.island.IslandDeletedEvent;
import world.bentobox.bentobox.api.events.island.IslandResettedEvent;
import world.bentobox.bentobox.api.events.team.TeamKickEvent;
import world.bentobox.bentobox.api.events.team.TeamLeaveEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.database.objects.IslandDeletion;

public class IslandListener implements Listener {
    private final ShopChest plugin;

    public IslandListener(ShopChest plugin) {
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

    // Utility methods

    private void deleteShops(IslandDeletion deletedIsland) {
        deleteShops(deletedIsland.getWorld(), deletedIsland.getBox(), null);
    }

    private void deleteShops(Island island, UUID vendorUuid) {
        deleteShops(island.getWorld(), island.getBoundingBox(), vendorUuid);
    }

    private void deleteShops(World world, BoundingBox box, UUID vendorUuid) {
        plugin.getShopManager().getShops(world).stream()
            .filter(shop -> vendorUuid == null || (!shop.isAdminShop() && shop.getVendor().get().getUniqueId().equals(vendorUuid)))
            .forEach(shop -> {
                int x = shop.getLocation().getBlockX();
                int z = shop.getLocation().getBlockZ();
                if (box.contains(x, 0, z)) {
                    plugin.getShopManager().removeShop(shop);
                }
            });
    }
}