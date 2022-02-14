package de.epiceric.shopchest.nms.v1_17_R1;

import de.epiceric.shopchest.nms.FakeEntity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class FakeEntityImpl<T> implements FakeEntity {

    private final static AtomicInteger ENTITY_COUNTER;
    private final static EntityDataAccessor<Boolean> DATA_NO_GRAVITY;
    private final static EntityDataAccessor<Boolean> DATA_SILENT;
    private final static Field packedItemField;

    static {
        try {
            final Field entityCounterField = Entity.class.getDeclaredField("b"); // ENTITY_COUNTER
            entityCounterField.setAccessible(true);
            ENTITY_COUNTER = (AtomicInteger) entityCounterField.get(null);
            final Field dataNoGravityField = Entity.class.getDeclaredField("aM"); // DATA_NO_GRAVITY
            dataNoGravityField.setAccessible(true);
            DATA_NO_GRAVITY = forceCast(dataNoGravityField.get(null));
            final Field dataSilentField = Entity.class.getDeclaredField("aL"); // DATA_SILENT
            dataSilentField.setAccessible(true);
            DATA_SILENT = forceCast(dataSilentField.get(null));
            packedItemField = ClientboundSetEntityDataPacket.class.getDeclaredField("b"); // packedItems
            packedItemField.setAccessible(true);
        }catch (ReflectiveOperationException e){
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected static <T> T forceCast(Object o){
        return (T) o;
    }

    protected final int entityId;

    public FakeEntityImpl() {
        entityId = ENTITY_COUNTER.incrementAndGet();
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    protected void sendPacket(Packet<?> packet, Iterable<Player> receivers){
        for(Player receiver : receivers){
            ((CraftPlayer)receiver).getHandle().connection.send(packet);
        }
    }

    @Override
    public void spawn(UUID uuid, Location location, Iterable<Player> receivers) {
        final ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(
                entityId,
                uuid,
                location.getX(),
                location.getY() + getSpawnOffSet(),
                location.getZ(),
                0f,
                0f,
                getEntityType(),
                0,
                Vec3.ZERO
        );
        sendPacket(spawnPacket, receivers);
    }

    @Override
    public void remove(Iterable<Player> receivers) {
        final ClientboundRemoveEntityPacket removePacket = new ClientboundRemoveEntityPacket(entityId);
        sendPacket(removePacket, receivers);
    }

    protected void sendData(Iterable<Player> receivers, T data){
        // Create packet
        final SynchedEntityData entityData = new SynchedEntityData(null);
        final ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(entityId, entityData, false);
        final List<SynchedEntityData.DataItem<?>> packedItems = new ArrayList<>(2 + getDataItemCount());

        // Setup data
        packedItems.add(new SynchedEntityData.DataItem<>(DATA_NO_GRAVITY, true));
        packedItems.add(new SynchedEntityData.DataItem<>(DATA_SILENT, true));
        addSpecificData(packedItems, data);

        try {
            packedItemField.set(dataPacket, packedItems);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // Send packet
        sendPacket(dataPacket, receivers);
    }

    protected abstract EntityType<?> getEntityType();

    protected float getSpawnOffSet(){
        return 0f;
    }

    protected abstract int getDataItemCount();

    protected abstract void addSpecificData(List<SynchedEntityData.DataItem<?>> packedItems, T data);

}
