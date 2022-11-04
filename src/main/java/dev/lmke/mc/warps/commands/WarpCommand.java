package dev.lmke.mc.warps.commands;

import dev.lmke.mc.warps.annotations.IsPlayerCommand;
import dev.lmke.mc.warps.annotations.SubCommand;
import dev.lmke.mc.warps.annotations.TabComplete;
import dev.lmke.mc.warps.database.Database;
import dev.lmke.mc.warps.database.WarpPoint;
import dev.lmke.mc.warps.utils.CommandBase;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.dizitart.no2.WriteResult;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WarpCommand extends CommandBase {
    private final FileConfiguration locale = MessageLocaleManager.getConfig();

    public WarpCommand() {
        super();
    }

    @SubCommand("help")
    public void help(CommandSender sender, Command command, String[] args) {
        List<String> warps = locale.getStringList("warp.help");
        String header = locale.getString("common.header");

        sender.sendMessage(header);
        sender.sendMessage(warps.toArray(new String[0]));
    }

    /**
     * Default method that get executed if no subcommand is selected
     */
    @Override
    @IsPlayerCommand
    public void perform(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(MessageLocaleManager.getText("errors.missing_args"));
            return;
        }

        WarpPoint wp = this.getPlayerWarpPoint(args[0], p.getUniqueId());

        if (wp != null) {
            p.teleport(wp.location);
        } else {
            p.sendMessage(MessageLocaleManager.getText("errors.not_found"));
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

        return this.getPlayerWarpsList(p.getUniqueId());
    }

    /**
     * Delete a warp point
     */
    @SubCommand("list")
    @IsPlayerCommand
    public void list(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        List<String> wpList = this.getPlayerWarpsList(p.getUniqueId());

        String count = "(" + wpList.size() + ")";

        p.sendMessage(locale.getString("common.prefix") + count + " " + String.join(", ", wpList));
    }

    /**
     * Create a warp point
     */
    @SubCommand("create")
    @IsPlayerCommand
    public void create(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(locale.getString("common.prefix") + locale.getString("errors.missing_args"));
            return;
        }

        WarpPoint wp = new WarpPoint(args[0], p.getUniqueId(), p.getLocation());

        Database.getRepo(WarpPoint.class).insert(wp);

        p.sendMessage(locale.getString("common.prefix") + locale.getString("warp.created"));
    }

    /**
     * Delete a warp point
     */
    @SubCommand("delete")
    @IsPlayerCommand
    public void delete(CommandSender sender, Command command, String[] args) {
        Player p = (Player) sender;

        if (args.length < 1) {
            p.sendMessage(MessageLocaleManager.getText("errors.missing_args"));
            return;
        }

        WarpPoint wp = this.getPlayerWarpPoint(args[0], p.getUniqueId());

        if (wp != null) {
            ObjectRepository<WarpPoint> repo = Database.getRepo(WarpPoint.class);
            int res = repo.remove(ObjectFilters.eq("_id", wp.id)).getAffectedCount();

            p.sendMessage(MessageLocaleManager.getText("warp.deleted"));
        } else {
            p.sendMessage(MessageLocaleManager.getText("errors.not_found"));
        }
    }

    private List<String> getPlayerWarpsList(UUID player) {
        ObjectRepository<WarpPoint> repo = Database.getRepo(WarpPoint.class);
        Cursor<WarpPoint> cursor = repo.find(
                ObjectFilters.and(
                        ObjectFilters.eq("player", player.toString())
                )
        );

        return cursor.toList().stream().map(x -> x.name).collect(Collectors.toList());
    }

    private WarpPoint getPlayerWarpPoint(String name, UUID player) {
        ObjectRepository<WarpPoint> repo = Database.getRepo(WarpPoint.class);
        Cursor<WarpPoint> cursor = repo.find(
                ObjectFilters.and(
                        ObjectFilters.eq("player", player.toString()),
                        ObjectFilters.eq("name", name)
                )
        );

        return cursor.firstOrDefault();
    }
}
