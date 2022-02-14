package de.epiceric.shopchest.nms;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface FakeEntity {

    int getEntityId();

    void spawn(UUID uuid, Location location, Iterable<Player> receivers);

    void remove(Iterable<Player> receivers);

}
