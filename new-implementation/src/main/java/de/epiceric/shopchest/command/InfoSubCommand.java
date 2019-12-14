package de.epiceric.shopchest.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.command.SubCommand;
import de.epiceric.shopchest.api.event.ShopPreInfoEvent;
import de.epiceric.shopchest.api.player.ShopPlayer;

public class InfoSubCommand extends SubCommand {
    private ShopChest plugin;

    InfoSubCommand(ShopChest plugin) {
        super("info", true);
        this.plugin = plugin;
    }

    @Override
    public String getDescription() {
        return "Retrieve information about a shop";
    }

    @Override
    public void onExecute(CommandSender sender, String... args) {
        ShopPlayer player = plugin.wrapPlayer((Player) sender);
        plugin.getServer().getPluginManager().callEvent(new ShopPreInfoEvent(player));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return new ArrayList<>();
    }
}