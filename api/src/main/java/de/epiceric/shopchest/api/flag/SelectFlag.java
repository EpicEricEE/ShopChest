package de.epiceric.shopchest.api.flag;

import org.bukkit.GameMode;

import de.epiceric.shopchest.api.player.ShopPlayer;

/**
 * Represents the flag a player has after entering the create command
 */
public class SelectFlag implements Flag {
    private GameMode gameMode;

    public SelectFlag(GameMode gameMode) {
        this.gameMode = gameMode;
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
        player.getPlayer().setGameMode(getGameMode());;
    }

}