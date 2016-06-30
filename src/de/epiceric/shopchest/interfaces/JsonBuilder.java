package de.epiceric.shopchest.interfaces;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public interface JsonBuilder {

    JsonBuilder parse(String text);

    JsonBuilder withText(String text);

    JsonBuilder withColor(ChatColor color);

    JsonBuilder withColor(String color);

    JsonBuilder withClickEvent(ClickAction action, String value);

    JsonBuilder withHoverEvent(HoverAction action, String value);

    String toString();

    void sendJson(Player p);

    enum ClickAction {
        RUN_COMMAND, SUGGEST_COMMAND, OPEN_URL
    }

    enum HoverAction {
        SHOW_TEXT
    }

}
