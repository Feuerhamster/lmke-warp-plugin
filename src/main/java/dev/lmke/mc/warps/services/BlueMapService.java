package dev.lmke.mc.warps.services;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import dev.lmke.mc.warps.DTO.POIObject;
import dev.lmke.mc.warps.LMKEWarps;
import dev.lmke.mc.warps.database.DAL;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlueMapService {

    private static BlueMapAPI api = null;
    private static final String markerSetKey = "lmke-warps";

    public static void setup(BlueMapAPI blueMapAPI) {
        api = blueMapAPI;

        Bukkit.getScheduler().runTaskAsynchronously(LMKEWarps.getPlugin(LMKEWarps.class), bukkitTask -> {
            System.out.println("[lmke-warps] Loading all poi's into BlueMaps...");

            List<POIObject> pois = DAL.getAllPOIs();

            for (BlueMapWorld world : api.getWorlds()) {
                for (BlueMapMap map : world.getMaps()) {
                    MarkerSet markerSet = new MarkerSet(MessageLocaleManager.getText("map.marker_set_label"));

                    List<POIObject> filteredPois = pois.stream().filter(p -> p.location.getWorld().getName().equals(map.getName())).collect(Collectors.toList());

                    for (POIObject poi : filteredPois) {
                        POIMarker marker = buildPOIMarker(poi);

                        markerSet.getMarkers()
                                .put(markerSetKey + "-poi-" + poi.id.getIdValue(), marker);
                    }

                    map.getMarkerSets().put(markerSetKey, markerSet);
                }
            }

            System.out.println("[lmke-warps] Loaded all poi's into BlueMaps");
        });
    }

    public static void addPOI(POIObject poi) {
        if (api == null) return;

        for (BlueMapWorld world : api.getWorlds()) {
            Optional<BlueMapMap> map = getBlueMapMapByWorldName(world, poi.location.getWorld().getName());

            if (map.isPresent()) {
                POIMarker marker = buildPOIMarker(poi);
                map.get().getMarkerSets().get(markerSetKey).getMarkers().put(markerSetKey + "-poi-" + poi.id.getIdValue(), marker);
            }
        }
    }

    public static void removePOI(POIObject poi) {
        if (api == null) return;

        for (BlueMapWorld world : api.getWorlds()) {
            Optional<BlueMapMap> map = getBlueMapMapByWorldName(world, poi.location.getWorld().getName());

            if (map.isPresent()) {
                map.get().getMarkerSets().get(markerSetKey).getMarkers().remove(markerSetKey + "-poi-" + poi.id.getIdValue());
            }
        }
    }

    private static POIMarker buildPOIMarker(POIObject poi) {
        String playerName = Bukkit.getOfflinePlayer(poi.player).getName();

        return POIMarker.builder()
                .label(poi.name + " " + "@" + playerName)
                .position(poi.location.getBlockX(), poi.location.getBlockY(), poi.location.getBlockZ())
                .maxDistance(1000)
                .build();
    }

    private static Optional<BlueMapMap> getBlueMapMapByWorldName(BlueMapWorld world, String name) {
        return world.getMaps().stream().filter(m -> m.getName().equals(name)).findFirst();
    }

    public static boolean hasAPI() {
        return api != null;
    }
}
