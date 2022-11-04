package dev.lmke.mc.warps.utils;

import dev.lmke.mc.warps.LMKEWarps;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class MessageLocaleManager {

    private static final Plugin plugin = LMKEWarps.getPlugin(LMKEWarps.class);
    private static File file;
    private static FileConfiguration content;

    private static String[] locales = { "de", "en" };

    public static void setup() {
        for (String locale : locales) {

            File f = new File(plugin.getDataFolder().getAbsolutePath(), locale + ".messages.yml");

            if (f.exists()) continue;

            InputStream stream = plugin.getResource(locale + ".messages.yml");

            if (stream != null) {
                try {
                    Files.copy(stream, f.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void loadMessageFile(String locale) {
        file = new File(plugin.getDataFolder().getAbsolutePath(), locale + ".messages.yml");
        content = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration getConfig() {
        return content;
    }

    public static String getText(String key) {
        return content.getString("common.prefix") + content.getString(key);
    }
}
