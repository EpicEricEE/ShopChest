package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JsonBuilder {

    private List<String> extras = new ArrayList<>();
    private ShopChest plugin;

    private Class<?> iChatBaseComponentClass = Utils.getNMSClass("IChatBaseComponent");
    private Class<?> packetPlayOutChatClass = Utils.getNMSClass("PacketPlayOutChat");
    private Class<?> chatSerializerClass;

    public JsonBuilder(ShopChest plugin, String text, String hoverText, String downloadLink) {
        this.plugin = plugin;

        if (Utils.getServerVersion().equals("v1_8_R1")) {
            chatSerializerClass = Utils.getNMSClass("ChatSerializer");
        } else {
            chatSerializerClass = Utils.getNMSClass("IChatBaseComponent$ChatSerializer");
        }

        Class[] requiredClasses = new Class[] {
          iChatBaseComponentClass, packetPlayOutChatClass, chatSerializerClass
        };

        for (Class c : requiredClasses) {
            if (c == null) {
                plugin.debug("Failed to instantiate JsonBuilder: Could not find all required classes");
                return;
            }
        }

        parse(text, hoverText, downloadLink);
    }

    private JsonBuilder parse(String text, String hoverText, String downloadLink) {
        String regex = "[&§]{1}([a-fA-Fl-oL-O0-9]){1}";
        text = text.replaceAll(regex, "§$1");
        if (!Pattern.compile(regex).matcher(text).find()) {
            withText(text).withHoverEvent(hoverText).withClickEvent(downloadLink);
            return this;
        }
        String[] words = text.split(regex);

        int index = words[0].length();
        for (String word : words) {
            try {
                if (index != words[0].length())
                    withText(word).withColor("§" + text.charAt(index - 1)).withHoverEvent(hoverText).withClickEvent(downloadLink);
            } catch (Exception e) {}
            index += word.length() + 2;
        }
        return this;
    }

    private JsonBuilder withText(String text) {
        extras.add("{\"text\":\"" + text + "\"}");
        return this;
    }

    private JsonBuilder withColor(ChatColor color) {
        String c = color.name().toLowerCase();
        addSegment(color.isColor() ? "\"color\":\"" + c + "\"" : "\"" + c + "\"" + ":true");
        return this;
    }

    private JsonBuilder withColor(String color) {
        while (color.length() != 1) color = color.substring(1).trim();
        withColor(ChatColor.getByChar(color));
        return this;
    }

    private JsonBuilder withClickEvent(String value) {
        addSegment("\"clickEvent\":{\"action\":\"open_url"
                + "\",\"value\":\"" + value + "\"}");
        return this;
    }

    private JsonBuilder withHoverEvent(String value) {
        addSegment("\"hoverEvent\":{\"action\":\"show_text"
                + "\",\"value\":\"" + value + "\"}");
        return this;
    }

    private void addSegment(String segment) {
        String lastText = extras.get(extras.size() - 1);
        lastText = lastText.substring(0, lastText.length() - 1)
                + "," + segment + "}";
        extras.remove(extras.size() - 1);
        extras.add(lastText);
    }

    public String toString() {
        if (extras.size() <= 1) return extras.size() == 0 ? "{\"text\":\"\"}" : extras.get(0);
        String text = extras.get(0).substring(0, extras.get(0).length() - 1) + ",\"extra\":[";
        extras.remove(0);
        for (String extra : extras)
            text = text + extra + ",";
        text = text.substring(0, text.length() - 1) + "]}";
        return text;
    }

    public void sendJson(Player p) {
        try {
            Object iChatBaseComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, toString());
            Object packetPlayOutChat = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass).newInstance(iChatBaseComponent);

            Utils.sendPacket(plugin, packetPlayOutChat, p);
        } catch (InstantiationException | InvocationTargetException |
                IllegalAccessException | NoSuchMethodException e) {
            plugin.getLogger().severe("Failed to send JSON with reflection");
            plugin.debug("Failed to send JSON with reflection");
            plugin.debug(e);
        }
    }

}
