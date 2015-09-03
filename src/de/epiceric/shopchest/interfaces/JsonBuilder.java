package de.epiceric.shopchest.interfaces;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public interface JsonBuilder {

	 public enum ClickAction {
         RUN_COMMAND, SUGGEST_COMMAND, OPEN_URL
     }
	 
     public enum HoverAction {
         SHOW_TEXT
     }
     
     public JsonBuilder parse(String text);
     public JsonBuilder withText(String text);
     public JsonBuilder withColor(ChatColor color);
     public JsonBuilder withColor(String color);
     public JsonBuilder withClickEvent(ClickAction action, String value);
     public JsonBuilder withHoverEvent(HoverAction action, String value);
     public String toString();
     public void sendJson(Player p);
	
}
