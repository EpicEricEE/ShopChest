package de.epiceric.shopchest.config;

public enum Placeholder {

    VENDOR("%VENDOR%"),
    AMOUNT("%AMOUNT%"),
    ITEM_NAME("%ITEMNAME%"),
    CREATION_PRICE("%CREATION-PRICE%"),
    ERROR("%ERROR%"),
    ENCHANTMENT("%ENCHANTMENT%"),
    MIN_PRICE("%MIN-PRICE%"),
    MAX_PRICE("%MAX-PRICE%"),
    VERSION("%VERSION%"),
    BUY_PRICE("%BUY-PRICE%"),
    SELL_PRICE("%SELL-PRICE%"),
    LIMIT("%LIMIT%"),
    PLAYER("%PLAYER%"),
    POTION_EFFECT("%POTION-EFFECT%"),
    MUSIC_TITLE("%MUSIC-TITLE%"),
    PROPERTY("%PROPERTY%"),
    VALUE("%VALUE%"),
    EXTENDED("%EXTENDED%"),
    REVENUE("%REVENUE%"),
    GENERATION("%GENERATION%"),
    STOCK("%STOCK%"),
    MAX_STACK("%MAX-STACK%");

    private String name;

    Placeholder(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
