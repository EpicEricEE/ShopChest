package de.epiceric.shopchest.interfaces;


import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.List;

public interface Hologram {

    Location getLocation();

    void showPlayer(OfflinePlayer p);

    void hidePlayer(OfflinePlayer p);

    boolean isVisible(OfflinePlayer p);

    boolean exists();

    void remove();

}
