package de.epiceric.shopchest.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

public class ItemCustomNameListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemSpawn(ItemSpawnEvent e) {
        if (e.getEntity().hasMetadata("shopItem")) {
            e.getEntity().setCustomNameVisible(false);
        }
    }

}
