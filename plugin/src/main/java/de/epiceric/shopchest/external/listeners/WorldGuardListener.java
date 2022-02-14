package de.epiceric.shopchest.external.listeners;

import java.util.Optional;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.event.ShopCreateEvent;
import de.epiceric.shopchest.event.ShopExtendEvent;
import de.epiceric.shopchest.utils.Utils;

public class WorldGuardListener implements Listener {

    private final ShopChest plugin;
    private final WorldGuardWrapper wgWrapper;

    public WorldGuardListener(ShopChest plugin) {
        this.plugin = plugin;
        this.wgWrapper = WorldGuardWrapper.getInstance();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreateShop(ShopCreateEvent e) {
        if (!Config.enableWorldGuardIntegration)
            return;

        Set<Location> chestLocations = Utils.getChestLocations(e.getShop());
        IWrappedFlag<WrappedState> flag = getStateFlag("create-shop");
        for (Location loc : chestLocations) {
            if (handleForLocation(e.getPlayer(), loc, e, flag))
                return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExtendShop(ShopExtendEvent e) {
        if (!Config.enableWorldGuardIntegration)
            return;

        handleForLocation(e.getPlayer(), e.getNewChestLocation(), e, getStateFlag("create-shop"));
    }

    // TODO: Outsource shop use external permission

    // @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    // public void onBuySell(ShopBuySellEvent e) {
    //     if (!Config.enableWorldGuardIntegration)
    //         return;

    //     Set<Location> chestLocations = Utils.getChestLocations(e.getShop());
    //     String flagName = e.getShop().getShopType() == ShopType.ADMIN ? "use-admin-shop" : "use-shop";
    //     IWrappedFlag<WrappedState> flag = getStateFlag(flagName);
    //     for (Location loc : chestLocations) {
    //         WrappedState state = wgWrapper.queryFlag(e.getPlayer(), loc, flag).orElse(WrappedState.DENY);
    //         if (state == WrappedState.DENY) {
    //             e.setCancelled(true);
    //             return;
    //         }
    //     }
    // }

    private boolean handleForLocation(Player player, Location loc, Cancellable e, IWrappedFlag<WrappedState> flag) {
        if (flag == null) {
            // Flag may have not been registered successfully, so ignore them.
            return false;
        }

        WrappedState state = wgWrapper.queryFlag(player, loc, flag).orElse(WrappedState.DENY);
        if (state == WrappedState.DENY) {
            e.setCancelled(true);
            plugin.debug("Cancel Reason: WorldGuard");
            return true;
        }
        return false;
    }

    private IWrappedFlag<WrappedState> getStateFlag(String flagName) {
        Optional<IWrappedFlag<WrappedState>> flagOptional = wgWrapper.getFlag(flagName, WrappedState.class);
        if (!flagOptional.isPresent()) {
            plugin.getLogger().severe("Failed to get WorldGuard state flag '" + flagName + "'.");
            plugin.debug("WorldGuard state flag '" + flagName + "' is not present!");
            return null;
        }
        return flagOptional.get();
    }
    
}