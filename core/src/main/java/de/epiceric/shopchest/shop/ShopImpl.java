package de.epiceric.shopchest.shop;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.exceptions.ChestNotFoundException;
import de.epiceric.shopchest.api.shop.Shop;
import de.epiceric.shopchest.api.shop.ShopProduct;
import de.epiceric.shopchest.database.Database;
import de.epiceric.shopchest.shop.hologram.Hologram;
import de.epiceric.shopchest.util.Logger;

public class ShopImpl implements Shop {
    private int id;
    private OfflinePlayer vendor;
    private ShopProduct product;
    private Location location;
    private double buyPrice;
    private double sellPrice;
    private boolean exists;

    private Hologram hologram;

    public ShopImpl(OfflinePlayer vendor, ShopProduct product, Location location, double buyPrice, double sellPrice) {
        this(-1, vendor, product, location, buyPrice, sellPrice);
    }

    public ShopImpl(int id, OfflinePlayer vendor, ShopProduct product, Location location, double buyPrice, double sellPrice) {
        this.id = id;
        this.vendor = vendor;
        this.product = product;
        this.location = location.clone();
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    /**
     * Sets the ID of this shop
     * <p>
     * This method is only used internally and should not be used otherwise.
     * 
     * @param id the ID
     * @throws IllegalStateException when the ID has already been set
     */
    public void setId(int id) {
        if (getId() == -1) {
            this.id = id;
        } else {
            throw new IllegalStateException("ID has already been set");
        }
    }

    /**
     * Gets the location of the other chest block if this shop is on a double chest
     * 
     * @return the other location or an empty optional if there is no other chest
     */
    public Optional<Location> getOtherLocation() {
        try {
            Inventory inv = getInventory();
            if (inv instanceof DoubleChestInventory) {
                Location left = ((DoubleChestInventory) inv).getLeftSide().getLocation();
                Location right = ((DoubleChestInventory) inv).getRightSide().getLocation();

                if (location.getWorld().getName().equals(left.getWorld().getName()) &&
                        location.getBlockX() == left.getBlockX() &&
                        location.getBlockY() == left.getBlockY() &&
                        location.getBlockZ() == left.getBlockZ()) {
                    return Optional.of(right);
                } else {
                    return Optional.of(left);
                }
            }
        } catch (ChestNotFoundException e) {
            Logger.severe(e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Gets the hologram of this shop
     * 
     * @return the hologram
     */
    public Hologram getHologram() {
        return hologram;
    }

    /**
     * Creates this shop's hologram and item
     * <p>
     * This method is only used internally and should not be used otherwise.
     */
    public void create() {
        if (exists) {
            return;
        }

        hologram = new Hologram((ShopChest) Bukkit.getPluginManager().getPlugin("ShopChest"), this);
        exists = true;
    }

    /**
     * Removes this shop's hologram and item
     * <p>
     * This method is only used internally and should not be used otherwise.
     */
    public void destroy() {
        if (!exists) {
            return;
        }

        hologram.destroy();
        exists = false;
    }

    /* API Implementation */

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Optional<OfflinePlayer> getVendor() {
        return Optional.ofNullable(vendor);
    }

    @Override
    public ShopProduct getProduct() {
        return product.clone();
    }

    /**
     * Sets the product this shop is buying or selling
     * <p>
     * To update the shop in the database, it is necessary to update the shop in the database.
     * 
     * @param product the product
     * @see Database#updateShop(Shop)
     */
    public void setProduct(ShopProduct product) {
        this.product = product;
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public boolean isDoubleChest() {
        try {
            return getInventory() instanceof DoubleChestInventory;
        } catch (ChestNotFoundException e) {
            Logger.severe(e.getMessage());
            return false;
        }
    }

    @Override
    public Inventory getInventory() throws ChestNotFoundException {
        Block block = getLocation().getBlock();
        if (block.getState() instanceof Chest) {
            return ((Chest) block.getState()).getInventory();
        }
        throw new ChestNotFoundException(getLocation());
    }

    @Override
    public double getBuyPrice() {
        return buyPrice;
    }

    /**
     * Sets the price for which a player can sell the product to this shop
     * <p>
     * If set to zero, a player cannot buy from this shop.
     * <p>
     * To update the shop in the database, it is necessary to update the shop in the database.
     * 
     * @param buyPrice the buy price
     * @throws IllegalStateException when a player can neither buy nor sell from this shop
     * @see Database#updateShop(Shop)
     * @since 1.13
     */
    public void setBuyPrice(double buyPrice) {
        if (buyPrice <= 0 && !canPlayerSell()) {
            throw new IllegalStateException("Cannot set both buy price and sell price to 0");
        }
        this.buyPrice = buyPrice;
    }

    @Override
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * Sets the price for which a player can sell the product to this shop
     * <p>
     * If set to zero, a player cannot sell to this shop.
     * <p>
     * To update the shop in the database, it is necessary to update the shop in the database.
     * 
     * @param sellPrice the sell price
     * @throws IllegalStateException when a player can neither buy nor sell from this shop
     * @see Database#updateShop(Shop)
     * @since 1.13
     */
    public void setSellPrice(double sellPrice) {
        if (sellPrice <= 0 && !canPlayerBuy()) {
            throw new IllegalStateException("Cannot set both sell price and buy price to 0");
        }
        this.sellPrice = sellPrice;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Shop)) return false;
        if (getId() == -1) return super.equals(obj);
        return ((Shop) obj).getId() == getId();
    }

    @Override
    public int hashCode() {
        return getId() != -1 ? Integer.hashCode(getId()) : super.hashCode();
    }
}