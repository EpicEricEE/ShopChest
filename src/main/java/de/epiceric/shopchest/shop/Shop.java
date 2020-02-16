package de.epiceric.shopchest.shop;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.HologramFormat;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.exceptions.ChestNotFoundException;
import de.epiceric.shopchest.exceptions.NotEnoughSpaceException;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.nms.Hologram;
import de.epiceric.shopchest.utils.ItemUtils;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Shop {

    public enum ShopType {
        NORMAL,
        ADMIN,
    }

    private static class PreCreateResult {
        private final Inventory inventory;
        private final Chest[] chests;
        private final BlockFace face;

        private PreCreateResult(Inventory inventory, Chest[] chests, BlockFace face) {
            this.inventory = inventory;
            this.chests = chests;
            this.face = face;
        }
    }

    private final ShopChest plugin;
    private final OfflinePlayer vendor;
    private final ShopProduct product;
    private final Location location;
    private final double buyPrice;
    private final double taxedBuyPrice;
    private final double sellPrice;
    private final ShopType shopType;

    private boolean created;
    private int id;
    private Hologram hologram;
    private Location holoLocation;
    private ShopItem item;

    public Shop(int id, ShopChest plugin, OfflinePlayer vendor, ShopProduct product, Location location, double buyPrice, double sellPrice, ShopType shopType) {
        this.id = id;
        this.plugin = plugin;
        this.vendor = vendor;
        this.product = product;
        this.location = location;
        this.buyPrice = buyPrice;
        this.taxedBuyPrice = buyPrice + calculateVat(buyPrice, Config.vat, Config.allowDecimalsInPrice);
        this.sellPrice = sellPrice;
        this.shopType = shopType;
    }

    public Shop(ShopChest plugin, OfflinePlayer vendor, ShopProduct product, Location location, double buyPrice, double sellPrice, ShopType shopType) {
        this(-1, plugin, vendor, product, location, buyPrice, sellPrice, shopType);
    }

    /**
     * Calculate VAT on the given price
     *
     * @param price to make calculation on
     * @param vat in percentage
     * @param allowDecimal if round to int
     * @return VAT, returns 0 if price is equal to 0
     */
    private double calculateVat(double price, double vat, boolean allowDecimal) {
        if (Double.compare(vat, 0) == 0) { // zero if disabled
            return 0;
        }

        double result = vat * price;
        return allowDecimal ? result : (int) Math.max(result, 1);
    }

    /**
     * Test if this shop is equals to another
     *
     * @param o Other object to test against
     * @return true if we are sure they are the same, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Shop shop = (Shop) o;

        // id = -1 means temp shop
        return id != -1 && id == shop.id;
    }

    @Override
    public int hashCode() {
        return id != -1 ? id : super.hashCode();
    }

    /**
     * Create the shop
     *
     * @param showConsoleMessages to log exceptions to console
     * @return Whether is was created or not
     */
    public boolean create(boolean showConsoleMessages) {
        if (created) return false;

        plugin.debug("Creating shop (#" + id + ")");

        Block b = location.getBlock();
        if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST) {
            ChestNotFoundException ex = new ChestNotFoundException(String.format("No Chest found in world '%s' at location: %d; %d; %d",
                    b.getWorld().getName(), b.getX(), b.getY(), b.getZ()));
            plugin.getShopUtils().removeShop(this, Config.removeShopOnError);
            if (showConsoleMessages) plugin.getLogger().severe(ex.getMessage());
            plugin.debug("Failed to create shop (#" + id + ")");
            plugin.debug(ex);
            return false;
        } else if ((!ItemUtils.isAir(b.getRelative(BlockFace.UP).getType()))) {
            NotEnoughSpaceException ex = new NotEnoughSpaceException(String.format("No space above chest in world '%s' at location: %d; %d; %d",
                    b.getWorld().getName(), b.getX(), b.getY(), b.getZ()));
            plugin.getShopUtils().removeShop(this, Config.removeShopOnError);
            if (showConsoleMessages) plugin.getLogger().severe(ex.getMessage());
            plugin.debug("Failed to create shop (#" + id + ")");
            plugin.debug(ex);
            return false;
        }

        PreCreateResult preResult = preCreateHologram();

        if (preResult == null) {
            return false;
        }

        plugin.getShopCreationThreadPool().execute(() -> {
            if (hologram == null || !hologram.exists()) createHologram(preResult);
            if (item == null) createItem();

            // Update shops for players in the same world after creation has finished
            plugin.getUpdater().queue(() -> {
                for (Player player : location.getWorld().getPlayers()) {
                    plugin.getShopUtils().resetPlayerLocation(player);
                }
            });
            plugin.getUpdater().updateShops(location.getWorld());
        });

        created = true;
        return true;
    }

    /**
     * Removes the hologram of the shop
     */
    public void removeHologram() {
        if (hologram != null && hologram.exists()) {
            plugin.debug("Removing hologram (#" + id + ")");
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
        plugin.debug("Creating item (#" + id + ")");

        Location itemLocation;

        itemLocation = new Location(location.getWorld(), holoLocation.getX(), location.getY() + 0.9, holoLocation.getZ());
        item = new ShopItem(plugin, product.getItemStack(), itemLocation);
    }

    /**
     * Runs everything that needs to be called synchronously in order 
     * to prepare creating the hologram.
     */
    private PreCreateResult preCreateHologram() {
        plugin.debug("Creating hologram (#" + id + ")");

        InventoryHolder ih = getInventoryHolder();

        if (ih == null) return null;

        Chest[] chests = new Chest[2];
        BlockFace face;

        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            Chest r = (Chest) dc.getRightSide();
            Chest l = (Chest) dc.getLeftSide();

            chests[0] = r;
            chests[1] = l;
        } else {
            chests[0] = (Chest) ih;
        }

        if (Utils.getMajorVersion() < 13) {
            face = ((org.bukkit.material.Directional) chests[0].getData()).getFacing();
        } else {
            face = ((Directional) chests[0].getBlockData()).getFacing();
        }

        return new PreCreateResult(ih.getInventory(), chests, face);
    }

    /**
     * Acuatlly creates the hologram (async)
     */
    private void createHologram(PreCreateResult preResult) {
        String[] holoText = getHologramText(preResult.inventory);
        holoLocation = getHologramLocation(preResult.chests, preResult.face);

        new BukkitRunnable(){
            @Override
            public void run() {
                hologram = new Hologram(plugin, holoText, holoLocation);
            }
        }.runTask(plugin);
    }

    /**
     * Keep hologram text up to date.
     * <p><b>Has to be called synchronously!</b></p>
     */
    public void updateHologramText() {
        String[] lines = getHologramText(getInventoryHolder().getInventory());
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

    private String[] getHologramText(Inventory inventory) {
        List<String> lines = new ArrayList<>();

        ItemStack itemStack = getProduct().getItemStack();

        Map<HologramFormat.Requirement, Object> requirements = new EnumMap<>(HologramFormat.Requirement.class);
        requirements.put(HologramFormat.Requirement.VENDOR, getVendor().getName());
        requirements.put(HologramFormat.Requirement.AMOUNT, getProduct().getAmount());
        requirements.put(HologramFormat.Requirement.ITEM_TYPE, itemStack.getType() + (itemStack.getDurability() > 0 ? ":" + itemStack.getDurability() : ""));
        requirements.put(HologramFormat.Requirement.ITEM_NAME, itemStack.hasItemMeta() ? itemStack.getItemMeta().getDisplayName() : null);
        requirements.put(HologramFormat.Requirement.HAS_ENCHANTMENT, !LanguageUtils.getEnchantmentString(ItemUtils.getEnchantments(itemStack)).isEmpty());
        requirements.put(HologramFormat.Requirement.BUY_PRICE, getBuyPrice());
        requirements.put(HologramFormat.Requirement.SELL_PRICE, getSellPrice());
        requirements.put(HologramFormat.Requirement.HAS_POTION_EFFECT, ItemUtils.getPotionEffect(itemStack) != null);
        requirements.put(HologramFormat.Requirement.IS_MUSIC_DISC, itemStack.getType().isRecord());
        requirements.put(HologramFormat.Requirement.IS_POTION_EXTENDED, ItemUtils.isExtendedPotion(itemStack));
        requirements.put(HologramFormat.Requirement.IS_WRITTEN_BOOK, itemStack.getType() == Material.WRITTEN_BOOK);
        requirements.put(HologramFormat.Requirement.IS_BANNER_PATTERN, ItemUtils.isBannerPattern(itemStack));
        requirements.put(HologramFormat.Requirement.ADMIN_SHOP, getShopType() == ShopType.ADMIN);
        requirements.put(HologramFormat.Requirement.NORMAL_SHOP, getShopType() == ShopType.NORMAL);
        requirements.put(HologramFormat.Requirement.IN_STOCK, Utils.getAmount(inventory, itemStack));
        requirements.put(HologramFormat.Requirement.MAX_STACK, itemStack.getMaxStackSize());
        requirements.put(HologramFormat.Requirement.CHEST_SPACE, Utils.getFreeSpaceForItem(inventory, itemStack));
        requirements.put(HologramFormat.Requirement.DURABILITY, itemStack.getDurability());

        Map<Placeholder, Object> placeholders = new EnumMap<>(Placeholder.class);
        placeholders.put(Placeholder.VENDOR, getVendor().getName());
        placeholders.put(Placeholder.AMOUNT, getProduct().getAmount());
        placeholders.put(Placeholder.ITEM_NAME, getProduct().getLocalizedName());
        placeholders.put(Placeholder.ENCHANTMENT, LanguageUtils.getEnchantmentString(ItemUtils.getEnchantments(itemStack)));
        placeholders.put(Placeholder.BUY_PRICE, getBuyPrice());
        placeholders.put(Placeholder.BUY_TAXED_PRICE, getTaxedBuyPrice());
        placeholders.put(Placeholder.SELL_PRICE, getSellPrice());
        placeholders.put(Placeholder.POTION_EFFECT, LanguageUtils.getPotionEffectName(itemStack));
        placeholders.put(Placeholder.MUSIC_TITLE, LanguageUtils.getMusicDiscName(itemStack.getType()));
        placeholders.put(Placeholder.BANNER_PATTERN_NAME, LanguageUtils.getBannerPatternName(itemStack.getType()));
        placeholders.put(Placeholder.GENERATION, LanguageUtils.getBookGenerationName(itemStack));
        placeholders.put(Placeholder.STOCK, Utils.getAmount(inventory, itemStack));
        placeholders.put(Placeholder.MAX_STACK, itemStack.getMaxStackSize());
        placeholders.put(Placeholder.CHEST_SPACE, Utils.getFreeSpaceForItem(inventory, itemStack));
        placeholders.put(Placeholder.DURABILITY, itemStack.getDurability());

        int lineCount = plugin.getHologramFormat().getLineCount();

        for (int i = 0; i < lineCount; i++) {
            String format = plugin.getHologramFormat().getFormat(i, requirements, placeholders);
            for (Placeholder placeholder : placeholders.keySet()) {
                String replace;

                switch (placeholder) {
                    case BUY_PRICE:
                        replace = plugin.getEconomy().format(getTaxedBuyPrice());
                        break;
                    case SELL_PRICE:
                        replace = plugin.getEconomy().format(getSellPrice());
                        break;
                    default:
                        replace = String.valueOf(placeholders.get(placeholder));
                }

                format = format.replace(placeholder.toString(), replace);
            }

            if (!format.isEmpty()) {
                lines.add(format);
            }
        }

        return lines.toArray(new String[0]);
    }

    private Location getHologramLocation(Chest[] chests, BlockFace face) {
        World w = location.getWorld();
        int x = location.getBlockX();
        int y  = location.getBlockY();
        int z = location.getBlockZ();

        Location holoLocation = new Location(w, x, y, z);

        double deltaY = -0.6;

        if (Config.hologramFixedBottom) deltaY = -0.85;

        if (chests[1] != null) {
            Chest c1 = Utils.getMajorVersion() >= 13 && (face == BlockFace.NORTH || face == BlockFace.EAST) ? chests[1] : chests[0];
            Chest c2 = Utils.getMajorVersion() >= 13 && (face == BlockFace.NORTH || face == BlockFace.EAST) ? chests[0] : chests[1];

            if (holoLocation.equals(c1.getLocation())) {
                if (c1.getX() != c2.getX()) {
                    holoLocation.add(0, deltaY, 0.5);
                } else if (c1.getZ() != c2.getZ()) {
                    holoLocation.add(0.5, deltaY, 0);
                } else {
                    holoLocation.add(0.5, deltaY, 0.5);
                }
            } else {
                if (c1.getX() != c2.getX()) {
                    holoLocation.add(1, deltaY, 0.5);
                } else if (c1.getZ() != c2.getZ()) {
                    holoLocation.add(0.5, deltaY, 1);
                } else {
                    holoLocation.add(0.5, deltaY, 0.5);
                }
            }
        } else {
            holoLocation.add(0.5, deltaY, 0.5);
        }

        holoLocation.add(0, Config.hologramLift, 0);

        return holoLocation;
    }

    /**
     * @return Whether an ID has been assigned to the shop
     */
    public boolean hasId() {
        return id != -1;
    }

    /**
     * <p>Assign an ID to the shop.</p>
     * Only works for the first time!
     * @param id ID to set for this shop
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
    public ShopProduct getProduct() {
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
     * @return Taxed price of the shop
     */
    public double getTaxedBuyPrice() {
        return taxedBuyPrice;
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

    public boolean hasHologram() {
        return hologram != null;
    }

    public boolean hasItem() {
        return item != null;
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

}
