package de.epiceric.shopchest.nms;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

public interface IHologram {

    /**
     * @return Location of the hologram
     */
    Location getLocation();

    /**
     * @param p Player to which the hologram should be shown
     */
    void showPlayer(OfflinePlayer p);


    /**
     * @param p Player from which the hologram should be hidden
     */
    void hidePlayer(OfflinePlayer p);

    /**
     * @param p Player to check
     * @return Whether the hologram is visible to the player
     */
    boolean isVisible(OfflinePlayer p);

    /**
     * @return Whether the hologram exists and is not dead
     */
    boolean exists();

    /**
     * Removes the hologram. <br>
     * IHologram will be hidden from all players and will be killed
     */
    void remove();

}
