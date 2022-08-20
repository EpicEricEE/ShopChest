package de.epiceric.shopchest.addon.askyblock;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class AddonMain extends JavaPlugin {
    private ShopListener shopListener;

    @Override
    public void onEnable() {
        shopListener = new ShopListener();

        getServer().getPluginManager().registerEvents(shopListener, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(shopListener);
    }
}