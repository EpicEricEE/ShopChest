package de.epiceric.shopchest.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.ClickType.SelectClickType;

public class CreativeModeListener implements Listener {
    private ShopChest plugin;

    public CreativeModeListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e) {
        HumanEntity entity = e.getWhoClicked();
        if (!(entity instanceof Player))
            return;

        Player p = (Player) entity;

        ClickType clickType = ClickType.getPlayerClickType(p);
        if (clickType instanceof SelectClickType) {
            e.setCancelled(true);

            if (e.getCursor() == null || e.getCursor().getType() == Material.AIR)
                return;

            ClickType.removePlayerClickType(p);
            ((SelectClickType) clickType).setItem(e.getCursor());
            p.closeInventory();

            p.sendMessage(LanguageUtils.getMessage(Message.ITEM_SELECTED,
                    new Replacement(Placeholder.ITEM_NAME, LanguageUtils.getItemName(e.getCursor()))));
            plugin.getShopCommand().createShopAfterSelected(p, (SelectClickType) clickType);
        }
    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent e) {
        HumanEntity entity = e.getPlayer();
        if (!(entity instanceof Player))
            return;

        Player p = (Player) entity;

        ClickType clickType = ClickType.getPlayerClickType(p);
        if (!(clickType instanceof SelectClickType))
            return;

        ClickType.removePlayerClickType(p);
        p.sendMessage(LanguageUtils.getMessage(Message.CREATION_CANCELLED));
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Reset game mode on quit if SelectClickType is set
        Player p = e.getPlayer();
        ClickType.removePlayerClickType(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent e) {
        // Cancel any inventory drags if SelectClickType is set
        HumanEntity entity = e.getWhoClicked();
        if (!(entity instanceof Player))
            return;

        ClickType clickType = ClickType.getPlayerClickType((Player) entity);
        if (clickType instanceof SelectClickType)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryMove(InventoryMoveItemEvent e) {
        // Cancel any inventory movement if SelectClickType is set
        if (e.getSource().getHolder() instanceof Player) {
            Player p = (Player) e.getSource().getHolder();

            ClickType clickType = ClickType.getPlayerClickType(p);
            if (clickType instanceof SelectClickType)
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickup(PlayerPickupItemEvent e) {
        // Cancel any item pickups if SelectClickType is set
        ClickType clickType = ClickType.getPlayerClickType(e.getPlayer());
        if (clickType instanceof SelectClickType)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        // Cancel any block breaks if SelectClickType is set
        ClickType clickType = ClickType.getPlayerClickType(e.getPlayer());
        if (clickType instanceof SelectClickType)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        // Cancel any block places if SelectClickType is set
        ClickType clickType = ClickType.getPlayerClickType(e.getPlayer());
        if (clickType instanceof SelectClickType)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockMultiPlace(BlockMultiPlaceEvent e) {
        // Cancel any block places if SelectClickType is set
        ClickType clickType = ClickType.getPlayerClickType(e.getPlayer());
        if (clickType instanceof SelectClickType)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        // Cancel any interactions if SelectClickType is set
        ClickType clickType = ClickType.getPlayerClickType(e.getPlayer());
        if (clickType instanceof SelectClickType)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        // Cancel any entity interactions if SelectClickType is set
        ClickType clickType = ClickType.getPlayerClickType(e.getPlayer());
        if (clickType instanceof SelectClickType)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
        // Cancel any entity damaging if SelectClickType is set
        Entity entity = e.getDamager();
        if (!(entity instanceof Player))
            return;

        ClickType clickType = ClickType.getPlayerClickType((Player) entity);
        if (clickType instanceof SelectClickType)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent e) {
        // Cancel any player movement if SelectClickType is set
        ClickType clickType = ClickType.getPlayerClickType(e.getPlayer());
        if (clickType instanceof SelectClickType)
            e.setCancelled(true);
    }

}