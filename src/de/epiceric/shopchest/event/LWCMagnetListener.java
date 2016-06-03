package de.epiceric.shopchest.event;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCMagnetPullEvent;
import de.epiceric.shopchest.ShopChest;

public class LWCMagnetListener {

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
            ShopChest.logger.warning("Shop items can be sucked up by the magnet flag of a protected chest of LWC.");
            ShopChest.logger.warning("Use 'LWC Unofficial - Entity locking' v1.7.3 or later by 'Me_Goes_RAWR' to prevent this.");
        };
    }


}
