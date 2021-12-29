package de.epiceric.shopchest.shop;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.nms.FakeItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopItem {

    // concurrent since update task is in async thread
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
    private final ItemStack itemStack;
    private final Location location;
    private final UUID uuid = UUID.randomUUID();
    private final FakeItem fakeItem;

    public ShopItem(ShopChest plugin, ItemStack itemStack, Location location) {
        this.itemStack = itemStack;
        this.location = location;
        this.fakeItem = plugin.getPlatform().createFakeItem();
    }

    /**
     * @return Clone of the location, where the shop item should be (it could have been moved by something, even though it shouldn't)
     */
    public Location getLocation() {
        return location.clone();
    }

    /**
     * @return A clone of this Item's {@link ItemStack}
     */
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    /**
     * @param p Player to check
     * @return Whether the item is visible to the player
     */
    public boolean isVisible(Player p) {
        return viewers.contains(p.getUniqueId());
    }

    /**
     * @param p Player to which the item should be shown
     */
    public void showPlayer(Player p) {
        showPlayer(p, false);
    }

    /**
     * @param p Player to which the item should be shown
     * @param force whether to force or not
     */
    public void showPlayer(Player p, boolean force) {
        if (viewers.add(p.getUniqueId()) || force) {
            final List<Player> receiver = Collections.singletonList(p);
            fakeItem.spawn(uuid, location, receiver);
            fakeItem.sendData(itemStack, receiver);
            fakeItem.resetVelocity(receiver);
        }
    }

    /**
     * @param p Player from which the item should be hidden
     */
    public void hidePlayer(Player p) {
        hidePlayer(p, false);
    }

    /**
     * @param p Player from which the item should be hidden
     * @param force whether to force or not
     */
    public void hidePlayer(Player p, boolean force) {
        if (viewers.remove(p.getUniqueId()) || force) {
            if (p.isOnline()) {
                fakeItem.remove(Collections.singletonList(p));
            }
        }
    }

    public void resetVisible(Player p) {
        viewers.remove(p.getUniqueId());
    }

    /**
     * Removes the item. <br>
     * Item will be hidden from all players
     */
    public void remove() {
        // Avoid ConcurrentModificationException
        for (UUID uuid : new ArrayList<>(viewers)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) hidePlayer(p);
        }
    }

    /**
     * Respawns the item at the set location for a player
     * @param p Player, for which the item should be reset
     */
    public void resetForPlayer(Player p) {
        hidePlayer(p);
        showPlayer(p);
    }

}
