package net.bridgesplash.placestom.util;

import net.bridgesplash.placestom.PlaceServer;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Objects;

public final class PropertyLoader {

    private PropertyLoader() {
    }

    @ApiStatus.Internal
    public static void loadProperties() throws IOException, URISyntaxException {
        File propertiesFile = new File("./server.properties");
        if (!propertiesFile.exists()) {
            try {
                URI defaultPropertiesUrl = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("server.properties")).toURI();
                fixPathFromJar(defaultPropertiesUrl);
                Files.copy(Paths.get(defaultPropertiesUrl), new FileOutputStream(propertiesFile));
            }catch(NullPointerException e){
                PlaceServer.logger.error("Failed to load default server.properties (due to not being in resource folder of plugin)");
                return;
            }
        }
        try (FileReader reader = new FileReader(propertiesFile)) {
            System.getProperties().load(reader);
        }
    }

    private static void fixPathFromJar(URI uri) {
        // this function is a hack to enable reading modules from within a JAR file
        // see https://stackoverflow.com/a/48298758
        if ("jar".equals(uri.getScheme())) {
            for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
                if ("jar".equalsIgnoreCase(provider.getScheme())) {
                    try {
                        provider.getFileSystem(uri);
                    } catch (FileSystemNotFoundException e) {
                        // in this case we need to initialize it first:
                        try {
                            provider.newFileSystem(uri, Collections.emptyMap());
                        }catch(Exception ex){
                            PlaceServer.logger.error("Failed to fix path from jar");
                        }
                    }
                }
            }
        }
    }

}
