package de.epiceric.shopchest.nms.reflection;

import de.epiceric.shopchest.nms.FakeItem;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import java.util.UUID;

public class FakeItemImpl extends FakeEntityImpl implements FakeItem {

    private final OBCClassResolver obcClassResolver = new OBCClassResolver();
    private final Class<?> packetPlayOutEntityVelocityClass = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayOutEntityVelocity");
    private final Class<?> vec3dClass = nmsClassResolver.resolveSilent("world.phys.Vec3D");
    private final Class<?> craftItemStackClass = obcClassResolver.resolveSilent("inventory.CraftItemStack");

    public FakeItemImpl(ShopChestDebug debug) {
        super(debug);

        Class<?> nmsItemStackClass = nmsClassResolver.resolveSilent("world.item.ItemStack");

        Class<?>[] requiredClasses = new Class<?>[] {
                nmsItemStackClass, craftItemStackClass, packetPlayOutEntityMetadataClass, dataWatcherClass,
                packetPlayOutEntityDestroyClass, packetPlayOutEntityVelocityClass,
        };

        for (Class<?> c : requiredClasses) {
            if (c == null) {
                debug.debug("Failed to create shop item: Could not find all required classes");
                return;
            }
        }
    }

    @Override
    public void sendData(ItemStack item, Iterable<Player> receivers) {
        try {
            Object nmsItemStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            Object dataWatcher = ReflectionUtils.createDataWatcher(debug, null, nmsItemStack);
            for (Player receiver : receivers) {
                ReflectionUtils.sendPacket(debug, packetPlayOutEntityMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class).newInstance(entityId, dataWatcher, true), receiver);
            }
        }catch (ReflectiveOperationException e){
            // TODO Handle this properly
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resetVelocity(Iterable<Player> receivers) {
        try{
            Object velocityPacket;
            if (ReflectionUtils.getMajorVersion() < 14) {
                velocityPacket = packetPlayOutEntityVelocityClass.getConstructor(int.class, double.class, double.class, double.class).newInstance(entityId, 0D, 0D, 0D);
            } else {
                Object vec3d = vec3dClass.getConstructor(double.class, double.class, double.class).newInstance(0D, 0D, 0D);
                velocityPacket =  packetPlayOutEntityVelocityClass.getConstructor(int.class, vec3dClass).newInstance(entityId, vec3d);
            }
            for(Player receiver : receivers) {
                ReflectionUtils.sendPacket(debug, velocityPacket, receiver);
            }
        }catch (ReflectiveOperationException e){
            // TODO Handle this properly
            throw new RuntimeException(e);
        }
    }

    @Override
    public void spawn(UUID uuid, Location location, Iterable<Player> receivers) {
        for(Player receiver : receivers) {
            ReflectionUtils.sendPacket(debug, ReflectionUtils.createPacketSpawnEntity(debug, entityId, uuid, location, EntityType.DROPPED_ITEM), receiver);
        }
    }
}
