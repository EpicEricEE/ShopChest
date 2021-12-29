package de.epiceric.shopchest.nms.reflection;

import de.epiceric.shopchest.nms.FakeArmorStand;
import de.epiceric.shopchest.nms.FakeItem;
import de.epiceric.shopchest.nms.Platform;
import de.epiceric.shopchest.nms.TextComponentHelper;

public class PlatformImpl implements Platform {

    private final ShopChestDebug debug;

    public PlatformImpl(ShopChestDebug debug) {
        this.debug = debug;
    }


    @Override
    public FakeArmorStand createFakeArmorStand() {
        return new FakeArmorStandImpl(debug);
    }

    @Override
    public FakeItem createFakeItem() {
        return new FakeItemImpl(debug);
    }

    @Override
    public TextComponentHelper getTextComponentHelper() {
        return new TextComponentHelperImpl(debug);
    }


}
