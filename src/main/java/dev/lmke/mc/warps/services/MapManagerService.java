package dev.lmke.mc.warps.services;

import dev.lmke.mc.warps.DTO.POIObject;

public class MapManagerService {
    public static void addPOI(POIObject poi) {
        if (BlueMapService.hasAPI()) {
            BlueMapService.addPOI(poi);

        } else if (DynmapService.hasAPI()) {
            DynmapService.addMarker(poi);
        }
    }

    public static void removePOI(POIObject poi) {
        if (BlueMapService.hasAPI()) {
            BlueMapService.removePOI(poi);

        } else if (DynmapService.hasAPI()) {
            DynmapService.removeMarker(poi);
        }
    }
}
