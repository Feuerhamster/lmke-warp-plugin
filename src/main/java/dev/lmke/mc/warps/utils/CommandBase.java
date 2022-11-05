package dev.lmke.mc.warps.utils;

import dev.lmke.mc.warps.annotations.HasPermission;
import dev.lmke.mc.warps.annotations.IsPlayerCommand;
import dev.lmke.mc.warps.annotations.SubCommand;
import dev.lmke.mc.warps.annotations.TabComplete;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class CommandBase implements CommandExecutor, TabExecutor {

    private final Map<String, Method> subCommands = new HashMap<>();
    private final Map<String, Method> tabCompletes = new HashMap<>();

    public CommandBase() {
        Method[] methods = this.getClass().getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(SubCommand.class)) {
                SubCommand annotation = method.getAnnotation(SubCommand.class);
                subCommands.put(annotation.value(), method);

            } else if(method.isAnnotationPresent(TabComplete.class)) {
                TabComplete annotation = method.getAnnotation(TabComplete.class);
                tabCompletes.put(annotation.value(), method);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Method commandMethod = null;

        // Use subcommand if exists or use default perform method
        if (args.length > 0 && subCommands.containsKey(args[0])) {
            commandMethod = subCommands.get(args[0]);

            // Remove first element because it's the subcommand
            args = Arrays.copyOfRange(args, 1, args.length);
        } else {
            try {
                commandMethod = this.getClass().getDeclaredMethod("perform", CommandSender.class, Command.class, String[].class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        // Check if it is a player command
        if (commandMethod.isAnnotationPresent(IsPlayerCommand.class) && !(sender instanceof Player)) {
            return false;
        }

        // Check if player has permission
        if (commandMethod.isAnnotationPresent(HasPermission.class) && sender instanceof Player) {
            HasPermission annotation = commandMethod.getAnnotation(HasPermission.class);

            if (!sender.hasPermission(annotation.value())) {
                sender.sendMessage(MessageLocaleManager.getText("errors.missing_permission"));
                return false;
            }
        }

        // Perform command
        try {
            commandMethod.invoke(this, sender, command, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length < 2) {
            List<String> values = new ArrayList<String>(subCommands.keySet());
            List<String> res = new ArrayList<String>();

            try {
                res = (List<String>) this.getClass().getDeclaredMethod("onTabComplete", CommandSender.class, Command.class, String[].class)
                         .invoke(this, sender, command, args);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            values.addAll(res);

            return values;
        } else {
            Method method = null;
            String[] newArgs = args;

            // Use subcommand if exists or use default perform method
            if (tabCompletes.containsKey(args[0])) {
                method = tabCompletes.get(args[0]);

                // Remove first element because it's the subcommand
                newArgs = Arrays.copyOfRange(args, 1, args.length);
            } else {
                return new ArrayList<String>();
            }

            try {
                return (List<String>) method.invoke(this, sender, command, newArgs);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Method that gets executed if the command is triggered without a subcommand
     */
    public abstract void perform(CommandSender sender, Command command, String[] args);
    public abstract List<String> onTabComplete(CommandSender sender, Command command, String[] args);
}
