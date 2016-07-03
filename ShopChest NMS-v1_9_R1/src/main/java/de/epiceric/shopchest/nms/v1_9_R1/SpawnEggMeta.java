package de.epiceric.shopchest.nms.v1_9_R1;

import de.epiceric.shopchest.nms.ISpawnEggMeta;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class SpawnEggMeta extends ISpawnEggMeta {

    private ItemStack stack;

    public SpawnEggMeta(ItemStack stack) {
        this.stack = stack;
    }

    public String getNBTEntityID() {
        net.minecraft.server.v1_9_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

        NBTTagCompound tag = nmsStack.getTag();

        return tag == null ? null : tag.getCompound("EntityTag").getString("id");
    }
}