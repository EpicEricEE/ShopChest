package de.epiceric.shopchest.nms.v1_8_R2;

import de.epiceric.shopchest.nms.ISpawnEggMeta;
import net.minecraft.server.v1_8_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class SpawnEggMeta extends ISpawnEggMeta {

    private ItemStack stack;

    public SpawnEggMeta(ItemStack stack) {
        this.stack = stack;
    }

    public String getNBTEntityID() {
        net.minecraft.server.v1_8_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

        NBTTagCompound tag = nmsStack.getTag();

        return tag == null ? null : tag.getCompound("EntityTag").getString("id");
    }
}