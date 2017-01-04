package de.epiceric.shopchest.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopUpdateEvent extends Event {

    public enum UpdateQuality {
        SLOWEST(31L),
        SLOWER(24L),
        SLOW(17L),
        NORMAL(10L),
        FAST(7L),
        FASTER(4L),
        FASTEST(1L);

        private long time;

        UpdateQuality(long time) {
            this.time = time;
        }

        public long getTime() {
            return time;
        }
    }

    private static final HandlerList handlers = new HandlerList();

    public ShopUpdateEvent() {
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
