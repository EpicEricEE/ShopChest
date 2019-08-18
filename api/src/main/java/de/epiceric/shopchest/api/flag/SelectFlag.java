package de.epiceric.shopchest.api.flag;

import org.bukkit.GameMode;

import de.epiceric.shopchest.api.player.ShopPlayer;

/**
 * Represents the flag a player has after entering the create command
 */
public class SelectFlag implements Flag {
    private int amount;
    private double buyPrice;
    private double sellPrice;
    private boolean admin;
    private GameMode gameMode;

    public SelectFlag(int amount, double buyPrice, double sellPrice, boolean admin, GameMode gameMode) {
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.admin = admin;
        this.gameMode = gameMode;
    }

    /**
     * Gets the amount of items the player wants their shop to sell or buy
     * 
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Gets the price the player wants others to buy the product for
     * 
     * @return the buy price
     */
    public double getBuyPrice() {
        return buyPrice;
    }

    /**
     * Gets whether the player wants to create an admin shop
     * 
     * @return whether the players wants to create an admin shop
     */
    public boolean isAdminShop() {
        return admin;
    }

    /**
     * Gets the price the player wants others to sell the product for
     * 
     * @return the sell price
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * Gets the game mode the player had before entering the command
     * <p>
     * This is used for resetting the game mode when the player has selected the
     * product from the creative inventory.
     * 
     * @return the game mode
     */
    public GameMode getGameMode() {
        return gameMode;
    }

    @Override
    public void onRemove(ShopPlayer player) {
        player.getBukkitPlayer().setGameMode(getGameMode());;
    }

}