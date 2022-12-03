package dev.lmke.mc.warps;

import de.bluecolored.bluemap.api.BlueMapAPI;
import dev.lmke.mc.warps.commands.POICommand;
import dev.lmke.mc.warps.commands.WarpCommand;
import dev.lmke.mc.warps.database.Database;
import dev.lmke.mc.warps.events.POISignEvent;
import dev.lmke.mc.warps.services.BlueMapService;
import dev.lmke.mc.warps.services.DynmapService;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public final class LMKEWarps extends JavaPlugin {
    private static Economy economy = null;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        MessageLocaleManager.setup();
        MessageLocaleManager.loadMessageFile(getConfig().getString("locale"));

        String dataPath = Paths.get(getDataFolder().getAbsolutePath(), "database.nitrite").toString();
        Database.openDatabase(dataPath);

        if(getConfig().getBoolean("warp.enable")) {
            getCommand("warp").setExecutor(new WarpCommand());
        }

        if(getConfig().getBoolean("poi.enable")) {
            getCommand("poi").setExecutor(new POICommand());
        }

        if(getConfig().getBoolean("poi.enable_signs")) {
            getServer().getPluginManager().registerEvents(new POISignEvent(), this);
        }

        if (!setupEconomy()) {
            getLogger().info("Vault plugin not found. Proceeding without economy support!");
        }

        if (getConfig().getBoolean("map_support.enable_bluemap") && getServer().getPluginManager().getPlugin("BlueMap") != null) {
            BlueMapAPI.onEnable(BlueMapService::setup);
        }

        if (getConfig().getBoolean("map_support.enable_dynmap")) {
            DynmapService.registerDynmap();
        }

        getLogger().info("Plugin loaded");
    }

    @Override
    public void onDisable() {
        Database.commit();
        Database.closeDatabase();

        getLogger().info("Plugin disabled");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static Economy getEconomy() {
        return economy;
    }
}
