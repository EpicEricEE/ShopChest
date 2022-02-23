package de.epiceric.shopchest.nms.v1_17_R1;

import de.epiceric.shopchest.nms.FakeItem;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.List;

public class FakeItemImpl extends FakeEntityImpl<ItemStack> implements FakeItem {

    private final static EntityDataAccessor<net.minecraft.world.item.ItemStack> DATA_ITEM;

    static {
        try{
            final Field dataItemField = ItemEntity.class.getDeclaredField("c"); // DATA_ITEM
            dataItemField.setAccessible(true);
            DATA_ITEM = forceCast(dataItemField.get(null));
        }catch (ReflectiveOperationException e){
            throw new RuntimeException(e);
        }
    }

    public FakeItemImpl() {
        super();
    }

    @Override
    public void sendData(ItemStack item, Iterable<Player> receivers) {
        sendData(receivers, item);
    }

    @Override
    public void resetVelocity(Iterable<Player> receivers) {
        final ClientboundSetEntityMotionPacket velocityPacket = new ClientboundSetEntityMotionPacket(entityId, Vec3.ZERO);
        sendPacket(velocityPacket, receivers);
    }

    @Override
    protected EntityType<?> getEntityType() {
        return EntityType.ITEM;
    }

    @Override
    protected int getDataItemCount() {
        return 1;
    }

    @Override
    protected void addSpecificData(List<SynchedEntityData.DataItem<?>> packedItems, ItemStack data) {
        packedItems.add(new SynchedEntityData.DataItem<>(DATA_ITEM, CraftItemStack.asNMSCopy(data)));
    }
}
