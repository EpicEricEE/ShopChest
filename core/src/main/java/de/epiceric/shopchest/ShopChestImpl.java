package de.epiceric.shopchest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.ShopManager;
import de.epiceric.shopchest.api.command.ShopCommand;
import de.epiceric.shopchest.api.config.Config;
import de.epiceric.shopchest.api.database.DatabaseType;
import de.epiceric.shopchest.api.event.ShopLoadedEvent;
import de.epiceric.shopchest.api.player.ShopPlayer;
import de.epiceric.shopchest.command.ShopCommandImpl;
import de.epiceric.shopchest.config.ConfigManager;
import de.epiceric.shopchest.database.Database;
import de.epiceric.shopchest.database.MySQL;
import de.epiceric.shopchest.database.SQLite;
import de.epiceric.shopchest.listener.ChestInteractListener;
import de.epiceric.shopchest.listener.CreativeSelectListener;
import de.epiceric.shopchest.listener.PlayerJoinQuitListener;
import de.epiceric.shopchest.listener.internal.ShopCommandListener;
import de.epiceric.shopchest.listener.internal.ShopInteractListener;
import de.epiceric.shopchest.listener.internal.monitor.ShopCommandMonitorListener;
import de.epiceric.shopchest.listener.internal.monitor.ShopInteractMonitorListener;
import de.epiceric.shopchest.player.ShopPlayerImpl;
import de.epiceric.shopchest.shop.ShopImpl;
import de.epiceric.shopchest.util.Logger;
import de.epiceric.shopchest.util.NmsUtil;
import net.milkbowl.vault.economy.Economy;

public class ShopChestImpl extends ShopChest {
    private ConfigManager configManager;
    private Database database;
    private ShopCommand command;
    private Economy economy;

    @Override
    public void onLoad() {
        if (!loadConfigData()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    @Override
    public void onEnable() {
        if (!loadEconomy()) {
            Logger.severe("Failed to load economy. Do you have an economy plugin installed that supports Vault?");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerCommand();
        registerEvents();
        loadDatabase();
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        unregisterCommand();
        saveConfigData();
        unloadDatabase();

        getShopManager().getShops().forEach(shop -> ((ShopImpl) shop).destroy());
    }

    private boolean loadConfigData() {
        configManager = ConfigManager.get(this);

        try {
            configManager.load();
        } catch (IOException e) {
            Logger.severe("Failed to load configuration file. Plugin will be disabled");
            Logger.severe(e);
            return false;
        }

        return true;
    }

    private void saveConfigData() {
        if (configManager != null) {
            try {
                configManager.save();
            } catch (IOException e) {
                Logger.severe("Failed to save configuration file. Config may have been lost");
                Logger.severe(e);
            }
        }
    }

    private boolean loadEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        this.economy = rsp.getProvider();
        return this.economy != null;
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new ShopCommandMonitorListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopCommandListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopInteractMonitorListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopInteractListener(this), this);

        getServer().getPluginManager().registerEvents(new ChestInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new CreativeSelectListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(this), this);
    }

    private void registerCommand() {
        command = new ShopCommandImpl(this);

        try {
            Field fieldCommandMap = getServer().getClass().getDeclaredField("commandMap");
            fieldCommandMap.setAccessible(true);

            CommandMap commandMap = (CommandMap) fieldCommandMap.get(getServer());
            commandMap.register("shopchest", ((ShopCommandImpl) command).getPluginCommand());
        } catch (ReflectiveOperationException e) {
            Logger.severe("Failed to register shop command");
            Logger.severe(e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void unregisterCommand() {
        try {
            Command pluginCommand = ((ShopCommandImpl) command).getPluginCommand();
            Field fieldCommandMap = getServer() .getClass().getDeclaredField("commandMap");
            fieldCommandMap.setAccessible(true);

            CommandMap commandMap = (CommandMap) fieldCommandMap.get(getServer());
            pluginCommand.unregister(commandMap);

            Field fieldKnownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
            fieldKnownCommands.setAccessible(true);

            Map<?, ?> knownCommands = (Map<?, ?>) fieldKnownCommands.get(commandMap);
            knownCommands.remove("shopchest:" + command.getName());
            if (pluginCommand.equals(knownCommands.get(command.getName()))) {
                knownCommands.remove(command.getName());
            }
        } catch (ReflectiveOperationException e) {
            Logger.severe("Failed to unregister shop command");
            Logger.severe(e);
        }
    }

    private void loadDatabase() {
        if (Config.DATABASE_TYPE.get() == DatabaseType.SQLITE) {
            database = new SQLite(this);
        } else {
            database = new MySQL(this);
        }

        ((ShopManagerImpl) getShopManager()).loadShopAmounts(
            shopAmounts -> {
                Logger.info("Loaded shop amounts from the database");
            },
            error -> {
                Logger.severe("Failed to load shops amounts from the database");
                Logger.severe("Shop limits will not be working correctly");
                Logger.severe(error);
            }
        );

        List<Chunk> chunks = new ArrayList<>();
        for (World world : getServer().getWorlds()) {
            chunks.addAll(Arrays.asList(world.getLoadedChunks()));
        }

        ((ShopManagerImpl) getShopManager()).loadShops(chunks.toArray(new Chunk[chunks.size()]),
            shops -> {
                getServer().getPluginManager().callEvent(new ShopLoadedEvent(shops));
                Logger.info("Loaded {0} shops from the database", shops.size());
            },
            error -> {
                Logger.severe("Failed to load shops from the database");
                Logger.severe(error);
                getServer().getPluginManager().disablePlugin(this);
            }
        );
    }

    private void unloadDatabase() {
        if (database != null) {
            if (database instanceof SQLite) {
                ((SQLite) database).vacuum();
            }
            database.disconnect();
        }
    }
    
    private void checkForUpdates() {
        if (!Config.CORE_ENABLE_UPDATE_CHECKER.get()) {
            return;
        }

        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                URL url = new URL("https://api.spiget.org/v2/resources/11431/versions?size=1&page=1&sort=-releaseDate");
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("User-Agent", "ShopChest/UpdateChecker");
    
                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                JsonElement element = new JsonParser().parse(reader);
    
                if (element.isJsonArray()) {
                    JsonObject result = element.getAsJsonArray().get(0).getAsJsonObject();
                    String version = result.get("name").getAsString();
                    String link = "https://www.spigotmc.org/resources/shopchest.11431/";
    
                    String current = getDescription().getVersion();
                    boolean isNew = version.compareToIgnoreCase(current) > 0;
                    if (isNew) {
                        Logger.warning("Version {0} of ShopChest is available, you are using v{1}", version, current);
                        Logger.warning("Download here: {0}", link);
    
                        // TODO: i18n
                        String updateMessage = "[{" +
                                "\"text\":\"ShopChest version \"," +
                                "\"color\":\"red\"" +
                            "},{" +
                                "\"text\":\"" + version + "\"," +
                                "\"color\":\"yellow\"" +
                            "},{" +
                                "\"text\":\" is available and can be downoladed \"," +
                                "\"color\":\"red\"" +
                            "},{" +
                                "\"text\":\"here\"," +
                                "\"color\":\"yellow\"," +
                                "\"clickEvent\":{" +
                                    "\"action\":\"open_url\"," +
                                    "\"value\":\"" + link + "\"" +
                                "}," +
                                "\"hoverEvent\":{" +
                                    "\"action\":\"show_text\"," +
                                    "\"value\":\"Click to download\"" +
                                "}" +
                            "},{" +
                                "\"text\":\".\"," +
                                "\"color\":\"red\"" +
                            "}]";
    
                        getServer().getOnlinePlayers().stream()
                                .filter(player -> player.hasPermission("shopchest.notification.update"))
                                .forEach(player -> NmsUtil.sendJsonMessage(player, updateMessage));
                    } else {
                        Logger.info("You are using the latest version");
                    }
                } else {
                    Logger.warning("Failed to check for updates: Connection returned {0}", element.toString());
                }
            } catch (IOException e) {
                Logger.warning("Failed to check for updates");
                Logger.warning(e);
            }
        });
    }

    /**
     * Gets an instance of the plugin's database
     * 
     * @return the database
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Gets an instance of the config manager
     * 
     * @return the config manager
     * @see ConfigManager#get(ShopChestImpl)
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gets an instance of the Vault economy provider
     * 
     * @return the economy provider
     */
    public Economy getEconomy() {
        return economy;
    }

    /* API Implementation */

    @Override
    public ShopManager getShopManager() {
        return ShopManagerImpl.get(this);
    }

    @Override
    public ShopPlayer wrapPlayer(Player player) {
        return ShopPlayerImpl.get(this, player);
    }

    @Override
    public String formatEconomy(double amount) {
        return economy.format(amount);
    }

    @Override
    public ShopCommand getShopCommand() {
        return command;
    }
}