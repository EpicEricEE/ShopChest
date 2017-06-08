package de.epiceric.shopchest.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public abstract class ShopSubCommand {
    private String name;
    private boolean playerCommand;
    private CommandExecutor executor;
    private TabCompleter tabCompleter;

    public ShopSubCommand(String name, boolean playerCommand, CommandExecutor executor, TabCompleter tabCompleter) {
        this.name = name;
        this.playerCommand = playerCommand;
        this.executor = executor;
        this.tabCompleter = tabCompleter;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns whether the command can only be used by players, not by the console
     */
    public boolean isPlayerCommand() {
        return playerCommand;
    }

    /**
     * Execute the sub command
     * @param sender Sender of the command
     * @param args Arguments of the command ({@code args[0]} is the sub command's name)
     * @return Whether the sender should be sent the help message
     */
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        return executor.onCommand(sender, command, label, args);
    }

    /**
     * <p>Returns a list of tab completions for the sub command</p>
     * The main sub command will be tab completed by ShopChest
     * @param sender Sender of the command
     * @param args Arguments of the command ({@code args[0]} is the sub command's name)
     * @return A list of tab completions (may be an empty list)
     */
    public List<String> getTabCompletions(CommandSender sender, Command command, String label, String[] args) {
        if (tabCompleter == null) {
            return new ArrayList<>();
        }

        return tabCompleter.onTabComplete(sender, command, label, args);
    }

    /**
     * Returns the help message for the command.
     * @param sender Sender to receive the help message
     */
    public abstract String getHelpMessage(CommandSender sender);
}
