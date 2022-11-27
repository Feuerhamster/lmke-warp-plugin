package dev.lmke.mc.warps.commands;

import dev.lmke.mc.warps.DTO.WarpPoint;
import dev.lmke.mc.warps.annotations.HasPermission;
import dev.lmke.mc.warps.annotations.IsPlayerCommand;
import dev.lmke.mc.warps.annotations.SubCommand;
import dev.lmke.mc.warps.annotations.TabComplete;
import dev.lmke.mc.warps.database.DAL;
import dev.lmke.mc.warps.database.Database;
import dev.lmke.mc.warps.services.EconomyService;
import dev.lmke.mc.warps.utils.CommandBase;
import dev.lmke.mc.warps.utils.ConfigManager;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.List;

public class WarpCommand extends CommandBase {

    public WarpCommand() {
        super();
    }

    @SubCommand("help")
    @HasPermission("lmke-warps.warp")
    public void help(CommandSender sender, Command command, String[] args) {
        List<String> warps = MessageLocaleManager.getConfig().getStringList("warp.help");
        String header = MessageLocaleManager.getTextRaw("common.header");

        sender.sendMessage(header);
        sender.sendMessage(warps.toArray(new String[0]));

        int warpCount = DAL.getPlayerWarpCount(((Player) sender).getUniqueId());

        if (EconomyService.warpEconomyEnabled()) {
            double create = EconomyService.getWarpCreatePrice();
            double delete = EconomyService.getWarpDeletePrice();

            String refund = MessageLocaleManager.getTextRaw("common.refund");

            String createPriceText = create >= 0 ? String.valueOf(create) : Math.abs(create) + " " + refund;
            String deletePriceText = delete >= 0 ? String.valueOf(delete) : Math.abs(delete) + " " + refund;

            if (sender.hasPermission("lmke-warps.bypass.economy")) {
                createPriceText = MessageLocaleManager.getTextRaw("common.free");
                deletePriceText = MessageLocaleManager.getTextRaw("common.free");
            }

            sender.sendMessage("");

            sender.sendMessage(String.format(MessageLocaleManager.getTextRaw("warp.help_economy.create"), createPriceText));
            sender.sendMessage(String.format(MessageLocaleManager.getTextRaw("warp.help_economy.delete"), deletePriceText));

            int forFree = ConfigManager.getConfig().getInt("warp.for_free");

            if (forFree > 0) {
                int hasLeft = Math.max(forFree - warpCount, 0);

                String msg = MessageLocaleManager.getTextRaw("common.for_free") + hasLeft + "/" + forFree;
                sender.sendMessage(msg);
            }
        }

        int limit = ConfigManager.getConfig().getInt("warp.limit");

        if (limit > 0 && !sender.hasPermission("lmke-warps.bypass.limit")) {
            int hasLeft = Math.max(limit - warpCount, 0);
            sender.sendMessage(String.format(MessageLocaleManager.getTextRaw("common.limit"), hasLeft + "/" + limit));
        } else if(sender.hasPermission("lmke-warps.bypass.limit")) {
            sender.sendMessage(String.format(MessageLocaleManager.getTextRaw("common.limit"), MessageLocaleManager.getTextRaw("common.bypass")));
        }
    }

    /**
     * Default method that get executed if no subcommand is selected
     */
    @Override
    @IsPlayerCommand
    @HasPermission("lmke-warps.warp")
    public void perform(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            this.help(sender, command, args);
            return;
        }

        WarpPoint wp = DAL.getPlayerWarpPoint(args[0], p.getUniqueId());

        if (wp != null) {
            p.teleport(wp.location);
        } else {
            p.sendMessage(MessageLocaleManager.getChatText("errors.not_found"));
        }
    }

    /**
     * Autocomplete for perform and delete method
     */
    @Override
    @TabComplete("delete")
    public List<String> onTabComplete(CommandSender sender, Command command, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        Player p = (Player) sender;

        return DAL.getPlayerWarpsList(p.getUniqueId());
    }

    /**
     * List a player's warp points
     */
    @SubCommand("list")
    @IsPlayerCommand
    @HasPermission("lmke-warps.warp.list")
    public void list(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        List<String> wpList = DAL.getPlayerWarpsList(p.getUniqueId());

        int limit = ConfigManager.getConfig().getInt("warp.limit");

        String count = "(" + wpList.size() + "/" + limit + ")";

        p.sendMessage(MessageLocaleManager.getTextRaw("common.prefix") + count + " " + String.join(", ", wpList));
    }

    /**
     * Create a warp point
     */
    @SubCommand("create")
    @IsPlayerCommand
    @HasPermission("lmke-warps.warp.create")
    public void create(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.missing_args"));
            return;
        }

        if (DAL.getPlayerWarpPoint(args[0], p.getUniqueId()) != null) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.already_exists"));
            return;
        }

        String pattern = ConfigManager.getConfig().getString("validation.pattern");
        int minLength = ConfigManager.getConfig().getInt("validation.minlength");
        int maxLength = ConfigManager.getConfig().getInt("validation.maxlength");
        String lengths = minLength + "-" + maxLength;

        // validate
        if (pattern != null && !args[0].matches(pattern)) {
            String msg = String.format(MessageLocaleManager.getChatText("errors.invalid_requirements"), lengths, pattern);
            p.sendMessage(MessageLocaleManager.getChatText("errors.invalid"));
            p.sendMessage(msg);
            return;
        }

        if (args[0].length() < minLength || args[0].length() >= maxLength) {
            String msg = String.format(MessageLocaleManager.getChatText("errors.invalid_requirements"), lengths, pattern);
            p.sendMessage(MessageLocaleManager.getChatText("errors.invalid"));
            p.sendMessage(msg);
            return;
        }

        int warpCount = DAL.getPlayerWarpCount(p.getUniqueId());
        int limit = ConfigManager.getConfig().getInt("warp.limit");

        if (warpCount >= limit && limit > 0 && !p.hasPermission("lmke-warps.bypass.limit")) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.limit_reached"));
            return;
        }

        if (EconomyService.warpEconomyEnabled() && warpCount >= ConfigManager.getConfig().getInt("warp.for_free") && !p.hasPermission("lmke-warps.bypass.economy")) {
            double price = EconomyService.getWarpCreatePrice();

            if (EconomyService.playerHasMoney(p, price)) {
                EconomyService.modifyBalance(p, price);
            } else {
                p.sendMessage(MessageLocaleManager.getChatText("errors.no_money"));
                return;
            }
        }

        WarpPoint wp = new WarpPoint(args[0], p.getUniqueId(), p.getLocation());

        Database.getRepo(WarpPoint.class).insert(wp);

        p.sendMessage(MessageLocaleManager.getChatText("warp.created"));
    }

    /**
     * Delete a warp point
     */
    @SubCommand("delete")
    @IsPlayerCommand
    @HasPermission("lmke-warps.warp.delete")
    public void delete(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.missing_args"));
            return;
        }

        WarpPoint wp = DAL.getPlayerWarpPoint(args[0], p.getUniqueId());

        if (wp != null) {
            if (EconomyService.warpEconomyEnabled() && !p.hasPermission("lmke-warps.bypass.economy")) {
                double price = EconomyService.getWarpDeletePrice();

                if (EconomyService.playerHasMoney(p, price)) {
                    EconomyService.modifyBalance(p, price);
                } else {
                    p.sendMessage(MessageLocaleManager.getChatText("errors.no_money"));
                    return;
                }
            }

            ObjectRepository<WarpPoint> repo = Database.getRepo(WarpPoint.class);
            repo.remove(ObjectFilters.eq("_id", wp.id));

            p.sendMessage(MessageLocaleManager.getChatText("warp.deleted"));
        } else {
            p.sendMessage(MessageLocaleManager.getChatText("errors.not_found"));
        }
    }
}
