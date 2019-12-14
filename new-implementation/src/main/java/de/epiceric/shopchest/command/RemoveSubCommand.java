package de.epiceric.shopchest.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.command.SubCommand;
import de.epiceric.shopchest.api.event.ShopPreRemoveEvent;
import de.epiceric.shopchest.api.player.ShopPlayer;

public class RemoveSubCommand extends SubCommand {
    private ShopChest plugin;

    RemoveSubCommand(ShopChest plugin) {
        super("remove", true);
        this.plugin = plugin;
    }

    @Override
    public String getDescription() {
        return "Remove a shop";
    }

    @Override
    public void onExecute(CommandSender sender, String... args) {
        ShopPlayer player = plugin.wrapPlayer((Player) sender);
        plugin.getServer().getPluginManager().callEvent(new ShopPreRemoveEvent(player));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        return new ArrayList<>();
    }
}