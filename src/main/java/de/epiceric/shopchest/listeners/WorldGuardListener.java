package de.epiceric.shopchest.listeners;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.event.WrappedUseBlockEvent;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.ClickType.EnumClickType;

public class WorldGuardListener implements Listener {

    private ShopChest plugin;

    public WorldGuardListener(ShopChest plugin) {
        this.plugin = plugin;
    }


    private boolean isAllowed(Player player, Location location) {
        ClickType clickType = ClickType.getPlayerClickType(player);

        if (clickType != null && clickType.getClickType() == EnumClickType.CREATE) {
            // If the player is about to create a shop, but does not have
            // access to the chest, show the 'permission denied' message
            // (if not previously set to allowed by another plugin).
            // If the player can open the chest, that message should be hidden.
            WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
            Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag("chest-access", WrappedState.class);
            if (!flag.isPresent()) plugin.debug("WorldGuard flag 'chest-access' is not present!");
            WrappedState state = flag.map(f -> wgWrapper.queryFlag(player, location, f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
            return state == WrappedState.ALLOW;
        }

        Shop shop = plugin.getShopUtils().getShop(location);

        if (shop != null) {
            // Don't show 'permission denied' messages for any kind of
            // shop interaction even if block interaction is not
            // allowed in the region.
            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUseBlock(WrappedUseBlockEvent event) {
        if (Config.enableWorldGuardIntegration) {
            Player player = event.getPlayer();

            if (event.getOriginalEvent() instanceof PlayerInteractEvent) {
                Block block = event.getBlocks().get(0);
                Material type = block.getType();
                
                if (type == Material.CHEST || type == Material.TRAPPED_CHEST || type == Material.SHULKER_BOX || type == Material.BARREL) {
                    if (isAllowed(player, block.getLocation())) {
                        event.setResult(Result.ALLOW);
                    }
                }
            } else if (event.getOriginalEvent() instanceof InventoryOpenEvent) {
                InventoryOpenEvent orig = (InventoryOpenEvent) event.getOriginalEvent();

                if (orig.getInventory().getHolder() instanceof Chest || orig.getInventory().getHolder() instanceof ShulkerBox || orig.getInventory().getHolder() instanceof Barrel) {
                    if (isAllowed(player, ((BlockState) orig.getInventory().getHolder()).getLocation())) {
                        event.setResult(Result.ALLOW);
                    }
                }
            }
        }
    }

}
