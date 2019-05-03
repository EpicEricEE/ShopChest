package de.epiceric.shopchest.external.listeners;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.ShopCreateEvent;
import de.epiceric.shopchest.event.ShopExtendEvent;
import de.epiceric.shopchest.utils.Utils;
import us.talabrek.ultimateskyblock.api.IslandInfo;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

public class USkyBlockListener implements Listener {
    private final ShopChest plugin;
    private final uSkyBlockAPI uSkyBlockAPI;

    public USkyBlockListener(ShopChest plugin) {
        this.plugin = plugin;
        this.uSkyBlockAPI = plugin.getUSkyBlock();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreateShop(ShopCreateEvent e) {
        if (!Config.enableUSkyblockIntegration)
            return;

        Set<Location> chestLocations = Utils.getChestLocations(e.getShop());
        for (Location loc : chestLocations) {
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

    private boolean handleForLocation(Player player, Location loc, Cancellable e) {
        IslandInfo islandInfo = uSkyBlockAPI.getIslandInfo(loc);
        if (islandInfo == null) 
            return false;

        if (!player.getName().equals(islandInfo.getLeader()) && !islandInfo.getMembers().contains(player.getName())) {
            e.setCancelled(true);
            plugin.debug("Cancel Reason: uSkyBlock");
            return true;
        }
        return false;
    }
}