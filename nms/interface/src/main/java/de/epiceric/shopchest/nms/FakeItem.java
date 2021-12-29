package de.epiceric.shopchest.nms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface FakeItem extends FakeEntity{

    void sendData(ItemStack item, Iterable<Player> receivers);

}
