package dev.lmke.mc.warps.database;

import dev.lmke.mc.warps.DTO.POIObject;
import dev.lmke.mc.warps.DTO.WarpPoint;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DAL {

    /**
     * Get a list of the warps of a player
     * @param player Minecraft player UUID
     * @return List of warp names
     */
    public static List<String> getPlayerWarpsList(UUID player) {
        ObjectRepository<WarpPoint> repo = Database.getRepo(WarpPoint.class);
        Cursor<WarpPoint> cursor = repo.find(ObjectFilters.eq("player", player.toString()));

        return cursor.toList().stream().map(x -> x.name).collect(Collectors.toList());
    }

    /**
     * Get a player's warp point by name
     * @param name Name of the warp point
     * @param player Minecraft player UUID
     * @return WarpPoint object
     */
    public static WarpPoint getPlayerWarpPoint(String name, UUID player) {
        ObjectRepository<WarpPoint> repo = Database.getRepo(WarpPoint.class);
        Cursor<WarpPoint> cursor = repo.find(
                ObjectFilters.and(
                        ObjectFilters.eq("player", player.toString()),
                        ObjectFilters.eq("name", name)
                )
        );

        return cursor.firstOrDefault();
    }

    /**
     * Get the count of warp point a player currently has
     * @param player Minecraft player UUID
     * @return Number of warp points the player have
     */
    public static int getPlayerWarpCount(UUID player) {
        ObjectRepository<WarpPoint> repo = Database.getRepo(WarpPoint.class);
        Cursor<WarpPoint> cursor = repo.find(ObjectFilters.eq("player", player.toString()));
        return cursor.totalCount();
    }

    /**
     * Get a poi by name
     * @param name Name of the poi
     * @return A POI object
     */
    public static POIObject getPOI(String name) {
        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        Cursor<POIObject> cursor = repo.find(ObjectFilters.eq("name", name));

        return cursor.firstOrDefault();
    }

    /**
     * Get a poi by id
     * @param id ID of a poi
     * @return A POI object
     */
    public static POIObject getPOIById(long id) {
        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        Cursor<POIObject> cursor = repo.find(ObjectFilters.eq("_id", NitriteId.createId(id)));

        return cursor.firstOrDefault();
    }

    /**
     * Get a list of all pois that are owned by a specific player
     * @param player Minecraft player UUID
     * @return List of poi names
     */
    public static List<String> getPlayerPOIList(UUID player) {
        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        Cursor<POIObject> cursor = repo.find(ObjectFilters.eq("player", player.toString()));

        return cursor.toList().stream().map(x -> x.name).collect(Collectors.toList());
    }

    /**
     * Get the count of poi's that are owned by a specific player
     * @param player Minecraft player UUID
     * @return Number
     */
    public static int getPlayerPOICount(UUID player) {
        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        Cursor<POIObject> cursor = repo.find(ObjectFilters.eq("player", player.toString()));
        return cursor.totalCount();
    }

    /**
     * Get all poi's
     * @return List of POI Objects
     */
    public static List<POIObject> getAllPOIs() {
        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        Cursor<POIObject> cursor = repo.find();

        return cursor.toList();
    }
}
