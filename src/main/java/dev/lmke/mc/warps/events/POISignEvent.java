package dev.lmke.mc.warps.events;

import dev.lmke.mc.warps.DTO.POIObject;
import dev.lmke.mc.warps.LMKEWarps;
import dev.lmke.mc.warps.database.DAL;
import dev.lmke.mc.warps.utils.MessageLocaleManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class POISignEvent implements Listener {

    /**
     * Player creates a sign
     */
    @EventHandler
    public void onSignEvent(SignChangeEvent e) {

        if (!Objects.requireNonNull(e.getLine(0)).equalsIgnoreCase("[poi]")) {
            return;
        }

        if (!e.getPlayer().hasPermission("lmke-warps.sign.create")) {
            e.getPlayer().sendMessage(MessageLocaleManager.getText("errors.missing_permission"));
            return;
        }

        POIObject poi = DAL.getPOI(e.getLine(1));

        if (poi == null) {
            e.getPlayer().sendMessage(MessageLocaleManager.getText("errors.not_found"));
            return;
        }

        e.setLine(0, "§c[ POI ]");
        e.setLine(1, "§6" + e.getPlayer().getName());
        e.setLine(2, poi.name);

        TileState state = (TileState) e.getBlock().getState();
        PersistentDataContainer container = state.getPersistentDataContainer();

        container.set(
                new NamespacedKey(LMKEWarps.getPlugin(LMKEWarps.class), "poi_id"),
                PersistentDataType.LONG,
                poi.id.getIdValue()
        );

        state.update();
    }

    /**
     * Player right clicks on sign
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) return;

        if (!block.getType().equals(Material.OAK_WALL_SIGN) || !block.getType().equals(Material.OAK_SIGN) || !action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

         TileState state = (TileState) block.getState();
         PersistentDataContainer container = state.getPersistentDataContainer();

        NamespacedKey key = new NamespacedKey(LMKEWarps.getPlugin(LMKEWarps.class), "poi_id");

        if (!container.has(key, PersistentDataType.LONG)) { return; }

        if (!player.hasPermission("lmke-warps.sign.use")) {
            player.sendMessage(MessageLocaleManager.getText("errors.missing_permission"));
            return;
        }

        Long poiId = container.get(key, PersistentDataType.LONG);

        POIObject poi = DAL.getPOIById(poiId);

        if (poi == null) return;

        player.teleport(poi.location);
    }
}
