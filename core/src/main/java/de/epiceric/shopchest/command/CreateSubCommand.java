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
import de.epiceric.shopchest.api.event.ShopPreCreateEvent;
import de.epiceric.shopchest.api.player.ShopPlayer;

public class CreateSubCommand extends SubCommand {
    private static final List<String> AMOUNT_ARGS = Arrays.asList("amount", "count", "number", "num", "n");
    private static final List<String> BUY_PRICE_ARGS = Arrays.asList("buy-price", "buy_price", "buyprice", "price-buy", "price_buy", "buy", "b");
    private static final List<String> SELL_PRICE_ARGS = Arrays.asList("sell-price", "sell_price", "sellprice", "price-sell", "price_sell", "sell", "s");
    private static final List<String> ADMIN_ARGS = Arrays.asList("admin", "admin-shop", "admin_shop", "adminshop");

    private ShopChest plugin;

    CreateSubCommand(ShopChest plugin) {
        super("create", true);
        this.plugin = plugin;
    }

    @Override
    public String getDescription() {
        return "Create a shop";
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
        List<String> amountArgs = getNamedArgs(AMOUNT_ARGS, args);
        List<String> buyPriceArgs = getNamedArgs(BUY_PRICE_ARGS, args);
        List<String> sellPriceArgs = getNamedArgs(SELL_PRICE_ARGS, args);
        
        if (amountArgs.isEmpty()) {
            sender.sendMessage("§cYou have to set an amount."); // i18n
            return;
        }
        
        if (buyPriceArgs.isEmpty() && sellPriceArgs.isEmpty()) {
            sender.sendMessage("§cYou have to set either a buy price or a sell price."); // i18n
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

        int amount;
        try {
            amount = Integer.parseInt(amountArgs.get(0).split("=")[1]);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            sender.sendMessage("§cThe amount you entered is not a valid number."); // i18n
            return;
        }

        double buyPrice = 0;
        if (buyPriceArgs.size() > 0) {
            try {
                buyPrice = Double.parseDouble(buyPriceArgs.get(0).split("=")[1]);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                sender.sendMessage("§cThe buy price you entered is not a valid number."); // i18n
                return;
            }
        }

        double sellPrice = 0;
        if (sellPriceArgs.size() > 0) {
            try {
                sellPrice = Double.parseDouble(sellPriceArgs.get(0).split("=")[1]);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                sender.sendMessage("§cThe sell price you entered is not a valid number."); // i18n
                return;
            }
        }

        boolean admin = !getNamedArgs(ADMIN_ARGS, args).isEmpty();
        ShopPlayer player = plugin.wrapPlayer((Player) sender);
        ItemStack item = getItemInHand(player.getBukkitPlayer());

        plugin.getServer().getPluginManager()
                .callEvent(new ShopPreCreateEvent(player, item, amount, buyPrice, sellPrice, admin));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String... args) {
        String[] argsWithoutLast = Arrays.copyOf(args, Math.max(0, args.length - 1));

        boolean isAmountSet = !getNamedArgs(AMOUNT_ARGS, argsWithoutLast).isEmpty();
        boolean isBuyPriceSet = !getNamedArgs(BUY_PRICE_ARGS, argsWithoutLast).isEmpty();
        boolean isSellPriceSet = !getNamedArgs(SELL_PRICE_ARGS, argsWithoutLast).isEmpty();
        boolean isAdminSet = !getNamedArgs(ADMIN_ARGS, argsWithoutLast).isEmpty();

        String lastArg = args[args.length - 1].toLowerCase(Locale.US);

        List<String> ret = new ArrayList<>();

        if (!isAmountSet) {
            AMOUNT_ARGS.stream().filter(arg -> arg.startsWith(lastArg)).findFirst().ifPresent(arg -> ret.add(arg));
        }

        if (!isBuyPriceSet) {
            BUY_PRICE_ARGS.stream().filter(arg -> arg.startsWith(lastArg)).findFirst().ifPresent(arg -> ret.add(arg));
        }

        if (!isSellPriceSet) {
            SELL_PRICE_ARGS.stream().filter(arg -> arg.startsWith(lastArg)).findFirst().ifPresent(arg -> ret.add(arg));
        }

        // Add equals signs
        for (int i = 0; i < ret.size(); i++) {
            ret.set(i, ret.get(i) + "=");
        }

        if (!isAdminSet && sender.hasPermission("shopchest.create.admin")) {
            ADMIN_ARGS.stream().filter(arg -> arg.startsWith(lastArg)).findFirst().ifPresent(arg -> ret.add(arg));
        }

        return ret;
    }
}