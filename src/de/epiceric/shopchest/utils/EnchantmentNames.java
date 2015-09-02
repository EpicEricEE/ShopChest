package de.epiceric.shopchest.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;

import com.google.common.collect.ImmutableMap;

public class EnchantmentNames {

	private static final Map<String, String> enchMap = ImmutableMap.<String, String>builder()
			.put("PROTECTION_ENVIRONMENTAL", "Protection")
			.put("PROTECTION_FIRE", "Fire Protection")
			.put("PROTECTION_FALL", "Feather Falling")
			.put("PROTECTION_EXPLOSIONS", "Blast Protection")
			.put("OXYGEN", "Respiration")
			.put("WATER_WORKER", "Aqua Affinity")
			.put("THORNS", "Thorns")
			.put("DEPTH_STRIDER", "Depth Strider")
			.put("DAMAGE_ALL", "Sharpness")
			.put("DAMAGE_UNDEAD", "smite")
			.put("DAMAGE_ARTHROPODS", "Bane of Arthropods")
			.put("KNOCKBACK", "Knockback")
			.put("FIRE_ASPECT", "Fire Aspect")
			.put("LOOT_BONUS_MOBS", "Looting")
			.put("DIG_SPEED", "Efficiency")
			.put("SILK_TOUCH", "Silk Touch")
			.put("DURABILITY", "Unbreaking")
			.put("LOOT_BONUS_BLOCKS", "Fortune")
			.put("ARROW_DAMAGE", "Power")
			.put("ARROW_KNOCKBACK", "Punch")
			.put("ARROW_FIRE", "Flame")
			.put("ARROW_INFINITE", "Infinity")
			.put("LUCK", "Luck of the Sea")
			.put("LURE", "Lure")
			.build();
	
	public static String lookup(Enchantment enchantment, int level) {
    	String key = enchantment.getName();
    	String name = enchMap.get(key);
    	
    	String levelString = getRomanNumber(level);
    	
    	return name + " " + levelString;
    }
	
	public static String getRomanNumber(int Int) {
		
	    LinkedHashMap<String, Integer> roman_numerals = new LinkedHashMap<String, Integer>();
	    roman_numerals.put("M", 1000);
	    roman_numerals.put("CM", 900);
	    roman_numerals.put("D", 500);
	    roman_numerals.put("CD", 400);
	    roman_numerals.put("C", 100);
	    roman_numerals.put("XC", 90);
	    roman_numerals.put("L", 50);
	    roman_numerals.put("XL", 40);
	    roman_numerals.put("X", 10);
	    roman_numerals.put("IX", 9);
	    roman_numerals.put("V", 5);
	    roman_numerals.put("IV", 4);
	    roman_numerals.put("I", 1);
	    String res = "";
	    for(Map.Entry<String, Integer> entry : roman_numerals.entrySet()){
	      int matches = Int/entry.getValue();
	      res += repeat(entry.getKey(), matches);
	      Int = Int % entry.getValue();
	    }
	    return res;
	}
	
	public static String repeat(String s, int n) {
	    if(s == null) {
	        return null;
	    }
	    final StringBuilder sb = new StringBuilder();
	    for(int i = 0; i < n; i++) {
	        sb.append(s);
	    }
	    return sb.toString();
	  }
	
}
