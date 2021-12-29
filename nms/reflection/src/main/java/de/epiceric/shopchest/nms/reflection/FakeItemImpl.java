package de.epiceric.shopchest.nms.reflection;

import de.epiceric.shopchest.nms.FakeItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FakeItemImpl extends FakeEntityImpl implements FakeItem {

    public FakeItemImpl(ShopChestDebug debug) {
        super(debug);
    }

    @Override
    public void sendData(ItemStack item, Iterable<Player> receivers) {

    }

    @Override
    public void resetVelocity(Iterable<Player> receivers) {

    }
}
