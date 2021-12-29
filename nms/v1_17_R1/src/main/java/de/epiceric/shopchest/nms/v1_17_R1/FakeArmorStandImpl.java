package de.epiceric.shopchest.nms.v1_17_R1;

import de.epiceric.shopchest.nms.FakeArmorStand;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class FakeArmorStandImpl extends FakeEntityImpl<String> implements FakeArmorStand {

    private final static byte INVISIBLE_FLAG = 0b100000;
    protected static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;
    private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME;
    private static final EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE;
    private final static float MARKER_ARMOR_STAND_OFFSET = 1.975f;

    static {
        try {
            final Field dataSharedFlagsId = Entity.class.getDeclaredField("Z"); // DATA_SHARED_FLAGS_ID
            DATA_SHARED_FLAGS_ID = FakeEntityImpl.forceCast(dataSharedFlagsId.get(null));
            final Field dataCustomNameField = Entity.class.getDeclaredField("aJ"); // DATA_CUSTOM_NAME
            DATA_CUSTOM_NAME = FakeEntityImpl.forceCast(dataCustomNameField.get(null));
            final Field dataCustomNameVisibleField = Entity.class.getDeclaredField("aK"); // DATA_CUSTOM_NAME_VISIBLE
            DATA_CUSTOM_NAME_VISIBLE = FakeEntityImpl.forceCast(dataCustomNameVisibleField.get(null));
        } catch (ReflectiveOperationException e){
            throw new RuntimeException(e);
        }
    }

    public FakeArmorStandImpl() {
        super();
    }

    @Override
    public void sendData(String name, Iterable<Player> receivers) {
        sendData(receivers, name);
    }

    @Override
    protected EntityType<?> getEntityType() {
        return EntityType.ARMOR_STAND;
    }

    @Override
    protected float getSpawnOffSet() {
        return MARKER_ARMOR_STAND_OFFSET;
    }

    @Override
    protected int getDataItemCount() {
        return 4;
    }

    @Override
    protected void addSpecificData(List<SynchedEntityData.DataItem<?>> packedItems, String name) {
        packedItems.add(new SynchedEntityData.DataItem<>(DATA_SHARED_FLAGS_ID, INVISIBLE_FLAG));
        packedItems.add(new SynchedEntityData.DataItem<>(DATA_CUSTOM_NAME, Optional.ofNullable(
                Component.Serializer.fromJson(
                        ComponentSerializer.toString(
                                TextComponent.fromLegacyText(name)
                        )
                )
        )));
        packedItems.add(new SynchedEntityData.DataItem<>(DATA_CUSTOM_NAME_VISIBLE, true));
        // TODO Add Marker (specific to ArmorStand)
    }

    @Override
    public void setLocation(Location location, Iterable<Player> receivers) {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeVarInt(entityId);
        buffer.writeDouble(location.getX());
        buffer.writeDouble(location.getY() + MARKER_ARMOR_STAND_OFFSET);
        buffer.writeDouble(location.getZ());
        buffer.writeByte(0);
        buffer.writeByte(0);
        buffer.writeBoolean(false);
        final ClientboundTeleportEntityPacket positionPacket = new ClientboundTeleportEntityPacket(buffer);
        sendPacket(positionPacket, receivers);
    }

}