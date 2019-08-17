package de.epiceric.shopchest.api.command;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Represents a sub command for the plugin's main command
 * 
 * @see ShopCommand#addSubCommand(SubCommand)
 * @since 1.13
 */
public abstract class SubCommand {
    private final String name;
    private final boolean onlyPlayer;

    /**
     * Creates a sub command with the given name
     * <p>
     * The sub command has to be added to the main command via
     * {@link ShopCommand#addSubCommand(SubCommand)}.
     * 
     * @param name       the name
     * @param onlyPlayer whether only players can run this sub command
     * @see ShopCommand#addSubCommand(SubCommand)
     * @since 1.13
     */
    public SubCommand(String name, boolean onlyPlayer) {
        this.name = name;
        this.onlyPlayer = onlyPlayer;
    }

    /**
     * Gets the name of this sub command
     * 
     * @return the name
     * @since 1.13
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the permission the command sender needs to run this sub command
     * <p>
     * If no permission is needed, this should return an empty string.
     * 
     * @return the permission or an empty string
     * @since 1.13
     */
    public String getPermission() {
        return "";
    }

    /**
     * Gets whether the given sender is permitted to run this sub command
     * 
     * @param sender the sender
     * @return whether the sender is permitted
     */
    public boolean isPermitted(CommandSender sender) {
        return getPermission() == null || getPermission().isEmpty() || sender.hasPermission(getPermission());
    }

    /**
     * Gets whether the given sender can run this sub command
     * <p>
     * This checks whether the sender is a player if the command can only be run by
     * players. This does not check for permission.
     * 
     * @param sender the sender
     * @return whether the sender can run this sub command
     * @since 1.13
     */
    public boolean canExecute(CommandSender sender) {
        return sender instanceof Player || !onlyPlayer;
    }

    /**
     * Gets the description of this sub command
     * 
     * @return the description
     * @since 1.13
     */
    public abstract String getDescription();

    /**
     * Called when this sub command is executed
     * <p>
     * The first argument {@code args[0]} is not the sub command itself, but the
     * argument after it (if entered)..
     * 
     * @param sender the sender
     * @param args   the arguments of the sub command
     * @since 1.13
     */
    public abstract void onExecute(CommandSender sender, String... args);

    /**
     * Called when a sender tab completes an argument in this sub command
     * <p>
     * The tab completion for the sub command itself is already handled. The first
     * argument {@code args[0]} is not the sub command itself, but the argument
     * after it (if entered)..
     * 
     * @param sender the command sender
     * @param args the arguments of the sub command
     * @return the tab completions
     * @since 1.13
     */
    public abstract List<String> onTabComplete(CommandSender sender, String... args);

}