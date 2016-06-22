package de.epiceric.shopchest.shop;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Regex;
import de.epiceric.shopchest.interfaces.Hologram;
import de.epiceric.shopchest.interfaces.hologram.*;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;

public class Shop {

    private int id;
    private ShopChest plugin;
    private OfflinePlayer vendor;
    private ItemStack product;
    private Location location;
    private Hologram hologram;
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

        if (hologram == null || !hologram.exists()) createHologram();
        if (item == null || item.isDead()) createItem();
    }

    public void removeHologram() {
        if (hologram != null && hologram.exists()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                hologram.hidePlayer(p);
            }

            hologram.remove();
        }
    }

    public void removeItem() {
        if (item != null && !item.isDead())
            item.remove();
    }

    private void createItem() {
        Item item;
        Location itemLocation;
        ItemStack itemStack;
        ItemMeta itemMeta = product.getItemMeta().clone();
        itemMeta.setDisplayName(UUID.randomUUID().toString());

        ArrayList<String> lore = new ArrayList<>();
        lore.add("Shop Item");
        itemMeta.setLore(lore);

        itemLocation = new Location(location.getWorld(), hologram.getLocation().getX(), location.getY() + 1, hologram.getLocation().getZ());
        itemStack = new ItemStack(product.getType(), 1, product.getDurability());
        itemStack.setItemMeta(itemMeta);

        item = location.getWorld().dropItem(itemLocation, itemStack);
        item.setVelocity(new Vector(0, 0, 0));
        item.setMetadata("shopItem", new FixedMetadataValue(plugin, true));
        item.setCustomNameVisible(false);

        this.item = item;
    }

    private void createHologram() {

        boolean doubleChest;

        Chest[] chests = new Chest[2];

        Block b = location.getBlock();

        if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {

            Chest c = (Chest) b.getState();
            InventoryHolder ih = c.getInventory().getHolder();

            if (ih instanceof DoubleChest) {
                DoubleChest dc = (DoubleChest) ih;

                Chest r = (Chest) dc.getRightSide();
                Chest l = (Chest) dc.getLeftSide();

                chests[0] = r;
                chests[1] = l;

                doubleChest = true;

            } else {
                doubleChest = false;
                chests[0] = c;
            }

        } else {
            try {
                throw new Exception("No Chest found at specified Location: " + b.getX() + "; " + b.getY() + "; " + b.getZ());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
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
        else
            holoText[1] = LanguageUtils.getMessage(LocalizedMessage.Message.HOLOGRAM_BUY_SELL, new LocalizedMessage.ReplacedRegex(Regex.BUY_PRICE, String.valueOf(buyPrice)),
                    new LocalizedMessage.ReplacedRegex(Regex.SELL_PRICE, String.valueOf(sellPrice)));

        switch (Utils.getServerVersion()) {
            case "v1_8_R1":
                hologram = new Hologram_1_8_R1(holoText, holoLocation);
                break;
            case "v1_8_R2":
                hologram = new Hologram_1_8_R2(holoText, holoLocation);
                break;
            case "v1_8_R3":
                hologram = new Hologram_1_8_R3(holoText, holoLocation);
                break;
            case "v1_9_R1":
                hologram = new Hologram_1_9_R1(holoText, holoLocation);
                break;
            case "v1_9_R2":
                hologram = new Hologram_1_9_R2(holoText, holoLocation);
                break;
            case "v1_10_R1":
                hologram = new Hologram_1_10_R1(holoText, holoLocation);
                break;
        }

    }

    public int getID() {
        return id;
    }

    public OfflinePlayer getVendor() {
        return vendor;
    }

    public ItemStack getProduct() {
        return product;
    }

    public Location getLocation() {
        return location;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public ShopType getShopType() {
        return shopType;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public enum ShopType {
        NORMAL,
        ADMIN
    }

}
