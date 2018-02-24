package de.epiceric.shopchest.language;

import org.bukkit.ChatColor;

public class LocalizedMessage {

    private Message message;
    private String localizedString;

    public LocalizedMessage(Message message, String localizedString) {
        this.message = message;
        this.localizedString = ChatColor.translateAlternateColorCodes('&', localizedString);
    }

    /**
     * @return {@link Message} linked to this object
     */
    public Message getMessage() {
        return message;
    }

    /**
     * @return Localized Message
     */
    public String getLocalizedString() {
        return localizedString;
    }

}
