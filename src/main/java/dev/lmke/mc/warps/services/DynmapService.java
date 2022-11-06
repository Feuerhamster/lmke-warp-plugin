package dev.lmke.mc.warps.services;

import dev.lmke.mc.warps.DTO.POIObject;
import dev.lmke.mc.warps.database.DAL;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.*;

import java.awt.geom.Area;
import java.util.List;

public class DynmapService {
    private static DynmapAPI dapi = null;
    private static MarkerSet markerset = null;
    private static final String markerSetKey = "lmke-warps";


    public static void registerDynmap(JavaPlugin p) {
        dapi = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");

        if (dapi == null) return;

        System.out.println("[lmke-warps] Loading all poi's into dynmap...");

        markerset = dapi.getMarkerAPI().createMarkerSet(markerSetKey, "lmke warps", dapi.getMarkerAPI().getMarkerIcons(), false);

        MarkerIcon markerAPI = dapi.getMarkerAPI().getMarkerIcon("pin");
        markerset.setDefaultMarkerIcon(markerAPI);

        List<POIObject> pois = DAL.getAllPOIs();

        for (POIObject poi : pois) {
            Marker marker = buildPOIMarker(poi);
        }

        System.out.println("[lmke-warps] Loaded all poi's into dynmap");
    }

    private static Marker buildPOIMarker(POIObject poi) {
        String id = markerSetKey + "-poi-" + poi.id.getIdValue();
        String world = poi.location.getWorld().getName();
        String playerName = Bukkit.getOfflinePlayer(poi.player).getName();
        String label = poi.name + " " + "@" + playerName;

        double[] x = new double[] { poi.location.getBlockX() , poi.location.getBlockX() + 3 };
        double[] z = new double[] { poi.location.getBlockZ() - 3, poi.location.getBlockZ() + 3 };

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
