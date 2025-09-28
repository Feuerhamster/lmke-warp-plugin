package dev.lmke.mc.warps.commands;

import dev.lmke.mc.warps.DTO.WarpPoint;
import dev.lmke.mc.warps.commands.warp.WarpCreateCommand;
import dev.lmke.mc.warps.commands.warp.WarpDeleteCommand;
import dev.lmke.mc.warps.commands.warp.WarpHelpCommand;
import dev.lmke.mc.warps.commands.warp.WarpListCommand;
import dev.lmke.mc.warps.database.DAL;
import dev.lmke.mc.warps.utils.CommandBase;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WarpCommand extends CommandBase {

    public WarpCommand() {
        super(
            new WarpHelpCommand(),
            new WarpListCommand(),
            new WarpCreateCommand(),
            new WarpDeleteCommand()
        );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            return this.executeSubCommand(WarpHelpCommand.class, sender, command, args);
        }

        WarpPoint wp = DAL.getPlayerWarpPoint(args[0], p.getUniqueId());

        if (wp != null) {
            p.teleport(wp.location);
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
