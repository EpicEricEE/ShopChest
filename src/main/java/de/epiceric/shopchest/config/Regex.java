package de.epiceric.shopchest.config;

public enum Regex {

    VENDOR("%VENDOR%"),
    AMOUNT("%AMOUNT%"),
    ITEM_NAME("%ITEMNAME%"),
    CREATION_PRICE("%CREATION-PRICE%"),
    ERROR("%ERROR%"),
    ENCHANTMENT("%ENCHANTMENT%"),
    MIN_PRICE("%MIN-PRICE%"),
    VERSION("%VERSION%"),
    BUY_PRICE("%BUY-PRICE%"),
    SELL_PRICE("%SELL-PRICE%"),
    LIMIT("%LIMIT%"),
    PLAYER("%PLAYER%"),
    POTION_EFFECT("%POTION-EFFECT%"),
    MUSIC_TITLE("%MUSIC-TITLE%"),
    PROPERTY("%PROPERTY%"),
    VALUE("%VALUE%"),
    EXTENDED("%EXTENDED%");

    private String name;

    Regex(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
