package dev.lmke.mc.warps.utils;

import dev.lmke.mc.warps.annotations.HasPermission;
import dev.lmke.mc.warps.annotations.IsPlayerCommand;
import dev.lmke.mc.warps.annotations.SubCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class CommandBase implements CommandExecutor, TabExecutor {

    private final Map<String, CommandBase> subCommandsMap = new HashMap<>();

    public CommandBase(CommandBase ...subCommands) {
        for (CommandBase subCommand : subCommands) {
            Class<?> subCommandClass = subCommand.getClass();

            if (subCommandClass.isAnnotationPresent(SubCommand.class)) {
                String name = subCommandClass.getAnnotation(SubCommand.class).value();
                subCommandsMap.put(name, subCommand);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        CommandBase cmd = null;
        Class<?> cmdClass = null;

        // Use subcommand if exists or use default
        if (args.length > 0 && subCommandsMap.containsKey(args[0])) {
            cmd = subCommandsMap.get(args[0]);

            // Remove first element because it's the subcommand
            args = Arrays.copyOfRange(args, 1, args.length);
        } else {
            cmd = this;
        }

        cmdClass = cmd.getClass();

        // Check if it is a player command
        if (cmdClass.isAnnotationPresent(IsPlayerCommand.class) && !(sender instanceof Player)) {
            return true;
        }

        // Check if player has permission
        if (cmdClass.isAnnotationPresent(HasPermission.class) && sender instanceof Player) {
            HasPermission annotation = cmdClass.getAnnotation(HasPermission.class);

            if (!sender.hasPermission(annotation.value())) {
                sender.sendMessage(MessageLocaleManager.getChatText("errors.missing_permission"));
                return true;
            }
        }

        // Perform command
        cmd.onCommand(sender, command, args);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String alias, String[] args) {

        if (args.length < 2) {
            List<String> values = new ArrayList<String>(subCommandsMap.keySet());

            List<String> res = this.onTabComplete(sender, command, args);

            if (res != null) {
                values.addAll(res);
            }

            return values;
        } else {
            CommandBase cmd;
            String[] newArgs;

            // Use subcommand if exists or use default perform method
            if (subCommandsMap.containsKey(args[0])) {
                cmd = subCommandsMap.get(args[0]);

                // Remove first element because it's the subcommand
                newArgs = Arrays.copyOfRange(args, 1, args.length);
            } else {
                return null;
            }

            List<String> res = cmd.onTabComplete(sender, command, newArgs);

            if (res != null && !res.isEmpty()) {
                return res;
            }

            return null;
        }
    }

    public boolean executeSubCommand(Class<? extends CommandBase> target, CommandSender sender, org.bukkit.command.Command command, String[] args) {
        for (CommandBase cmd : subCommandsMap.values()) {
            if (target.isInstance(cmd)) {
                return cmd.onCommand(sender, command, args);
            }
        }

        return false;
    }

    /**
     * Method that gets executed if the command is triggered without a subcommand
     */
    public abstract boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String[] args);
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String[] args) {
        return new ArrayList<String>();
    }
}
