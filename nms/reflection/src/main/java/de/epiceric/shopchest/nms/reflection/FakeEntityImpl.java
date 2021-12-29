package de.epiceric.shopchest.nms.reflection;

import de.epiceric.shopchest.nms.FakeEntity;
import org.bukkit.entity.Player;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

public abstract class FakeEntityImpl implements FakeEntity {

    protected final NMSClassResolver nmsClassResolver = new NMSClassResolver();
    protected final Class<?> packetPlayOutEntityDestroyClass = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayOutEntityDestroy");
    protected final Class<?> packetPlayOutEntityMetadataClass = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayOutEntityMetadata");
    protected final Class<?> dataWatcherClass = nmsClassResolver.resolveSilent("network.syncher.DataWatcher");

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
    public void remove(Iterable<Player> receivers) {
        try {
            for(Player receiver : receivers) {
                ReflectionUtils.sendPacket(debug, packetPlayOutEntityDestroyClass.getConstructor(int[].class).newInstance((Object) new int[]{entityId}), receiver);
            }
        } catch (ReflectiveOperationException e){
            // TODO Handle this properly
            throw new RuntimeException(e);
        }
    }
}
