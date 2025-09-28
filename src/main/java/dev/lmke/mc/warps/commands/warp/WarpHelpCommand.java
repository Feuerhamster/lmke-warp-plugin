package dev.lmke.mc.warps.commands.warp;

import dev.lmke.mc.warps.annotations.HasPermission;
import dev.lmke.mc.warps.annotations.SubCommand;
import dev.lmke.mc.warps.database.DAL;
import dev.lmke.mc.warps.services.EconomyService;
import dev.lmke.mc.warps.utils.CommandBase;
import dev.lmke.mc.warps.utils.ConfigManager;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@SubCommand("help")
@HasPermission("lmke-warps.warp")
public class WarpHelpCommand extends CommandBase {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String[] args) {
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

        return true;
    }
}
