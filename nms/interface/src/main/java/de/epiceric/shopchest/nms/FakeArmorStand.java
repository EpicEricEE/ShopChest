package de.epiceric.shopchest.nms;

import org.bukkit.Location;

public interface FakeArmorStand {

    int getEntityId();

    void sendData(String name);

    void remove();

    void setLocation(Location location);

    void spawn();

}
