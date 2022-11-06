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
    public static List<String> getPlayerWarpsList(UUID player) {
        ObjectRepository<WarpPoint> repo = Database.getRepo(WarpPoint.class);
        Cursor<WarpPoint> cursor = repo.find(ObjectFilters.eq("player", player.toString()));

        return cursor.toList().stream().map(x -> x.name).collect(Collectors.toList());
    }

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

    public static int getPlayerWarpCount(UUID player) {
        ObjectRepository<WarpPoint> repo = Database.getRepo(WarpPoint.class);
        Cursor<WarpPoint> cursor = repo.find(ObjectFilters.eq("player", player.toString()));
        return cursor.totalCount();
    }

    public static POIObject getPOI(String name) {
        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        Cursor<POIObject> cursor = repo.find(ObjectFilters.eq("name", name));

        return cursor.firstOrDefault();
    }

    public static POIObject getPOIById(long id) {
        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        Cursor<POIObject> cursor = repo.find(ObjectFilters.eq("_id", NitriteId.createId(id)));

        return cursor.firstOrDefault();
    }

    public static List<String> getPlayerPOIList(UUID player) {
        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        Cursor<POIObject> cursor = repo.find(ObjectFilters.eq("player", player.toString()));

        return cursor.toList().stream().map(x -> x.name).collect(Collectors.toList());
    }

    public static int getPlayerPOICount(UUID player) {
        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        Cursor<POIObject> cursor = repo.find(ObjectFilters.eq("player", player.toString()));
        return cursor.totalCount();
    }

    public static List<POIObject> getAllPOIs() {
        ObjectRepository<POIObject> repo = Database.getRepo(POIObject.class);
        Cursor<POIObject> cursor = repo.find();

        return cursor.toList();
    }
}
