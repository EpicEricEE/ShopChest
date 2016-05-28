package de.epiceric.shopchest.interfaces;


import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.List;

public interface Hologram {

    public Location getLocation();

    public List<?> getEntities();

    public void showPlayer(OfflinePlayer p);

    public void hidePlayer(OfflinePlayer p);

    public boolean isVisible(OfflinePlayer p);

}
