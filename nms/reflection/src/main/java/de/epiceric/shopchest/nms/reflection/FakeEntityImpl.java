package de.epiceric.shopchest.nms.reflection;

import de.epiceric.shopchest.nms.FakeEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FakeEntityImpl implements FakeEntity {

    protected final int entityId;
    protected final ShopChestDebug debug;

    public FakeEntityImpl(ShopChestDebug debug) {
        this.entityId = ReflectionUtils.getFreeEntityId();
        this.debug = debug;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public void spawn(UUID uuid, Location location, Iterable<Player> receivers) {

    }

    @Override
    public void remove(Iterable<Player> receivers) {

    }
}
