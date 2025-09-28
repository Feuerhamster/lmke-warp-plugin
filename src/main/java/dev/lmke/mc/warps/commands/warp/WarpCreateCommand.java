package dev.lmke.mc.warps.commands.warp;

import dev.lmke.mc.warps.DTO.WarpPoint;
import dev.lmke.mc.warps.annotations.HasPermission;
import dev.lmke.mc.warps.annotations.IsPlayerCommand;
import dev.lmke.mc.warps.annotations.SubCommand;
import dev.lmke.mc.warps.database.DAL;
import dev.lmke.mc.warps.database.Database;
import dev.lmke.mc.warps.services.EconomyService;
import dev.lmke.mc.warps.utils.CommandBase;
import dev.lmke.mc.warps.utils.ConfigManager;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SubCommand("create")
@IsPlayerCommand
@HasPermission("lmke-warps.warp.create")
public class WarpCreateCommand extends CommandBase {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.missing_args"));
            return true;
        }

        if (DAL.getPlayerWarpPoint(args[0], p.getUniqueId()) != null) {
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

        int warpCount = DAL.getPlayerWarpCount(p.getUniqueId());
        int limit = ConfigManager.getConfig().getInt("warp.limit");

        if (warpCount >= limit && limit > 0 && !p.hasPermission("lmke-warps.bypass.limit")) {
            p.sendMessage(MessageLocaleManager.getChatText("errors.limit_reached"));
            return true;
        }

        if (EconomyService.warpEconomyEnabled() && warpCount >= ConfigManager.getConfig().getInt("warp.for_free") && !p.hasPermission("lmke-warps.bypass.economy")) {
            double price = EconomyService.getWarpCreatePrice();

            if (EconomyService.playerHasMoney(p, price)) {
                EconomyService.modifyBalance(p, price);
            } else {
                p.sendMessage(MessageLocaleManager.getChatText("errors.no_money"));
                return true;
            }
        }

        WarpPoint wp = new WarpPoint(args[0], p.getUniqueId(), p.getLocation());

        Database.getRepo(WarpPoint.class).insert(wp);

        p.sendMessage(MessageLocaleManager.getChatText("warp.created"));

        return true;
    }
}
