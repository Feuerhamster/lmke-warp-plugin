package dev.lmke.mc.warps.commands.warp;

import dev.lmke.mc.warps.DTO.WarpPoint;
import dev.lmke.mc.warps.annotations.HasPermission;
import dev.lmke.mc.warps.annotations.IsPlayerCommand;
import dev.lmke.mc.warps.annotations.SubCommand;
import dev.lmke.mc.warps.database.DAL;
import dev.lmke.mc.warps.database.Database;
import dev.lmke.mc.warps.services.EconomyService;
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
@HasPermission("lmke-warps.warp.delete")
public class WarpDeleteCommand extends CommandBase {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.missing_args"));
            return true;
        }

        WarpPoint wp = DAL.getPlayerWarpPoint(args[0], p.getUniqueId());

        if (wp != null) {
            if (EconomyService.warpEconomyEnabled() && !p.hasPermission("lmke-warps.bypass.economy")) {
                double price = EconomyService.getWarpDeletePrice();

                if (EconomyService.playerHasMoney(p, price)) {
                    EconomyService.modifyBalance(p, price);
                } else {
                    p.sendMessage(MessageLocaleManager.getChatText("errors.no_money"));
                    return true;
                }
            }

            ObjectRepository<WarpPoint> repo = Database.getRepo(WarpPoint.class);
            repo.remove(ObjectFilters.eq("_id", wp.id));

            p.sendMessage(MessageLocaleManager.getChatText("warp.deleted"));
        } else {
            p.sendMessage(MessageLocaleManager.getChatText("errors.not_found"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        return DAL.getPlayerWarpsList(p.getUniqueId());
    }
}
