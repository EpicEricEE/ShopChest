package de.epiceric.shopchest.shop;

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
     * @return the other location or {@code null} if there is no other chest
     */
    public Location getOtherLocation() {
        try {
            Inventory inv = getInventory();
            if (inv instanceof DoubleChestInventory) {
                Location left = ((DoubleChestInventory) inv).getLeftSide().getLocation();
                Location right = ((DoubleChestInventory) inv).getRightSide().getLocation();

                if (location.getWorld().getName().equals(left.getWorld().getName()) &&
                        location.getBlockX() == left.getBlockX() &&
                        location.getBlockY() == left.getBlockY() &&
                        location.getBlockZ() == left.getBlockZ()) {
                    return right;
                } else {
                    return left;
                }
            }
        } catch (ChestNotFoundException e) {
            Logger.severe(e.getMessage());
        }
        return null;
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
    public OfflinePlayer getVendor() {
        return vendor;
    }

    @Override
    public ShopProduct getProduct() {
        return product.clone();
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

    @Override
    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
        // TODO: update
    }

    @Override
    public double getSellPrice() {
        return sellPrice;
    }

	@Override
	public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
		// TODO: update
    }
    
    /**
     * Indicates whether some other object is "equal to" this one
     * <p>
     * Returns {@code true} if both objects are a {@link Shop} and have the same ID.
     */
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