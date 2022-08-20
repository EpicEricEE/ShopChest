package de.epiceric.shopchest.listener.internal;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.event.ShopSelectItemEvent;
import de.epiceric.shopchest.api.flag.SelectFlag;
import de.epiceric.shopchest.api.player.ShopPlayer;

public class CreativeSelectListener implements Listener {
    private final ShopChest plugin;

    public CreativeSelectListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        ShopPlayer player = plugin.wrapPlayer((Player) e.getWhoClicked());
        player.getFlag().filter(flag -> flag instanceof SelectFlag).ifPresent(f -> {
            e.setCancelled(true);

            if (e.getCursor() == null || e.getCursor().getType() == Material.AIR) {
                return;
            }

            SelectFlag flag = (SelectFlag) f;
            player.removeFlag();
            plugin.getServer().getScheduler().runTask(plugin, () -> player.getBukkitPlayer().closeInventory());

            plugin.getServer().getPluginManager().callEvent(new ShopSelectItemEvent(player, e.getCursor(),
                    flag.getAmount(), flag.getBuyPrice(), flag.getSellPrice(), flag.getType()));
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) {
            return;
        }

        ShopPlayer player = plugin.wrapPlayer((Player) e.getPlayer());
        if (hasSelectFlag(player)) {
            player.removeFlag();
            player.sendMessage("§cShop creation has been cancelled.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Remove flag to reset game mode if SelectFlag is assigned
        plugin.wrapPlayer(e.getPlayer()).removeFlag();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent e) {
        // Cancel any inventory drags if SelectFlag is assigned
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        ShopPlayer player = plugin.wrapPlayer((Player) e.getWhoClicked());
        if (hasSelectFlag(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryMove(InventoryMoveItemEvent e) {
        // Cancel any inventory movement if SelectFlag is assigned
        if (!(e.getSource().getHolder() instanceof Player)) {
            return;
        }

        ShopPlayer player = plugin.wrapPlayer((Player) e.getSource().getHolder());
        if (hasSelectFlag(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPickup(EntityPickupItemEvent e) {
        // Cancel any item pickups if SelectFlag is assigned
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        ShopPlayer player = plugin.wrapPlayer((Player) e.getEntity());
        if (hasSelectFlag(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        // Cancel any block breaks if SelectFlag is assigned
        ShopPlayer player = plugin.wrapPlayer(e.getPlayer());
        if (hasSelectFlag(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        // Cancel any block places if SelectFlag is assigned
        ShopPlayer player = plugin.wrapPlayer(e.getPlayer());
        if (hasSelectFlag(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockMultiPlace(BlockMultiPlaceEvent e) {
        // Cancel any block places if SelectFlag is assigned
        ShopPlayer player = plugin.wrapPlayer(e.getPlayer());
        if (hasSelectFlag(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        // Cancel any interactions if SelectFlag is assigned
        ShopPlayer player = plugin.wrapPlayer(e.getPlayer());
        if (hasSelectFlag(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        // Cancel any entity interactions if SelectFlag is assigned
        ShopPlayer player = plugin.wrapPlayer(e.getPlayer());
        if (hasSelectFlag(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
        // Cancel any entity damaging if SelectFlag is assigned
        if (!(e.getDamager() instanceof Player)) {
            return;
        }

        ShopPlayer player = plugin.wrapPlayer((Player) e.getDamager());
        if (hasSelectFlag(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent e) {
        // Cancel any player movement if SelectFlag is assigned
        ShopPlayer player = plugin.wrapPlayer(e.getPlayer());
        if (hasSelectFlag(player)) {
            e.setCancelled(true);
        }
    }

    private boolean hasSelectFlag(ShopPlayer player) {
        return player.getFlag().filter(flag -> flag instanceof SelectFlag).isPresent();
    }
}