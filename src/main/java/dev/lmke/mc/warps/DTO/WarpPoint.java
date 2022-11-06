package dev.lmke.mc.warps.DTO;

import org.bukkit.Location;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;

import java.util.Map;
import java.util.UUID;

public class WarpPoint implements Mappable {
    @Id
    public NitriteId id;
    public String name;
    public UUID player;
    public Location location;

    public WarpPoint(String name, UUID player, Location location) {
        this.name = name;
        this.player = player;
        this.location = location;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("name", this.name);
        document.put("player", this.player.toString());
        document.put("location", this.location.serialize());

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            this.id = document.getId();
            this.name = (String) document.get("name");
            this.player = UUID.fromString((String) document.get("player"));
            this.location = Location.deserialize((Map<String, Object>) document.get("location"));
        }
    }
}