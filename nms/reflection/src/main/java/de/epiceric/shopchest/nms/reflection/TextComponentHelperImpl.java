package de.epiceric.shopchest.nms.reflection;

import com.google.gson.JsonPrimitive;
import de.epiceric.shopchest.nms.TextComponentHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextComponentHelperImpl implements TextComponentHelper {

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile(".*([§]([a-fA-F0-9]))");
    private static final Pattern FORMAT_CODE_PATTERN = Pattern.compile(".*([§]([l-oL-OkK]))");

    private final ShopChestDebug debug;

    public TextComponentHelperImpl(ShopChestDebug debug) {
        this.debug = debug;
    }

    @Override
    public void sendUpdateMessage(Player player, String updateMessage, String hoverMessage, String downloadUrl) {
        JsonBuilder jb = new JsonBuilder(debug);
        Map<String, JsonBuilder.Part> hoverEvent = new HashMap<>();
        hoverEvent.put("action", new JsonBuilder.Part("show_text"));
        hoverEvent.put("value", new JsonBuilder.Part(hoverMessage));

        Map<String, JsonBuilder.Part> clickEvent = new HashMap<>();
        clickEvent.put("action", new JsonBuilder.Part("open_url"));
        clickEvent.put("value", new JsonBuilder.Part(downloadUrl));

        JsonBuilder.PartMap rootPart = JsonBuilder.parse(updateMessage).toMap();

        rootPart.setValue("hoverEvent", new JsonBuilder.PartMap(hoverEvent));
        rootPart.setValue("clickEvent", new JsonBuilder.PartMap(clickEvent));

        jb.setRootPart(rootPart);
        jb.sendJson(player);
    }

    @Override
    public String getNbt(ItemStack itemStack) {
        try {
            OBCClassResolver obcClassResolver = new OBCClassResolver();
            NMSClassResolver nmsClassResolver = new NMSClassResolver();

            Class<?> craftItemStackClass = obcClassResolver.resolveSilent("inventory.CraftItemStack");
            Object nmsStack = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
            Class<?> nbtTagCompoundClass = nmsClassResolver.resolveSilent("nbt.NBTTagCompound");
            Object nbtTagCompound = nbtTagCompoundClass.getConstructor().newInstance();
            nmsStack.getClass().getMethod("save", nbtTagCompoundClass).invoke(nmsStack, nbtTagCompound);
            return nbtTagCompound.toString();
        }catch (ReflectiveOperationException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Consumer<Player> getSendableItemInfo(String message, String itemPlaceHolder, ItemStack itemStack, String productName) {
        // Add spaces at start and end, so there will always be a part before and after
        // the item name after splitting at Placeholder.ITEM_NAME
        String productString = " " + message + " ";

        String[] parts = productString.split(itemPlaceHolder);
        String jsonItem = "";
        JsonBuilder jb = new JsonBuilder(debug);
        JsonBuilder.PartArray rootArray = new JsonBuilder.PartArray();

        try {
            jsonItem = new JsonPrimitive(getNbt(itemStack)).toString();
        } catch (RuntimeException e) {
            debug.getLogger().severe("Failed to create JSON from item. Product preview will not be available.");
            debug.debug("Failed to create JSON from item:");
            debug.debug(e.getCause());
            jb.setRootPart(new JsonBuilder.Part(productString.replace(itemPlaceHolder, productName)));
            return jb::sendJson;
        }

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            // Remove spaces at start and end that were added before
            if (i == 0 && part.startsWith(" ")) {
                part = part.substring(1);
            } else if (i == parts.length - 1 && part.endsWith(" ")) {
                part = part.substring(0, part.length() - 1);
            }

            String formatPrefix = "";

            // A color code resets all format codes, so only format codes
            // after the last color code have to be found.
            int lastColorGroupEndIndex = 0;

            Matcher colorMatcher = COLOR_CODE_PATTERN.matcher(part);
            if (colorMatcher.find()) {
                formatPrefix = colorMatcher.group(1);
                lastColorGroupEndIndex = colorMatcher.end();
            }

            Matcher formatMatcher = FORMAT_CODE_PATTERN.matcher(part);
            while (formatMatcher.find(lastColorGroupEndIndex)) {
                formatPrefix += formatMatcher.group(1);
            }

            rootArray.addPart(new JsonBuilder.Part(part));

            if (i < parts.length - 1) {
                JsonBuilder.PartMap hoverEvent = new JsonBuilder.PartMap();
                hoverEvent.setValue("action", new JsonBuilder.Part("show_item"));
                hoverEvent.setValue("value", new JsonBuilder.Part(jsonItem, false));

                JsonBuilder.PartMap itemNameMap = JsonBuilder.parse(formatPrefix + productName).toMap();
                itemNameMap.setValue("hoverEvent", hoverEvent);

                rootArray.addPart(itemNameMap);
            }
        }

        jb.setRootPart(rootArray);
        return jb::sendJson;
    }

}
