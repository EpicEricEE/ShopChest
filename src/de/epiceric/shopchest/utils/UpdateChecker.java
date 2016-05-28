package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
            Connection con = Jsoup.connect("http://textuploader.com/all1l/raw");
            con.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0");

            Document doc = con.get();

            version = doc.text().split("\\|")[0];
            link = url + "download?version=" + doc.text().split("\\|")[1];

            if (plugin.getDescription().getVersion().equals(version))
                return UpdateCheckerResult.FALSE;
            else
                return UpdateCheckerResult.TRUE;

        } catch (Exception | Error e) {
            return UpdateCheckerResult.ERROR;
        }
    }

    public String[] getBroadcast() {
        try {
            Connection con = Jsoup.connect("http://textuploader.com/5b51f/raw");
            con.userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0");

            Document doc = con.get();

            String broadcast = doc.text();

            String[] messages = broadcast.split("#n");

            if (!broadcast.equals("/"))
                return messages;

        } catch (Exception | Error e) {
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
