package de.epiceric.shopchest.api.flag;

import org.bukkit.GameMode;

import de.epiceric.shopchest.api.player.ShopPlayer;

/**
 * Represents the flag a player has after entering the create command
 */
public class SelectFlag implements Flag {
    public enum Type {
        ADMIN, NORMAL, EDIT
    }

    private int amount;
    private double buyPrice;
    private double sellPrice;
    private Type type;
    private GameMode gameMode;

    public SelectFlag(int amount, double buyPrice, double sellPrice, Type type, GameMode gameMode) {
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.type = type;
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
        return type == Type.ADMIN;
    }

    /**
     * Gets whether the player wants to edit a shop
     * 
     * @return whether the players wants to edit a shop
     */
    public boolean isEditingShop() {
        return type == Type.EDIT;
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
     * Gets either the shop type of the shop being created, or whether the shop
     * is being edited
     * 
     * @return the shop type or {@link Type#EDIT}
     */
    public Type getType() {
        return type;
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