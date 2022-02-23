package de.epiceric.shopchest.nms;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface TextComponentHelper {

    default void sendUpdateMessage(Player player, String updateMessage, String hoverMessage, String downloadUrl){
        final TextComponent component = new TextComponent();
        component.setExtra(Arrays.asList(TextComponent.fromLegacyText(updateMessage)));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverMessage)));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl));
        player.spigot().sendMessage(component);
    }

    /**
     * Get the 'tag' json object containing the item's data
     * @param itemStack The item stack that will be displayed
     * @return A string representing a json object of the 'tag'
     */
    String getNbt(ItemStack itemStack);

    default Consumer<Player> getSendableItemInfo(String message, String itemPlaceHolder, ItemStack itemStack, String productName){
        final TextComponent baseComponent = new TextComponent();
        final TextComponent replacement = new TextComponent(productName);
        replacement.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                new Item(
                        itemStack.getType().getKey().toString(),
                        1,
                        ItemTag.ofNbt(getNbt(itemStack))
                )
        ));
        final List<BaseComponent> extras = new ArrayList<>();
        final Matcher matcher = Pattern.compile(itemPlaceHolder, Pattern.LITERAL).matcher(message);
        if (matcher.find()) {
            int cursor = 0;
            do {
                final String pre = message.substring(cursor, matcher.start());
                if (!pre.isEmpty()) {
                    extras.addAll(Arrays.asList(TextComponent.fromLegacyText(pre)));
                }
                extras.add(copyPreviousFormatting(extras, replacement));
                cursor = matcher.end();
            } while (matcher.find());
            final String end = message.substring(cursor);
            if (!end.isEmpty()) {
                TextComponent endBaseComponent = new TextComponent();
                endBaseComponent = copyPreviousFormatting(extras, endBaseComponent);
                endBaseComponent.setExtra(Arrays.asList(TextComponent.fromLegacyText(end)));
                extras.add(endBaseComponent);
            }
        }
        else {
            extras.addAll(Arrays.asList(TextComponent.fromLegacyText(message)));
        }
        baseComponent.setExtra(extras);

        return player -> player.spigot().sendMessage(baseComponent);
    }

    static TextComponent copyPreviousFormatting(List<BaseComponent> extras, TextComponent replacement){
        TextComponent formattedReplacement = replacement;
        if(!extras.isEmpty()) {
            formattedReplacement = replacement.duplicate();
            final BaseComponent previousComponent = extras.get(extras.size() - 1);
            // Check parent also (not done in copyFormatting)
            if (formattedReplacement.getColorRaw() == null) {
                formattedReplacement.setColor(previousComponent.getColor());
            }
            if (formattedReplacement.getFontRaw() == null) {
                formattedReplacement.setFont(previousComponent.getFont());
            }
            if (formattedReplacement.isBoldRaw() == null) {
                formattedReplacement.setBold(previousComponent.isBold());
            }
            if (formattedReplacement.isItalicRaw() == null) {
                formattedReplacement.setItalic(previousComponent.isItalic());
            }
            if (formattedReplacement.isUnderlinedRaw() == null) {
                formattedReplacement.setUnderlined(previousComponent.isUnderlined());
            }
            if (formattedReplacement.isStrikethroughRaw() == null) {
                formattedReplacement.setStrikethrough(previousComponent.isStrikethrough());
            }
            if (formattedReplacement.isObfuscatedRaw() == null) {
                formattedReplacement.setObfuscated(previousComponent.isObfuscated());
            }
            if (formattedReplacement.getInsertion() == null) {
                formattedReplacement.setInsertion(previousComponent.getInsertion());
            }
        }
        return formattedReplacement;
    }

}
