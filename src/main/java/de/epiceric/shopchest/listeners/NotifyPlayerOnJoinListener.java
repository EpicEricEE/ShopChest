package de.epiceric.shopchest.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;
import de.epiceric.shopchest.utils.Callback;
import de.epiceric.shopchest.utils.Permissions;
import de.epiceric.shopchest.utils.Utils;

public class NotifyPlayerOnJoinListener implements Listener {

    private ShopChest plugin;

    public NotifyPlayerOnJoinListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        if (plugin.isUpdateNeeded() && Config.enableUpdateChecker) {
            if (p.hasPermission(Permissions.UPDATE_NOTIFICATION)) {
                Utils.sendUpdateMessage(plugin, p);
            }
        }

        plugin.getShopDatabase().getLastLogout(p, new Callback<Long>(plugin) {
            @Override
            public void onResult(Long result) {
                if (result < 0) {
                    // No logout saved, probably first time joining.
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
