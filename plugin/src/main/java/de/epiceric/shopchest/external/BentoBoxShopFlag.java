package de.epiceric.shopchest.external;

import org.bukkit.Material;

import de.epiceric.shopchest.ShopChest;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.managers.RanksManager;

public class BentoBoxShopFlag {
    public static final Flag SHOP_FLAG = new Flag.Builder("CREATE_SHOPS", Material.CHEST)
            .type(Flag.Type.PROTECTION)
            .mode(Flag.Mode.BASIC)
            .defaultRank(RanksManager.TRUSTED_RANK)
            .build();

    public static void register(ShopChest plugin) {
        if (BentoBox.getInstance().getFlagsManager().registerFlag(SHOP_FLAG)) {
            plugin.debug("Registered BentoBox shop flag");
        } else {
            plugin.getLogger().warning("Failed to register BentoBox shop flag");
            plugin.debug("Failed to register BentoBox shop flag");
        }
    }
}