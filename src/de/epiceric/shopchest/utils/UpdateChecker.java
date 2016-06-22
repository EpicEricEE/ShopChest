package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {

    private ShopChest plugin;
    private String url;
    private String version;
    private String link;

    public UpdateChecker(ShopChest plugin, String url) {
        this.plugin = plugin;
        this.url = url;
    }

    public UpdateCheckerResult updateNeeded() {
        try {
            URL url = new URL("http://textuploader.com/all1l/raw");
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0");
            conn.connect();

            InputStreamReader isr = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            String line = br.readLine();

            isr.close();
            br.close();

            version = line.split("\\|")[0];
            link = url + "download?version=" + line.split("\\|")[1];

            if (plugin.getDescription().getVersion().equals(version))
                return UpdateCheckerResult.FALSE;
            else
                return UpdateCheckerResult.TRUE;

        } catch (Exception e) {
            return UpdateCheckerResult.ERROR;
        }
    }

    public String[] getBroadcast() {
        try {
            URL url = new URL("http://textuploader.com/5b51f/raw");
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0");
            conn.connect();

            InputStreamReader isr = new InputStreamReader(conn.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            String line = br.readLine();

            isr.close();
            br.close();

            String[] messages = line.split("#n");

            if (!line.equals("/"))
                return messages;

        } catch (Exception | Error e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getVersion() {
        return version;
    }

    public String getLink() {
        return link;
    }

    public enum UpdateCheckerResult {
        TRUE,
        FALSE,
        ERROR;
    }


}
