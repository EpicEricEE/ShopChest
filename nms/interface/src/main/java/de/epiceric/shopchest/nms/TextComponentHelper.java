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
                extras.add(replacement);
                cursor = matcher.end();
            } while (matcher.find());
            final String end = message.substring(cursor);
            if (!end.isEmpty()) {
                extras.addAll(Arrays.asList(TextComponent.fromLegacyText(end)));
            }
        }
        else {
            extras.addAll(Arrays.asList(TextComponent.fromLegacyText(message)));
        }
        baseComponent.setExtra(extras);

        return player -> player.spigot().sendMessage(baseComponent);
    }

}
