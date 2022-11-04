package dev.lmke.mc.warps;

import dev.lmke.mc.warps.commands.WarpCommand;
import dev.lmke.mc.warps.database.Database;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public final class LMKEWarps extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        MessageLocaleManager.setup();
        MessageLocaleManager.loadMessageFile(getConfig().getString("locale"));

        String dataPath = Paths.get(getDataFolder().getAbsolutePath(), "database.nitrite").toString();
        Database.openDatabase(dataPath);

        getCommand("warp").setExecutor(new WarpCommand());

        System.out.println("[ProWarps] Plugin loaded");
    }

    @Override
    public void onDisable() {
        Database.closeDatabase();

        System.out.println("[ProWarps] Plugin unloaded");
    }
}
