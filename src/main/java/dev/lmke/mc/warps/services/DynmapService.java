package dev.lmke.mc.warps.services;

import dev.lmke.mc.warps.DTO.POIObject;
import dev.lmke.mc.warps.LMKEWarps;
import dev.lmke.mc.warps.database.DAL;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.List;

public class DynmapService {
    private static DynmapAPI dapi = null;
    private static MarkerSet markerset = null;
    private static final String markerSetKey = "lmke-warps";


    public static void registerDynmap() {
        dapi = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");

        if (dapi == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(LMKEWarps.getPlugin(LMKEWarps.class), bukkitTask -> {
            System.out.println("[lmke-warps] Loading all poi's into dynmap...");

            String label = MessageLocaleManager.getTextRaw("map.marker_set_label");

            markerset = dapi.getMarkerAPI().createMarkerSet(markerSetKey, label, dapi.getMarkerAPI().getMarkerIcons(), false);

            MarkerIcon markerAPI = dapi.getMarkerAPI().getMarkerIcon("pin");
            markerset.setDefaultMarkerIcon(markerAPI);

            List<POIObject> pois = DAL.getAllPOIs();

            for (POIObject poi : pois) {
                buildPOIMarker(poi);
            }

            System.out.println("[lmke-warps] Loaded all poi's into dynmap");
        });
    }

    private static Marker buildPOIMarker(POIObject poi) {
        String id = markerSetKey + "-poi-" + poi.id.getIdValue();
        String world = poi.location.getWorld().getName();
        String playerName = Bukkit.getOfflinePlayer(poi.player).getName();
        String label = poi.name + " " + "@" + playerName;

        MarkerIcon icon = markerset.getDefaultMarkerIcon();

        return markerset.createMarker(id, label, false, world, poi.location.getBlockX(), poi.location.getBlockY(), poi.location.getBlockZ(), icon, false);
    }

    public static void addMarker(POIObject poi) {
        buildPOIMarker(poi);
    }

    public static void removeMarker(POIObject poi) {
        String id = markerSetKey + "-poi-" + poi.id.getIdValue();

        markerset.findMarker(id).deleteMarker();
    }

    public static boolean hasAPI() {
        return dapi != null;
    }
}
