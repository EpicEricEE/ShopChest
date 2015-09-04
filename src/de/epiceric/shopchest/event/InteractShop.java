package de.epiceric.shopchest.event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.yi.acru.bukkit.Lockette.Lockette;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.utils.EnchantmentNames;
import de.epiceric.shopchest.utils.ItemNames;
import de.epiceric.shopchest.utils.ShopUtils;
import de.epiceric.shopchest.interfaces.Utils;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

public class InteractShop implements Listener{

	private ShopChest plugin;
	private Permission perm = ShopChest.perm;
	private Economy econ = ShopChest.econ;
	private YamlConfiguration shopChests;
	
	public InteractShop(ShopChest plugin) {
		this.plugin = plugin;
		shopChests = plugin.shopChests;
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
								
								if (ShopChest.lwc != null) {
									if (ShopChest.lwc.getPhysicalDatabase().loadProtection(b.getLocation().getWorld().getName(), b.getX(), b.getY(), b.getZ()) != null) {
										Protection protection = ShopChest.lwc.getPhysicalDatabase().loadProtection(b.getLocation().getWorld().getName(), b.getX(), b.getY(), b.getZ());
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
								boolean infinite = clickType.isInfinite();
								
								create(p, b.getLocation(), product, buyPrice, sellPrice, infinite);
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
								
								if (shop.getVendor().equals(p) || perm.has(p, "shopchest.removeOther")) {
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
							
							if (p.equals(shop.getVendor())) {
								e.setCancelled(false);
								return;
							} else {
								
								if (p.isSneaking()) {
									if (perm.has(p, "shopchest.openOther")) {
										p.sendMessage(Config.opened_shop(shop.getVendor().getName()));
										e.setCancelled(false);
										
									} else {
										p.sendMessage(Config.noPermission_openOthers());
										e.setCancelled(true);
									}
								} else {
									
									if (shop.getBuyPrice() > 0) {
										e.setCancelled(true);

										if (perm.has(p, "shopchest.buy")) {
											if (shop.isInfinite()) {
												buy(p, shop);
											} else {
												Chest c = (Chest) b.getState();
												if (Utils.getAmount(c.getInventory(), shop.getProduct().clone().getType(), shop.getProduct().clone().getDurability(), shop.getProduct().getItemMeta()) >= shop.getProduct().getAmount()) {
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
								}
								
							}
							
						}
						
					}
					
					
					
				} else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
					
					if (ShopUtils.isShop(b.getLocation())) {
						Shop shop = ShopUtils.getShop(b.getLocation());
						
						if (!p.equals(shop.getVendor())) {
							if (shop.getSellPrice() > 0) {
								if (perm.has(p, "shopchest.sell")) {
									if (Utils.getAmount(p.getInventory(), shop.getProduct().getType(), shop.getProduct().getDurability(), shop.getProduct().getItemMeta()) >= shop.getProduct().getAmount()) {
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
						}

					}				
					
				}
				
			}
			
		} else {			
			if (ClickType.getPlayerClickType(p) != null) ClickType.removePlayerClickType(p);			
		}
		
	}
	
	private void create(Player executor, Location location, ItemStack product, double buyPrice, double sellPrice, boolean infinite) {
		
		Shop shop = new Shop(plugin, executor, product, location, buyPrice, sellPrice, infinite);
		
		shopChests.set(ShopUtils.getConfigTitle(location) + ".vendor", executor);
		shopChests.set(ShopUtils.getConfigTitle(location) + ".location.world", location.getWorld().getName());
		shopChests.set(ShopUtils.getConfigTitle(location) + ".location.x", location.getBlockX());
		shopChests.set(ShopUtils.getConfigTitle(location) + ".location.y", location.getBlockY());
		shopChests.set(ShopUtils.getConfigTitle(location) + ".location.z", location.getBlockZ());
		shopChests.set(ShopUtils.getConfigTitle(location) + ".product", product);
		shopChests.set(ShopUtils.getConfigTitle(location) + ".price.buy", buyPrice);
		shopChests.set(ShopUtils.getConfigTitle(location) + ".price.sell", sellPrice);
		shopChests.set(ShopUtils.getConfigTitle(location) + ".infinite", infinite);
		
		try {shopChests.save(plugin.shopChestsFile);} catch (IOException ex) {ex.printStackTrace();}
		
		ShopUtils.addShop(shop);
		executor.sendMessage(Config.shop_created());
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			Bukkit.getPluginManager().callEvent(new PlayerMoveEvent(p, p.getLocation(), p.getLocation()));
		}
				
	}
	
	private void remove(Player executor, Shop shop) {
		
		shop.getItem().remove();
		ShopUtils.removeShop(shop);
		
		shopChests.set(ShopUtils.getConfigTitle(shop.getLocation()), null);
		try {shopChests.save(plugin.shopChestsFile);} catch (IOException ex) {ex.printStackTrace();}
		
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			shop.getHologram().hidePlayer(player);
		}
		
		executor.sendMessage(Config.shop_removed());

	}
	
	private void info(Player executor, Shop shop) {
				
		String vendor = Config.shopInfo_vendor(shop.getVendor().getName());
		String product = Config.shopInfo_product(shop.getProduct().getAmount(), ItemNames.lookup(shop.getProduct()));
		String enchantmentString = "";
		String price = Config.shopInfo_price(shop.getBuyPrice(), shop.getSellPrice());
		String infinite = (shop.isInfinite() ? Config.shopInfo_isInfinite() : Config.shopInfo_isNormal());
		
		
		Map<Enchantment, Integer> enchantmentMap = shop.getProduct().getItemMeta().getEnchants();
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
		if (enchantmentString.length() > 0) executor.sendMessage(Config.shopInfo_enchantment(enchantmentString));
		executor.sendMessage(price);
		executor.sendMessage(infinite);
		executor.sendMessage(" ");
		
		
	}
	
	private void buy(Player executor, Shop shop) {
					
			if (econ.getBalance(executor) >= shop.getBuyPrice()) {
				
				Block b = shop.getLocation().getBlock();
				Chest c = (Chest) b.getState();
				
				HashMap<Integer, Integer> slotFree = new HashMap<>();
				ItemStack product = shop.getProduct().clone();
				Inventory inventory = executor.getInventory();
				
				for (int i = 0; i < 36; i++) {
					
					ItemStack item = inventory.getItem(i);
					if (item == null) {
						slotFree.put(i, product.getMaxStackSize());
					} else {
						if ((item.getType().equals(product.getType())) && (item.getDurability() == product.getDurability()) && (item.getItemMeta().equals(product.getItemMeta())) && (item.getData().equals(product.getData()))) {
							int amountInSlot = item.getAmount();
							int amountToFullStack = product.getMaxStackSize() - amountInSlot;
							slotFree.put(i, amountToFullStack);
						}
					}
					
				}
				
				int leftAmount = product.getAmount();
				
				int freeAmount = 0;
				for (int value : slotFree.values()) {
					freeAmount += value;
				}
				
				EconomyResponse r = econ.withdrawPlayer(executor, shop.getBuyPrice());
				EconomyResponse r2 = econ.depositPlayer(shop.getVendor(), shop.getBuyPrice());
				
				if (r.transactionSuccess()) {				
					if (r2.transactionSuccess()) {					
						if (freeAmount >= leftAmount) {						
							for (int slot : slotFree.keySet()) {
								if (leftAmount >= 0) {
									int amountInSlot = -(slotFree.get(slot) - product.getMaxStackSize());
									if (amountInSlot == -0) amountInSlot = 0;
									for (int i = amountInSlot; i < product.getMaxStackSize() + 1; i++) {
										if (leftAmount > 0) {
											ItemStack boughtProduct = new ItemStack(product.clone().getType(), 1, product.clone().getDurability());
											boughtProduct.setItemMeta(product.clone().getItemMeta());
											if (!shop.isInfinite()) c.getInventory().removeItem(boughtProduct);
											inventory.addItem(boughtProduct);
											executor.updateInventory();
											leftAmount--;
										} else if (leftAmount == 0) {
											executor.sendMessage(Config.buy_success(product.getAmount(), ItemNames.lookup(product), shop.getBuyPrice(), shop.getVendor().getName()));
											return;
										}
									}
								}
							}							
						} else {
							executor.sendMessage(Config.not_enough_inventory_space());
						}
					} else {
						executor.sendMessage(Config.error_occurred(r2.errorMessage));
					}
				} else {
					executor.sendMessage(Config.error_occurred(r.errorMessage));
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
			ItemStack product = shop.getProduct().clone();
			Inventory inventory = chest.getInventory();
			
			for (int i = 0; i < chest.getInventory().getSize(); i++) {
				
				ItemStack item = inventory.getItem(i);
				if (item == null) {
					slotFree.put(i, product.getMaxStackSize());
				} else {
					if ((item.getType().equals(product.getType())) && (item.getDurability() == product.getDurability()) && (item.getItemMeta().equals(product.getItemMeta())) && (item.getData().equals(product.getData()))) {
						int amountInSlot = item.getAmount();
						int amountToFullStack = product.getMaxStackSize() - amountInSlot;
						slotFree.put(i, amountToFullStack);
					}
				}
				
			}
			
			int leftAmount = product.getAmount();
			
			int freeAmount = 0;
			for (int value : slotFree.values()) {
				freeAmount += value;
			}
			
			EconomyResponse r = econ.withdrawPlayer(shop.getVendor(), shop.getSellPrice());
			EconomyResponse r2 = econ.depositPlayer(executor, shop.getSellPrice());
			
			if (r.transactionSuccess()) {
				if (r2.transactionSuccess()) {
					if (freeAmount >= leftAmount) {
						for (int slot : slotFree.keySet()) {
							if (leftAmount >= 0) {
								int amountInSlot = -(slotFree.get(slot) - product.getMaxStackSize());
								if (amountInSlot == -0) amountInSlot = 0;
								for (int i = amountInSlot; i < product.getMaxStackSize() + 1; i++) {
									if (leftAmount > 0) {
										ItemStack boughtProduct = new ItemStack(product.clone().getType(), 1, product.clone().getDurability());
										boughtProduct.setItemMeta(product.clone().getItemMeta());
										if (!shop.isInfinite()) inventory.addItem(boughtProduct);
										executor.getInventory().removeItem(boughtProduct);
										executor.updateInventory();
										leftAmount--;
									} else if (leftAmount == 0) {
										executor.sendMessage(Config.sell_success(product.getAmount(), ItemNames.lookup(product), shop.getSellPrice(), shop.getVendor().getName()));
										return;
									}
								}
							}
						}
					} else {
						executor.sendMessage(Config.chest_not_enough_inventory_space());
					}
				} else {
					executor.sendMessage(Config.error_occurred(r2.errorMessage));
				}
			} else {
				executor.sendMessage(Config.error_occurred(r.errorMessage));
			}				
		} else {
			executor.sendMessage(Config.vendor_not_enough_money());
		}
		
	}
	
}
