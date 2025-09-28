package dev.lmke.mc.warps.commands.poi;

import dev.lmke.mc.warps.DTO.POIObject;
import dev.lmke.mc.warps.annotations.HasPermission;
import dev.lmke.mc.warps.annotations.IsPlayerCommand;
import dev.lmke.mc.warps.annotations.SubCommand;
import dev.lmke.mc.warps.database.DAL;
import dev.lmke.mc.warps.database.Database;
import dev.lmke.mc.warps.services.EconomyService;
import dev.lmke.mc.warps.services.MapManagerService;
import dev.lmke.mc.warps.utils.CommandBase;
import dev.lmke.mc.warps.utils.ConfigManager;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dizitart.no2.WriteResult;
import org.dizitart.no2.util.Iterables;

@SubCommand("create")
@IsPlayerCommand
@HasPermission("lmke-warps.poi.create")
public class PoiCreateCommand extends CommandBase {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.missing_args"));
            return true;
        }

        if (DAL.getPOI(args[0]) != null) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.already_exists"));
            return true;
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
            return true;
        }

        if (args[0].length() < minLength || args[0].length() >= maxLength) {
            String msg = String.format(MessageLocaleManager.getChatText("errors.invalid_requirements"), lengths, pattern);
            p.sendMessage(MessageLocaleManager.getChatText("errors.invalid"));
            p.sendMessage(msg);
            return true;
        }

        int poiCount = DAL.getPlayerPOICount(p.getUniqueId());
        int limit = ConfigManager.getConfig().getInt("poi.limit");

        if (poiCount >= limit && limit > 0 && !p.hasPermission("lmke-warps.bypass.limit")) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.limit_reached"));
            return true;
        }

        if (EconomyService.POIEconomyEnabled() && poiCount >= ConfigManager.getConfig().getInt("poi.for_free") && !p.hasPermission("lmke-warps.bypass.economy")) {
            double price = EconomyService.getPOICreatePrice();

            if (EconomyService.playerHasMoney(p, price)) {
                EconomyService.modifyBalance(p, price);
            } else {
                p.sendMessage(MessageLocaleManager.getChatText("errors.no_money"));
                return true;
            }
        }

        POIObject poi = new POIObject(args[0], p.getUniqueId(), p.getLocation());

        WriteResult res = Database.getRepo(POIObject.class).insert(poi);

        poi.id = Iterables.firstOrDefault(res);

        MapManagerService.addPOI(poi);

        p.sendMessage(MessageLocaleManager.getChatText("poi.created"));
        return true;
    }
}
