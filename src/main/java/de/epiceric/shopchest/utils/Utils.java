package de.epiceric.shopchest.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;
import de.epiceric.shopchest.nms.CustomBookMeta;
import de.epiceric.shopchest.nms.JsonBuilder;
import de.epiceric.shopchest.shop.Shop;

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
            } else {
                if (bookMeta1.getGeneration() == null) bookMeta1.setGeneration(BookMeta.Generation.ORIGINAL);
                if (bookMeta2.getGeneration() == null) bookMeta2.setGeneration(BookMeta.Generation.ORIGINAL);
            }

            itemStack1.setItemMeta(bookMeta1);
            itemStack2.setItemMeta(bookMeta2);

            itemStack1 = decode(encode(itemStack1));
            itemStack2 = decode(encode(itemStack2));
        }

        return itemStack1.isSimilar(itemStack2);
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
            for (int i = 0; i < 37; i++) {
                if (i == 36) {
                    if (getMajorVersion() < 9) {
                        break;
                    }
                    i = 40;
                }
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
            for (int i = 0; i < 37; i++) {
                if (i == 36) {
                    if (getMajorVersion() < 9) {
                        break;
                    }
                    i = 40;
                }

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
        }

        if (p.getInventory().getItemInMainHand().getType() == Material.AIR)
            return null;
        else
            return p.getInventory().getItemInMainHand();
    }

    /**
     * @param p Player whose item in his off hand should be returned
     * @return {@link ItemStack} in his off hand, or {@code null} if he doesn't hold one or the server version is below 1.9
     */
    public static ItemStack getItemInOffHand(Player p) {
        if (getMajorVersion() < 9)
            return null;
        else if (p.getInventory().getItemInOffHand().getType() == Material.AIR)
            return null;
        else
            return p.getInventory().getItemInOffHand();
    }

    /**
     * @param p Player whose item in his hand should be returned
     * @return Item in his main hand, or the item in his off if he doesn't have one in this main hand, or {@code null}
     *         if he doesn't have one in both hands
     */
    public static ItemStack getPreferredItemInHand(Player p) {
        if (getMajorVersion() < 9)
            return getItemInMainHand(p);
        else if (getItemInMainHand(p) != null)
            return getItemInMainHand(p);
        else
            return getItemInOffHand(p);
    }

    /**
     * @param p Player to check if he has an axe in one of his hands
     * @return Whether a player has an axe in one of his hands
     */
    public static boolean hasAxeInHand(Player p) {
        List<String> axes;
        if (Utils.getMajorVersion() < 13)
            axes = Arrays.asList("WOOD_AXE", "STONE_AXE", "IRON_AXE", "GOLD_AXE", "DIAMOND_AXE");
        else 
            axes = Arrays.asList("WOODEN_AXE", "STONE_AXE", "IRON_AXE", "GOLDEN_AXE", "DIAMOND_AXE");

        ItemStack item = getItemInMainHand(p);
        if (item == null || !axes.contains(item.getType().toString())) {
            item = getItemInOffHand(p);
        }

        return item != null && axes.contains(item.getType().toString());
    }

    /**
     * <p>Check if a player is allowed to create a shop that sells or buys the given item.</p>
     * @param player Player to check
     * @param item Item to be sold or bought
     * @param buy Whether buying should be enabled
     * @param sell Whether selling should be enabled
     * @return Whether the player is allowed
     */
    public static boolean hasPermissionToCreateShop(Player player, ItemStack item, boolean buy, boolean sell) {
        if (hasPermissionToCreateShop(player, item, Permissions.CREATE)) {
            return true;
        } else if (!sell && buy && hasPermissionToCreateShop(player, item,Permissions.CREATE_BUY)) {
            return true;
        } else if (!buy && sell && hasPermissionToCreateShop(player, item, Permissions.CREATE_SELL)) {
            return true;
        } else if (buy && sell && hasPermissionToCreateShop(player, item, Permissions.CREATE_BUY, Permissions.CREATE_SELL)) {
            return true;
        }

        return false;
    }

    private static boolean hasPermissionToCreateShop(Player player, ItemStack item, String... permissions) {
        for (String permission : permissions) {
            boolean b1 = false;
            boolean b2 = false;
            boolean b3 = false;

            if (player.hasPermission(permission)) {
                b1 = true;
            }

            if (item != null) {
                if (item.getDurability() == 0) {
                    String perm1 = permission + "." + item.getType().toString();
                    String perm2 = permission + "." + item.getType().toString() + ".0";

                    if (player.hasPermission(perm1) || player.hasPermission(perm2)) {
                        b2 = true;
                    }
                }

                if (player.hasPermission(permission + "." + item.getType().toString() + "." + item.getDurability())) {
                    b3 = true;
                }
            }

            if (!(b1 || b2 || b3)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get a set for the location(s) of the shop's chest(s)
     * @param shop The shop
     * @return A set of 1 or 2 locations
     */
    public static Set<Location> getChestLocations(Shop shop) {
        Set<Location> chestLocations = new HashSet<>();
        InventoryHolder ih = shop.getInventoryHolder();
        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            chestLocations.add(((Chest) dc.getLeftSide()).getLocation());
            chestLocations.add(((Chest) dc.getRightSide()).getLocation());
        } else {
            chestLocations.add(shop.getLocation());
        }
        return chestLocations;
    }

    /**
     * Send a clickable update notification to the given player.
     * @param plugin An instance of the {@link ShopChest} plugin
     * @param p The player to receive the notification
     */
    public static void sendUpdateMessage(ShopChest plugin, Player p) {
        JsonBuilder jb = new JsonBuilder(plugin);
        Map<String, JsonBuilder.Part> hoverEvent = new HashMap<>();
        hoverEvent.put("action", new JsonBuilder.Part("show_text"));
        hoverEvent.put("value", new JsonBuilder.Part(LanguageUtils.getMessage(Message.UPDATE_CLICK_TO_DOWNLOAD)));

        Map<String, JsonBuilder.Part> clickEvent = new HashMap<>();
        clickEvent.put("action", new JsonBuilder.Part("open_url"));
        clickEvent.put("value", new JsonBuilder.Part(plugin.getDownloadLink()));

        JsonBuilder.PartMap rootPart = JsonBuilder.parse(LanguageUtils.getMessage(Message.UPDATE_AVAILABLE,
                new Replacement(Placeholder.VERSION, plugin.getLatestVersion()))).toMap();
                
        rootPart.setValue("hoverEvent", new JsonBuilder.PartMap(hoverEvent));
        rootPart.setValue("clickEvent", new JsonBuilder.PartMap(clickEvent));
        
        jb.setRootPart(rootPart);
        jb.sendJson(p);
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
     * Create a NMS data watcher object to send via a {@code PacketPlayOutEntityMetadata} packet.
     * Gravity will be disabled and the custom name will be displayed if available.
     * @param customName Custom Name of the entity or {@code null}
     * @param nmsItemStack NMS ItemStack or {@code null} if armor stand
     */
    public static Object createDataWatcher(String customName, Object nmsItemStack) {
        String version = getServerVersion();
        int majorVersion = getMajorVersion();

        try {
            Class<?> entityClass = getNMSClass("Entity");
            Class<?> entityArmorStandClass = getNMSClass("EntityArmorStand");
            Class<?> entityItemClass = getNMSClass("EntityItem");
            Class<?> dataWatcherClass = getNMSClass("DataWatcher");
            Class<?> dataWatcherObjectClass = getNMSClass("DataWatcherObject");

            byte entityFlags = nmsItemStack == null ? (byte) 0b100000 : 0; // invisible if armor stand
            byte armorStandFlags = nmsItemStack == null ? (byte) 0b10000 : 0; // marker (since 1.8_R2)

            Object dataWatcher = dataWatcherClass.getConstructor(entityClass).newInstance((Object) null);
            if (majorVersion < 9) {
                if (getRevision() == 1) armorStandFlags = 0; // Marker not supported on 1.8_R1

                Method a = dataWatcherClass.getMethod("a", int.class, Object.class);
                a.invoke(dataWatcher, 0, entityFlags); // flags
                a.invoke(dataWatcher, 1, (short) 300); // air ticks (?)
                a.invoke(dataWatcher, 3, (byte) (customName != null ? 1 : 0)); // custom name visible
                a.invoke(dataWatcher, 2, customName != null ? customName : ""); // custom name
                a.invoke(dataWatcher, 4, (byte) 1); // silent
                a.invoke(dataWatcher, 10, nmsItemStack == null ? armorStandFlags : nmsItemStack); // item / armor stand flags
            } else {
                Method register = dataWatcherClass.getMethod("register", dataWatcherObjectClass, Object.class);
                String[] dataWatcherObjectFieldNames;

                if ("v1_9_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"ax", "ay", "aA", "az", "aB", null, "c", "a"};
                } else if ("v1_9_R2".equals(version)){
                    dataWatcherObjectFieldNames = new String[] {"ay", "az", "aB", "aA", "aC", null, "c", "a"};
                } else if ("v1_10_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"aa", "az", "aB", "aA", "aC", "aD", "c", "a"};
                } else if ("v1_11_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"Z", "az", "aB", "aA", "aC", "aD", "c", "a"};
                } else if ("v1_12_R1".equals(version) || "v1_12_R2".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"Z", "aA", "aC", "aB", "aD", "aE", "c", "a"};
                } else if ("v1_13_R1".equals(version) || "v1_13_R2".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"ac", "aD", "aF", "aE", "aG", "aH", "b", "a"};
                } else if ("v1_14_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"W", "AIR_TICKS", "aA", "az", "aB", "aC", "ITEM", "b"};
                } else if ("v1_15_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"T", "AIR_TICKS", "aA", "az", "aB", "aC", "ITEM", "b"};
                } else if ("v1_16_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"T", "AIR_TICKS", "ay", "ax", "az", "aA", "ITEM", "b"};
                } else if ("v1_16_R2".equals(version) || "v1_16_R3".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"S", "AIR_TICKS", "ar", "aq", "as", "at", "ITEM", "b"};
                } else {
                    return null;
                }

                Field fEntityFlags = entityClass.getDeclaredField(dataWatcherObjectFieldNames[0]);
                Field fAirTicks = entityClass.getDeclaredField(dataWatcherObjectFieldNames[1]);
                Field fNameVisible = entityClass.getDeclaredField(dataWatcherObjectFieldNames[2]);
                Field fCustomName = entityClass.getDeclaredField(dataWatcherObjectFieldNames[3]);
                Field fSilent = entityClass.getDeclaredField(dataWatcherObjectFieldNames[4]);
                Field fNoGravity = majorVersion >= 10 ? entityClass.getDeclaredField(dataWatcherObjectFieldNames[5]) : null;
                Field fItem = entityItemClass.getDeclaredField(dataWatcherObjectFieldNames[6]);
                Field fArmorStandFlags = entityArmorStandClass.getDeclaredField(dataWatcherObjectFieldNames[7]);

                fEntityFlags.setAccessible(true);
                fAirTicks.setAccessible(true);
                fNameVisible.setAccessible(true);
                fCustomName.setAccessible(true);
                fSilent.setAccessible(true);
                if (majorVersion >= 10) fNoGravity.setAccessible(true);
                fItem.setAccessible(true);
                fArmorStandFlags.setAccessible(true);
                
                register.invoke(dataWatcher, fEntityFlags.get(null), entityFlags);
                register.invoke(dataWatcher, fAirTicks.get(null), 300);
                register.invoke(dataWatcher, fNameVisible.get(null), customName != null);
                register.invoke(dataWatcher, fSilent.get(null), true);
                if (majorVersion < 13) register.invoke(dataWatcher, fCustomName.get(null), customName != null ? customName : "");
                
                if (nmsItemStack != null) {
                    register.invoke(dataWatcher, fItem.get(null), majorVersion < 11 ? com.google.common.base.Optional.of(nmsItemStack) : nmsItemStack);
                } else {
                    register.invoke(dataWatcher, fArmorStandFlags.get(null), armorStandFlags);
                }

                if (majorVersion >= 10) {
                    register.invoke(dataWatcher, fNoGravity.get(null), true);
                    if (majorVersion >= 13) {
                        if (customName != null) {
                            Class<?> chatSerializerClass = Utils.getNMSClass("IChatBaseComponent$ChatSerializer");
                            Object iChatBaseComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, JsonBuilder.parse(customName).toString());
                            register.invoke(dataWatcher, fCustomName.get(null), Optional.of(iChatBaseComponent));
                        } else {
                            register.invoke(dataWatcher, fCustomName.get(null), Optional.empty());
                        }
                    }
                }
            }
            return dataWatcher;
        } catch (InstantiationException | InvocationTargetException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            ShopChest.getInstance().getLogger().severe("Failed to create data watcher!");
            ShopChest.getInstance().debug("Failed to create data watcher");
            ShopChest.getInstance().debug(e);
        }
        return null;
    }

    /**
     * Get a free entity ID for use in {@link #createPacketSpawnEntity(ShopChest, int, UUID, Location, Vector, EntityType)}
     * 
     * @return The id or {@code -1} if a free entity ID could not be retrieved.
     */
    public static int getFreeEntityId() {
        try {
            Class<?> entityClass = getNMSClass("Entity");
            Field entityCountField = entityClass.getDeclaredField("entityCount");
            entityCountField.setAccessible(true);
            if (entityCountField.getType() == int.class) {
                int id = entityCountField.getInt(null);
                entityCountField.setInt(null, id+1);
                return id;
            } else if (entityCountField.getType() == AtomicInteger.class) {
                return ((AtomicInteger) entityCountField.get(null)).incrementAndGet();
            }

            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Create a {@code PacketPlayOutSpawnEntity} object.
     * Only {@link EntityType#ARMOR_STAND} and {@link EntityType#DROPPED_ITEM} are supported! 
     */
    public static Object createPacketSpawnEntity(ShopChest plugin, int id, UUID uuid, Location loc, EntityType type) {
        try {
            Class<?> packetClass = getNMSClass("PacketPlayOutSpawnEntity");
            Object packet = packetClass.getConstructor().newInstance();
            boolean isPre9 = getMajorVersion() < 9;
            boolean isPre14 = getMajorVersion() < 14;

            Field[] fields = new Field[12];
            fields[0] = packetClass.getDeclaredField("a"); // ID
            fields[1] = packetClass.getDeclaredField("b"); // UUID (Only 1.9+)
            fields[2] = packetClass.getDeclaredField(isPre9 ? "b" : "c"); // Loc X
            fields[3] = packetClass.getDeclaredField(isPre9 ? "c" : "d"); // Loc Y
            fields[4] = packetClass.getDeclaredField(isPre9 ? "d" : "e"); // Loc Z
            fields[5] = packetClass.getDeclaredField(isPre9 ? "e" : "f"); // Mot X
            fields[6] = packetClass.getDeclaredField(isPre9 ? "f" : "g"); // Mot Y
            fields[7] = packetClass.getDeclaredField(isPre9 ? "g" : "h"); // Mot Z
            fields[8] = packetClass.getDeclaredField(isPre9 ? "h" : "i"); // Pitch
            fields[9] = packetClass.getDeclaredField(isPre9 ? "i" : "j"); // Yaw
            fields[10] = packetClass.getDeclaredField(isPre9 ? "j" : "k"); // Type
            fields[11] = packetClass.getDeclaredField(isPre9 ? "k" : "l"); // Data

            for (Field field : fields) {
                field.setAccessible(true);
            }

            Object entityType = null;
            if (!isPre14) {
                Class<?> entityTypesClass = getNMSClass("EntityTypes");
                entityType = entityTypesClass.getField(type == EntityType.ARMOR_STAND ? "ARMOR_STAND" : "ITEM").get(null);
            }

            double y = loc.getY();
            if (type == EntityType.ARMOR_STAND && !getServerVersion().equals("v1_8_R1")) {
                // Marker armor stand => lift by normal armor stand height
                y += 1.975;
            }

            fields[0].set(packet, id);
            if (!isPre9) fields[1].set(packet, uuid);
            if (isPre9) {
                fields[2].set(packet, (int)(loc.getX() * 32));
                fields[3].set(packet, (int)(y * 32));
                fields[4].set(packet, (int)(loc.getZ() * 32));
            } else {
                fields[2].set(packet, loc.getX());
                fields[3].set(packet, y);
                fields[4].set(packet, loc.getZ());
            }
            fields[5].set(packet, 0);
            fields[6].set(packet, 0);
            fields[7].set(packet, 0);
            fields[8].set(packet, 0);
            fields[9].set(packet, 0);
            if (isPre14) fields[10].set(packet, type == EntityType.ARMOR_STAND ? 78 : 2);
            else fields[10].set(packet, entityType);
            fields[11].set(packet, 0);

            return packet;
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            plugin.getLogger().severe("Failed to create packet to spawn entity!");
            plugin.debug("Failed to create packet to spawn entity!");
            plugin.debug(e);
            return null;
        }
    }

    /**
     * Send a packet to a player
     * @param plugin An instance of the {@link ShopChest} plugin
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
        return Base64.getEncoder().encodeToString(config.saveToString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes an {@link ItemStack} from a Base64 String
     * @param string Base64 encoded String to decode
     * @return Decoded {@link ItemStack}
     */
    public static ItemStack decode(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(Base64.getDecoder().decode(string), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        return config.getItemStack("i", null);
    }


}
