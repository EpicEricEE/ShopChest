package de.epiceric.shopchest.utils;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.external.PlotSquaredShopFlag;
import de.epiceric.shopchest.nms.CustomBookMeta;
import de.epiceric.shopchest.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.*;

import javax.xml.bind.DatatypeConverter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Utils {

    /**
     * Check if two items are similar to each other
     * @param itemStack1 The first item
     * @param itemStack2 The second item
     * @return {@code true} if the given items are similar or {@code false} if not
     */
    public static boolean isItemSimilar(ItemStack itemStack1, ItemStack itemStack2) {
        if (itemStack1 == null || itemStack2 == null) {
            return false;
        }

        boolean similar = (itemStack1.getType() == itemStack2.getType());
        similar &= (itemStack1.getDurability() == itemStack2.getDurability());
        similar &= (itemStack1.getEnchantments().equals(itemStack2.getEnchantments()));
        similar &= (itemStack1.getMaxStackSize() == itemStack2.getMaxStackSize());
        similar &= (itemStack1.getData().equals(itemStack2.getData()));

        if (!similar) return false;

        ItemMeta itemMeta1 = itemStack1.getItemMeta();
        ItemMeta itemMeta2 = itemStack2.getItemMeta();

        if (itemMeta1 instanceof BookMeta && itemMeta2 instanceof BookMeta) {
            BookMeta bookMeta1 = (BookMeta) itemStack1.getItemMeta();
            BookMeta bookMeta2 = (BookMeta) itemStack2.getItemMeta();

            if ((getMajorVersion() == 9 && getRevision() == 1) || getMajorVersion() == 8) {
                CustomBookMeta.Generation generation1 = CustomBookMeta.getGeneration(itemStack1);
                CustomBookMeta.Generation generation2 = CustomBookMeta.getGeneration(itemStack2);

                if (generation1 == null) CustomBookMeta.setGeneration(itemStack1, CustomBookMeta.Generation.ORIGINAL);
                if (generation2 == null) CustomBookMeta.setGeneration(itemStack2, CustomBookMeta.Generation.ORIGINAL);

            } else if (getMajorVersion() >= 10) {
                if (bookMeta1.getGeneration() == null) bookMeta1.setGeneration(BookMeta.Generation.ORIGINAL);
                if (bookMeta2.getGeneration() == null) bookMeta2.setGeneration(BookMeta.Generation.ORIGINAL);
            }

            itemStack1.setItemMeta(bookMeta1);
            itemStack2.setItemMeta(bookMeta2);
        }

        YamlConfiguration yml1 = new YamlConfiguration();
        YamlConfiguration yml2 = new YamlConfiguration();
        yml1.set("itemMeta", itemMeta1);
        yml2.set("itemMeta", itemMeta2);

        String sYml1 = yml1.saveToString();
        String sYml2 = yml2.saveToString();

        return sYml1.equals(sYml2);
    }

    /**
     * Gets the amount of items in an inventory
     *
     * @param inventory The inventory, in which the items are counted
     * @param itemStack The items to count
     * @return Amount of given items in the given inventory
     */
    public static int getAmount(Inventory inventory, ItemStack itemStack) {
        int amount = 0;

        ArrayList<ItemStack> inventoryItems = new ArrayList<>();

        if (inventory instanceof PlayerInventory) {
            if (getMajorVersion() >= 9) {
                inventoryItems.add(inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.add(inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.add(inventory.getItem(i));
            }
        }

        for (ItemStack item : inventoryItems) {
            if (isItemSimilar(item, itemStack)) {
                amount += item.getAmount();
            }
        }

        return amount;
    }

    /**
     * Get the amount of the given item, that fits in the given inventory
     *
     * @param inventory Inventory, where to search for free space
     * @param itemStack Item, of which the amount that fits in the inventory should be returned
     * @return Amount of the given item, that fits in the given inventory
     */
    public static int getFreeSpaceForItem(Inventory inventory, ItemStack itemStack) {
        HashMap<Integer, Integer> slotFree = new HashMap<>();

        if (inventory instanceof PlayerInventory) {
            for (int i = 0; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null || item.getType() == Material.AIR) {
                    slotFree.put(i, itemStack.getMaxStackSize());
                } else {
                    if (isItemSimilar(item, itemStack)) {
                        int amountInSlot = item.getAmount();
                        int amountToFullStack = itemStack.getMaxStackSize() - amountInSlot;
                        slotFree.put(i, amountToFullStack);
                    }
                }
            }

            if (getMajorVersion() >= 9) {
                ItemStack item = inventory.getItem(40);
                if (item == null || item.getType() == Material.AIR) {
                    slotFree.put(40, itemStack.getMaxStackSize());
                } else {
                    if (isItemSimilar(item, itemStack)) {
                        int amountInSlot = item.getAmount();
                        int amountToFullStack = itemStack.getMaxStackSize() - amountInSlot;
                        slotFree.put(40, amountToFullStack);
                    }
                }
            }
        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null || item.getType() == Material.AIR) {
                    slotFree.put(i, itemStack.getMaxStackSize());
                } else {
                    if (isItemSimilar(item, itemStack)) {
                        int amountInSlot = item.getAmount();
                        int amountToFullStack = itemStack.getMaxStackSize() - amountInSlot;
                        slotFree.put(i, amountToFullStack);
                    }
                }
            }
        }

        int freeAmount = 0;
        for (int value : slotFree.values()) {
            freeAmount += value;
        }

        return freeAmount;
    }

    /**
     * @param p Player whose item in his main hand should be returned
     * @return {@link ItemStack} in his main hand, or {@code null} if he doesn't hold one
     */
    public static ItemStack getItemInMainHand(Player p) {
        if (getMajorVersion() < 9) {
            if (p.getItemInHand().getType() == Material.AIR)
                return null;
            else
                return p.getItemInHand();
        } else {
            if (p.getInventory().getItemInMainHand().getType() == Material.AIR)
                return null;
            else
                return p.getInventory().getItemInMainHand();
        }
    }

    /**
     * @param p Player whose item in his off hand should be returned
     * @return {@link ItemStack} in his off hand, or {@code null} if he doesn't hold one or the server version is below 1.9
     */
    public static ItemStack getItemInOffHand(Player p) {
        if (getMajorVersion() < 9) {
            return null;
        } else {
            if (p.getInventory().getItemInOffHand().getType() == Material.AIR)
                return null;
            else
                return p.getInventory().getItemInOffHand();
        }
    }

    /**
     * @param p Player whose item in his hand should be returned
     * @return Item in his main hand, or the item in his off if he doesn't have one in this main hand, or {@code null}
     *         if he doesn't have one in both hands
     */
    public static ItemStack getPreferredItemInHand(Player p) {
        if (getMajorVersion() < 9) {
            return getItemInMainHand(p);
        } else {
            if (getItemInMainHand(p) != null)
                return getItemInMainHand(p);
            else
                return getItemInOffHand(p);
        }
    }

    /**
     * @param p Player to check if he has an axe in one of his hands
     * @return Whether a player has an axe in one of his hands
     */
    public static boolean hasAxeInHand(Player p) {
        List<Material> axes = Arrays.asList(Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLD_AXE, Material.DIAMOND_AXE);

        ItemStack item = getItemInMainHand(p);
        if (item == null || !axes.contains(item.getType())) {
            item = getItemInOffHand(p);
        }

        return item != null && axes.contains(item.getType());
    }

    /**
     * Check if a flag is allowed for a player on a plot from PlotSquared
     * @param plot Plot from PlotSquared
     * @param flag Flag to check
     * @param p Player to check
     * @return Whether the flag is allowed for the player
     */
    public static boolean isFlagAllowedOnPlot(Plot plot, Flag flag, Player p) {
        if (plot != null && flag != null) {
            Object o = plot.getFlag(flag, PlotSquaredShopFlag.Group.NONE);

            if (o instanceof PlotSquaredShopFlag.Group) {
                PlotSquaredShopFlag.Group group = (PlotSquaredShopFlag.Group) o;

                switch (group) {
                    case OWNERS:
                        return plot.getOwners().contains(p.getUniqueId());
                    case TRUSTED:
                        return plot.getOwners().contains(p.getUniqueId()) || plot.getTrusted().contains(p.getUniqueId());
                    case MEMBERS:
                        return plot.getOwners().contains(p.getUniqueId()) || plot.getTrusted().contains(p.getUniqueId()) || plot.getMembers().contains(p.getUniqueId());
                    case EVERYONE:
                        return true;
                    case NONE:
                        return false;
                }
            }
        }

        return true;
    }

    /**
     * @param className Name of the class
     * @return Class in {@code net.minecraft.server.[VERSION]} package with the specified name or {@code null} if the class was not found
     */
    public static Class<?> getNMSClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + getServerVersion() + "." + className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * @param className Name of the class
     * @return Class in {@code org.bukkit.craftbukkit.[VERSION]} package with the specified name or {@code null} if the class was not found
     */
    public static Class<?> getCraftClass(String className) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getServerVersion() + "." + className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Send a packet to a player
     * @param packet Packet to send
     * @param player Player to which the packet should be sent
     * @return {@code true} if the packet was sent, or {@code false} if an exception was thrown
     */
    public static boolean sendPacket(ShopChest plugin, Object packet, Player player) {
        try {
            if (packet == null) {
                plugin.debug("Failed to send packet: Packet is null");
                return false;
            }

            Class<?> packetClass = getNMSClass("Packet");
            if (packetClass == null) {
                plugin.debug("Failed to send packet: Could not find Packet class");
                return false;
            }

            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);

            playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);

            return true;
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().severe("Failed to send packet " + packet.getClass().getName());
            plugin.debug("Failed to send packet " + packet.getClass().getName());
            plugin.debug(e);
            return false;
        }
    }

    /**
     * @return The current server version with revision number (e.g. v1_9_R2, v1_10_R1)
     */
    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();

        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * @return The revision of the current server version (e.g. <i>2</i> for v1_9_R2, <i>1</i> for v1_10_R1)
     */
    public static int getRevision() {
        return Integer.parseInt(getServerVersion().substring(getServerVersion().length() - 1));
    }

    /**
     * @return The major version of the server (e.g. <i>9</i> for 1.9.2, <i>10</i> for 1.10)
     */
    public static int getMajorVersion() {
        return Integer.parseInt(getServerVersion().split("_")[1]);
    }

    /**
     * Encodes an {@link ItemStack} in a Base64 String
     * @param itemStack {@link ItemStack} to encode
     * @return Base64 encoded String
     */
    public static String encode(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i", itemStack);
        return DatatypeConverter.printBase64Binary(config.saveToString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes an {@link ItemStack} from a Base64 String
     * @param string Base64 encoded String to decode
     * @return Decoded {@link ItemStack}
     */
    public static ItemStack decode(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(DatatypeConverter.parseBase64Binary(string), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        return config.getItemStack("i", null);
    }


}
