package de.epiceric.shopchest.interfaces;


import java.util.List;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public interface Hologram {

	public Location getLocation();
	public List<?> getEntities();
	public void showPlayer(OfflinePlayer p);
	public void hidePlayer(OfflinePlayer p);
	public boolean isVisible(OfflinePlayer p);
	
}
