package de.epiceric.shopchest.addon.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.flag.CreateFlag;
import de.epiceric.shopchest.api.player.ShopPlayer;

public class WorldGuardListener implements Listener {
    private final ShopChest plugin;

    public WorldGuardListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    private boolean hidePermissionMessage(ShopPlayer player, Location location) {
        if (player.getFlag().filter(flag -> flag instanceof CreateFlag).isPresent()) {
            RegionQuery query = WorldGuard.getInstance().getPlatform()
                    .getRegionContainer().createQuery();

            return query.testState(BukkitAdapter.adapt(location),
                    WorldGuardPlugin.inst().wrapPlayer(player.getBukkitPlayer()),
                    AddonMain.SHOP_CREATE_FLAG);
        }

        if (plugin.getShopManager().getShop(location).isPresent()) {
            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUseBlock(UseBlockEvent e) {
        if (e.getCause().getFirstPlayer() == null) {
            return;
        }

        ShopPlayer player = plugin.wrapPlayer(e.getCause().getFirstPlayer());

        if (e.getOriginalEvent() instanceof PlayerInteractEvent) {
            Block block = e.getBlocks().get(0);
            Material type = block.getType();
            
            if (type == Material.CHEST || type == Material.TRAPPED_CHEST) {
                if (hidePermissionMessage(player, block.getLocation())) {
                    e.setResult(Result.ALLOW);
                }
            }
        } else if (e.getOriginalEvent() instanceof InventoryOpenEvent) {
            InventoryOpenEvent orig = (InventoryOpenEvent) e.getOriginalEvent();

            if (orig.getInventory().getHolder() instanceof Chest) {
                if (hidePermissionMessage(player, ((Chest) orig.getInventory().getHolder()).getLocation())) {
                    e.setResult(Result.ALLOW);
                }
            }
        }
    }
}