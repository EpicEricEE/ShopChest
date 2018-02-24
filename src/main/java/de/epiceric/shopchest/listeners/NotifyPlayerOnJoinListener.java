package de.epiceric.shopchest.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;
import de.epiceric.shopchest.nms.JsonBuilder;
import de.epiceric.shopchest.utils.Callback;
import de.epiceric.shopchest.utils.Permissions;

public class NotifyPlayerOnJoinListener implements Listener {

    private ShopChest plugin;

    public NotifyPlayerOnJoinListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        if (plugin.isUpdateNeeded()) {
            if (p.hasPermission(Permissions.UPDATE_NOTIFICATION)) {
                JsonBuilder jb = new JsonBuilder(plugin, LanguageUtils.getMessage(Message.UPDATE_AVAILABLE, new Replacement(Placeholder.VERSION, plugin.getLatestVersion())), LanguageUtils.getMessage(Message.UPDATE_CLICK_TO_DOWNLOAD), plugin.getDownloadLink());
                jb.sendJson(p);
            }
        }

        plugin.getShopDatabase().getLastLogout(p, new Callback<Long>(plugin) {
            @Override
            public void onResult(Long result) {
                if (result < 0) {
                    p.sendMessage(LanguageUtils.getMessage(Message.ERROR_OCCURRED,
                            new Replacement(Placeholder.ERROR, "Could not get last time you logged out")));
                    return;
                }

                plugin.getShopDatabase().getRevenue(p, result, new Callback<Double>(plugin) {
                    @Override
                    public void onResult(Double result) {
                        if (result != 0) {
                            p.sendMessage(LanguageUtils.getMessage(Message.REVENUE_WHILE_OFFLINE,
                                    new Replacement(Placeholder.REVENUE, String.valueOf(result))));
                        }
                    }
                });
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        long time = System.currentTimeMillis();
        plugin.getShopDatabase().logLogout(e.getPlayer(), time, null);
    }

}
