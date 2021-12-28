package de.epiceric.shopchest.nms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface FakeArmorStand {

    int getEntityId();

    void sendData(String name, Iterable<Player> receivers);

    void remove();

    void setLocation(Location location);

    void spawn();

}
