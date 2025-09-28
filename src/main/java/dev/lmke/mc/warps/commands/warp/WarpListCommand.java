package dev.lmke.mc.warps.commands.warp;

import dev.lmke.mc.warps.annotations.HasPermission;
import dev.lmke.mc.warps.annotations.IsPlayerCommand;
import dev.lmke.mc.warps.annotations.SubCommand;
import dev.lmke.mc.warps.database.DAL;
import dev.lmke.mc.warps.utils.CommandBase;
import dev.lmke.mc.warps.utils.ConfigManager;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@SubCommand("list")
@IsPlayerCommand
@HasPermission("lmke-warps.warp.list")
public class WarpListCommand extends CommandBase {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        List<String> wpList = DAL.getPlayerWarpsList(p.getUniqueId());

        int limit = ConfigManager.getConfig().getInt("warp.limit");

        String count = "(" + wpList.size() + "/" + limit + ")";

        p.sendMessage(MessageLocaleManager.getTextRaw("common.prefix") + count + " " + String.join(", ", wpList));
        return true;
    }
}
