package de.epiceric.shopchest;

import java.lang.reflect.Method;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.utils.ClickType;
import de.epiceric.shopchest.interfaces.JsonBuilder;
import de.epiceric.shopchest.utils.ClickType.EnumClickType;
import de.epiceric.shopchest.interfaces.JsonBuilder.ClickAction;
import de.epiceric.shopchest.interfaces.JsonBuilder.HoverAction;
import de.epiceric.shopchest.interfaces.Utils;
import de.epiceric.shopchest.interfaces.jsonbuilder.*;
import de.epiceric.shopchest.utils.UpdateChecker;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.permission.Permission;

public class Commands extends BukkitCommand {

	private ShopChest plugin;

	private Permission perm = ShopChest.perm;
	
	public Commands(ShopChest plugin, String name, String description, String usageMessage, List<String> aliases) {
		super(name, description, usageMessage, aliases);
		this.plugin = plugin;
	}
	
	public static void registerCommand(Command command, ShopChest plugin)
            throws ReflectiveOperationException {
            Method commandMap = plugin.getServer().getClass().getMethod("getCommandMap");
            Object cmdmap = commandMap.invoke(plugin.getServer());
            Method register = cmdmap.getClass().getMethod("register", String.class,Command.class);
            register.invoke(cmdmap, command.getName(),command);
    }


	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {

		if (sender instanceof Player) {
			
			Player p = (Player) sender;
			
			if (args.length == 0) {
				
				sendBasicHelpMessage(p);
				return true;
				
			} else {
				
				if (args[0].equalsIgnoreCase("create")) {
					
					if (perm.has(p, "shopchest.create")) {
						
						if (args.length == 4) {
							
							create(args, false, p);
							return true;
							
							
						} else if (args.length == 5) {
							
							if (args[4].equalsIgnoreCase("infinite")) {
								
								if (perm.has(p, "shopchest.create.infinite")) {
									
									create(args, true, p);
									return true;
									
								} else {
									
									p.sendMessage(Config.noPermission_createInfinite());
									return true;
									
								}
								
							} else if (args[4].equalsIgnoreCase("normal")){
								
								create(args, false, p);
								return true;
								
							} else {
								
								sendBasicHelpMessage(p);
								return true;
								
							}
							
						} else {
							
							sendBasicHelpMessage(p);
							return true;
							
						}
						
					} else {
						
						p.sendMessage(Config.noPermission_create());
						return true;
						
					}
					
				} else if (args[0].equalsIgnoreCase("remove")) {
					
					remove(p);
					return true;
					
				} else if (args[0].equalsIgnoreCase("info")) {
					
					info(p);
					return true;
					
				} else if (args[0].equalsIgnoreCase("reload")) {
					
					if (perm.has(p, "shopchest.reload")) {
						reload(p);
						return true;
					} else {
						p.sendMessage(Config.noPermission_reload());
						return true;
					}
					
				} else if (args[0].equalsIgnoreCase("update")) {
					
					if (perm.has(p, "shopchest.update")) {
						checkUpdates(p);
						return true;
					} else {
						p.sendMessage(Config.noPermission_update());
						return true;
					}
					
				}
					
				return true;
				
			}
			
			
		} else {
			
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Only players can execute this command.");
			return true;
			
		}
		
	}
	
	private void checkUpdates(Player player) {
		
		player.sendMessage(Config.checking_update());
		
		UpdateChecker uc = new UpdateChecker(ShopChest.getInstance(), ShopChest.getInstance().getDescription().getWebsite());
		if (uc.updateNeeded()) {
			ShopChest.latestVersion = uc.getVersion();
			ShopChest.downloadLink = uc.getLink();
			ShopChest.isUpdateNeeded = true;
			
			JsonBuilder jb;
			switch (Utils.getVersion(plugin.getServer())) {
				case "v1_8_R1": jb = new JsonBuilder_R1(Config.update_available(ShopChest.latestVersion)).withHoverEvent(HoverAction.SHOW_TEXT, Config.click_to_download()).withClickEvent(ClickAction.OPEN_URL, ShopChest.downloadLink); break;
				case "v1_8_R2": jb = new JsonBuilder_R2(Config.update_available(ShopChest.latestVersion)).withHoverEvent(HoverAction.SHOW_TEXT, Config.click_to_download()).withClickEvent(ClickAction.OPEN_URL, ShopChest.downloadLink); break;
				case "v1_8_R3": jb = new JsonBuilder_R3(Config.update_available(ShopChest.latestVersion)).withHoverEvent(HoverAction.SHOW_TEXT, Config.click_to_download()).withClickEvent(ClickAction.OPEN_URL, ShopChest.downloadLink); break;
				default: return;
			}		
			jb.sendJson(player);
			
		} else {
			ShopChest.latestVersion = "";
			ShopChest.downloadLink = "";
			ShopChest.isUpdateNeeded = false;
			player.sendMessage(Config.no_new_update());
		}
		
	}
	
	private void reload(Player player) {
		
		ShopChest.utils.reload();		
		player.sendMessage(Config.reloaded_shops(ShopChest.getInstance().shopChests.getKeys(false).size()));
		
	}
	
	private void create(String[] args, boolean infinite, Player p) {
		int amount;
		double buyPrice, sellPrice;
		
		
		try {
			amount = Integer.parseInt(args[1]);
			buyPrice = Double.parseDouble(args[2]);
			sellPrice = Double.parseDouble(args[3]);
		} catch (NumberFormatException e) {
			p.sendMessage(Config.amount_and_price_not_number());
			return;
		}
		
		boolean buyEnabled = !(buyPrice <= 0), sellEnabled = !(sellPrice <= 0);
		
		if (!buyEnabled && !sellEnabled) {
			p.sendMessage(Config.buy_and_sell_disabled());
			return;
		}
		
		if (p.getItemInHand().getType().equals(Material.AIR)) {
			p.sendMessage(Config.no_item_in_hand());
			return;
		}
		
		for (String key : Config.minimum_prices()) {
			
			ItemStack itemStack;
			double price = plugin.getConfig().getDouble("minimum-prices." + key);
			
			if (key.contains(":")) {						
				itemStack = new ItemStack(Material.getMaterial(key.split(":")[0]), 1, Short.parseShort(key.split(":")[1]));								
			} else {					
				itemStack = new ItemStack(Material.getMaterial(key), 1);								
			}
			
			if (itemStack.getType().equals(p.getItemInHand().getType())) {						
				if (buyEnabled) {					
					if ((buyPrice <= amount * price) && (buyPrice > 0)) {						
						p.sendMessage(Config.buyPrice_too_low(amount * price));
						return;						
					}					
				}
				
				if (sellEnabled) {					
					if ((sellPrice <= amount * price) && (sellPrice > 0)) {						
						p.sendMessage(Config.sellPrice_too_low(amount * price));
						return;						
					}					
				}													
			}		
		}
		
		if (sellEnabled && buyEnabled) {
			if (Config.buy_greater_or_equal_sell()) {
				if (buyPrice < sellPrice) {
					p.sendMessage(Config.buyPrice_too_low(sellPrice));
					return;
				}
			}
		}

		
		ItemStack itemStack = new ItemStack(p.getItemInHand().getType(), amount, p.getItemInHand().getDurability());
		itemStack.setItemMeta(p.getItemInHand().getItemMeta());
		
		if (Enchantment.DURABILITY.canEnchantItem(itemStack)) {		
			if (itemStack.getDurability() > 0) {			
				p.sendMessage(Config.cannot_sell_broken_item());
				return;			
			}			
		}
		
		ClickType.addPlayerClickType(p, new ClickType(EnumClickType.CREATE, itemStack, buyPrice, sellPrice, infinite));	
		p.sendMessage(Config.click_chest_to_create());
		
	}

	private void remove(Player p) {
		p.sendMessage(Config.click_chest_to_remove());
		ClickType.addPlayerClickType(p, new ClickType(EnumClickType.REMOVE));
	}
	
	private void info(Player p) {
		p.sendMessage(Config.click_chest_for_info());
		ClickType.addPlayerClickType(p, new ClickType(EnumClickType.INFO));
	}
	
	private void sendBasicHelpMessage(Player player) {
		
		player.sendMessage(ChatColor.GREEN + "/" + Config.main_command_name() + " create <amount> <buy-price> <sell-price> [infinite|normal]- " + Config.cmdDesc_create());
		player.sendMessage(ChatColor.GREEN + "/" + Config.main_command_name() + " remove - " + Config.cmdDesc_remove());
		player.sendMessage(ChatColor.GREEN + "/" + Config.main_command_name() + " info - " + Config.cmdDesc_info());		
		player.sendMessage(ChatColor.GREEN + "/" + Config.main_command_name() + " reload - " + Config.cmdDesc_reload());	
		player.sendMessage(ChatColor.GREEN + "/" + Config.main_command_name() + " update - " + Config.cmdDesc_update());		
		
	}
	
}
