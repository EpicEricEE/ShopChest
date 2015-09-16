package de.epiceric.shopchest.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.epiceric.shopchest.ShopChest;

public class Config {

	private static ShopChest plugin = ShopChest.getInstance();
	
	public static Set<String> minimum_prices() {return (plugin.getConfig().getConfigurationSection("minimum-prices") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("minimum-prices").getKeys(true);}
	public static Set<String> shopLimits_group() {return (plugin.getConfig().getConfigurationSection("shop-limits.group") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("shop-limits.group").getKeys(true);}
	public static Set<String> shopLimits_player() {return (plugin.getConfig().getConfigurationSection("shop-limits.player") == null) ? new HashSet<String>() : plugin.getConfig().getConfigurationSection("shop-limits.player").getKeys(true);}
	
	public static List<String> blacklist() {return (plugin.getConfig().getStringList("blacklist") == null) ? new ArrayList<String>() : plugin.getConfig().getStringList("blacklist");};
	
 	public static boolean buy_greater_or_equal_sell() {return plugin.getConfig().getBoolean("buy-greater-or-equal-sell");}
	public static double maximal_distance() {return plugin.getConfig().getDouble("maximal-distance");}
	public static int default_limit() {return plugin.getConfig().getInt("shop-limits.default");}	
	
	public static String main_command_name() { return plugin.getConfig().getString("main-command-name");}
	public static String currency_symbol() { return plugin.getConfig().getString("currency-symbol").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String shop_created() { return plugin.getConfig().getString("messages.shop-created").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String chest_already_shop() { return plugin.getConfig().getString("messages.chest-already-shop").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String shop_removed() { return plugin.getConfig().getString("messages.shop-removed").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String chest_no_shop() { return plugin.getConfig().getString("messages.chest-no-shop").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String block_no_chest() { return plugin.getConfig().getString("messages.block-no-chest").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String not_enough_inventory_space() { return plugin.getConfig().getString("messages.not-enough-inventory-space").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String chest_not_enough_inventory_space() { return plugin.getConfig().getString("messages.chest-not-enough-inventory-space").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String not_enough_money() { return plugin.getConfig().getString("messages.not-enough-money").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String not_enough_items() { return plugin.getConfig().getString("messages.not-enough-items").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String vendor_not_enough_money() { return plugin.getConfig().getString("messages.vendor-not-enough-money").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String out_of_stock() { return plugin.getConfig().getString("messages.out-of-stock").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String amount_and_price_not_number() { return plugin.getConfig().getString("messages.amount-and-price-not-number").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String no_item_in_hand() { return plugin.getConfig().getString("messages.no-item-in-hand").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String click_chest_to_create() { return plugin.getConfig().getString("messages.click-chest-to-create-shop").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String click_chest_to_remove() { return plugin.getConfig().getString("messages.click-chest-to-remove-shop").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String click_chest_for_info() { return plugin.getConfig().getString("messages.click-chest-for-info").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String cmdDesc_create() { return plugin.getConfig().getString("messages.command-description.create").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String cmdDesc_remove() { return plugin.getConfig().getString("messages.command-description.remove").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String cmdDesc_info() { return plugin.getConfig().getString("messages.command-description.info").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String cmdDesc_reload() { return plugin.getConfig().getString("messages.command-description.reload").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String cmdDesc_update() { return plugin.getConfig().getString("messages.command-description.update").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String cmdDesc_limits() { return plugin.getConfig().getString("messages.command-description.limits").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String shopInfo_isInfinite() { return plugin.getConfig().getString("messages.shop-info.is-infinite").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");};
	public static String shopInfo_isNormal() { return plugin.getConfig().getString("messages.shop-info.is-not-infinite").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");};
	public static String noPermission_create() { return plugin.getConfig().getString("messages.no-permission.create").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String noPermission_createInfinite() { return plugin.getConfig().getString("messages.no-permission.create-infinite").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String noPermission_openOthers() { return plugin.getConfig().getString("messages.no-permission.open-others").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String noPermission_removeOthers() { return plugin.getConfig().getString("messages.no-permission.remove-others").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String noPermission_buy() { return plugin.getConfig().getString("messages.no-permission.buy").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String noPermission_sell() { return plugin.getConfig().getString("messages.no-permission.sell").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String noPermission_reload() { return plugin.getConfig().getString("messages.no-permission.reload").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String noPermission_update() { return plugin.getConfig().getString("messages.no-permission.update").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String noPermission_limits() { return plugin.getConfig().getString("messages.no-permission.limits").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String cannot_break_shop() { return plugin.getConfig().getString("messages.cannot-break-shop").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String cannot_sell_broken_item() { return plugin.getConfig().getString("messages.cannot-sell-broken-item").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String disabled() {return plugin.getConfig().getString("messages.shop-info.disabled").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
	public static String buy_and_sell_disabled() {return plugin.getConfig().getString("messages.buy-and-sell-disabled").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
 	public static String selling_disabled() {return plugin.getConfig().getString("messages.selling-disabled").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
 	public static String buying_disabled() {return plugin.getConfig().getString("messages.buying-disabled").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
 	public static String checking_update() {return plugin.getConfig().getString("messages.update.checking").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
 	public static String no_new_update() {return plugin.getConfig().getString("messages.update.no-update").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
 	public static String click_to_download() {return plugin.getConfig().getString("messages.update.click-to-download").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
 	public static String cannot_sell_item() {return plugin.getConfig().getString("messages.cannot-sell-item").replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");}
 	
 	public static String limit_reached(int limit) {
 		return plugin.getConfig().getString("messages.shop-limit-reached").replace(Regex.limit, String.valueOf(limit)).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
 	}

 	public static String reloaded_shops(int amount) {
 		return plugin.getConfig().getString("messages.reloaded-shops").replace(Regex.amount, String.valueOf(amount)).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
 	}

	public static String opened_shop(String vendor) {
		return plugin.getConfig().getString("messages.opened-shop").replace(Regex.vendor, vendor).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String buyPrice_too_low(double minPrice) {
		return plugin.getConfig().getString("messages.buy-price-too-low").replace(Regex.minPrice, String.valueOf(minPrice)).replace(Regex.currencySymbol, currency_symbol()).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String sellPrice_too_low(double minPrice) {
		return plugin.getConfig().getString("messages.sell-price-too-low").replace(Regex.minPrice, String.valueOf(minPrice)).replace(Regex.currencySymbol, currency_symbol()).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String update_available(String version) {
		return plugin.getConfig().getString("messages.update.update-available").replace(Regex.version, version);
	}
	
	public static String hologram_format(int amount, String itemName) {
		return plugin.getConfig().getString("messages.hologram.format").replace(Regex.amount, String.valueOf(amount)).replace(Regex.itemName, itemName).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String hologram_buy_sell(double buyPrice, double sellPrice) {
		return plugin.getConfig().getString("messages.hologram.buy-and-sell").replace(Regex.currencySymbol, currency_symbol()).replace(Regex.buyPrice, String.valueOf(buyPrice)).replace(Regex.sellPrice, String.valueOf(sellPrice)).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String hologram_buy(double buyPrice) {
		return plugin.getConfig().getString("messages.hologram.only-buy").replace(Regex.currencySymbol, currency_symbol()).replace(Regex.buyPrice, String.valueOf(buyPrice)).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String hologram_sell(double sellPrice) {
		return plugin.getConfig().getString("messages.hologram.only-sell").replace(Regex.currencySymbol, currency_symbol()).replace(Regex.sellPrice, String.valueOf(sellPrice)).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String error_occurred(String error) {
		return plugin.getConfig().getString("messages.error-occurred").replace(Regex.error, error).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String shopInfo_vendor(String vendor) {
		return plugin.getConfig().getString("messages.shop-info.vendor").replace(Regex.vendor, vendor).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String shopInfo_product(int amount, String itemName) {
		return plugin.getConfig().getString("messages.shop-info.product").replace(Regex.amount, String.valueOf(amount)).replace(Regex.itemName, itemName).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String shopInfo_enchantment(String enchantment) {
		return plugin.getConfig().getString("messages.shop-info.enchantments").replace(Regex.enchantment, enchantment).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String shopInfo_price(double buyPrice, double sellPrice) {
		if ((buyPrice <= 0) && (sellPrice > 0)) {
			return plugin.getConfig().getString("messages.shop-info.price").replace(Regex.currencySymbol, currency_symbol()).replace(Regex.buyPrice + currency_symbol(), disabled()).replace(Regex.sellPrice, String.valueOf(sellPrice)).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
		} else if ((sellPrice <= 0) && (buyPrice > 0)) {
			return plugin.getConfig().getString("messages.shop-info.price").replace(Regex.currencySymbol, currency_symbol()).replace(Regex.buyPrice, String.valueOf(buyPrice)).replace(Regex.sellPrice + currency_symbol(), disabled()).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
		} else if ((sellPrice > 0) && (buyPrice > 0)) {
			return plugin.getConfig().getString("messages.shop-info.price").replace(Regex.currencySymbol, currency_symbol()).replace(Regex.buyPrice, String.valueOf(buyPrice)).replace(Regex.sellPrice, String.valueOf(sellPrice)).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
		} else {
			return plugin.getConfig().getString("messages.shop-info.price").replace(Regex.currencySymbol, currency_symbol()).replace(Regex.buyPrice + currency_symbol(), disabled()).replace(Regex.sellPrice + currency_symbol(), disabled()).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
		}
	}
	
	public static String buy_success(int amount, String itemName, double buyPrice, String vendor) {
		return plugin.getConfig().getString("messages.buy-success").replace(Regex.currencySymbol, currency_symbol()).replace(Regex.amount, String.valueOf(amount)).replace(Regex.itemName, itemName).replace(Regex.buyPrice, String.valueOf(buyPrice)).replace(Regex.vendor, vendor).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String sell_success(int amount, String itemName, double sellPrice, String vendor) {
		return plugin.getConfig().getString("messages.sell-success").replace(Regex.currencySymbol, currency_symbol()).replace(Regex.amount, String.valueOf(amount)).replace(Regex.itemName, itemName).replace(Regex.sellPrice, String.valueOf(sellPrice)).replace(Regex.vendor, vendor).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String someone_bought(int amount, String itemName, double sellPrice, String player) {
		return plugin.getConfig().getString("messages.someone-bought").replace(Regex.currencySymbol, currency_symbol()).replace(Regex.amount, String.valueOf(amount)).replace(Regex.itemName, itemName).replace(Regex.sellPrice, String.valueOf(sellPrice)).replace(Regex.player, player).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String someone_sold(int amount, String itemName, double sellPrice, String player) {
		return plugin.getConfig().getString("messages.someone-sold").replace(Regex.currencySymbol, currency_symbol()).replace(Regex.amount, String.valueOf(amount)).replace(Regex.itemName, itemName).replace(Regex.sellPrice, String.valueOf(sellPrice)).replace(Regex.player, player).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String occupied_shop_slots(int limit, int amount) {
		return plugin.getConfig().getString("messages.occupied-shop-slots").replace(Regex.limit, String.valueOf(limit)).replace(Regex.amount, String.valueOf(amount)).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
	
	public static String shopInfo_stock(int amount) {
		return plugin.getConfig().getString("messages.shop-info.stock").replace(Regex.amount, String.valueOf(amount)).replaceAll("(&([a-f0-9k-or]))", "\u00A7$2");
	}
}
