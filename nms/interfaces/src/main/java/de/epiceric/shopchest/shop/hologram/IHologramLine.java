package de.epiceric.shopchest.shop.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IHologramLine {

    void setLocation(Location location);

    void setText(String text);

    void showPlayer(Player player);

    void hidePlayer(Player player);

    void destroy();

}