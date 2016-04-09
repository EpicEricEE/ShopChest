package de.epiceric.shopchest.interfaces.jsonbuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.interfaces.JsonBuilder;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;


public class JsonBuilder_1_8_R1 implements JsonBuilder {
     
        /* JsonBuilder by FisheyLP */
           
        private List<String> extras = new ArrayList<String>();
     
        
        public JsonBuilder_1_8_R1(String... text) {
            for(String extra : text)
                parse(extra);
        }
     
        @Override
        public JsonBuilder_1_8_R1 parse(String text) {
               String regex = "[&§]{1}([a-fA-Fl-oL-O0-9]){1}";
               text = text.replaceAll(regex, "§$1");
               if(!Pattern.compile(regex).matcher(text).find()) {
                  withText(text).withHoverEvent(HoverAction.SHOW_TEXT, Config.click_to_download()).withClickEvent(ClickAction.OPEN_URL, ShopChest.downloadLink); 
                  return this;
               }
               String[] words = text.split(regex);
     
               int index = words[0].length();
               for(String word : words) {
                   try {
                       if(index != words[0].length())
                   withText(word).withColor("§"+text.charAt(index - 1)).withHoverEvent(HoverAction.SHOW_TEXT, Config.click_to_download()).withClickEvent(ClickAction.OPEN_URL, ShopChest.downloadLink);
                   } catch(Exception e){}
                   index += word.length() + 2;
               }
               return this;
           }
        
        @Override
        public JsonBuilder_1_8_R1 withText(String text) {
            extras.add("{\"text\":\"" + text + "\"}");
            return this;
        }
     
        @Override
        public JsonBuilder_1_8_R1 withColor(ChatColor color) {
            String c = color.name().toLowerCase();
            addSegment(color.isColor() ? "\"color\":\"" + c + "\"" : "\"" + c + "\"" + ":true");
            return this;
        }
     
        @Override
        public JsonBuilder_1_8_R1 withColor(String color) {
            while(color.length() != 1) color = color.substring(1).trim();
            withColor(ChatColor.getByChar(color));
            return this;
        }
     
        @Override
        public JsonBuilder_1_8_R1 withClickEvent(ClickAction action, String value) {
            addSegment("\"clickEvent\":{\"action\":\"" + action.toString().toLowerCase()
                    + "\",\"value\":\"" + value + "\"}");
            return this;
        }
     
        @Override
        public JsonBuilder_1_8_R1 withHoverEvent(HoverAction action, String value) {
            addSegment("\"hoverEvent\":{\"action\":\"" + action.toString().toLowerCase()
                    + "\",\"value\":\"" + value + "\"}");
            return this;
        }
     
        private void addSegment(String segment) {
            String lastText = extras.get(extras.size() - 1);
            lastText = lastText.substring(0, lastText.length() - 1)
                    + ","+segment+"}";
            extras.remove(extras.size() - 1);
            extras.add(lastText);
        }
     
        @Override
        public String toString() {
            if(extras.size() <= 1) return extras.size() == 0 ? "{\"text\":\"\"}" : extras.get(0);
            String text = extras.get(0).substring(0, extras.get(0).length() - 1) + ",\"extra\":[";
            extras.remove(0);;
            for (String extra : extras)
                text = text + extra + ",";
            text = text.substring(0, text.length() - 1) + "]}";
            return text;
        }
     
        @Override
        public void sendJson(Player p) {    	
        		((CraftPlayer) p).getHandle().playerConnection.sendPacket(
            			new PacketPlayOutChat(ChatSerializer.a(toString())));
        	

        }
    }