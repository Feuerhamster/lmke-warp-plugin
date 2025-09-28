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
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.List;

@SubCommand("delete")
@IsPlayerCommand
@HasPermission("lmke-warps.poi.delete")
public class PoiDeleteCommand extends CommandBase {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.missing_args"));
            return true;
        }

        POIObject poi = DAL.getPOI(args[0]);

        if (poi == null) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.not_found"));
            return true;
        }

        // Only can delete own pois except player has admin permission
        if (!poi.player.equals(p.getUniqueId()) && !p.hasPermission("lmke-warps.admin")) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.missing_permission"));
            return true;
        }

        if (EconomyService.POIEconomyEnabled() && !p.hasPermission("lmke-warps.bypass.economy")) {
            double price = EconomyService.getPOIDeletePrice();

            if (EconomyService.playerHasMoney(p, price)) {
                EconomyService.modifyBalance(p, price);
            } else {
                p.sendMessage(MessageLocaleManager.getChatText("errors.no_money"));
                return true;
            }
        }

        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        repo.remove(ObjectFilters.eq("_id", poi.id));

        MapManagerService.removePOI(poi);

        p.sendMessage(MessageLocaleManager.getChatText("poi.deleted"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        return DAL.getPlayerPOIList(p.getUniqueId());
    }
}
