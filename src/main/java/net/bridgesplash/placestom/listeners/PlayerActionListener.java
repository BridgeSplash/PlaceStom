package net.bridgesplash.placestom.listeners;

import com.j256.ormlite.dao.Dao;
import net.bridgesplash.placestom.commands.CooldownCommand;
import net.bridgesplash.placestom.util.PlayerActionCoolDown;
import net.bridgesplash.placestom.util.ProtectedLocations;
import net.bridgesplash.placestom.world.ChunkData;
import net.bridgesplash.placestom.world.PlaceBlocks;
import net.bridgesplash.placestom.world.PlayerPlacementLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.sql.SQLException;

public interface PlayerActionListener {

    default void tryPlaceBlock(Player player, Point location, PlayerActionCoolDown coolDown, Block block,
                               Dao<ChunkData, Integer> chunkDao, Dao<PlayerPlacementLog, Long> playerPlacementLogs) {
        if (ProtectedLocations.isProtected(location)) {
            player.sendActionBar(Component.text("Protected location.").color(TextColor.color(255,50,50)));
            return;
        }
        if (!player.hasPermission(CooldownCommand.NO_COOLDOWN_PERMISSION) && !coolDown.performAction(player.getUuid())) {
            player.sendActionBar(Component.text("You are on cooldown.").color(TextColor.color(255,50,50)));
            return;
        }
        coolDown.applyAnimation(player);
        Instance instance = player.getInstance();
        if(instance == null)return;
        try {
            ChunkData chunkData = chunkDao.queryForId(ChunkData.toDatabaseIndex(location.chunkX(), location.chunkZ()));
            chunkData.setBlockAt(ChunkData.worldCoordsToLocalIndex(location),
                    (byte) PlaceBlocks.ALLOWED_BLOCKS.indexOf(block.registry().material())
                    );
            chunkDao.update(chunkData);
            PlayerPlacementLog logEntry = new PlayerPlacementLog(location.blockX(), location.blockZ(), player);
            playerPlacementLogs.createOrUpdate(logEntry);
            instance.setBlock(location, block);
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage("Error while updating the database. Your edit was not saved.");
        }
    }

}
