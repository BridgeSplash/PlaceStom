package dev.weiiswurst.placestom.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;

public class StopCommand extends Command {

    public StopCommand() {
        super("stop");
        setCondition((sender, commandString) -> sender.hasPermission("placestom.stop"));
        setDefaultExecutor((sender, context) -> MinecraftServer.stopCleanly());
    }

}
