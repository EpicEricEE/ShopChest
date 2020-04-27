package de.epiceric.shopchest.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import de.epiceric.shopchest.ShopChest;

public class LanguageConfiguration extends FileConfiguration {

    private ArrayList<String> lines = new ArrayList<>();
    private HashMap<String, String> values = new HashMap<>();

    private ShopChest plugin;
    private boolean showMessages;
    private File file;

    public LanguageConfiguration(ShopChest plugin, boolean showMessages) {
        this.plugin = plugin;
        this.showMessages = showMessages;
    }

    @Override
    public String saveToString() {
        StringBuilder sb = new StringBuilder("");

        for (String line : lines) {
            sb.append(line);
            sb.append("\n");
        }

        return sb.toString();
    }

    @Override
    public String getString(String path, String def) {
        for (String key : values.keySet()) {
            if (key.equals(path)) {
                return values.get(key);
            }
        }
            
        values.put(path, def);

        if (file != null) {
            // Append missing entry to loaded language file
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(path + "=" + def + "\n");
                if (showMessages)
                    plugin.getLogger().info("Missing translation for \"" + path + "\" has been added as \"" + def + "\" to the selected language file.");
            } catch (IOException e) {
                plugin.debug("Failed to add language entry");
                plugin.debug(e);
                if (showMessages)
                    plugin.getLogger().severe("Failed to add missing translation for \"" + path + "\" to the selected langauge file.");
            }
        }

        return def;
    }

    @Override
    public void load(File file) throws IOException, InvalidConfigurationException {
        this.file = file;

        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder();

        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }

        fis.close();
        isr.close();
        br.close();

        loadFromString(sb.toString());
    }

    @Override
    public void loadFromString(String s) throws InvalidConfigurationException {
        String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.isEmpty()) {
                this.lines.add(line);

                if (!line.startsWith("#")) {
                    if (line.contains("=")) {
                        if (line.split("=").length >= 2) {
                            String key = line.split("=")[0];
                            StringBuilder sbValue = new StringBuilder();

                            for (int i = 1; i < line.split("=").length; i++) {
                                if (i > 1) {
                                    sbValue.append("=");
                                }
                                sbValue.append(line.split("=")[i]);
                            }

                            String value = sbValue.toString();

                            values.put(key, value);
                        } else if (line.split("=").length == 1) {
                            String key = line.split("=")[0];
                            values.put(key, "");
                        }
                    }
                }
            }
        }
    }

    @Override
    protected String buildHeader() {
        return null;
    }
}