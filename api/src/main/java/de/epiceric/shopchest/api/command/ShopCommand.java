package de.epiceric.shopchest.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

/**
 * Represents the plugin's main command
 * 
 * @since 1.13
 */
public abstract class ShopCommand {
    private List<SubCommand> subCommands = new ArrayList<>();

    /**
     * Gets the command's name
     * 
     * @return the name
     * @since 1.13
     */
    public abstract String getName();

    /**
     * Adds a sub command to this command
     * 
     * @param subCommand the sub command
     * @return {@code true} if the sub command has been registered, {@code false} if
     *         the name has already been taken
     * @since 1.13
     */
    public final boolean addSubCommand(SubCommand subCommand) {
        boolean nameTaken = subCommands.stream().filter(sub -> sub.getName().equalsIgnoreCase(subCommand.getName()))
                .findAny().isPresent();

        if (nameTaken) {
            return false;
        }

        this.subCommands.add(subCommand);
        return true;
    }

    /**
     * Removes a sub command from this command
     * 
     * @param subCommand the sub command
     * @since 1.13
     */
    public final void removeSubCommand(SubCommand subCommand) {
        this.subCommands.remove(subCommand);
    }

    /**
     * Called when this command is executed
     * <p>
     * Sub commands are handled here and their respective {@code onExecute} method
     * is called from here.
     * <p>
     * The first argument {@code args[0]} is the name of the sub command (if
     * entered).
     * 
     * @param sender the command sender
     * @param args   the arguments
     * @since 1.13
     */
    public final void onExecute(CommandSender sender, String... args) {
        if (args.length > 0) {
            Optional<SubCommand> optional = subCommands.stream()
                    .filter(sub -> sub.canExecute(sender) && sub.isPermitted(sender))
                    .filter(sub -> sub.getName().equalsIgnoreCase(args[0])).findAny();

            if (!optional.isPresent()) {
                sendUsage(sender);
                return;
            }

            optional.ifPresent(sub -> {
                if (args.length > 1) {
                    sub.onExecute(sender, Arrays.copyOfRange(args, 1, args.length));
                } else {
                    sub.onExecute(sender);
                }
            });
        } else {
           sendUsage(sender);
        }
    }

    /**
     * Called when a sender tab completes an argument in this command
     * <p>
     * Only tab completion for the name of the sub command is handled here.
     * Everything else has to be implemented in the sub commands respective
     * {@code onTabComplete} method.
     * <p>
     * The first argument {@code args[0]} is the name of the sub command (if
     * entered).
     * 
     * @param sender the command sender
     * @param args the arguments
     * @return the tab completions
     * @since 1.13
     */
    public final List<String> onTabComplete(CommandSender sender, String... args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(sub -> sub.canExecute(sender) && sub.isPermitted(sender))
                    .filter(sub -> sub.getName().toLowerCase(Locale.US).startsWith(args[0].toLowerCase(Locale.US)))
                    .map(SubCommand::getName).collect(Collectors.toList());
        } else if (args.length > 1) {
            return subCommands.stream()
                    .filter(sub -> sub.canExecute(sender) && sub.isPermitted(sender))
                    .filter(sub -> sub.getName().equalsIgnoreCase(args[0])).findAny()
                    .map(sub -> sub.onTabComplete(sender, args))
                    .orElse(new ArrayList<>());
        }

        return new ArrayList<>();
    }
    
    /**
     * Sends the help messages for all sub commands to the given sender
     * 
     * @param sender the command sender
     * @since 1.13
     */
    public void sendUsage(CommandSender sender) {
        // Use same help format as in default bukkit help message
        sender.sendMessage("§e--------- §fHelp: ShopChest §e-----------------------");
        sender.sendMessage("§7Below is a list of all ShopChest commands:");
        subCommands.stream().filter(sub -> sub.canExecute(sender) && sub.isPermitted(sender))
                .forEach(sub -> sender.sendMessage(String.format("§6/%s %s: §f%s", getName(), sub.getName(),
                        sub.getDescription())));
    }

}