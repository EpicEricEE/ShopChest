package de.epiceric.shopchest.event;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.utils.*;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.yi.acru.bukkit.Lockette.Lockette;

import java.util.HashMap;
import java.util.Map;

public class InteractShop implements Listener {

    private ShopChest plugin;
    private Permission perm = ShopChest.perm;
    private Economy econ = ShopChest.econ;
    private Database database = ShopChest.database;

    public InteractShop(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

        Block b = e.getClickedBlock();
        Player p = e.getPlayer();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {

            if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {

                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {

                    if (ClickType.getPlayerClickType(p) != null) {

                        switch (ClickType.getPlayerClickType(p).getClickType()) {

                            case CREATE:
                                e.setCancelled(true);

                                if (!p.isOp() || !perm.has(p, "shopchest.create.protected")) {
                                    if (ShopChest.lockette) {
                                        if (Lockette.isProtected(b)) {
                                            if (!Lockette.isOwner(b, p) || !Lockette.isUser(b, p, true)) {
                                                ClickType.removePlayerClickType(p);
                                                break;
                                            }
                                        }
                                    }

                                    if (ShopChest.lwc) {
                                        if (LWC.getInstance().getPhysicalDatabase().loadProtection(b.getLocation().getWorld().getName(), b.getX(), b.getY(), b.getZ()) != null) {
                                            Protection protection = LWC.getInstance().getPhysicalDatabase().loadProtection(b.getLocation().getWorld().getName(), b.getX(), b.getY(), b.getZ());
                                            if (!protection.isOwner(p) || !protection.isRealOwner(p)) {
                                                ClickType.removePlayerClickType(p);
                                                break;
                                            }
                                        }
                                    }
                                }


                                if (!ShopUtils.isShop(b.getLocation())) {
                                    ClickType clickType = ClickType.getPlayerClickType(p);
                                    ItemStack product = clickType.getProduct();
                                    double buyPrice = clickType.getBuyPrice();
                                    double sellPrice = clickType.getSellPrice();
                                    ShopType shopType = clickType.getShopType();

                                    create(p, b.getLocation(), product, buyPrice, sellPrice, shopType);
                                } else {
                                    p.sendMessage(Config.chest_already_shop());
                                }

                                ClickType.removePlayerClickType(p);
                                break;

                            case INFO:
                                e.setCancelled(true);

                                if (ShopUtils.isShop(b.getLocation())) {

                                    Shop shop = ShopUtils.getShop(b.getLocation());
                                    info(p, shop);

                                } else {
                                    p.sendMessage(Config.chest_no_shop());
                                }

                                ClickType.removePlayerClickType(p);
                                break;

                            case REMOVE:
                                e.setCancelled(true);

                                if (ShopUtils.isShop(b.getLocation())) {

                                    Shop shop = ShopUtils.getShop(b.getLocation());

                                    if (shop.getVendor().getUniqueId().equals(p.getUniqueId()) || perm.has(p, "shopchest.removeOther")) {
                                        remove(p, shop);
                                    } else {
                                        p.sendMessage(Config.noPermission_removeOthers());
                                    }

                                } else {
                                    p.sendMessage(Config.chest_no_shop());
                                }

                                ClickType.removePlayerClickType(p);
                                break;

                        }

                    } else {

                        if (ShopUtils.isShop(b.getLocation())) {
                            e.setCancelled(true);
                            Shop shop = ShopUtils.getShop(b.getLocation());

                            if (p.isSneaking()) {
                                if (!shop.getVendor().getUniqueId().equals(p.getUniqueId())) {
                                    if (perm.has(p, "shopchest.openOther")) {
                                        p.sendMessage(Config.opened_shop(shop.getVendor().getName()));
                                        e.setCancelled(false);
                                    } else {
                                        p.sendMessage(Config.noPermission_openOthers());
                                    }
                                } else {
                                    e.setCancelled(false);
                                }
                            } else {
                                if (shop.getShopType() == ShopType.ADMIN || !shop.getVendor().getUniqueId().equals(p.getUniqueId())) {
                                    if (shop.getBuyPrice() > 0) {
                                        if (perm.has(p, "shopchest.buy")) {
                                            if (shop.getShopType() == ShopType.ADMIN) {
                                                buy(p, shop);
                                            } else {
                                                Chest c = (Chest) b.getState();
                                                if (Utils.getAmount(c.getInventory(), shop.getProduct()) >= shop.getProduct().getAmount()) {
                                                    buy(p, shop);
                                                } else {
                                                    p.sendMessage(Config.out_of_stock());
                                                }
                                            }
                                        } else {
                                            p.sendMessage(Config.noPermission_buy());
                                        }
                                    } else {
                                        p.sendMessage(Config.buying_disabled());
                                    }
                                } else {
                                    e.setCancelled(false);
                                }
                            }
                        }

                    }


                } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {

                    if (ShopUtils.isShop(b.getLocation())) {
                        e.setCancelled(true);
                        Shop shop = ShopUtils.getShop(b.getLocation());

                        if ((shop.getShopType() == ShopType.ADMIN) || (!shop.getVendor().getUniqueId().equals(p.getUniqueId()))) {
                            if (shop.getSellPrice() > 0) {
                                if (perm.has(p, "shopchest.sell")) {
                                    if (Utils.getAmount(p.getInventory(), shop.getProduct()) >= shop.getProduct().getAmount()) {
                                        sell(p, shop);
                                    } else {
                                        p.sendMessage(Config.not_enough_items());
                                    }
                                } else {
                                    p.sendMessage(Config.noPermission_sell());
                                }
                            } else {
                                p.sendMessage(Config.selling_disabled());
                            }
                        } else {
                            e.setCancelled(false);
                        }
                    }

                }

            }

        } else {
            if (ClickType.getPlayerClickType(p) != null) ClickType.removePlayerClickType(p);
        }

    }

    private void create(Player executor, Location location, ItemStack product, double buyPrice, double sellPrice, ShopType shopType) {
        Shop shop = new Shop(database.getNextFreeID(), plugin, executor, product, location, buyPrice, sellPrice, shopType);

        ShopUtils.addShop(shop, true);
        executor.sendMessage(Config.shop_created());

        for (Player p : Bukkit.getOnlinePlayers()) {
            Bukkit.getPluginManager().callEvent(new PlayerMoveEvent(p, p.getLocation(), p.getLocation()));
        }

    }

    private void remove(Player executor, Shop shop) {
        ShopUtils.removeShop(shop, true);
        executor.sendMessage(Config.shop_removed());
    }

    private void info(Player executor, Shop shop) {

        Chest c = (Chest) shop.getLocation().getBlock().getState();

        int amount = Utils.getAmount(c.getInventory(), shop.getProduct());

        String vendor = Config.shopInfo_vendor(shop.getVendor().getName());
        String product = Config.shopInfo_product(shop.getProduct().getAmount(), ItemNames.lookup(shop.getProduct()));
        String enchantmentString = "";
        String arrowEffectString = "";
        String price = Config.shopInfo_price(shop.getBuyPrice(), shop.getSellPrice());
        String shopType;
        String stock = Config.shopInfo_stock(amount);

        if (shop.getShopType() == ShopType.NORMAL) shopType = Config.shopInfo_isNormal();
        else shopType = Config.shopInfo_isAdmin();

        Map<Enchantment, Integer> enchantmentMap;

        if (Utils.getVersion(Bukkit.getServer()).contains("1_9")) {
            if (shop.getProduct().getType() == Material.TIPPED_ARROW) {
                arrowEffectString = ArrowEffectNames.getTippedArrowName(shop.getProduct());
                if (arrowEffectString == null) arrowEffectString = Config.none();
            }
        }

        if (shop.getProduct().getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) shop.getProduct().getItemMeta();
            enchantmentMap = esm.getStoredEnchants();
        } else {
            enchantmentMap = shop.getProduct().getEnchantments();
        }

        Enchantment[] enchantments = enchantmentMap.keySet().toArray(new Enchantment[enchantmentMap.size()]);

        for (int i = 0; i < enchantments.length; i++) {

            Enchantment enchantment = enchantments[i];

            if (i == enchantments.length - 1) {
                enchantmentString += EnchantmentNames.lookup(enchantment, enchantmentMap.get(enchantment));
            } else {
                enchantmentString += EnchantmentNames.lookup(enchantment, enchantmentMap.get(enchantment)) + ", ";
            }

        }

        executor.sendMessage(" ");
        executor.sendMessage(vendor);
        executor.sendMessage(product);
        executor.sendMessage(stock);
        if (enchantmentString.length() > 0) executor.sendMessage(Config.shopInfo_enchantment(enchantmentString));
        if (arrowEffectString.length() > 0) executor.sendMessage(Config.shopInfo_arrowEffect(arrowEffectString));
        executor.sendMessage(price);
        executor.sendMessage(shopType);
        executor.sendMessage(" ");
    }

    private void buy(Player executor, Shop shop) {
        if (econ.getBalance(executor) >= shop.getBuyPrice()) {

            Block b = shop.getLocation().getBlock();
            Chest c = (Chest) b.getState();

            HashMap<Integer, Integer> slotFree = new HashMap<>();
            ItemStack product = new ItemStack(shop.getProduct());
            Inventory inventory = executor.getInventory();

            for (int i = 0; i < 36; i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null) {
                    slotFree.put(i, product.getMaxStackSize());
                } else {
                    if (item.isSimilar(product)) {
                        int amountInSlot = item.getAmount();
                        int amountToFullStack = product.getMaxStackSize() - amountInSlot;
                        slotFree.put(i, amountToFullStack);
                    }
                }
            }

            if (Utils.getVersion(Bukkit.getServer()).contains("1_9")) {
                ItemStack item = inventory.getItem(40);
                if (item == null) {
                    slotFree.put(40, product.getMaxStackSize());
                } else {
                    if (item.isSimilar(product)) {
                        int amountInSlot = item.getAmount();
                        int amountToFullStack = product.getMaxStackSize() - amountInSlot;
                        slotFree.put(40, amountToFullStack);
                    }
                }
            }

            int freeAmount = 0;
            for (int value : slotFree.values()) {
                freeAmount += value;
            }

            if (freeAmount >= product.getAmount()) {

                EconomyResponse r = econ.withdrawPlayer(executor, shop.getBuyPrice());
                EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.depositPlayer(shop.getVendor(), shop.getBuyPrice()) : null;

                if (r.transactionSuccess()) {
                    if (r2 != null) {
                        if (r2.transactionSuccess()) {
                            addToInventory(inventory, product);
                            removeFromInventory(c.getInventory(), product);
                            executor.updateInventory();
                            executor.sendMessage(Config.buy_success(product.getAmount(), ItemNames.lookup(product), shop.getBuyPrice(), shop.getVendor().getName()));

                            if (shop.getVendor().isOnline()) {
                                shop.getVendor().getPlayer().sendMessage(Config.someone_bought(product.getAmount(), ItemNames.lookup(product), shop.getBuyPrice(), executor.getName()));
                            }

                        } else {
                            executor.sendMessage(Config.error_occurred(r2.errorMessage));
                        }
                    } else {
                        addToInventory(inventory, product);
                        executor.updateInventory();
                        executor.sendMessage(Config.buy_success_admin(product.getAmount(), ItemNames.lookup(product), shop.getBuyPrice()));
                    }
                } else {
                    executor.sendMessage(Config.error_occurred(r.errorMessage));
                }
            } else {
                executor.sendMessage(Config.not_enough_inventory_space());
            }
        } else {
            executor.sendMessage(Config.not_enough_money());
        }
    }

    private void sell(Player executor, Shop shop) {
        if (econ.getBalance(shop.getVendor()) >= shop.getSellPrice()) {

            Block block = shop.getLocation().getBlock();
            Chest chest = (Chest) block.getState();

            HashMap<Integer, Integer> slotFree = new HashMap<>();
            ItemStack product = new ItemStack(shop.getProduct());
            Inventory inventory = chest.getInventory();

            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null) {
                    slotFree.put(i, product.getMaxStackSize());
                } else {
                    if (item.isSimilar(product)) {
                        int amountInSlot = item.getAmount();
                        int amountToFullStack = product.getMaxStackSize() - amountInSlot;
                        slotFree.put(i, amountToFullStack);
                    }
                }
            }

            int freeAmount = 0;
            for (int value : slotFree.values()) {
                freeAmount += value;
            }

            if (freeAmount >= product.getAmount()) {

                EconomyResponse r = econ.withdrawPlayer(executor, shop.getBuyPrice());
                EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.depositPlayer(shop.getVendor(), shop.getBuyPrice()) : null;

                if (r.transactionSuccess()) {
                    if (r2 != null) {
                        if (r2.transactionSuccess()) {
                            addToInventory(inventory, product);
                            removeFromInventory(executor.getInventory(), product);
                            executor.updateInventory();
                            executor.sendMessage(Config.sell_success(product.getAmount(), ItemNames.lookup(product), shop.getSellPrice(), shop.getVendor().getName()));

                            if (shop.getVendor().isOnline()) {
                                shop.getVendor().getPlayer().sendMessage(Config.someone_sold(product.getAmount(), ItemNames.lookup(product), shop.getSellPrice(), executor.getName()));
                            }

                        } else {
                            executor.sendMessage(Config.error_occurred(r2.errorMessage));
                        }

                    } else {
                        removeFromInventory(executor.getInventory(), product);
                        executor.updateInventory();
                        executor.sendMessage(Config.sell_success_admin(product.getAmount(), ItemNames.lookup(product), shop.getSellPrice()));
                    }

                } else {
                    executor.sendMessage(Config.error_occurred(r.errorMessage));
                }

            } else {
                executor.sendMessage(Config.chest_not_enough_inventory_space());
            }

        } else {
            executor.sendMessage(Config.vendor_not_enough_money());
        }
    }

    private boolean addToInventory(Inventory inventory, ItemStack itemStack) {
        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        int amount = itemStack.getAmount();
        int added = 0;

        if (inventory instanceof PlayerInventory) {
            if (Utils.getVersion(plugin.getServer()).contains("1_9")) {
                inventoryItems.put(40, inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }
        }

        slotLoop:
        for (int slot : inventoryItems.keySet()) {
            while (added < amount) {
                ItemStack item = inventory.getItem(slot);

                if (item != null) {
                    if (item.isSimilar(itemStack)) {
                        if (item.getAmount() != item.getMaxStackSize()) {
                            ItemStack newItemStack = new ItemStack(item);
                            newItemStack.setAmount(item.getAmount() + 1);
                            inventory.setItem(slot, newItemStack);
                            added++;
                        } else {
                            continue slotLoop;
                        }
                    } else {
                        continue slotLoop;
                    }
                } else {
                    ItemStack newItemStack = new ItemStack(itemStack);
                    newItemStack.setAmount(1);
                    inventory.setItem(slot, newItemStack);
                    added++;
                }
            }
        }

        return (added == amount);
    }

    private boolean removeFromInventory(Inventory inventory, ItemStack itemStack) {
        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        int amount = itemStack.getAmount();
        int removed = 0;

        if (inventory instanceof PlayerInventory) {
            if (Utils.getVersion(plugin.getServer()).contains("1_9")) {
                inventoryItems.put(40, inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }
        }

        slotLoop:
        for (int slot : inventoryItems.keySet()) {
            while (removed < amount) {
                ItemStack item = inventory.getItem(slot);

                if (item != null) {
                    if (item.isSimilar(itemStack)) {
                        if (item.getAmount() > 0) {
                            int newAmount = item.getAmount() - 1;

                            ItemStack newItemStack = new ItemStack(item);
                            newItemStack.setAmount(newAmount);

                            if (newAmount == 0)
                                inventory.setItem(slot, null);
                            else
                                inventory.setItem(slot, newItemStack);

                            removed++;
                        } else {
                            continue slotLoop;
                        }
                    } else {
                        continue slotLoop;
                    }
                } else {
                    continue slotLoop;
                }

            }
        }

        return (removed == amount);
    }

}
