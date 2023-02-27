package net.bridgesplash.placestom.listeners;

import net.bridgesplash.placestom.PlaceServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.ResponseData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.function.Consumer;


public class MotdListener implements Consumer<ServerListPingEvent> {

    private static final ComponentLogger logger = PlaceServer.logger;

    private final String serverIcon;

    public MotdListener() {
        String iconBase64;
        try {
            BufferedImage image = ImageIO.read(new File("./server-icon.png"));
            ByteArrayOutputStream iconOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", iconOutputStream);
            iconBase64 = Base64.getEncoder().encodeToString(iconOutputStream.toByteArray());
        } catch (IOException e) {
            logger.info("No server icon found. Place a server icon at ./server-icon.png (dimensions: 64x64)");
            iconBase64 = "";
        }
        this.serverIcon = "data:image/png;base64," + iconBase64;
    }

    @Override
    public void accept(ServerListPingEvent event) {
        ResponseData response = event.getResponseData();
        response.setDescription(Component.text("Tesseract Limbo "));
        response.setFavicon(serverIcon);
        event.setResponseData(response);
    }
}
