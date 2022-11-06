package dev.lmke.mc.warps.commands;

import dev.lmke.mc.warps.annotations.HasPermission;
import dev.lmke.mc.warps.annotations.IsPlayerCommand;
import dev.lmke.mc.warps.annotations.SubCommand;
import dev.lmke.mc.warps.database.DAL;
import dev.lmke.mc.warps.database.Database;
import dev.lmke.mc.warps.DTO.POIObject;
import dev.lmke.mc.warps.services.EconomyService;
import dev.lmke.mc.warps.services.MapManagerService;
import dev.lmke.mc.warps.utils.CommandBase;
import dev.lmke.mc.warps.utils.ConfigManager;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.WriteResult;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.dizitart.no2.util.Iterables;

import java.util.ArrayList;
import java.util.List;

public class POICommand extends CommandBase {
    private final FileConfiguration locale = MessageLocaleManager.getConfig();

    public POICommand() {
        super();
    }

    @SubCommand("help")
    @HasPermission("lmke-warps.poi")
    public void help(CommandSender sender, Command command, String[] args) {
        List<String> warps = locale.getStringList("poi.help");
        String header = locale.getString("common.header");

        sender.sendMessage(header);
        sender.sendMessage(warps.toArray(new String[0]));

        int poiCount = DAL.getPlayerPOICount(((Player) sender).getUniqueId());

        if (EconomyService.POIEconomyEnabled()) {
            double create = EconomyService.getPOICreatePrice();
            double delete = EconomyService.getPOIDeletePrice();

            String refund = locale.getString("common.refund");

            String createPriceText = create >= 0 ? String.valueOf(create) : Math.abs(create) + " " + refund;
            String deletePriceText = delete >= 0 ? String.valueOf(delete) : Math.abs(delete) + " " + refund;

            sender.sendMessage("");

            sender.sendMessage(String.format(locale.getString("poi.help_economy.create"), createPriceText));
            sender.sendMessage(String.format(locale.getString("poi.help_economy.delete"), deletePriceText));

            int forFree = ConfigManager.getConfig().getInt("poi.for_free");

            if (forFree > 0) {
                int hasLeft = Math.max(forFree - poiCount, 0);

                String msg = locale.getString("common.for_free") + hasLeft + "/" + forFree;
                sender.sendMessage(msg);
            }
        }

        int limit = ConfigManager.getConfig().getInt("poi.limit");

        if (limit > 0) {
            int hasLeft = Math.max(limit - poiCount, 0);
            sender.sendMessage(String.format(locale.getString("common.limit"), hasLeft + "/" + limit));
        }
    }

    /**
     * Default method that get executed if no subcommand is selected
     */
    @Override
    @IsPlayerCommand
    @HasPermission("lmke-warps.poi")
    public void perform(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            this.help(sender, command, args);
            return;
        }

        POIObject poi = DAL.getPOI(args[0]);

        if (poi != null) {
            p.teleport(poi.location);
        } else {
            p.sendMessage(MessageLocaleManager.getText("errors.not_found"));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String[] args) {
        return new ArrayList<String>();
    }

    /**
     * List the warp points that the player has created
     */
    @SubCommand("list")
    @IsPlayerCommand
    @HasPermission("lmke-warps.poi.list")
    public void list(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        List<String> list = DAL.getPlayerPOIList(p.getUniqueId());

        int limit = ConfigManager.getConfig().getInt("poi.limit");

        String count = "(" + list.size() + "/" + limit + ")";

        p.sendMessage(locale.getString("common.prefix") + count + " " + String.join(", ", list));
    }

    /**
     * Create a point of interest
     */
    @SubCommand("create")
    @IsPlayerCommand
    @HasPermission("lmke-warps.poi.create")
    public void create(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(MessageLocaleManager.getText("errors.missing_args"));
            return;
        }

        if (DAL.getPOI(args[0]) != null) {
            p.sendMessage(MessageLocaleManager.getText("errors.already_exists"));
            return;
        }

        String pattern = ConfigManager.getConfig().getString("validation.pattern");
        int minLength = ConfigManager.getConfig().getInt("validation.minlength");
        int maxLength = ConfigManager.getConfig().getInt("validation.maxlength");
        String lengths = minLength + "-" + maxLength;

        // validate
        if (pattern != null && !args[0].matches(pattern)) {
            String msg = String.format(MessageLocaleManager.getText("errors.invalid_requirements"), lengths, pattern);
            p.sendMessage(MessageLocaleManager.getText("errors.invalid"));
            p.sendMessage(msg);
            return;
        }

        if (args[0].length() < minLength || args[0].length() >= maxLength) {
            String msg = String.format(MessageLocaleManager.getText("errors.invalid_requirements"), lengths, pattern);
            p.sendMessage(MessageLocaleManager.getText("errors.invalid"));
            p.sendMessage(msg);
            return;
        }

        int poiCount = DAL.getPlayerPOICount(p.getUniqueId());
        int limit = ConfigManager.getConfig().getInt("poi.limit");

        if (poiCount >= limit && limit > 0 && !p.hasPermission("lmke-warps.bypass.limit")) {
            p.sendMessage(MessageLocaleManager.getText("errors.limit_reached"));
            return;
        }

        if (EconomyService.POIEconomyEnabled() && poiCount >= ConfigManager.getConfig().getInt("poi.for_free") && !p.hasPermission("lmke-warps.bypass.economy")) {
            double price = EconomyService.getPOICreatePrice();

            if (EconomyService.playerHasMoney(p, price)) {
                EconomyService.modifyBalance(p, price);
            } else {
                p.sendMessage(MessageLocaleManager.getText("errors.no_money"));
                return;
            }
        }

        POIObject poi = new POIObject(args[0], p.getUniqueId(), p.getLocation());

        WriteResult res = Database.getRepo(POIObject.class).insert(poi);

        poi.id = Iterables.firstOrDefault(res);

        MapManagerService.addPOI(poi);

        p.sendMessage(MessageLocaleManager.getText("poi.created"));
    }

    /**
     * Delete a point of interest
     */
    @SubCommand("delete")
    @IsPlayerCommand
    @HasPermission("lmke-warps.poi.delete")
    public void delete(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(MessageLocaleManager.getText("errors.missing_args"));
            return;
        }

        POIObject poi = DAL.getPOI(args[0]);

        if (poi == null) {
            p.sendMessage(MessageLocaleManager.getText("errors.not_found"));
            return;
        }

        // Only can delete own pois except player has admin permission
        if (poi.player != p.getUniqueId() && !p.hasPermission("lmke-warps.admin")) {
            p.sendMessage(MessageLocaleManager.getText("errors.missing_permission"));
            return;
        }

        if (EconomyService.POIEconomyEnabled() && !p.hasPermission("lmke-warps.bypass.economy")) {
            double price = EconomyService.getPOIDeletePrice();

            if (EconomyService.playerHasMoney(p, price)) {
                EconomyService.modifyBalance(p, price);
            } else {
                p.sendMessage(MessageLocaleManager.getText("errors.no_money"));
                return;
            }
        }

        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        repo.remove(ObjectFilters.eq("_id", poi.id));

        MapManagerService.removePOI(poi);

        p.sendMessage(MessageLocaleManager.getText("poi.deleted"));
    }



}
