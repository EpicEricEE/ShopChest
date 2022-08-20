package de.epiceric.shopchest.addon.bentobox;

import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import de.epiceric.shopchest.api.ShopChest;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.managers.RanksManager;

public class AddonMain extends JavaPlugin {
    public static final Flag SHOP_FLAG = new Flag.Builder("CREATE_SHOPS", Material.CHEST)
            .type(Flag.Type.PROTECTION)
            .mode(Flag.Mode.BASIC)
            .defaultRank(RanksManager.TRUSTED_RANK)
            .build();

    private IslandListener islandListener;
    private ShopListener shopListener;

    @Override
    public void onEnable() {
        if (!BentoBox.getInstance().getFlagsManager().registerFlag(SHOP_FLAG)) {
            getLogger().warning("Failed to register BentoBox shop flag");
        }

        ShopChest shopChest = getPlugin(ShopChest.class);

        islandListener = new IslandListener(shopChest);
        shopListener = new ShopListener();

        getServer().getPluginManager().registerEvents(islandListener, this);
        getServer().getPluginManager().registerEvents(shopListener, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(islandListener);
        HandlerList.unregisterAll(shopListener);
        
        BentoBox.getInstance().getFlagsManager().unregister(SHOP_FLAG);
    }
}