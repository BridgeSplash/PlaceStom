package net.bridgesplash.placestom.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerDisconnectEvent;

import java.util.function.Consumer;

public class PlayerLeaveListener implements Consumer<PlayerDisconnectEvent> {

    public PlayerLeaveListener() {}

    @Override
    public void accept(PlayerDisconnectEvent event) {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach( player ->
                player.sendMessage(Component.join(JoinConfiguration.separator(Component.space()), event.getPlayer().getName(),Component.text("left the game")))
        );
    }
}
