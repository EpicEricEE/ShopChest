package de.epiceric.shopchest.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.shop.ShopProduct;

public class ClickType {
    private static Map<UUID, ClickType> playerClickType = new HashMap<>();
    private static Map<UUID, BukkitTask> playerTimers = new HashMap<>();

    private EnumClickType enumClickType;

    public ClickType(EnumClickType enumClickType) {
        this.enumClickType = enumClickType;
    }

    /**
     * Clear click types, cancel timers, and reset game modes
     */
    public static void clear() {
        playerClickType.forEach((uuid, ct) -> {
            if (ct instanceof SelectClickType) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null)
                    p.setGameMode(((SelectClickType) ct).getGameMode());
            }
        });
        playerTimers.forEach((uuid, timer) -> timer.cancel());
    }

    /**
     * Gets the click type of a player
     *
     * @param player Player whose click type should be gotten
     * @return The Player's click type or <b>null</b> if he doesn't have one
     */
    public static ClickType getPlayerClickType(OfflinePlayer player) {
        return playerClickType.get(player.getUniqueId());
    }

    /**
     * Removes the click type from a player and cancels the 15 second timer
     * 
     * @param player Player to remove the click type from
     */
    public static void removePlayerClickType(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        if (playerClickType.get(uuid) instanceof SelectClickType && player instanceof Player) {
            // Reset gamemode player has select click type
            ((Player) player).setGameMode(((SelectClickType) playerClickType.get(uuid)).gameMode);
        }
        playerClickType.remove(uuid);

        // If a timer is still running, cancel it
        Optional.ofNullable(playerTimers.get(uuid)).ifPresent(task -> task.cancel());
        playerTimers.remove(uuid);
    }

    /**
     * Sets the click type of a player and removes it after 15 seconds
     *
     * @param player    Player whose click type should be set
     * @param clickType Click type to set
     */
    public static void setPlayerClickType(OfflinePlayer player, ClickType clickType) {
        UUID uuid = player.getUniqueId();
        if (playerClickType.get(uuid) instanceof SelectClickType && player instanceof Player) {
            // Reset gamemode player has select click type
            ((Player) player).setGameMode(((SelectClickType) playerClickType.get(uuid)).gameMode);
        }
        playerClickType.put(uuid, clickType);

        // If a timer is already running, cancel it
        Optional.ofNullable(playerTimers.get(uuid)).ifPresent(task -> task.cancel());

        if (clickType.getClickType() != EnumClickType.SELECT_ITEM) {
            // Remove ClickType after 15 seconds if player has not clicked a chest
            playerTimers.put(uuid, new BukkitRunnable() {
                @Override
                public void run() {
                    playerClickType.remove(uuid);
                }
            }.runTaskLater(ShopChest.getInstance(), 300));
        }
    }

    /**
     * @return Type of the click type
     */
    public EnumClickType getClickType() {
        return enumClickType;
    }

    public enum EnumClickType {
        CREATE, REMOVE, INFO, OPEN, SELECT_ITEM
    }

    public static class CreateClickType extends ClickType {
        private ShopProduct product;
        private double buyPrice;
        private double sellPrice;
        private ShopType shopType;

        public CreateClickType(ShopProduct product, double buyPrice, double sellPrice, ShopType shopType) {
            super(EnumClickType.CREATE);
            this.product = product;
            this.sellPrice = sellPrice;
            this.buyPrice = buyPrice;
            this.shopType = shopType;
        }

        /**
         * Returns the item, the player has hold in his hands
         */
        public ShopProduct getProduct() {
            return product;
        }

        /**
         * Returns the buy price, the player has entered
         */
        public double getBuyPrice() {
            return buyPrice;
        }

        /**
         * Returns the sell price, the player has entered
         */
        public double getSellPrice() {
            return sellPrice;
        }

        /**
         * Returns the shop type, the player has entered
         */
        public ShopType getShopType() {
            return shopType;
        }
    }

    public static class SelectClickType extends ClickType {
        private ItemStack itemStack;
        private GameMode gameMode;
        private int amount;
        private double buyPrice;
        private double sellPrice;
        private ShopType shopType;

        public SelectClickType(GameMode gameMode, int amount, double buyPrice, double sellPrice, ShopType shopType) {
            super(EnumClickType.SELECT_ITEM);
            this.gameMode = gameMode;
            this.amount = amount;
            this.sellPrice = sellPrice;
            this.buyPrice = buyPrice;
            this.shopType = shopType;
        }

        /**
         * Returns the selected item (or {@code null} if no item has been selected)
         */
        public ItemStack getItem() {
            return itemStack;
        }

        /**
         * Sets the selected item
         * @param itemStack The item to set as selected
         */
        public void setItem(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        /**
         * Returns the gamemode, the player was in before entering creative mode
         */
        public GameMode getGameMode() {
            return gameMode;
        }

        /**
         * Returns the amount, the player has entered
         */
        public int getAmount() {
            return amount;
        }

        /**
         * Returns the buy price, the player has entered
         */
        public double getBuyPrice() {
            return buyPrice;
        }

        /**
         * Returns the sell price, the player has entered
         */
        public double getSellPrice() {
            return sellPrice;
        }

        /**
         * Returns the shop type, the player has entered
         */
        public ShopType getShopType() {
            return shopType;
        }
    }

}
