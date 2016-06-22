package de.epiceric.shopchest.event;

import me.minebuilders.clearlag.events.EntityRemoveEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class RegenerateShopItemAfterRemove implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityRemove(EntityRemoveEvent e) {
        ArrayList<Entity> entityList = new ArrayList<>(e.getEntityList());

        for (Entity entity : entityList) {
            if (entity.hasMetadata("shopItem")) {
                e.getEntityList().remove(entity);
            }
        }
    }


}
