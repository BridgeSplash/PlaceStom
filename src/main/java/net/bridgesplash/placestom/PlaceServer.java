package net.bridgesplash.placestom;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import net.bridgesplash.placestom.commands.*;
import net.bridgesplash.placestom.listeners.*;
import net.bridgesplash.placestom.util.PlayerActionCoolDown;
import net.bridgesplash.placestom.util.PropertyLoader;
import net.bridgesplash.placestom.util.UpdateChecker;
import net.bridgesplash.placestom.world.ChunkData;
import net.bridgesplash.placestom.world.PlaceLoader;
import net.bridgesplash.placestom.world.PlayerPlacementLog;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public final class PlaceServer extends Extension {

    private static LuckPerms getLuckperms(){return LuckPermsProvider.get();}
    public static ComponentLogger logger;

    public static Boolean getPermissions(Player sender, String commandString) {
        User user = getLuckperms().getUserManager().getUser(sender.getUuid());
        if(user == null)return false;
        PermissionNode node = PermissionNode.builder(commandString).build();
        return user.getNodes().contains(node);
    }

    public void main() throws SQLException, IOException, URISyntaxException {
        PlaceServer.logger = getLogger();
        // Loading properties
        PropertyLoader.loadProperties();

        // Database connection
        ConnectionSource connectionSource = new JdbcConnectionSource(
                System.getProperty("database-url", "jdbc:h2:./database"));
        Dao<ChunkData, Integer> chunkDao = DaoManager.createDao(connectionSource, ChunkData.class);
        // Enable caching, because we don't support multiple servers on one database anyways
        chunkDao.setObjectCache(true);
        TableUtils.createTableIfNotExists(connectionSource, ChunkData.class);

        Dao<PlayerPlacementLog, Long> playerDao = DaoManager.createDao(connectionSource, PlayerPlacementLog.class);
        playerDao.setObjectCache(true);
        TableUtils.createTableIfNotExists(connectionSource, PlayerPlacementLog.class);

        // Create the instance (=world)
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        MinecraftServer.getDimensionTypeManager().addDimension(PlaceLoader.PLACE_DIMENSION);
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer(PlaceLoader.PLACE_DIMENSION);
        instanceContainer.setTag(Tag.Boolean("no_unload_instance"), true);
        // Setup of the instance
        instanceContainer.setGenerator(new PlaceLoader(chunkDao));
        instanceContainer.getWorldBorder().setCenter(0, 0);
        instanceContainer.getWorldBorder().setDiameter(Integer.getInteger("worldborder-size", 500));
        instanceContainer.setTime(6000);
        // No more sunset and sunrise
        instanceContainer.setTimeRate(0);

        // Instantiate the action cooldown
        PlayerActionCoolDown cooldown = new PlayerActionCoolDown();

        MinecraftServer.getSchedulerManager().scheduleTask(
                () -> instanceContainer.getPlayers().forEach(cooldown::applyAnimation),
                TaskSchedule.tick(1), TaskSchedule.tick(1));

        // Register commands
        registerCommands(cooldown, chunkDao, playerDao);

        // Register event listeners
        registerListeners(instanceContainer, chunkDao, playerDao, cooldown);

    }

    private static void registerCommands(PlayerActionCoolDown cooldown, Dao<ChunkData, Integer> chunkDao, Dao<PlayerPlacementLog, Long> playerDao) {
        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new VersionCommand());
        commandManager.register(new CooldownCommand(cooldown));
        commandManager.register(new SpawnCommand());
        commandManager.register(new TeleportCommand());
        commandManager.register(new ClearChunkCommand(chunkDao));
        commandManager.register(new BlameCommand(playerDao));
        commandManager.register(new UpdateServerIconCommand());
    }

    private static void registerListeners(InstanceContainer instanceContainer, Dao<ChunkData, Integer> chunkDao,
                                          Dao<PlayerPlacementLog, Long> playerDao, PlayerActionCoolDown cooldown) {
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, new PlayerJoinListener(instanceContainer));
        globalEventHandler.addListener(PlayerDisconnectEvent.class, new PlayerLeaveListener());
        globalEventHandler.addListener(PlayerBlockPlaceEvent.class, new PlayerPlaceBlockListener(cooldown, chunkDao, playerDao));
        globalEventHandler.addListener(PlayerStartDiggingEvent.class, new PlayerStartBreakBlockListener(cooldown, chunkDao, playerDao));
        globalEventHandler.addListener(PlayerBlockBreakEvent.class, new PlayerBreakBlockListener(cooldown, chunkDao, playerDao));
        globalEventHandler.addListener(ServerListPingEvent.class, new MotdListener());
        globalEventHandler.addListener(ItemDropEvent.class, event -> event.setCancelled(true));
    }

    @Override
    public void initialize() {
        try {
            main();
            UpdateChecker.checkForUpdates();
        } catch (SQLException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        getLogger().info("PlaceStom has been enabled!");
    }

    @Override
    public void terminate() {
        getLogger().info("Placestom Shutting down...");
    }
}
