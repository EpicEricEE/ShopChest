package de.epiceric.shopchest.shop;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Regex;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.nms.IHologram;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.UUID;

public class Shop {

    private int id;
    private ShopChest plugin;
    private OfflinePlayer vendor;
    private ItemStack product;
    private Location location;
    private IHologram hologram;
    private Item item;
    private double buyPrice;
    private double sellPrice;
    private ShopType shopType;

    public Shop(int id, ShopChest plugin, OfflinePlayer vendor, ItemStack product, Location location, double buyPrice, double sellPrice, ShopType shopType) {
        this.id = id;
        this.plugin = plugin;
        this.vendor = vendor;
        this.product = product;
        this.location = location;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.shopType = shopType;

        Block b = location.getBlock();
        if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST) {
            try {
                plugin.getShopUtils().removeShop(this, plugin.getShopChestConfig().remove_shop_on_error);
                Exception e = new Exception("No Chest found at specified Location: " + b.getX() + "; " + b.getY() + "; " + b.getZ());
                plugin.debug(e);
                throw e;
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        } else if ((b.getRelative(BlockFace.UP).getType() != Material.AIR) && plugin.getShopChestConfig().show_shop_items) {
            try {
                plugin.getShopUtils().removeShop(this, plugin.getShopChestConfig().remove_shop_on_error);
                Exception e = new Exception("No space above chest at specified Location: " + b.getX() + "; " + b.getY() + "; " + b.getZ());
                plugin.debug(e);
                throw e;
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }

        if (hologram == null || !hologram.exists()) createHologram();
        if (item == null || item.isDead()) createItem();
    }

    private Shop(OfflinePlayer vendor, ItemStack product, Location location, double buyPrice, double sellPrice, ShopType shopType) {
        this.id = 0;
        this.vendor = vendor;
        this.product = product;
        this.location = location;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.shopType = shopType;
    }

    /**
     * Creates the hologram of the shop if it doesn't exist
     */
    public void removeHologram() {
        if (hologram != null && hologram.exists()) {
            plugin.debug("Removing hologram (#" + id + ")");

            for (Player p : Bukkit.getOnlinePlayers()) {
                hologram.hidePlayer(p);
            }

            hologram.remove();
        }
    }

    /**
     * Removes the floating item of the shop
     */
    public void removeItem() {
        if (item != null) {
            plugin.debug("Removing shop item (#" + id + ")");
            item.remove();
        }
    }

    /**
     * <p>Creates the floating item of the shop</p>
     * <b>Call this after {@link #createHologram()}, because it depends on the hologram's location</b>
     */
    private void createItem() {
        if (plugin.getShopChestConfig().show_shop_items) {
            plugin.debug("Creating item (#" + id + ")");

            Item item;
            Location itemLocation;
            ItemStack itemStack;
            ItemMeta itemMeta = product.getItemMeta();
            itemMeta.setDisplayName(UUID.randomUUID().toString());

            itemLocation = new Location(location.getWorld(), hologram.getLocation().getX(), location.getY() + 1, hologram.getLocation().getZ());
            itemStack = new ItemStack(product);
            itemStack.setAmount(1);
            itemStack.setItemMeta(itemMeta);

            item = location.getWorld().dropItem(itemLocation, itemStack);
            item.setVelocity(new Vector(0, 0, 0));
            item.setMetadata("shopItem", new FixedMetadataValue(plugin, true));
            item.setMetadata("shopId", new FixedMetadataValue(plugin, id));
            item.setCustomNameVisible(false);
            item.setPickupDelay(Integer.MAX_VALUE);

            this.item = item;
        }
    }

    /**
     * Creates the hologram of the shop
     */
    private void createHologram() {
        plugin.debug("Creating hologram (#" + id + ")");

        boolean doubleChest;

        Chest[] chests = new Chest[2];
        Block b = location.getBlock();
        InventoryHolder ih = getInventoryHolder();

        if (ih == null) return;

        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;

            Chest r = (Chest) dc.getRightSide();
            Chest l = (Chest) dc.getLeftSide();

            chests[0] = r;
            chests[1] = l;

            doubleChest = true;

        } else {
            doubleChest = false;
            chests[0] = (Chest) ih;
        }

        Location holoLocation;
        String[] holoText = new String[2];

        if (doubleChest) {

            Chest r = chests[0];
            Chest l = chests[1];

            if (b.getLocation().equals(r.getLocation())) {

                if (r.getX() != l.getX())
                    holoLocation = new Location(b.getWorld(), b.getX(), b.getY() - 0.6, b.getZ() + 0.5);
                else if (r.getZ() != l.getZ())
                    holoLocation = new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 0.6, b.getZ());
                else holoLocation = new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 0.6, b.getZ() + 0.5);

            } else {

                if (r.getX() != l.getX())
                    holoLocation = new Location(b.getWorld(), b.getX() + 1, b.getY() - 0.6, b.getZ() + 0.5);
                else if (r.getZ() != l.getZ())
                    holoLocation = new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 0.6, b.getZ() + 1);
                else holoLocation = new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 0.6, b.getZ() + 0.5);

            }

        } else holoLocation = new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 0.6, b.getZ() + 0.5);

        holoText[0] = LanguageUtils.getMessage(LocalizedMessage.Message.HOLOGRAM_FORMAT, new LocalizedMessage.ReplacedRegex(Regex.AMOUNT, String.valueOf(product.getAmount())),
                new LocalizedMessage.ReplacedRegex(Regex.ITEM_NAME, LanguageUtils.getItemName(product)));

        if ((buyPrice <= 0) && (sellPrice > 0))
            holoText[1] = LanguageUtils.getMessage(LocalizedMessage.Message.HOLOGRAM_SELL, new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, String.valueOf(sellPrice)));
        else if ((buyPrice > 0) && (sellPrice <= 0))
            holoText[1] = LanguageUtils.getMessage(LocalizedMessage.Message.HOLOGRAM_BUY, new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, String.valueOf(buyPrice)));
        else if ((buyPrice > 0) && (sellPrice > 0))
            holoText[1] = LanguageUtils.getMessage(LocalizedMessage.Message.HOLOGRAM_BUY_SELL, new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, String.valueOf(buyPrice)),
                    new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, String.valueOf(sellPrice)));
        else holoText[1] = LanguageUtils.getMessage(LocalizedMessage.Message.HOLOGRAM_BUY_SELL, new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, String.valueOf(buyPrice)),
                    new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, String.valueOf(sellPrice)));

        switch (Utils.getServerVersion()) {
            case "v1_8_R1":
                hologram = new de.epiceric.shopchest.nms.v1_8_R1.Hologram(holoText, holoLocation);
                break;
            case "v1_8_R2":
                hologram = new de.epiceric.shopchest.nms.v1_8_R2.Hologram(holoText, holoLocation);
                break;
            case "v1_8_R3":
                hologram = new de.epiceric.shopchest.nms.v1_8_R3.Hologram(holoText, holoLocation);
                break;
            case "v1_9_R1":
                hologram = new de.epiceric.shopchest.nms.v1_9_R1.Hologram(holoText, holoLocation);
                break;
            case "v1_9_R2":
                hologram = new de.epiceric.shopchest.nms.v1_9_R2.Hologram(holoText, holoLocation);
                break;
            case "v1_10_R1":
                hologram = new de.epiceric.shopchest.nms.v1_10_R1.Hologram(holoText, holoLocation);
                break;
        }

    }

    /**
     * @return The ID of the shop
     */
    public int getID() {
        return id;
    }

    /**
     * @return Vendor of the shop; probably the creator of it
     */
    public OfflinePlayer getVendor() {
        return vendor;
    }

    /**
     * @return Product the shop sells (or buys)
     */
    public ItemStack getProduct() {
        return product;
    }

    /**
     * @return Location of (one of) the shop's chest
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return Buy price of the shop
     */
    public double getBuyPrice() {
        return buyPrice;
    }

    /**
     * @return Sell price of the shop
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * @return Type of the shop
     */
    public ShopType getShopType() {
        return shopType;
    }

    /**
     * @return Hologram of the shop
     */
    public IHologram getHologram() {
        return hologram;
    }

    /**
     * @return {@link InventoryHolder} of the shop or <b>null</b> if the shop has no chest.
     */
    public InventoryHolder getInventoryHolder() {
        Block b = getLocation().getBlock();

        if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
            Chest chest = (Chest) b.getState();
            return chest.getInventory().getHolder();
        }

        return null;
    }

    /**
     * @return A shop, which is not really a shop. It's just for "storing" the data (used in some events).
     */
    public static Shop createImaginaryShop(OfflinePlayer vendor, ItemStack product, Location location, double buyPrice, double sellPrice, ShopType shopType) {
        return new Shop(vendor, product, location, buyPrice, sellPrice, shopType);
    }

    public enum ShopType {
        NORMAL,
        ADMIN
    }

}
