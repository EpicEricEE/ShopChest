package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonBuilder {

    public static class Part {
        private String value;

        public Part() {
            this("");
        }

        public Part(Object value) {
            if (value instanceof CharSequence) {
                this.value = "\"" + value + "\"";
            } else {
                this.value = String.valueOf(value);
            }
        }

        @Override
        public String toString() {
            return value;
        }

        public PartArray toArray() {
            return new PartArray(this);
        }

        public PartMap toMap() {
            PartMap map = new PartMap();
            map.setValue("text", new Part());
            map.setValue("extra", toArray());
            return map;
        }
    }

    public static class PartMap extends Part {
        private Map<String, Part> values = new HashMap<>();

        public PartMap() {
        }

        public PartMap(Map<String, Part> values) {
            this.values.putAll(values);
        }

        public void setValue(String key, Part value) {
            values.put(key, value);
        }

        public void removeValue(String key) {
            values.remove(key);
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(",", "{", "}");
            values.forEach((key, value) -> joiner.add("\"" + key + "\":" + value.toString()));
            return joiner.toString();
        }

        @Override
        public PartMap toMap() {
            return this;
        }
    }

    public static class PartArray extends Part {
        private List<Part> parts = new ArrayList<>();

        public PartArray(Part... parts) {
            this.parts.addAll(Arrays.asList(parts));
        }

        public void addPart(Part part) {
            parts.add(part);
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            parts.forEach(part -> joiner.add(part.toString()));
            return joiner.toString();
        }

        @Override
        public PartArray toArray() {
            return this;
        }
    }
    
    private static final Pattern PART_PATTERN = Pattern.compile("(([§][a-fA-Fl-oL-OkK0-9])+)([^§]*)");

    private Part rootPart;
    private ShopChest plugin;

    private Class<?> iChatBaseComponentClass = Utils.getNMSClass("IChatBaseComponent");
    private Class<?> packetPlayOutChatClass = Utils.getNMSClass("PacketPlayOutChat");
    private Class<?> chatSerializerClass;

    public JsonBuilder(ShopChest plugin) {
        this.plugin = plugin;

        if (Utils.getServerVersion().equals("v1_8_R1")) {
            chatSerializerClass = Utils.getNMSClass("ChatSerializer");
        } else {
            chatSerializerClass = Utils.getNMSClass("IChatBaseComponent$ChatSerializer");
        }

        Class<?>[] requiredClasses = new Class<?>[] {
          iChatBaseComponentClass, packetPlayOutChatClass, chatSerializerClass
        };

        for (Class<?> c : requiredClasses) {
            if (c == null) {
                plugin.debug("Failed to instantiate JsonBuilder: Could not find all required classes");
                return;
            }
        }
    }

    public static Part parse(String text) {
        Matcher matcher = PART_PATTERN.matcher(text);
        
        if (!matcher.find()) {
            return new Part(text);
        }

        matcher.reset();

        PartArray array = new PartArray();
        int lastEndIndex = 0;

        while (matcher.find()) {
            int startIndex = matcher.start();
            int endIndex = matcher.end();

            if (lastEndIndex != startIndex) {
                String betweenMatches = text.substring(lastEndIndex, startIndex);
                array.addPart(new Part(betweenMatches));
            }

            String format = matcher.group(1);
            String value = matcher.group(3);

            PartMap part = new PartMap();
            part.setValue("text", new Part(value));

            String[] formats = format.split("§");
            for (String f : formats) {
                switch (f.toLowerCase()) {
                    case "":
                        break;
                    case "k":
                        part.setValue("obuscated", new Part(true));
                        break;
                    case "l":
                        part.setValue("bold", new Part(true));
                        break;
                    case "m":
                        part.setValue("strikethrough", new Part(true));
                        break;
                    case "n":
                        part.setValue("underlined", new Part(true));
                        break;
                    case "o":
                        part.setValue("italic", new Part(true));
                        break;
                    case "r":
                        part.removeValue("obfuscated");
                        part.removeValue("bold");
                        part.removeValue("strikethrough");
                        part.removeValue("underlined");
                        part.removeValue("italic");
                        part.removeValue("color");
                        break;
                    default:
                        part.setValue("color", new Part(ChatColor.getByChar(f).name().toLowerCase()));
                }
            }

            array.addPart(part);
            lastEndIndex = endIndex;
        }

        return array;
    }

    @Override
    public String toString() {
        return rootPart.toString();
    }

    public Part getRootPart() {
        return rootPart;
    }
    
    public void setRootPart(Part rootPart) {
        this.rootPart = rootPart;
    }

    public void sendJson(Player p) {        
        try {
            Object iChatBaseComponent = chatSerializerClass.getMethod("a", String.class).invoke(null, toString());
            Object packetPlayOutChat = packetPlayOutChatClass.getConstructor(iChatBaseComponentClass).newInstance(iChatBaseComponent);
            
            Utils.sendPacket(plugin, packetPlayOutChat, p);
        } catch (InstantiationException | InvocationTargetException |
                IllegalAccessException | NoSuchMethodException e) {
            plugin.getLogger().severe("Failed to send JSON with reflection");
            plugin.debug("Failed to send JSON with reflection: " + toString());
            plugin.debug(e);
        }
    }

}
