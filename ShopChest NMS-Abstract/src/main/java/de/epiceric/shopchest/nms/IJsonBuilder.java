package de.epiceric.shopchest.nms;

import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class IJsonBuilder {

    public abstract void sendJson(Player p);

    private List<String> extras = new ArrayList<>();

    public IJsonBuilder parse(String text, String hoverText, String downloadLink) {
        String regex = "[&�]{1}([a-fA-Fl-oL-O0-9]){1}";
        text = text.replaceAll(regex, "�$1");
        if (!Pattern.compile(regex).matcher(text).find()) {
            withText(text).withHoverEvent(HoverAction.SHOW_TEXT, hoverText).withClickEvent(ClickAction.OPEN_URL, downloadLink);
            return this;
        }
        String[] words = text.split(regex);

        int index = words[0].length();
        for (String word : words) {
            try {
                if (index != words[0].length())
                    withText(word).withColor("�" + text.charAt(index - 1)).withHoverEvent(HoverAction.SHOW_TEXT, hoverText).withClickEvent(ClickAction.OPEN_URL, downloadLink);
            } catch (Exception e) {}
            index += word.length() + 2;
        }
        return this;
    }

    private IJsonBuilder withText(String text) {
        extras.add("{\"text\":\"" + text + "\"}");
        return this;
    }

    private IJsonBuilder withColor(ChatColor color) {
        String c = color.name().toLowerCase();
        addSegment(color.isColor() ? "\"color\":\"" + c + "\"" : "\"" + c + "\"" + ":true");
        return this;
    }

    private IJsonBuilder withColor(String color) {
        while (color.length() != 1) color = color.substring(1).trim();
        withColor(ChatColor.getByChar(color));
        return this;
    }

    private IJsonBuilder withClickEvent(ClickAction action, String value) {
        addSegment("\"clickEvent\":{\"action\":\"" + action.toString().toLowerCase()
                + "\",\"value\":\"" + value + "\"}");
        return this;
    }

    private IJsonBuilder withHoverEvent(HoverAction action, String value) {
        addSegment("\"hoverEvent\":{\"action\":\"" + action.toString().toLowerCase()
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

    private enum ClickAction {
        RUN_COMMAND, SUGGEST_COMMAND, OPEN_URL
    }

    private enum HoverAction {
        SHOW_TEXT
    }

}
