package de.epiceric.shopchest.nms.v1_17_R1;

import de.epiceric.shopchest.nms.TextComponentHelper;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class TextComponentHelperImpl implements TextComponentHelper {
    @Override
    public String getNbt(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack).save(new CompoundTag()).getAsString();
    }
}
