package de.epiceric.shopchest.nms.reflection;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonBuilder {

    public static class Part {
        private String value;

        public Part() {
            this("", true);
        }

        public Part(Object value) {
            this(value, value instanceof CharSequence);
        }

        public Part(Object value, boolean appendQuotes) {
            if (appendQuotes) {
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
    
    private static final Pattern PART_PATTERN = Pattern.compile("((§(?:[a-fA-Fk-oK-OrR0-9]|#[a-fA-F0-9]{6}))+)([^§]*)");
    private static final Pattern HEX_PATTERN = Pattern.compile("(§[a-fA-F0-9]){6}");

    private Part rootPart;
    private ShopChestDebug debug;

    private final NMSClassResolver nmsClassResolver = new NMSClassResolver();
    private Class<?> iChatBaseComponentClass = nmsClassResolver.resolveSilent("network.chat.IChatBaseComponent");
    private Class<?> packetPlayOutChatClass = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayOutChat");
    private Class<?> chatSerializerClass = nmsClassResolver.resolveSilent("ChatSerializer", "network.chat.IChatBaseComponent$ChatSerializer");
    private Class<?> chatMessageTypeClass;

    public JsonBuilder(ShopChestDebug debug) {
        this.debug = debug;

        if (ReflectionUtils.getMajorVersion() >= 16) {
            chatMessageTypeClass = nmsClassResolver.resolveSilent("network.chat.ChatMessageType");
        }

        Class<?>[] requiredClasses = new Class<?>[] {
          iChatBaseComponentClass, packetPlayOutChatClass, chatSerializerClass
        };

        for (Class<?> c : requiredClasses) {
            if (c == null) {
                debug.debug("Failed to instantiate JsonBuilder: Could not find all required classes");
                return;
            }
        }
    }

    public static Part parse(String text) {
        Matcher hexMatcher = HEX_PATTERN.matcher(text);
        while (hexMatcher.find()) {
            String hexCode = hexMatcher.group(0).replace("§", "");
            text = text.replace(hexMatcher.group(0), "§#" + hexCode);
        }

        Matcher matcher = PART_PATTERN.matcher(text);
        
        if (!matcher.find()) {
            return new Part(text);
        }

        matcher.reset();

        PartArray array = new PartArray(new Part());
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
                        if (f.startsWith("#")) {
                            part.setValue("color", new Part(f));
                        } else {
                            part.setValue("color", new Part(ChatColor.getByChar(f).name().toLowerCase()));
                        }
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
            Object packetPlayOutChat = ReflectionUtils.getMajorVersion() < 16
                ? packetPlayOutChatClass.getConstructor(iChatBaseComponentClass).newInstance(iChatBaseComponent)
                : packetPlayOutChatClass.getConstructor(iChatBaseComponentClass, chatMessageTypeClass, UUID.class)
                        .newInstance(iChatBaseComponent, (new FieldResolver(chatMessageTypeClass)).resolve("CHAT", "a").get(null), UUID.randomUUID());
            
            ReflectionUtils.sendPacket(debug, packetPlayOutChat, p);
            debug.debug("Sent JSON: " + toString());
        } catch (ReflectiveOperationException e) {
            debug.getLogger().severe("Failed to send JSON with reflection");
            debug.debug("Failed to send JSON with reflection: " + toString());
            debug.debug(e);
        }
    }

}
