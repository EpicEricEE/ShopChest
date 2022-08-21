package de.epiceric.shopchest.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;
import java.util.StringJoiner;

import de.epiceric.shopchest.ShopChestImpl;
import de.epiceric.shopchest.api.config.Config;
import de.epiceric.shopchest.api.config.MaterialDoubleMap;
import de.epiceric.shopchest.api.config.MaterialList;
import de.epiceric.shopchest.api.config.Property;
import de.epiceric.shopchest.api.config.StringList;
import de.epiceric.shopchest.util.Logger;

/**
 * Class for accessing ShopChest configuration values
 */
public class ConfigManager {
    private static ConfigManager instance;

    /**
     * Gets the instance of the config manager
     * 
     * @param plugin an instance of the {@link ShopChestImpl plugin implementation}
     * @return the config manager
     * @see ShopChestImpl#getConfigManager()
     */
    public static ConfigManager get(ShopChestImpl plugin) {
        if (instance == null) {
            instance = new ConfigManager(plugin);
        }
        return instance;
    }

    private ShopChestImpl plugin;
    private File configFile;
    private Properties properties;

    private ConfigManager(ShopChestImpl plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.properties");
        this.properties = new Properties();
    }

    private String buildHeader() {
        return new StringJoiner("\n")
                .add("# ===============================================")
                .add("# ====== Configuration File of 'ShopChest' ======")
                .add("# ===============================================")
                .add("")
                .add("# You can find item names in the 'item_names.txt' file.")
                .add("")
                .add("# The documentation for this config file can be found here:")
                .add("# https://github.com/EpicEricEE/ShopChest/wiki/Configuration")
                .toString();
    }

    @SuppressWarnings("unchecked")
    private <T> T deserialize(String str, Class<T> type) {
        if (str == null) {
            return null;
        }
        
        if (type == String.class) {
            return (T) str;
        }

        if (type.isEnum()) {
            try {
                Method methodName = type.getMethod("name");
                for (T enumVal : type.getEnumConstants()) {
                    String name = (String) methodName.invoke(enumVal);
                    if (name.equalsIgnoreCase(str)) {
                        return enumVal;
                    }
                }
            } catch (ReflectiveOperationException e) {
                Logger.severe("Failed to parse \"{0}\" as enum {1}", str, type.getName());
                Logger.severe(e);
            }
        }

        try {
            Method methodValueOf = type.getMethod("valueOf", String.class);
            return (T) methodValueOf.invoke(null, str);
        } catch (ReflectiveOperationException e) {
            Logger.severe("Failed to parse \"{0}\" as {1}", str, type.getName());
            Logger.severe(e);
        }

        return null;
    }

    private String doubleToString(double d) {
        if (d == (int) d) {
            return String.valueOf((int) d);
        }
        return String.valueOf(d);
    }

    private <T> String serialize(T value, Class<T> type) {
        if (type.isEnum()) {
            return ((Enum<?>) value).name().toLowerCase(Locale.US);
        } else if (type == String.class) {
            return (String) value;
        } else if (type == MaterialDoubleMap.class) {
            return ((MaterialDoubleMap) value).toString();
        } else if (type == MaterialList.class) {
            return ((MaterialList) value).toString();
        } else if (type == StringList.class) {
            return ((StringList) value).toString();
        } else if (type == Double.class) {
            return doubleToString(((Double) value).doubleValue());
        } else if (type == Float.class) {
            return doubleToString(((Float) value).doubleValue());
        }
        return String.valueOf(value);
    }

    private <T> void loadProperty(Property<T> property) {
        property.set(deserialize(properties.getProperty(property.getKey()), property.getType()));
    }

    private <T> void saveProperty(Property<T> property) {
        properties.setProperty(property.getKey(), serialize(property.get(), property.getType()));
    }

    /**
     * Loads the config file and sets all properties' values
     * 
     * @throws IOException if the properties file cannot be loaded
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void load() throws IOException {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource("config.properties", false);
        }

        FileInputStream inputStream = new FileInputStream(configFile);
        properties.load(inputStream);

        boolean hasNewProperties = false;

        for (Field field : Config.class.getFields()) {
            if (field.getType() == Property.class) {
                try {
                    Property property = (Property) field.get(null);

                    if (!properties.containsKey(property.getKey())) {
                        hasNewProperties = true;
                        String strVal = serialize(property.get(), property.getType());
                        properties.setProperty(property.getKey(), strVal);
                        Logger.warning("New config property \"{0}\" has been added with a default value of \"{1}\"",
                                property.getKey(), strVal);
                    }

                    loadProperty(property);
                } catch (ReflectiveOperationException e) {
                    Logger.severe("Failed to load property \"{0}\"", field.getName());
                    Logger.severe(e);
                }
            }
        }

        if (hasNewProperties) {
            // Save new properties to file
            save();
        }
    }

    /**
     * Saves all properties' values to the config file
     * 
     * @throws IOException if the properties file cannot be written to
     */
    public void save() throws IOException {
        FileWriter writer = new FileWriter(configFile);
        writer.write(buildHeader());

        String sectionBefore = "";
        for (Field field : Config.class.getFields()) {
            if (field.getType() == Property.class) {
                try {
                    Property<?> property = (Property<?>) field.get(null);
                    saveProperty(property);

                    if (!sectionBefore.equals(property.getSection())) {
                        // Empty line between sections
                        writer.write("\n");
                        sectionBefore = property.getSection();
                    }

                    writer.write("\n" + property.getKey() + "=" + properties.getProperty(property.getKey()));
                } catch (ReflectiveOperationException | IOException e) {
                    Logger.severe("Failed to save property \"{0}\"", field.getName());
                    Logger.severe(e);
                }
            }
        }

        writer.close();
    }
}