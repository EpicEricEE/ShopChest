package de.epiceric.shopchest.config;

import de.epiceric.shopchest.ShopChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.util.List;
import java.util.Map;

public class HologramFormat {

    public enum Requirement {
        VENDOR, AMOUNT, ITEM_TYPE, ITEM_NAME, HAS_ENCHANTMENT, BUY_PRICE,
        SELL_PRICE, HAS_POTION_EFFECT, IS_MUSIC_DISC, IS_POTION_EXTENDED, IS_BOOK, ADMIN_SHOP,
        NORMAL_SHOP, IN_STOCK, MAX_STACK
    }

    private ShopChest plugin;
    private File configFile;
    private YamlConfiguration config;

    public HologramFormat(ShopChest plugin) {
        this.configFile = new File(plugin.getDataFolder(), "hologram-format.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.plugin = plugin;
    }

    /**
     * Get the format for the given line of the hologram
     * @param line Line of the hologram
     * @param values Values of the requirements that might be needed by the format (contains {@code null} if not comparable)
     * @return  The format of the first working option, or an empty String if no option is working
     *          because of not fulfilled requirements
     */
    public String getFormat(int line, Map<Requirement, Object> values) {
        ConfigurationSection options = config.getConfigurationSection("lines." + line + ".options");

        optionLoop:
        for (String key : options.getKeys(false)) {
            ConfigurationSection option = options.getConfigurationSection(key);
            List<String> requirements = option.getStringList("requirements");

            String format = option.getString("format");

            for (String sReq : requirements) {
                for (Requirement req : values.keySet()) {
                    if (sReq.contains(req.toString())) {
                        if (!eval(sReq, values)) {
                            continue optionLoop;
                        }
                    }
                }
            }

            return format;
        }

        return "";
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /** Returns whether the hologram text has to change dynamically without reloading */
    public boolean isDynamic() {
        int count = getLineCount();
        for (int i = 0; i < count; i++) {
            ConfigurationSection options = config.getConfigurationSection("lines." + i + ".options");

            for (String key : options.getKeys(false)) {
                ConfigurationSection option = options.getConfigurationSection(key);

                String format = option.getString("format");
                if (format.contains(Regex.STOCK.getName())) {
                    return true;
                }

                for (String req : option.getStringList("requirements")) {
                    if (req.contains(Requirement.IN_STOCK.toString())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /** Returns the amount of lines in a hologram */
    public int getLineCount() {
        return config.getConfigurationSection("lines").getKeys(false).size();
    }

    /** Returns the configuration of the "hologram-format.yml" file */
    public YamlConfiguration getConfig() {
        return config;
    }

    /**
     * Parse and evaluate a condition
     * @param condition Condition to evaluate
     * @param values Values of the requirements
     * @return Result of the condition
     */
    public boolean eval(String condition, Map<Requirement, Object> values) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");

            String c = condition;

            for (HologramFormat.Requirement req : HologramFormat.Requirement.values()) {
                if (c.contains(req.toString()) && values.containsKey(req)) {
                    String replace = String.valueOf(values.get(req));
                    if (values.get(req) instanceof String) {
                        replace = String.format("\"%s\"", replace);
                    }
                    c = c.replace(req.toString(), replace);
                }
            }

            return (boolean) engine.eval(c);
        } catch (ScriptException e) {
            plugin.debug("Failed to eval condition: " + condition);
            plugin.debug(e);
        }

        return false;
    }
}
