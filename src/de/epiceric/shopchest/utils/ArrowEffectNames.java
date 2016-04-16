package de.epiceric.shopchest.utils;

import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import com.google.common.collect.ImmutableMap;

public class ArrowEffectNames {

	private static final Map<String, String> effectMap = ImmutableMap.<String, String>builder()
			.put("FIRE_RESISTANCE", "Fire Resistance")
			.put("INSTANT_DAMAGE", "Instant Damage")
			.put("INSTANT_HEAL", "Instant Health")
			.put("INVISIBILITY", "Invisibility")
			.put("JUMP", "Jump Boost")
			.put("LUCK", "Luck")
			.put("NIGHT_VISION", "Night Vision")
			.put("POISION", "Poison")
			.put("REGEN", "Regeneration")
			.put("SLOWNESS", "Slowness")
			.put("SPEED", "Speed")
			.put("STRENGTH", "Strength")
			.put("WATER_BREATHING", "Water Breathing")
			.put("WEAKNESS", "Weakness")
			.build();
	
	
	public static String getTippedArrowName(ItemStack itemStack) {
		
		if (!(itemStack.getItemMeta() instanceof PotionMeta)){
			return null;
		}
		
		String name;
		
		PotionMeta meta = (PotionMeta) itemStack.getItemMeta();		
		
		name = effectMap.get(meta.getBasePotionData().getType().toString());
		
		if (meta.getBasePotionData().isUpgraded()){
			name += " II";
			switch (meta.getBasePotionData().getType()) {
			case JUMP: name += " (0:11)"; break;
			case SPEED: name += " (0:11)"; break;
			case POISON: name += " (0:02)"; break;
			case REGEN: name += " (0:02)"; break;
			case STRENGTH: name += " (0:11)"; break;
			default: break;
			}
		} else {
			switch (meta.getBasePotionData().getType()) {
			case FIRE_RESISTANCE: name += " (" + ((meta.getBasePotionData().isExtended()) ? "1:00" : "0:22") + ")"; break;
			case INVISIBILITY: name += " (" + ((meta.getBasePotionData().isExtended()) ? "1:00" : "0:22") + ")"; break;
			case JUMP: name += " (" + ((meta.getBasePotionData().isExtended()) ? "1:00" : "0:22") + ")"; break;
			case LUCK: name += " (0:37)"; break;
			case NIGHT_VISION: name += " (" + ((meta.getBasePotionData().isExtended()) ? "1:00" : "0:22") + ")"; break;
			case POISON: name += " (" + ((meta.getBasePotionData().isExtended()) ? "0:11" : "0:05") + ")"; break;
			case REGEN: name += " (" + ((meta.getBasePotionData().isExtended()) ? "0:11" : "0:05") + ")"; break;
			case SLOWNESS: name += " (" + ((meta.getBasePotionData().isExtended()) ? "0:30" : "0:11") + ")"; break;
			case SPEED: name += " (" + ((meta.getBasePotionData().isExtended()) ? "1:00" : "0:22") + ")"; break;
			case STRENGTH: name += " (" + ((meta.getBasePotionData().isExtended()) ? "1:00" : "0:22") + ")"; break;
			case WATER_BREATHING: name += " (" + ((meta.getBasePotionData().isExtended()) ? "1:00" : "0:22") + ")"; break;
			case WEAKNESS: name += " (" + ((meta.getBasePotionData().isExtended()) ? "0:30" : "0:11") + ")"; break;			
			default: break;
			}		
		}

		return name;
	}
	
}
