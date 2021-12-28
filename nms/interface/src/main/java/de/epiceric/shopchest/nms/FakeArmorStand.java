package de.epiceric.shopchest.nms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface FakeArmorStand {

    int getEntityId();

    void sendData(String name, Iterable<Player> receivers);

    void remove(Iterable<Player> receivers);

    void setLocation(Location location, Iterable<Player> receivers);

    void spawn(Iterable<Player> receivers);

}
