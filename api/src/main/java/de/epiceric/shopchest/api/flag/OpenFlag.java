package de.epiceric.shopchest.api.flag;

import de.epiceric.shopchest.api.ShopChest;

/**
 * Represents the flag a player has after entering the open command
 */
public class OpenFlag extends TimedFlag {
    public OpenFlag(ShopChest plugin) {
        super(plugin, 15);
	}
}