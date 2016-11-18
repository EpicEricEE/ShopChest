package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.nms.CustomBookMeta;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.*;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.Potion;

import javax.xml.bind.DatatypeConverter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Utils {

    /**
     * Check if two items are similar to each other
     * @param itemStack1 The first item
     * @param itemStack2 The second item
     * @param checkAmount Whether the amount should be checked or ignored
     * @return {@code true} if the given items are similar or {@code false} if not
     */
    public static boolean isItemSimilar(ItemStack itemStack1, ItemStack itemStack2, boolean checkAmount) {
        if (itemStack1 == null || itemStack2 == null) {
            return false;
        }

        boolean similar;

        similar = (!checkAmount || (itemStack1.getAmount() == itemStack2.getAmount()));
        similar &= (itemStack1.getType() == itemStack2.getType());
        similar &= (itemStack1.getDurability() == itemStack2.getDurability());
        similar &= (itemStack1.getEnchantments().equals(itemStack2.getEnchantments()));
        similar &= (itemStack1.getMaxStackSize() == itemStack2.getMaxStackSize());

        if (!similar) {
            return false;
        }

        MaterialData itemData1 = itemStack1.getData();
        MaterialData itemData2 = itemStack2.getData();

        if (itemData1 != null && itemData2 != null) {
            similar = itemData1.getItemType() == itemData2.getItemType();
        }

        ItemMeta itemMeta1 = itemStack1.getItemMeta();
        ItemMeta itemMeta2 = itemStack2.getItemMeta();

        if (itemMeta1.hasDisplayName()) similar  = (itemMeta1.getDisplayName().equals(itemMeta2.getDisplayName()));
        if (itemMeta1.hasEnchants())    similar &= (itemMeta1.getEnchants().equals(itemMeta2.getEnchants()));
        if (itemMeta1.hasLore())        similar &= (itemMeta1.getLore().equals(itemMeta2.getLore()));

        similar &= (itemMeta1.getItemFlags().equals(itemMeta2.getItemFlags()));
        similar &= (itemMeta1.getClass().equals(itemMeta2.getClass()));

        if (!similar) {
            return false;
        }

        if (itemMeta1 instanceof BannerMeta) {
            BannerMeta bannerMeta1 = (BannerMeta) itemMeta1;
            BannerMeta bannerMeta2 = (BannerMeta) itemMeta2;

            similar = (bannerMeta1.getBaseColor() == bannerMeta2.getBaseColor());
            similar &= (bannerMeta1.getPatterns().equals(bannerMeta2.getPatterns()));

        } else if (!getServerVersion().equals("v1_8_R1") && itemMeta1 instanceof BlockStateMeta) {
            BlockStateMeta bsMeta1 = (BlockStateMeta) itemMeta1;
            BlockStateMeta bsMeta2 = (BlockStateMeta) itemMeta2;

            similar = (bsMeta1.hasBlockState() == bsMeta2.hasBlockState());

            if (bsMeta1.hasBlockState()) similar &= (bsMeta1.getBlockState().equals(bsMeta2.getBlockState()));

        } else if (itemMeta1 instanceof BookMeta) {
            BookMeta bookMeta1 = (BookMeta) itemMeta1;
            BookMeta bookMeta2 = (BookMeta) itemMeta2;

            if (bookMeta1.hasAuthor())     similar  = (bookMeta1.getAuthor().equals(bookMeta2.getAuthor()));
            if (bookMeta1.hasTitle())      similar &= (bookMeta1.getTitle().equals(bookMeta2.getTitle()));
            if (bookMeta1.hasPages())      similar &= (bookMeta1.getPages().equals(bookMeta2.getPages()));

            if ((getMajorVersion() == 9 && getRevision() == 1) || getMajorVersion() == 8) {
                CustomBookMeta.Generation generation1 = CustomBookMeta.getGeneration(itemStack1);
                CustomBookMeta.Generation generation2 = CustomBookMeta.getGeneration(itemStack2);

                if (generation1 == null) generation1 = CustomBookMeta.Generation.ORIGINAL;
                if (generation2 == null) generation2 = CustomBookMeta.Generation.ORIGINAL;

                similar &= (generation1 == generation2);

            } else if (getMajorVersion() >= 10) {
                if (bookMeta1.getGeneration() == null) bookMeta1.setGeneration(BookMeta.Generation.ORIGINAL);
                if (bookMeta2.getGeneration() == null) bookMeta2.setGeneration(BookMeta.Generation.ORIGINAL);

                similar &= (bookMeta1.getGeneration() == bookMeta2.getGeneration());
            }

        } else if (itemMeta1 instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta esMeta1 = (EnchantmentStorageMeta) itemMeta1;
            EnchantmentStorageMeta esMeta2 = (EnchantmentStorageMeta) itemMeta2;

            if (esMeta1.hasStoredEnchants()) similar = (esMeta1.getStoredEnchants().equals(esMeta2.getStoredEnchants()));

        } else if (itemMeta1 instanceof FireworkEffectMeta) {
            FireworkEffectMeta feMeta1 = (FireworkEffectMeta) itemMeta1;
            FireworkEffectMeta feMeta2 = (FireworkEffectMeta) itemMeta2;

            if (feMeta1.hasEffect()) similar = (feMeta1.getEffect().equals(feMeta2.getEffect()));

        } else if (itemMeta1 instanceof FireworkMeta) {
            FireworkMeta fireworkMeta1 = (FireworkMeta) itemMeta1;
            FireworkMeta fireworkMeta2 = (FireworkMeta) itemMeta2;

            if (fireworkMeta1.hasEffects()) similar = (fireworkMeta1.getEffects().equals(fireworkMeta2.getEnchants()));
            similar &= (fireworkMeta1.getPower() == fireworkMeta2.getPower());

        } else if (itemMeta1 instanceof LeatherArmorMeta) {
            LeatherArmorMeta laMeta1 = (LeatherArmorMeta) itemMeta1;
            LeatherArmorMeta laMeta2 = (LeatherArmorMeta) itemMeta2;

            similar = (laMeta1.getColor() == laMeta2.getColor());
        } else if (itemMeta1 instanceof MapMeta) {
            MapMeta mapMeta1 = (MapMeta) itemMeta1;
            MapMeta mapMeta2 = (MapMeta) itemMeta2;

            similar = (mapMeta1.isScaling() == mapMeta2.isScaling());
        } else if (itemMeta1 instanceof PotionMeta) {
            PotionMeta potionMeta1 = (PotionMeta) itemMeta1;
            PotionMeta potionMeta2 = (PotionMeta) itemMeta2;

            if (potionMeta1.hasCustomEffects()) similar = (potionMeta1.getCustomEffects().equals(potionMeta2.getCustomEffects()));

            if (getMajorVersion() >= 9)  {
                similar &= (potionMeta1.getBasePotionData().equals(potionMeta2.getBasePotionData()));
            } else {
                Potion potion1 = Potion.fromItemStack(itemStack1);
                Potion potion2 = Potion.fromItemStack(itemStack2);

                similar &= (potion1.getType() == potion2.getType());
                similar &= (potion1.getEffects().equals(potion2.getEffects()));
                similar &= (potion1.getLevel() == potion2.getLevel());
            }

        } else if (itemMeta1 instanceof SkullMeta) {
            SkullMeta skullMeta1 = (SkullMeta) itemMeta1;
            SkullMeta skullMeta2 = (SkullMeta) itemMeta2;

            if (skullMeta1.hasOwner()) similar = skullMeta1.getOwner().equals(skullMeta2.getOwner());
        }

        return similar;
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
            if (isItemSimilar(item, itemStack, false)) {
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
                    if (isItemSimilar(item, itemStack, false)) {
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
                    if (isItemSimilar(item, itemStack, false)) {
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
                    if (isItemSimilar(item, itemStack, false)) {
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
     * Checks if a given String is a UUID
     * @param string String to check
     * @return Whether the string is a UUID
     */
    public static boolean isUUID(String string) {
        return string.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");
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
