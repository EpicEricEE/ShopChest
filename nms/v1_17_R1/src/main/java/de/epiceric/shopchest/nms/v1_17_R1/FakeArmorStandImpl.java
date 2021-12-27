package de.epiceric.shopchest.nms.v1_17_R1;

import de.epiceric.shopchest.nms.FakeArmorStand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeArmorStandImpl implements FakeArmorStand {

    private final static byte INVISIBLE_FLAG = 0b100000;
    private final static AtomicInteger ENTITY_COUNTER;
    protected static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;
    private static final EntityDataAccessor<Boolean> DATA_NO_GRAVITY;
    private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME;
    private static final EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE;
    private static final EntityDataAccessor<Boolean> DATA_SILENT;
    private final static Field packedItemField;

    static {
        try {
            final Field entityCounterField = Entity.class.getDeclaredField("b"); // ENTITY_COUNTER
            ENTITY_COUNTER = (AtomicInteger) entityCounterField.get(null);
            final Field dataSharedFlagsId = Entity.class.getDeclaredField("Z"); // DATA_SHARED_FLAGS_ID
            DATA_SHARED_FLAGS_ID = forceCast(dataSharedFlagsId.get(null));
            final Field dataNoGravityField = Entity.class.getDeclaredField("aM"); // DATA_NO_GRAVITY
            DATA_NO_GRAVITY = forceCast(dataNoGravityField.get(null));
            final Field dataCustomNameField = Entity.class.getDeclaredField("aJ"); // DATA_CUSTOM_NAME
            DATA_CUSTOM_NAME = forceCast(dataCustomNameField.get(null));
            final Field dataCustomNameVisibleField = Entity.class.getDeclaredField("aK"); // DATA_CUSTOM_NAME_VISIBLE
            DATA_CUSTOM_NAME_VISIBLE = forceCast(dataCustomNameVisibleField.get(null));
            final Field dataSilentField = Entity.class.getDeclaredField("aL"); // DATA_SILENT
            DATA_SILENT = forceCast(dataSilentField.get(null));
            packedItemField = ClientboundSetEntityDataPacket.class.getDeclaredField("b"); // packedItems
        } catch (ReflectiveOperationException e){
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T forceCast(Object o){
        return (T) o;
    }

    private final int entityId;

    public FakeArmorStandImpl() {
        entityId = ENTITY_COUNTER.incrementAndGet();
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public void sendData(String name) {
        // Create packet
        final SynchedEntityData entityData = new SynchedEntityData(null);
        final ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(entityId, entityData, false);
        final List<SynchedEntityData.DataItem<?>> packedItems = new ArrayList<>(5);

        // Setup data
        packedItems.add(new SynchedEntityData.DataItem<>(DATA_SHARED_FLAGS_ID, INVISIBLE_FLAG));
        packedItems.add(new SynchedEntityData.DataItem<>(DATA_NO_GRAVITY, true));
        packedItems.add(new SynchedEntityData.DataItem<>(DATA_CUSTOM_NAME, Optional.ofNullable(Component.Serializer.fromJson(
                "" // TODO Use chat-api to serialize the name
        ))));
        packedItems.add(new SynchedEntityData.DataItem<>(DATA_CUSTOM_NAME_VISIBLE, true));
        packedItems.add(new SynchedEntityData.DataItem<>(DATA_SILENT, true));

        try {
            packedItemField.set(dataPacket, packedItems);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // Send packet

    }

    @Override
    public void remove() {

    }

    @Override
    public void setLocation(Location location) {

    }

    @Override
    public void spawn() {

    }
}
