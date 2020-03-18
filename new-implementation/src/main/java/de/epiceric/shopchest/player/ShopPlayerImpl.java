package de.epiceric.shopchest.player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.config.Config;
import de.epiceric.shopchest.api.flag.Flag;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.api.shop.Shop;

public class ShopPlayerImpl implements ShopPlayer {
    private static Map<UUID, ShopPlayer> wrappedPlayers = new HashMap<>();

    /**
     * Gets an instance of a wrapped player
     * 
     * @param plugin an instance of the plugin
     * @param player the player to wrap
     * @return the wrapped player
     * @see ShopChest#wrapPlayer(Player)
     */
    public static ShopPlayer get(ShopChest plugin, Player player) {
        if (!wrappedPlayers.containsKey(player.getUniqueId())) {
            wrappedPlayers.put(player.getUniqueId(), new ShopPlayerImpl(plugin, player.getUniqueId()));
        }
        return wrappedPlayers.get(player.getUniqueId());
    }

    /**
     * Unregisters the wrapped player for the given player
     * 
     * @param player the player to unregister
     */
    public static void unregister(Player player) {
        wrappedPlayers.remove(player.getUniqueId());
    }

    private final ShopChest plugin;
    private final UUID uuid;
    private Flag flag;

    private ShopPlayerImpl(ShopChest plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }

    @Override
    public Player getBukkitPlayer() {
        return plugin.getServer().getPlayer(uuid);
    }

    @Override
    public Optional<Flag> getFlag() {
        return Optional.ofNullable(flag);
    }

    @Override
    public void setFlag(Flag flag) {
        if (this.flag != null) {
            this.flag.onRemove(this);
        }
        this.flag = flag;
        if (this.flag != null) {
            this.flag.onAssign(this);
        }
    }

    @Override
    public int getShopLimit() {
        return Config.CORE_DEFAULT_SHOP_LIMIT.get(); // TODO: permissions based
	}

	@Override
	public Collection<Shop> getShops() {
		return plugin.getShopManager().getShops(getBukkitPlayer());
	}
}