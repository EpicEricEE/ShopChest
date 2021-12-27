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
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GriefPreventionListener implements Listener {
    private final ShopChest plugin;
    private final GriefPrevention griefPrevention;

    public GriefPreventionListener(ShopChest plugin) {
        this.plugin = plugin;
        this.griefPrevention = plugin.getGriefPrevention();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreateShop(ShopCreateEvent e) {
        if (!Config.enableGriefPreventionIntegration)
            return;

        Set<Location> chestLocations = Utils.getChestLocations(e.getShop());
        for (Location loc : chestLocations) {
            if (handleForLocation(e.getPlayer(), loc, e))
                return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExtendShop(ShopExtendEvent e) {
        if (!Config.enableASkyblockIntegration)
            return;

        handleForLocation(e.getPlayer(), e.getNewChestLocation(), e);
    }

    private boolean handleForLocation(Player player, Location loc, Cancellable e) {
        Claim claim = griefPrevention.dataStore.getClaimAt(loc, false, null);
        if (claim == null) 
            return false;

        if (claim.allowContainers(player) != null) {
            e.setCancelled(true);
            plugin.debug("Cancel Reason: GriefPrevention");
            return true;
        }

        return false;
    }
}