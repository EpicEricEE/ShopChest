package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.nms.Hologram;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.ClickType.EnumClickType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.event.AbstractWrappedEvent;
import org.codemc.worldguardwrapper.event.WrappedDamageEntityEvent;
import org.codemc.worldguardwrapper.event.WrappedUseBlockEvent;
import org.codemc.worldguardwrapper.event.WrappedUseEntityEvent;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import java.util.Optional;

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
            // shop interaction even if entity/block interaction is not
            // allowed in the region.
            return true;
        }

        return false;
    }

    private void handleEntityInteraction(Player player, Entity entity, AbstractWrappedEvent event) {
        if (entity.getType() == EntityType.ARMOR_STAND) {
            if (!Hologram.isPartOfHologram((ArmorStand) entity))
                return;

            for (Shop shop : plugin.getShopUtils().getShops()) {
                if (shop.getHologram() != null && shop.getHologram().contains((ArmorStand) entity)) {
                    if (isAllowed(player, shop.getLocation())) {
                        event.setResult(Result.ALLOW);
                    }
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUseEntity(WrappedUseEntityEvent event) {
        if (Config.enableWorldGuardIntegration) {
            Player player = event.getPlayer();

            if (event.getOriginalEvent() instanceof PlayerInteractAtEntityEvent) {
                handleEntityInteraction(player, event.getEntity(), event);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamageEntity(WrappedDamageEntityEvent event) {
        if (Config.enableWorldGuardIntegration) {
            Player player = event.getPlayer();

            if (event.getOriginalEvent() instanceof EntityDamageByEntityEvent) {
                handleEntityInteraction(player, event.getEntity(), event);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUseBlock(WrappedUseBlockEvent event) {
        if (Config.enableWorldGuardIntegration) {
            Player player = event.getPlayer();

            if (event.getOriginalEvent() instanceof PlayerInteractEvent) {
                Block block = event.getBlocks().get(0);
                Material type = block.getType();
                
                if (type == Material.CHEST || type == Material.TRAPPED_CHEST) {
                    if (isAllowed(player, block.getLocation())) {
                        event.setResult(Result.ALLOW);
                    }
                }
            } else if (event.getOriginalEvent() instanceof InventoryOpenEvent) {
                InventoryOpenEvent orig = (InventoryOpenEvent) event.getOriginalEvent();

                if (orig.getInventory().getHolder() instanceof Chest) {
                    if (isAllowed(player, ((Chest) orig.getInventory().getHolder()).getLocation())) {
                        event.setResult(Result.ALLOW);
                    }
                }
            }
        }
    }

}
