package net.bridgesplash.placestom.listeners;

import net.bridgesplash.placestom.PlaceServer;
import net.bridgesplash.placestom.world.PlaceBlocks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;

import java.util.function.Consumer;

public record PlayerJoinListener(InstanceContainer instanceContainer) implements Consumer<PlayerLoginEvent> {

    private static final int ITEM_AMOUNT = Integer.getInteger("blocks.inventory-amount", 69);
    private static final ComponentLogger logger = PlaceServer.logger;

    @Override
    public void accept(final PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        logger.info(player.getUsername() + " joined the server. IP: " + player.getPlayerConnection().getRemoteAddress());
        logger.info("Player "+ player.getUsername() +" UUID (" + player.getUuid() + ") .");

        player.setAllowFlying(true);
        player.setLevel(69);
        player.setExp(0.5f);
        event.setSpawningInstance(instanceContainer);
        player.setRespawnPoint(new Pos(0.5, 42, 0.5));

        PlaceBlocks.ALLOWED_BLOCKS.stream()
                .filter(material -> !material.block().equals(PlaceBlocks.DEFAULT_BLOCK))
                .map(type -> ItemStack.of(type).withAmount(ITEM_AMOUNT))
                .forEach(player.getInventory()::addItemStack);
        final Component name;
        if(player.getDisplayName() == null){
            name = player.getName();
        }else{
            name = player.getDisplayName();
        }
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player1 -> player1.sendMessage(Component.join(JoinConfiguration.separator(Component.space()), name,Component.text("joined the game"))));
    }
}
