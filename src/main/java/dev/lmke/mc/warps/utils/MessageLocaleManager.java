package dev.lmke.mc.warps.utils;

import dev.lmke.mc.warps.LMKEWarps;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class MessageLocaleManager {
    private static final Plugin plugin = LMKEWarps.getPlugin(LMKEWarps.class);
    private static FileConfiguration content;

    private static FileConfiguration fallbackContent;

    private static final String[] locales = { "de", "en" };

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
        File file = new File(plugin.getDataFolder().getAbsolutePath(), locale + ".messages.yml");
        content = YamlConfiguration.loadConfiguration(file);

        InputStreamReader fallback = new InputStreamReader(plugin.getResource(locale + ".messages.yml"));
        fallbackContent = YamlConfiguration.loadConfiguration(fallback);
    }

    public static FileConfiguration getConfig() {
        return content;
    }

    public static String getChatText(String key) {
        return content.getString("common.prefix") + getTextRaw(key);
    }

    public static String getTextRaw(String key) {
        String text = content.getString(key);

        if (text == null) {
            return fallbackContent.getString(key);
        }

        return text;
    }
}
