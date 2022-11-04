package net.bridgesplash.placestom.commands;

import net.bridgesplash.placestom.PlaceServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class VersionCommand extends Command {

    // Get replaced by blossom at build time
    private static final String NAME = "&implementationName";
    private static final String VERSION = "&version";

    public VersionCommand() {
        super( "placestom");
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(String.format("The server is running %s %s", NAME, VERSION));
            if(sender instanceof Player && PlaceServer.getPermissions((Player)sender, "placestom.stop")) {
                sender.sendMessage("You can use /placestom stop to stop the server");
            }
        });
    }

}
