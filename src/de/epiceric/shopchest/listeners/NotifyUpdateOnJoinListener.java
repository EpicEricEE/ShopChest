package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Regex;
import de.epiceric.shopchest.interfaces.JsonBuilder;
import de.epiceric.shopchest.interfaces.jsonbuilder.*;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.utils.Utils;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NotifyUpdateOnJoinListener implements Listener {

    private ShopChest plugin;
    private Permission perm;

    public NotifyUpdateOnJoinListener(ShopChest plugin) {
        this.plugin = plugin;
        perm = plugin.getPermission();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        Player p = e.getPlayer();

        if (plugin.isUpdateNeeded()) {
            if (p.isOp() || perm.has(p, "shopchest.notification.update")) {
                JsonBuilder jb;

                switch (Utils.getServerVersion()) {
                    case "v1_8_R1":
                        jb = new JsonBuilder_1_8_R1(plugin, LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())));
                        break;
                    case "v1_8_R2":
                        jb = new JsonBuilder_1_8_R2(plugin, LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())));
                        break;
                    case "v1_8_R3":
                        jb = new JsonBuilder_1_8_R3(plugin, LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())));
                        break;
                    case "v1_9_R1":
                        jb = new JsonBuilder_1_9_R1(plugin, LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())));
                        break;
                    case "v1_9_R2":
                        jb = new JsonBuilder_1_9_R2(plugin, LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())));
                        break;
                    case "v1_10_R1":
                        jb = new JsonBuilder_1_10_R1(plugin, LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())));
                        break;
                    default:
                        return;
                }
                jb.sendJson(p);
            }
        }

        if (perm.has(p, "shopchest.broadcast")) {
            if (plugin.getBroadcast() != null) {
                for (String message : plugin.getBroadcast()) {
                    p.sendMessage(message);
                }
            }
        }

    }

}
