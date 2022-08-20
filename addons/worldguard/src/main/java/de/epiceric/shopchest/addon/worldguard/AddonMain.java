package de.epiceric.shopchest.addon.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import de.epiceric.shopchest.api.ShopChest;

public class AddonMain extends JavaPlugin {
    public static final StateFlag SHOP_CREATE_FLAG = new StateFlag("shop-creation", false);
    public static final StateFlag SHOP_USE_FLAG = new StateFlag("shop-usage", true);

    private WorldGuardListener worldGuardListener;
    private ShopListener shopListener;

    @Override
    public void onEnable() {
        ShopChest shopChest = getPlugin(ShopChest.class);

        worldGuardListener = new WorldGuardListener(shopChest);
        shopListener = new ShopListener();
        
        try {
            WorldGuard.getInstance().getFlagRegistry().register(SHOP_CREATE_FLAG);
            WorldGuard.getInstance().getFlagRegistry().register(SHOP_USE_FLAG);
        } catch (FlagConflictException ignored) {
            // Flags are probably already registered
        } catch (IllegalStateException ex) {
            getLogger().warning("Failed to register WorldGuard flags");
        }
        
        getServer().getPluginManager().registerEvents(worldGuardListener, this);
        getServer().getPluginManager().registerEvents(shopListener, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(worldGuardListener);
        HandlerList.unregisterAll(shopListener);
        
        // It's not possible to unregister flags at the time of writing
    }
}