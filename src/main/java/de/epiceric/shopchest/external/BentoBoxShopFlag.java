package de.epiceric.shopchest.external;

import org.bukkit.Material;

import de.epiceric.shopchest.ShopChest;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.managers.RanksManager;

public class BentoBoxShopFlag {
    public static final Flag SHOP_CHEST_FLAG = new Flag.Builder("CREATE_SHOPS", Material.CHEST)
            .type(Flag.Type.PROTECTION)
            .mode(Flag.Mode.BASIC)
            .defaultRank(RanksManager.TRUSTED_RANK)
            .build();
    public static final Flag SHOP_TRAPPED_CHEST_FLAG = new Flag.Builder("CREATE_SHOPS", Material.TRAPPED_CHEST)
            .type(Flag.Type.PROTECTION)
            .mode(Flag.Mode.BASIC)
            .defaultRank(RanksManager.TRUSTED_RANK)
            .build();
    public static final Flag SHOP_SHULKER_BOX_FLAG = new Flag.Builder("CREATE_SHOPS", Material.SHULKER_BOX)
            .type(Flag.Type.PROTECTION)
            .mode(Flag.Mode.BASIC)
            .defaultRank(RanksManager.TRUSTED_RANK)
            .build();
    public static final Flag SHOP_BARREL_FLAG = new Flag.Builder("CREATE_SHOPS", Material.BARREL)
            .type(Flag.Type.PROTECTION)
            .mode(Flag.Mode.BASIC)
            .defaultRank(RanksManager.TRUSTED_RANK)
            .build();

    public static void register(ShopChest plugin) {
        if (BentoBox.getInstance().getFlagsManager().registerFlag(SHOP_CHEST_FLAG) && BentoBox.getInstance().getFlagsManager().registerFlag(SHOP_TRAPPED_CHEST_FLAG) && BentoBox.getInstance().getFlagsManager().registerFlag(SHOP_SHULKER_BOX_FLAG) && BentoBox.getInstance().getFlagsManager().registerFlag(SHOP_BARREL_FLAG)) {
            plugin.debug("Registered BentoBox shop flags");
        } else {
            plugin.getLogger().warning("Failed to register BentoBox shop flags");
            plugin.debug("Failed to register BentoBox shop flags");
        }
    }
}