package de.epiceric.shopchest.shop.hologram;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.util.Logger;
import de.epiceric.shopchest.util.NmsUtil;

public class HologramLine {
    private final Class<?> packetCreateClass = NmsUtil.getNmsClass("PacketPlayOutSpawnEntity");
    private final Class<?> packetMetadataClass = NmsUtil.getNmsClass("PacketPlayOutEntityMetadata");
    private final Class<?> packetDestroyClass = NmsUtil.getNmsClass("PacketPlayOutEntityDestroy");
    private final Class<?> dataWatcherClass = NmsUtil.getNmsClass("DataWatcher");

    private final Set<UUID> visibleToPlayers = new HashSet<>();

    private Object createPacket;

    private Field xField;
    private Field yField;
    private Field zField;

    private Object dataWatcher;
    
    private Field nameField;
    private Field nameVisField;
    private Method register;
    private Method registerPre9;

    private final int entityId;
    private final UUID uuid = UUID.randomUUID();

    public HologramLine() {
        this.entityId = NmsUtil.getFreeEntityId();
        initCreatePacket();
        initDataWatcher();
    }

    public HologramLine(Location location, String name) {
        this();
        setLocation(location);
        setName(name);

        showForAll(); // TODO: remove
    }

    private void initCreatePacket() {
        try {
            Object packet = packetCreateClass.getConstructor().newInstance();
            boolean isPre9 = NmsUtil.getMajorVersion() < 9;
            boolean isPre14 = NmsUtil.getMajorVersion() < 14;

            Field[] fields = new Field[6];
            fields[0] = packetCreateClass.getDeclaredField("a"); // ID
            fields[1] = packetCreateClass.getDeclaredField("b"); // UUID (Only 1.9+)
            this.xField = fields[2] = packetCreateClass.getDeclaredField(isPre9 ? "b" : "c"); // Loc X
            this.yField = fields[3] = packetCreateClass.getDeclaredField(isPre9 ? "c" : "d"); // Loc Y
            this.zField = fields[4] = packetCreateClass.getDeclaredField(isPre9 ? "d" : "e"); // Loc Z
            fields[5] = packetCreateClass.getDeclaredField(isPre9 ? "j" : "k"); // Type

            Arrays.stream(fields).forEach(field -> field.setAccessible(true));

            Object entityType = null;
            if (!isPre14) {
                entityType = NmsUtil.getNmsClass("EntityTypes").getField("ARMOR_STAND").get(null);
            }

            fields[0].set(packet, entityId);

            if (!isPre9) {
                fields[1].set(packet, uuid);
            }

            if (isPre14) {
                fields[5].set(packet, 78);
            } else {
                fields[5].set(packet, entityType);
            }

            this.createPacket = packet;
        } catch (ReflectiveOperationException e) {
            Logger.severe("Failed to create packet to spawn entity");
            Logger.severe(e);
        }
    }

    private void initDataWatcher() {
        String version = NmsUtil.getServerVersion();
        int majorVersion = NmsUtil.getMajorVersion();

        try {
            Class<?> entityClass = NmsUtil.getNmsClass("Entity");
            Class<?> entityArmorStandClass = NmsUtil.getNmsClass("EntityArmorStand");
            Class<?> dataWatcherObjectClass = NmsUtil.getNmsClass("DataWatcherObject");

            byte entityFlags = (byte) 0b100000; // invisible if armor stand
            byte armorStandFlags = (byte) 0b10000; // marker (since 1.8_R2)

            Object dataWatcher = dataWatcherClass.getConstructor(entityClass).newInstance((Object) null);
            if (majorVersion < 9) {
                if (NmsUtil.getRevision() == 1) {
                    armorStandFlags = 0; // Marker not supported on 1.8_R1
                }

                Method a = this.registerPre9 = dataWatcherClass.getMethod("a", int.class, Object.class);
                a.invoke(dataWatcher, 0, entityFlags); // flags
                a.invoke(dataWatcher, 1, (short) 300); // air ticks (?)
                a.invoke(dataWatcher, 4, (byte) 1); // silent
                a.invoke(dataWatcher, 10, armorStandFlags); // armor stand flags
                
                this.dataWatcher = dataWatcher;
            } else {
                Method register = this.register = dataWatcherClass.getMethod("register", dataWatcherObjectClass, Object.class);
                String[] dataWatcherObjectFieldNames;

                if ("v1_9_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"ax", "ay", "aA", "az", "aB", null, "a"};
                } else if ("v1_9_R2".equals(version)){
                    dataWatcherObjectFieldNames = new String[] {"ay", "az", "aB", "aA", "aC", null, "a"};
                } else if ("v1_10_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"aa", "az", "aB", "aA", "aC", "aD", "a"};
                } else if ("v1_11_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"Z", "az", "aB", "aA", "aC", "aD", "a"};
                } else if ("v1_12_R1".equals(version) || "v1_12_R2".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"Z", "aA", "aC", "aB", "aD", "aE", "a"};
                } else if ("v1_13_R1".equals(version) || "v1_13_R2".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"ac", "aD", "aF", "aE", "aG", "aH", "a"};
                } else if ("v1_14_R1".equals(version)) {
                    dataWatcherObjectFieldNames = new String[] {"W", "AIR_TICKS", "aA", "az", "aB", "aC", "b"};
                } else {
                    Logger.severe("Failed to create armor stand data watcher: Invalid version {0}", version);
                    return;
                }

                Field fEntityFlags = entityClass.getDeclaredField(dataWatcherObjectFieldNames[0]);
                Field fAirTicks = entityClass.getDeclaredField(dataWatcherObjectFieldNames[1]);
                Field fNameVisible = this.nameVisField = entityClass.getDeclaredField(dataWatcherObjectFieldNames[2]);
                Field fCustomName = this.nameField = entityClass.getDeclaredField(dataWatcherObjectFieldNames[3]);
                Field fSilent = entityClass.getDeclaredField(dataWatcherObjectFieldNames[4]);
                Field fNoGravity = majorVersion >= 10 ? entityClass.getDeclaredField(dataWatcherObjectFieldNames[5]) : null;
                Field fArmorStandFlags = entityArmorStandClass.getDeclaredField(dataWatcherObjectFieldNames[6]);

                fEntityFlags.setAccessible(true);
                fAirTicks.setAccessible(true);
                fNameVisible.setAccessible(true);
                fCustomName.setAccessible(true);
                fSilent.setAccessible(true);
                fArmorStandFlags.setAccessible(true);
                
                if (majorVersion >= 10) {
                    fNoGravity.setAccessible(true);
                }
                
                register.invoke(dataWatcher, fEntityFlags.get(null), entityFlags);
                register.invoke(dataWatcher, fAirTicks.get(null), 300);
                register.invoke(dataWatcher, fSilent.get(null), true);
                register.invoke(dataWatcher, fArmorStandFlags.get(null), armorStandFlags);

                if (majorVersion >= 10) {
                    register.invoke(dataWatcher, fNoGravity.get(null), true);
                }

                this.dataWatcher = dataWatcher;
            }
        } catch (ReflectiveOperationException e) {
            Logger.severe("Failed to data watcher");
            Logger.severe(e);
        }
    }

    public void setLocation(Location location) {
        try {
            double y = location.getY();
            if (!NmsUtil.getServerVersion().equals("v1_8_R1")) {
                // Marker armor stand => lift by armor stand height
                y += 1.975;
            }

            if (NmsUtil.getMajorVersion() < 9) {
                xField.set(createPacket, (int)(location.getX() * 32));
                yField.set(createPacket, (int)(y * 32));
                zField.set(createPacket, (int)(location.getZ() * 32));
            } else {
                xField.set(createPacket, location.getX());
                yField.set(createPacket, y);
                zField.set(createPacket, location.getZ());
            }
        } catch (ReflectiveOperationException e) {
            Logger.severe("Failed to set location of hologram packet");
            Logger.severe(e);
        }
    }

    public void setName(String name) {
        int majorVersion = NmsUtil.getMajorVersion();
        boolean hasName = name != null && !name.isEmpty();

        try {
            if (majorVersion < 9) {
                registerPre9.invoke(dataWatcher, 3, (byte) (hasName ? 1 : 0)); // custom name visible
                registerPre9.invoke(dataWatcher, 2, hasName ? name : ""); // custom name
            } else {
                register.invoke(dataWatcher, nameVisField.get(null), hasName);
                if (majorVersion < 13) {
                    register.invoke(dataWatcher, nameField.get(null), hasName ? name : "");
                } else {
                    if (hasName) {
                        Class<?> chatSerializerClass = NmsUtil.getNmsClass("IChatBaseComponent$ChatSerializer");
                        // TODO: JSONify: Object iChatBaseComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, JsonBuilder.parse(customName).toString());
                        Object iChatBaseComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, "[\"" + name + "\"]");
                        register.invoke(dataWatcher, nameField.get(null), Optional.of(iChatBaseComponent));
                    } else {
                        register.invoke(dataWatcher, nameField.get(null), Optional.empty());
                    }
                }
            }

            Object metadataPacket = packetMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class)
                    .newInstance(entityId, dataWatcher, true);
                    
            visibleToPlayers.forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    NmsUtil.sendPacket(metadataPacket, player);
                }
            });
        } catch (ReflectiveOperationException e) {
            Logger.severe("Failed to set custom name of hologram data watcher");
            Logger.severe(e);
        }
    }

    public void showPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        try {
            NmsUtil.sendPacket(createPacket, player);
            NmsUtil.sendPacket(packetMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class)
                    .newInstance(entityId, dataWatcher, true), player);

            visibleToPlayers.add(player.getUniqueId());
        } catch (ReflectiveOperationException e) {
            Logger.severe("Failed to show hologram line to player");
            Logger.severe(e);
        }
    }

    public void hidePlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        try {
            NmsUtil.sendPacket(packetDestroyClass.getConstructor(int[].class)
                    .newInstance((Object) new int[]{entityId}), player);

            visibleToPlayers.remove(player.getUniqueId());
        } catch (ReflectiveOperationException e) {
            Logger.severe("Failed to hide hologram line from player");
            Logger.severe(e);
        }
    }

    public void destroy() {
        visibleToPlayers.forEach(uuid -> {
            hidePlayer(Bukkit.getPlayer(uuid));
        });;
    }

    // TODO: remove
    public void showForAll() {
        Bukkit.getOnlinePlayers().forEach(player -> showPlayer(player));
    }
}