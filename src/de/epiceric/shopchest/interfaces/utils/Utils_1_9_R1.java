package de.epiceric.shopchest.interfaces.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.interfaces.Hologram;
import de.epiceric.shopchest.interfaces.Utils;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.utils.ShopUtils;
import net.minecraft.server.v1_9_R1.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class Utils_1_9_R1 extends Utils {

    @Override
    public void reload(Player player) {

        for (Shop shop : ShopUtils.getShops()) {
            Hologram hologram = shop.getHologram();

            if (shop.hasItem()) shop.getItem().remove();
            ShopUtils.removeShop(shop);

            for (Player p : ShopChest.getInstance().getServer().getOnlinePlayers()) {
                hologram.hidePlayer(p);
            }

            for (Object o : hologram.getEntities()) {
                EntityArmorStand e = (EntityArmorStand) o;
                e.getWorld().removeEntity(e);
            }


        }

        int count = 0;

        for (int id = 1; id < ShopChest.database.getHighestID() + 1; id++) {

            try {
                Shop shop = (Shop) ShopChest.database.get(id, Database.ShopInfo.SHOP);
                shop.createHologram();
                shop.createItem();
                ShopUtils.addShop(shop);
            } catch (NullPointerException e) {
                continue;
            }

            count++;

        }

        if (player != null) player.sendMessage(Config.reloaded_shops(count));

        for (Player p : Bukkit.getOnlinePlayers()) {
            Bukkit.getPluginManager().callEvent(new PlayerMoveEvent(p, p.getLocation(), p.getLocation()));
        }


    }


    @Override
    public void removeShops() {
        for (Shop shop : ShopUtils.getShops()) {
            Hologram hologram = shop.getHologram();

            for (Player p : Bukkit.getOnlinePlayers()) {
                hologram.hidePlayer(p);
            }

            for (Object o : hologram.getEntities()) {
                EntityArmorStand e = (EntityArmorStand) o;
                e.getWorld().removeEntity(e);
            }


            if (shop.hasItem()) shop.getItem().remove();

        }
    }


}
