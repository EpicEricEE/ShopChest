package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ArmorStandWrapper {

    private final UUID uuid = UUID.randomUUID();
    private final FakeArmorStand fakeArmorStand;

    private Location location;
    private String customName;

    public ArmorStandWrapper(ShopChest plugin, Location location, String customName) {
        this.location = location;
        this.customName = customName;
        this.fakeArmorStand = plugin.getPlatform().createFakeArmorStand();
    }

    public void setVisible(Player player, boolean visible) {
        final List<Player> receiver = Collections.singletonList(player);
        if(visible){
            fakeArmorStand.spawn(uuid, location, receiver);
            fakeArmorStand.sendData(customName, receiver);
        }
        else if(fakeArmorStand.getEntityId() != -1){
            fakeArmorStand.remove(receiver);
        }
    }

    public void setLocation(Location location) {
        this.location = location;
        fakeArmorStand.setLocation(location, Objects.requireNonNull(location.getWorld()).getPlayers());
    }

    public void setCustomName(String customName) {
        this.customName = customName;
        fakeArmorStand.sendData(customName, Objects.requireNonNull(location.getWorld()).getPlayers());
    }

    public void remove() {
        for (Player player : Objects.requireNonNull(location.getWorld()).getPlayers()) {
            setVisible(player, false);
        }
    }

    public int getEntityId() {
        return fakeArmorStand.getEntityId();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Location getLocation() {
        return location.clone();
    }

    public String getCustomName() {
        return customName;
    }
}
