package de.epiceric.shopchest.external.listeners;

import java.util.Optional;
import java.util.Set;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;

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

public class TownyListener implements Listener {
    private final ShopChest plugin;

    public TownyListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreateShop(ShopCreateEvent e) {
        if (!Config.enableTownyIntegration)
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
        TownBlock townBlock = TownyUniverse.getTownBlock(loc);
        if (townBlock == null)
            return false;

        try {
            Town town = townBlock.getTown();
            Optional<Resident> playerResident = town.getResidents().stream()
                    .filter(r -> r.getName().equals(player.getName()))
                    .findFirst();
                    
            if (!playerResident.isPresent()) {
                e.setCancelled(true);
                plugin.debug("Cancel Reason: Towny (no resident)");
                return true;
            }

            Resident resident = playerResident.get();
            String plotType = townBlock.getType().name();
            boolean cancel = (resident.isMayor() && !Config.townyShopPlotsMayor.contains(plotType))
                    || (resident.isKing() && !Config.townyShopPlotsKing.contains(plotType))
                    || (!resident.isKing() && !resident.isMayor() && !Config.townyShopPlotsResidents.contains(plotType));
            
            if (cancel) {
                e.setCancelled(true);
                plugin.debug("Cancel Reason: Towny (no permission)");
                return true;
            }
        } catch (NotRegisteredException ignored) {
        }
        return false;
    }
}