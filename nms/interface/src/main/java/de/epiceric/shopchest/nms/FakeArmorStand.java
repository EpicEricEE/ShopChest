package de.epiceric.shopchest.nms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface FakeArmorStand extends FakeEntity {

    void sendData(String name, Iterable<Player> receivers);

    void setLocation(Location location, Iterable<Player> receivers);

}
