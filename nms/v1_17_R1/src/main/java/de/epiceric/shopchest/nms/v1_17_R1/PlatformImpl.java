package de.epiceric.shopchest.nms.v1_17_R1;

import de.epiceric.shopchest.nms.FakeArmorStand;
import de.epiceric.shopchest.nms.Platform;

public class PlatformImpl implements Platform {

    @Override
    public FakeArmorStand createFakeArmorStand() {
        return new FakeArmorStandImpl();
    }

}
