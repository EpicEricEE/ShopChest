package de.epiceric.shopchest.nms.v1_17_R1;

import de.epiceric.shopchest.nms.FakeItem;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class FakeItemImpl extends FakeEntityImpl<ItemStack> implements FakeItem {

    public FakeItemImpl() {
        super();
    }

    @Override
    public void sendData(ItemStack item, Iterable<Player> receivers) {

    }

    @Override
    protected EntityType<?> getEntityType() {
        return EntityType.ITEM;
    }

    @Override
    protected int getDataItemCount() {
        return 0;
    }

    @Override
    protected void addSpecificData(List<SynchedEntityData.DataItem<?>> packedItems, ItemStack data) {

    }
}
