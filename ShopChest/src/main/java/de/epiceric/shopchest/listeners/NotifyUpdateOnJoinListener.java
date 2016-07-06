package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Regex;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.LocalizedMessage;
import de.epiceric.shopchest.nms.IJsonBuilder;
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
                IJsonBuilder jb;

                switch (Utils.getServerVersion()) {
                    case "v1_8_R1":
                        jb = new de.epiceric.shopchest.nms.v1_8_R1.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), plugin.getDownloadLink());
                        break;
                    case "v1_8_R2":
                        jb = new de.epiceric.shopchest.nms.v1_8_R2.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), plugin.getDownloadLink());
                        break;
                    case "v1_8_R3":
                        jb = new de.epiceric.shopchest.nms.v1_8_R3.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), plugin.getDownloadLink());
                        break;
                    case "v1_9_R1":
                        jb = new de.epiceric.shopchest.nms.v1_9_R1.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), plugin.getDownloadLink());
                        break;
                    case "v1_9_R2":
                        jb = new de.epiceric.shopchest.nms.v1_9_R2.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), plugin.getDownloadLink());
                        break;
                    case "v1_10_R1":
                        jb = new de.epiceric.shopchest.nms.v1_10_R1.JsonBuilder(LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_AVAILABLE, new LocalizedMessage.ReplacedRegex(Regex.VERSION, plugin.getLatestVersion())), LanguageUtils.getMessage(LocalizedMessage.Message.UPDATE_CLICK_TO_DOWNLOAD), plugin.getDownloadLink());
                        break;
                    default:
                        return;
                }
                jb.sendJson(p);
            }
        }

    }

}
