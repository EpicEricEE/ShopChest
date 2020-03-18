package de.epiceric.shopchest.api.flag;

import de.epiceric.shopchest.api.ShopChest;

/**
 * Represents the flag a player has after entering the info command
 */
public class InfoFlag extends TimedFlag {
    public InfoFlag(ShopChest plugin) {
        super(plugin, 15);
    }
}