package de.epiceric.shopchest.nms.reflection;

import de.epiceric.shopchest.nms.FakeArmorStand;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FakeArmorStandImpl extends FakeEntityImpl implements FakeArmorStand {

    public FakeArmorStandImpl(ShopChestDebug debug) {
        super(debug);
    }

    @Override
    public void sendData(String name, Iterable<Player> receivers) {

    }

    @Override
    public void setLocation(Location location, Iterable<Player> receivers) {

    }
}
