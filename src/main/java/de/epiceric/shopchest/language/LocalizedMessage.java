package de.epiceric.shopchest.language;

import de.epiceric.shopchest.config.Regex;
import org.bukkit.ChatColor;

public class LocalizedMessage {

    private Message message;
    private Regex[] regexes;
    private String localizedString;

    public LocalizedMessage(Message message, String localizedString, Regex... regexes) {
        this.message = message;
        this.regexes = regexes;
        this.localizedString = ChatColor.translateAlternateColorCodes('&', localizedString);
    }

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
     * @return Array of {@link Regex}, which are required by the message
     */
    public Regex[] getRegexes() {
        return regexes;
    }

    /**
     * @return Localized Message
     */
    public String getLocalizedString() {
        return localizedString;
    }

    public enum Message {
        SHOP_CREATED,
        CHEST_ALREADY_SHOP,
        CHEST_BLOCKED,
        DOUBLE_CHEST_BLOCKED,
        SHOP_REMOVED,
        CHEST_NO_SHOP,
        SHOP_CREATE_NOT_ENOUGH_MONEY,
        SHOP_INFO_VENDOR,
        SHOP_INFO_PRODUCT,
        SHOP_INFO_STOCK,
        SHOP_INFO_ENCHANTMENTS,
        SHOP_INFO_POTION_EFFECT,
        SHOP_INFO_MUSIC_TITLE,
        SHOP_INFO_NONE,
        SHOP_INFO_PRICE,
        SHOP_INFO_DISABLED,
        SHOP_INFO_NORMAL,
        SHOP_INFO_ADMIN,
        SHOP_INFO_EXTENDED,
        BUY_SELL_DISABLED,
        BUY_SUCCESS,
        BUY_SUCESS_ADMIN,
        SELL_SUCESS,
        SELL_SUCESS_ADMIN,
        SOMEONE_BOUGHT,
        SOMEONE_SOLD,
        NOT_ENOUGH_INVENTORY_SPACE,
        CHEST_NOT_ENOUGH_INVENTORY_SPACE,
        NOT_ENOUGH_MONEY,
        NOT_ENOUGH_ITEMS,
        VENDOR_NOT_ENOUGH_MONEY,
        OUT_OF_STOCK,
        ERROR_OCCURRED,
        AMOUNT_PRICE_NOT_NUMBER,
        AMOUNT_IS_ZERO,
        PRICES_CONTAIN_DECIMALS,
        NO_ITEM_IN_HAND,
        CLICK_CHEST_CREATE,
        CLICK_CHEST_REMOVE,
        CLICK_CHEST_INFO,
        OPENED_SHOP,
        CANNOT_BREAK_SHOP,
        CANNOT_SELL_BROKEN_ITEM,
        BUY_PRICE_TOO_LOW,
        SELL_PRICE_TOO_LOW,
        BUYING_DISABLED,
        SELLING_DISABLED,
        RELOADED_SHOPS,
        SHOP_LIMIT_REACHED,
        OCCUPIED_SHOP_SLOTS,
        CANNOT_SELL_ITEM,
        UPDATE_AVAILABLE,
        UPDATE_CLICK_TO_DOWNLOAD,
        UPDATE_NO_UPDATE,
        UPDATE_CHECKING,
        UPDATE_ERROR,
        HOLOGRAM_FORMAT,
        HOLOGRAM_BUY_SELL,
        HOLOGRAM_BUY,
        HOLOGRAM_SELL,
        NO_PERMISSION_CREATE,
        NO_PERMISSION_CREATE_ADMIN,
        NO_PERMISSION_CREATE_PROTECTED,
        NO_PERMISSION_OPEN_OTHERS,
        NO_PERMISSION_BUY,
        NO_PERMISSION_SELL,
        NO_PERMISSION_WG_BUY,
        NO_PERMISSION_WG_SELL,
        NO_PERMISSION_REMOVE_OTHERS,
        NO_PERMISSION_RELOAD,
        NO_PERMISSION_UPDATE,
        NO_PERMISSION_CONFIG,
        NO_PERMISSION_EXTEND_OTHERS,
        NO_PERMISSION_EXTEND_PROTECTED,
        COMMAND_DESC_CREATE,
        COMMAND_DESC_REMOVE,
        COMMAND_DESC_INFO,
        COMMAND_DESC_RELOAD,
        COMMAND_DESC_UPDATE,
        COMMAND_DESC_LIMITS,
        COMMAND_DESC_CONFIG,
        CHANGED_CONFIG_SET,
        CHANGED_CONFIG_REMOVED,
        CHANGED_CONFIG_ADDED
    }

    public static class ReplacedRegex {

        private Regex regex;
        private String replace;

        public ReplacedRegex(Regex regex, String replace) {
            this.regex = regex;
            this.replace = replace;
        }

        /**
         * @return String which will replace the regex
         */
        public String getReplace() {
            return replace;
        }

        /**
         * @return Regex that will be replaced
         */
        public Regex getRegex() {
            return regex;
        }

    }

}
