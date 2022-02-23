package de.epiceric.shopchest.utils;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.epiceric.shopchest.ShopChest;

public class UpdateChecker {

    private ShopChest plugin;
    private String version;
    private String link;

    public UpdateChecker(ShopChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if an update is needed
     *
     * @return {@link UpdateCheckerResult#TRUE} if an update is available,
     *         {@link UpdateCheckerResult#FALSE} if no update is needed or
     *         {@link UpdateCheckerResult#ERROR} if an error occurred
     */
    public UpdateCheckerResult check() {
        try {
            plugin.debug("Checking for updates...");

            URL url = new URL("https://api.spiget.org/v2/resources/11431/versions?size=1&page=1&sort=-releaseDate");
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "ShopChest/UpdateChecker");

            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            JsonElement element = new JsonParser().parse(reader);

            if (element.isJsonArray()) {
                JsonObject result = element.getAsJsonArray().get(0).getAsJsonObject();
                String id = result.get("id").getAsString();
                version = result.get("name").getAsString();
                link = "https://www.spigotmc.org/resources/shopchest.11431/download?version=" + id;
            } else {
                plugin.debug("Failed to check for updates");
                plugin.debug("Result: " + element.toString());
                return UpdateCheckerResult.ERROR;
            }

            if (plugin.getDescription().getVersion().equals(version)) {
                plugin.debug("No update found");
                return UpdateCheckerResult.FALSE;
            } else {
                plugin.debug("Update found: " + version);
                return UpdateCheckerResult.TRUE;
            }

        } catch (Exception e) {
            plugin.debug("Failed to check for updates");
            plugin.debug(e);
            return UpdateCheckerResult.ERROR;
        }
    }

    /**
     * @return Latest Version or <b>null</b> if no update is available
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return Download Link of the latest version of <b>null</b> if no update is available
     */
    public String getLink() {
        return link;
    }

    public enum UpdateCheckerResult {
        TRUE,
        FALSE,
        ERROR
    }


}
