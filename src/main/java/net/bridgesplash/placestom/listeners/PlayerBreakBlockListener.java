package net.bridgesplash.placestom.listeners;

import com.j256.ormlite.dao.Dao;
import net.bridgesplash.placestom.util.PlayerActionCoolDown;
import net.bridgesplash.placestom.world.ChunkData;
import net.bridgesplash.placestom.world.PlaceBlocks;
import net.bridgesplash.placestom.world.PlayerPlacementLog;
import net.minestom.server.event.player.PlayerBlockBreakEvent;

import java.util.function.Consumer;

public record PlayerBreakBlockListener (PlayerActionCoolDown coolDown, Dao<ChunkData, Integer> chunkDao,
                                        Dao<PlayerPlacementLog, Long> playerDao) implements Consumer<PlayerBlockBreakEvent>,
        PlayerActionListener {
    @Override
    public void accept(PlayerBlockBreakEvent event) {
        if (event.getPlayer().isCreative()) {
            // Ignore creative players (admins)
            return;
        }
        event.setCancelled(true);
        tryPlaceBlock(event.getPlayer(), event.getBlockPosition(), coolDown, PlaceBlocks.DEFAULT_BLOCK, chunkDao, playerDao);

    }

}
