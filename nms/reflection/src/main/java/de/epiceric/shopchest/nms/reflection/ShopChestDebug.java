package de.epiceric.shopchest.nms.reflection;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class ShopChestDebug {

    private final Logger logger;
    private final Consumer<String> debugConsumer;
    private final Consumer<Throwable> throwableConsumer;

    public ShopChestDebug(Logger logger, Consumer<String> debugConsumer, Consumer<Throwable> throwableConsumer) {
        this.logger = logger;
        this.debugConsumer = debugConsumer;
        this.throwableConsumer = throwableConsumer;
    }

    public Logger getLogger() {
        return logger;
    }

    public void debug(String message){
        debugConsumer.accept(message);
    }

    public void debug(Throwable e){
        throwableConsumer.accept(e);
    }

}
