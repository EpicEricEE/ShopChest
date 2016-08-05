package de.epiceric.shopchest.listeners;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCMagnetPullEvent;
import de.epiceric.shopchest.ShopChest;

public class LWCMagnetListener {

    private ShopChest plugin;

    public LWCMagnetListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    public void initializeListener() {
        try {
            Class.forName("com.griefcraft.scripting.event.LWCMagnetPullEvent");

            LWC.getInstance().getModuleLoader().registerModule(ShopChest.getInstance(), new JavaModule() {

                @Override
                public void onMagnetPull(LWCMagnetPullEvent event) {
                    if (event.getItem().hasMetadata("shopItem")) {
                        event.setCancelled(true);
                    }
                }

            });

        } catch (ClassNotFoundException ex) {
            plugin.debug("Using not recommended version of LWC");
            plugin.getLogger().warning("Shop items can be sucked up by the magnet flag of a protected chest of LWC.");
            plugin.getLogger().warning("Use 'LWC Unofficial - Entity locking' v1.7.3 or later by 'Me_Goes_RAWR' to prevent this.");
        }
    }


}
