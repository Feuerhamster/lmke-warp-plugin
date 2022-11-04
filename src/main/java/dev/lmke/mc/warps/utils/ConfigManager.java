package dev.lmke.mc.warps.utils;

import dev.lmke.mc.warps.LMKEWarps;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigManager {
    private static final Plugin plugin = LMKEWarps.getPlugin(LMKEWarps.class);

    public static FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
