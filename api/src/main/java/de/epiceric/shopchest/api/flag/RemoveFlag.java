package de.epiceric.shopchest.api.flag;

import de.epiceric.shopchest.api.ShopChest;

/**
 * Represents the flag a player has after entering the remove command
 */
public class RemoveFlag extends TimedFlag {
    public RemoveFlag(ShopChest plugin) {
        super(plugin, 15);
    }
}