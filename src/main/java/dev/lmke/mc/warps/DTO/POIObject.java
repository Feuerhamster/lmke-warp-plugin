package dev.lmke.mc.warps.DTO;

import org.bukkit.Location;
import org.dizitart.no2.mapper.Mappable;

import java.util.UUID;

public class POIObject extends WarpPoint implements Mappable {
    public POIObject(String name, UUID player, Location location) {
        super(name, player, location);
    }
}