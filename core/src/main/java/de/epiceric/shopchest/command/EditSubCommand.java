package de.epiceric.shopchest.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.epiceric.shopchest.api.ShopChest;
import de.epiceric.shopchest.api.command.SubCommand;
import de.epiceric.shopchest.api.event.ShopPreEditEvent;
import de.epiceric.shopchest.api.player.ShopPlayer;

public class EditSubCommand extends SubCommand {
    private static final List<String> ITEM_ARGS = Arrays.asList("set-item", "select-item", "edit-item", "item", "i");
    private static final List<String> AMOUNT_ARGS = Arrays.asList("amount", "count", "number", "num", "n");
    private static final List<String> BUY_PRICE_ARGS = Arrays.asList("buy-price", "buyprice", "price-buy", "buy", "b");
    private static final List<String> SELL_PRICE_ARGS = Arrays.asList("sell-price", "sellprice", "price-sell", "sell", "s");

    private ShopChest plugin;

    EditSubCommand(ShopChest plugin) {
        super("edit", true);
        this.plugin = plugin;
    }

    @Override
    public String getDescription() {
        return "Edit a shop";
    }

    private ItemStack getItemInHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType() != Material.AIR) {
            return item.clone();
        }

        item = player.getInventory().getItemInOffHand();
        if (item != null && item.getType() != Material.AIR) {
            return item.clone();
        }

        return null;
    }

    private List<String> getNamedArgs(List<String> lookup, String... args) {
        return Arrays.stream(args)
                .filter(arg -> lookup.contains(arg.toLowerCase(Locale.US).split("=")[0]))
                .collect(Collectors.toList());
    }

    @Override
    public void onExecute(CommandSender sender, String... args) {
        List<String> itemArgs = getNamedArgs(ITEM_ARGS, args);
        List<String> amountArgs = getNamedArgs(AMOUNT_ARGS, args);
        List<String> buyPriceArgs = getNamedArgs(BUY_PRICE_ARGS, args);
        List<String> sellPriceArgs = getNamedArgs(SELL_PRICE_ARGS, args);
        
        if (itemArgs.isEmpty() && amountArgs.isEmpty() && buyPriceArgs.isEmpty() && sellPriceArgs.isEmpty()) {
            sender.sendMessage("§cYou have to set either the amount or a price, or that you want to set the item."); // i18n
            return;
        }

        if (amountArgs.size() > 1) {
            sender.sendMessage("§cYou can only set one amount."); // i18n
            return;
        }

        if (buyPriceArgs.size() > 1) {
            sender.sendMessage("§cYou can only set one buy price."); // i18n
            return;
        }

        if (sellPriceArgs.size() > 1) {
            sender.sendMessage("§cYou can only set one sell price."); // i18n
            return;
        }

        int amount = -1;
        try {
            amount = Integer.parseInt(amountArgs.get(0).split("=")[1]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            sender.sendMessage("§cThe amount you entered is not valid."); // i18n
            return;
        }

        double buyPrice = -1;
        if (buyPriceArgs.size() > 0) {
            try {
                buyPrice = Double.parseDouble(buyPriceArgs.get(0).split("=")[1]);
                if (buyPrice < 0) throw new NumberFormatException();
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                sender.sendMessage("§cThe buy price you entered is not valid."); // i18n
                return;
            }
        }

        double sellPrice = -1;
        if (sellPriceArgs.size() > 0) {
            try {
                sellPrice = Double.parseDouble(sellPriceArgs.get(0).split("=")[1]);
                if (sellPrice < 0) throw new NumberFormatException();
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                sender.sendMessage("§cThe sell price you entered is not valid."); // i18n
                return;
            }
        }

        ShopPlayer player = plugin.wrapPlayer((Player) sender);
        ItemStack item = itemArgs.size() > 0 ? getItemInHand(player.getBukkitPlayer()) : null;

        plugin.getServer().getPluginManager().callEvent(
                    new ShopPreEditEvent(player, item, amount, buyPrice, sellPrice, itemArgs.size() > 0));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        String[] argsWithoutLast = Arrays.copyOf(args, Math.max(0, args.length - 1));

        boolean isItemSet = !getNamedArgs(ITEM_ARGS, argsWithoutLast).isEmpty();
        boolean isAmountSet = !getNamedArgs(AMOUNT_ARGS, argsWithoutLast).isEmpty();
        boolean isBuyPriceSet = !getNamedArgs(BUY_PRICE_ARGS, argsWithoutLast).isEmpty();
        boolean isSellPriceSet = !getNamedArgs(SELL_PRICE_ARGS, argsWithoutLast).isEmpty();

        String lastArg = args[args.length - 1].toLowerCase(Locale.US);

        List<String> ret = new ArrayList<>();

        if (!isItemSet) {
            ITEM_ARGS.stream().filter(arg -> arg.startsWith(lastArg)).findFirst().ifPresent(ret::add);
        }

        if (!isAmountSet) {
            AMOUNT_ARGS.stream().filter(arg -> arg.startsWith(lastArg)).findFirst().ifPresent(ret::add);
        }

        if (!isBuyPriceSet) {
            BUY_PRICE_ARGS.stream().filter(arg -> arg.startsWith(lastArg)).findFirst().ifPresent(ret::add);
        }

        if (!isSellPriceSet) {
            SELL_PRICE_ARGS.stream().filter(arg -> arg.startsWith(lastArg)).findFirst().ifPresent(ret::add);
        }

        // Add equals signs
        ret.replaceAll(arg -> arg + "=");

        return ret;
    }
}