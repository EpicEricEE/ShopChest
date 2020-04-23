package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;

public class PluginMessageTask extends BukkitRunnable {
    private final ShopChest plugin;
    private final Player player;
    private final ByteArrayOutputStream bytes;

    public PluginMessageTask(ShopChest plugin, Player player, ByteArrayOutputStream bytes)
    {
        this.plugin = plugin;
        this.player = player;
        this.bytes = bytes;
    }

    public PluginMessageTask(ShopChest plugin, ByteArrayOutputStream bytes) throws Exception {
        this.plugin = plugin;
        this.bytes = bytes;

        if (plugin.getServer().getOnlinePlayers().size() == 0)
            throw new Exception("PluginMessage requires an online player to be sent.");

        this.player = plugin.getServer().getOnlinePlayers().iterator().next();
    }

    public void run() {
        player.sendPluginMessage(plugin, "BungeeCord", bytes.toByteArray());
    }
}
