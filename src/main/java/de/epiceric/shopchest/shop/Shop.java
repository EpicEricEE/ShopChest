package de.epiceric.shopchest.shop;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.HologramFormat;
import de.epiceric.shopchest.config.Regex;
import de.epiceric.shopchest.exceptions.ChestNotFoundException;
import de.epiceric.shopchest.exceptions.NotEnoughSpaceException;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.nms.CustomBookMeta;
import de.epiceric.shopchest.nms.Hologram;
import de.epiceric.shopchest.utils.ItemUtils;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shop {

    private boolean created;
    private int id;
    private ShopChest plugin;
    private OfflinePlayer vendor;
    private ItemStack product;
    private Location location;
    private Hologram hologram;
    private ShopItem item;
    private double buyPrice;
    private double sellPrice;
    private ShopType shopType;
    private Config config;

    public Shop(int id, ShopChest plugin, OfflinePlayer vendor, ItemStack product, Location location, double buyPrice, double sellPrice, ShopType shopType) {
        this.id = id;
        this.plugin = plugin;
        this.vendor = vendor;
        this.product = product;
        this.location = location;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.shopType = shopType;
        this.config = plugin.getShopChestConfig();
    }

    public Shop(ShopChest plugin, OfflinePlayer vendor, ItemStack product, Location location, double buyPrice, double sellPrice, ShopType shopType) {
        this(-1, plugin, vendor, product, location, buyPrice, sellPrice, shopType);
    }

    public boolean create(boolean showConsoleMessages) {
        if (created) return false;

        plugin.debug("Creating shop (#" + id + ")");

        Block b = location.getBlock();
        if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST) {
            ChestNotFoundException ex = new ChestNotFoundException(String.format("No Chest found in world '%s' at location: %d; %d; %d", b.getWorld().getName(), b.getX(), b.getY(), b.getZ()));
            plugin.getShopUtils().removeShop(this, config.remove_shop_on_error);
            if (showConsoleMessages) plugin.getLogger().severe(ex.getMessage());
            plugin.debug("Failed to create shop (#" + id + ")");
            plugin.debug(ex);
            return false;
        } else if ((b.getRelative(BlockFace.UP).getType() != Material.AIR) && config.show_shop_items) {
            NotEnoughSpaceException ex = new NotEnoughSpaceException(String.format("No space above chest in world '%s' at location: %d; %d; %d", b.getWorld().getName(), b.getX(), b.getY(), b.getZ()));
            plugin.getShopUtils().removeShop(this, config.remove_shop_on_error);
            if (showConsoleMessages) plugin.getLogger().severe(ex.getMessage());
            plugin.debug("Failed to create shop (#" + id + ")");
            plugin.debug(ex);
            return false;
        }

        if (hologram == null || !hologram.exists()) createHologram();
        if (item == null) createItem();

        created = true;
        return true;
    }

    /**
     * Removes the hologram of the shop
     */
    public void removeHologram(boolean useCurrentThread) {
        if (hologram != null && hologram.exists()) {
            plugin.debug("Removing hologram (#" + id + ")");

            for (Player p : Bukkit.getOnlinePlayers()) {
                hologram.hidePlayer(p, useCurrentThread);
            }

            hologram.remove();
        }
    }

    /**
     * Removes the hologram of the shop
     */
    public void removeHologram() {
        removeHologram(false);
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
        if (config.show_shop_items) {
            plugin.debug("Creating item (#" + id + ")");

            Location itemLocation;
            ItemStack itemStack;

            itemLocation = new Location(location.getWorld(), hologram.getLocation().getX(), location.getY() + 0.9, hologram.getLocation().getZ());
            itemStack = product.clone();
            itemStack.setAmount(1);

            this.item = new ShopItem(plugin, itemStack, itemLocation);

            for (Player p : Bukkit.getOnlinePlayers()) {
                item.setVisible(p, true);
            }
        }
    }

    /**
     * Creates the hologram of the shop
     */
    private void createHologram() {
        plugin.debug("Creating hologram (#" + id + ")");

        InventoryHolder ih = getInventoryHolder();

        if (ih == null) return;

        Chest[] chests = new Chest[2];
        boolean doubleChest;

        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            Chest r = (Chest) dc.getRightSide();
            Chest l = (Chest) dc.getLeftSide();

            chests[0] = r;
            chests[1] = l;
            doubleChest = true;
        } else {
            chests[0] = (Chest) ih;
            doubleChest = false;
        }

        String[] holoText = getHologramText();
        Location holoLocation = getHologramLocation(doubleChest, chests);

        hologram = new Hologram(plugin, holoText, holoLocation);
    }

    public void updateHologramText() {
        String[] lines = getHologramText();
        String[] currentLines = hologram.getLines();

        int max = Math.max(lines.length, currentLines.length);

        for (int i = 0; i < max; i++) {
            if (i < lines.length) {
                hologram.setLine(i, lines[i]);
            } else {
                hologram.removeLine(i);
            }
        }
    }

    private String[] getHologramText() {
        List<String> lines = new ArrayList<>();

        Map<HologramFormat.Requirement, Object> requirements = new HashMap<>();

        requirements.put(HologramFormat.Requirement.VENDOR, getVendor().getName());
        requirements.put(HologramFormat.Requirement.AMOUNT, getProduct().getAmount());
        requirements.put(HologramFormat.Requirement.ITEM_TYPE, getProduct().getType() + (getProduct().getDurability() > 0 ? ":" + getProduct().getDurability() : ""));
        requirements.put(HologramFormat.Requirement.ITEM_NAME, getProduct().getItemMeta().getDisplayName());
        requirements.put(HologramFormat.Requirement.HAS_ENCHANTMENT, !LanguageUtils.getEnchantmentString(ItemUtils.getEnchantments(getProduct())).isEmpty());
        requirements.put(HologramFormat.Requirement.BUY_PRICE, getBuyPrice());
        requirements.put(HologramFormat.Requirement.SELL_PRICE, getSellPrice());
        requirements.put(HologramFormat.Requirement.HAS_POTION_EFFECT, ItemUtils.getPotionEffect(getProduct()) != null);
        requirements.put(HologramFormat.Requirement.IS_MUSIC_DISC, ItemUtils.isMusicDisc(getProduct()));
        requirements.put(HologramFormat.Requirement.IS_POTION_EXTENDED, ItemUtils.isExtendedPotion(getProduct()));
        requirements.put(HologramFormat.Requirement.IS_BOOK, ItemUtils.getBookGeneration(getProduct()) != null);
        requirements.put(HologramFormat.Requirement.ADMIN_SHOP, getShopType() == ShopType.ADMIN);
        requirements.put(HologramFormat.Requirement.NORMAL_SHOP, getShopType() == ShopType.NORMAL);
        requirements.put(HologramFormat.Requirement.IN_STOCK, Utils.getAmount(getInventoryHolder().getInventory(), getProduct()));
        requirements.put(HologramFormat.Requirement.MAX_STACK, getProduct().getMaxStackSize());

        int lineCount = plugin.getHologramFormat().getLineCount();

        for (int i = 0; i < lineCount; i++) {
            String format = plugin.getHologramFormat().getFormat(i, requirements);
            for (Regex regex : Regex.values()) {
                String replace = "";

                switch (regex) {
                    case VENDOR:
                        replace = getVendor().getName();
                        break;
                    case AMOUNT:
                        replace = String.valueOf(getProduct().getAmount());
                        break;
                    case ITEM_NAME:
                        replace = LanguageUtils.getItemName(getProduct());
                        break;
                    case ENCHANTMENT:
                        replace = LanguageUtils.getEnchantmentString(ItemUtils.getEnchantments(getProduct()));
                        break;
                    case BUY_PRICE:
                        replace = plugin.getEconomy().format(getBuyPrice());
                        break;
                    case SELL_PRICE:
                        replace = plugin.getEconomy().format(getSellPrice());
                        break;
                    case POTION_EFFECT:
                        replace = LanguageUtils.getPotionEffectName(getProduct());
                        break;
                    case MUSIC_TITLE:
                        replace = LanguageUtils.getMusicDiscName(getProduct().getType());
                        break;
                    case GENERATION:
                        CustomBookMeta.Generation gen = ItemUtils.getBookGeneration(getProduct());
                        if (gen != null) replace = LanguageUtils.getBookGenerationName(gen);
                        break;
                    case STOCK:
                        replace = String.valueOf(Utils.getAmount(getInventoryHolder().getInventory(), getProduct()));
                        break;
                }

                format = format.replace(regex.getName(), replace);
            }

            lines.add(format);
        }

        return lines.toArray(new String[lines.size()]);
    }

    private Location getHologramLocation(boolean doubleChest, Chest[] chests) {
        Block b = location.getBlock();
        Location holoLocation;

        World w = b.getWorld();
        int x = b.getX();
        int y  = b.getY();
        int z = b.getZ();

        if (doubleChest) {
            Chest r = chests[0];
            Chest l = chests[1];

            if (b.getLocation().equals(r.getLocation())) {
                if (r.getX() != l.getX()) {
                    holoLocation = new Location(w, x, y - 0.6, z + 0.5);
                } else if (r.getZ() != l.getZ()) {
                    holoLocation = new Location(w, x + 0.5, y - 0.6, z);
                } else {
                    holoLocation = new Location(w, x + 0.5, y - 0.6, z + 0.5);
                }
            } else {
                if (r.getX() != l.getX()) {
                    holoLocation = new Location(w, x + 1, y - 0.6, z + 0.5);
                } else if (r.getZ() != l.getZ()) {
                    holoLocation = new Location(w, x + 0.5, y - 0.6, z + 1);
                } else {
                    holoLocation = new Location(w, x + 0.5, y - 0.6, z + 0.5);
                }
            }
        } else {
            holoLocation = new Location(w, x + 0.5, y - 0.6, z + 0.5);
        }

        holoLocation.add(0, config.hologram_lift, 0);

        return holoLocation;
    }

    /**
     * @return Whether an ID has been assigned to the shop
     */
    public boolean hasId() {
        return id != -1;
    }

    /**
     * Assign an ID to the shop. <br/>
     * Only works for the first time!
     */
    public void setId(int id) {
        if (this.id == -1) {
            this.id = id;
        }
    }

    /**
     * @return Whether the shop has already been created
     */
    public boolean isCreated() {
        return created;
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
    public Hologram getHologram() {
        return hologram;
    }

    /**
     * @return Floating {@link ShopItem} of the shop
     */
    public ShopItem getItem() {
        return item;
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

    public enum ShopType {
        NORMAL,
        ADMIN
    }

}
