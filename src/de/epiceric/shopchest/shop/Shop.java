package de.epiceric.shopchest.shop;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Item;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.interfaces.Hologram;
import de.epiceric.shopchest.interfaces.Utils;
import de.epiceric.shopchest.interfaces.hologram.Hologram_R1;
import de.epiceric.shopchest.interfaces.hologram.Hologram_R2;
import de.epiceric.shopchest.interfaces.hologram.Hologram_R3;
import de.epiceric.shopchest.utils.ItemNames;

public class Shop {

	private ShopChest plugin;
	private OfflinePlayer vendor;
	private ItemStack product;
	private Location location;
	private Hologram hologram;
	private Item item;
	private double buyPrice;
	private double sellPrice;
	private boolean infinite;
	
	public Shop(ShopChest plugin, OfflinePlayer vendor, ItemStack product, Location location, double buyPrice, double sellPrice, boolean infinite) {
		this.plugin = plugin;
		this.vendor = vendor;
		this.product = product;
		this.location = location;
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
		this.infinite = infinite;				
	}
	
	public void createItem() {
		
		Item item;
		Location itemLocation;
		ItemStack itemStack;
		ItemMeta itemMeta = product.getItemMeta().clone();
		itemMeta.setDisplayName(UUID.randomUUID().toString());
		
		ArrayList<String> lore = new ArrayList<>();
		lore.add("Shop Item");
		itemMeta.setLore(lore);
		
		itemLocation = new Location(location.getWorld(), hologram.getLocation().getX(), location.getY() + 1, hologram.getLocation().getZ());
		itemStack = new ItemStack(product.getType(), 1, product.getDurability());
		itemStack.setItemMeta(itemMeta);		
		
		item = location.getWorld().dropItem(itemLocation, itemStack);
		item.getItemStack().getItemMeta().setDisplayName(UUID.randomUUID().toString());
		item.setVelocity(new Vector(0, 0, 0));
		item.setMetadata("shopItem", new FixedMetadataValue(plugin, true));
		
		this.item = item;
	}
	
	public void createHologram() {
				
		boolean doubleChest;
				
		Chest[] chests = new Chest[2];
		
		Block b = location.getBlock();
		
		if (b.getType().equals(Material.CHEST) || b.getType().equals(Material.TRAPPED_CHEST)) {
			
			Chest c = (Chest) b.getState();
			InventoryHolder ih = c.getInventory().getHolder();
			
			if (ih instanceof DoubleChest) {				
				DoubleChest dc = (DoubleChest) ih;
				
				Chest r = (Chest) dc.getRightSide();
				Chest l = (Chest) dc.getLeftSide();
				
				chests[0] = r;
				chests[1] = l;
				
				doubleChest = true;
				
			} else {
				doubleChest = false;
				chests[0] = c;
			}
			
		} else {
			try {
				throw new Exception("No Chest found at specified Location: " + b.getX() + "; " + b.getY() + "; " + b.getZ());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		
		Location holoLocation;
		String[] holoText = new String[2];
		
		if (doubleChest) {
			
			Chest r = chests[0];
			Chest l = chests[1];
			
			if (b.getLocation().equals(r.getLocation())) {
				
				if (r.getX() != l.getX()) holoLocation = new Location(b.getWorld(), b.getX(), b.getY() - 0.6, b.getZ() + 0.5);
				else if (r.getZ() != l.getZ()) holoLocation = new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 0.6, b.getZ());
				else holoLocation = new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 0.6, b.getZ() + 0.5);
				
			} else {
				
				if (r.getX() != l.getX()) holoLocation = new Location(b.getWorld(), b.getX() + 1, b.getY() - 0.6, b.getZ() + 0.5);
				else if (r.getZ() != l.getZ()) holoLocation = new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 0.6, b.getZ() + 1);
				else holoLocation = new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 0.6, b.getZ() + 0.5);
				
			}
			
		} else holoLocation = new Location(b.getWorld(), b.getX() + 0.5, b.getY() - 0.6, b.getZ() + 0.5);
		
		
		holoText[0] = String.valueOf(product.getAmount()) + " x " + ItemNames.lookup(product);
		
		if ((buyPrice <= 0) && (sellPrice > 0)) holoText[1] = Config.hologram_sell(sellPrice);
		else if ((buyPrice > 0) && (sellPrice <= 0)) holoText[1] = Config.hologram_buy(buyPrice);
		else if ((buyPrice > 0) && (sellPrice > 0)) holoText[1] = Config.hologram_buy_sell(buyPrice, sellPrice);
		else holoText[1] = Config.hologram_buy_sell(buyPrice, sellPrice);
		
		switch (Utils.getVersion(plugin.getServer())) {
			case "v1_8_R1": hologram = new Hologram_R1(holoText, holoLocation); break;
			case "v1_8_R2": hologram = new Hologram_R2(holoText, holoLocation); break;
			case "v1_8_R3": hologram = new Hologram_R3(holoText, holoLocation); break;
			default: return;
		}
							
	}
	
	public OfflinePlayer getVendor() {
		return vendor;
	}
	
	public ItemStack getProduct() {
		return product;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public double getBuyPrice() {
		return buyPrice;
	}
	
	public double getSellPrice() {
		return sellPrice;
	}
	
	public boolean isInfinite() {
		return infinite;
	}
	
	public Hologram getHologram() {
		return hologram;
	}
	
	public Item getItem() {
		return item;
	}
	
}
