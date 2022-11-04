package net.bridgesplash.placestom.world;

import net.bridgesplash.placestom.PlaceServer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static net.minestom.server.item.Material.*;

public final class PlaceBlocks {

    private static final ComponentLogger LOGGER = PlaceServer.logger;

    public static final @NotNull Block DEFAULT_BLOCK = Objects.requireNonNull(Block.fromNamespaceId(System.getProperty("blocks.default", "barrier")));
    public static final @NotNull Block SPAWN_BLOCK = Objects.requireNonNull(Block.fromNamespaceId(System.getProperty("blocks.spawn", "bedrock")));

    public static final List<Material> ALLOWED_BLOCKS;

    public static final Map<Material, Integer> COLOR_MAP;

    static {
        Map<Material, Integer> materialToColorMap = new ConcurrentHashMap<>();
        List<Material> configMaterials = Arrays.stream(System.getProperty("blocks.allowed-blocks", "").split(","))
                .map(combination -> combination.split("#"))
                .map(array -> {
                    Material material = fromNamespaceId(array[0]);
                    Integer colorValue = Integer.parseInt(array[1], 16);
                    materialToColorMap.put(material, colorValue);
                    return material;
                })
                .collect(Collectors.toList());

        if (configMaterials.isEmpty() || configMaterials.size() >= Byte.MAX_VALUE) {
            ALLOWED_BLOCKS = List.of(
                    Objects.requireNonNull(DEFAULT_BLOCK.registry().material()),
                    RED_CONCRETE,
                    ORANGE_CONCRETE,
                    YELLOW_CONCRETE,
                    GREEN_CONCRETE,
                    LIME_CONCRETE,
                    LIGHT_BLUE_CONCRETE,
                    CYAN_CONCRETE,
                    BLUE_CONCRETE,
                    MAGENTA_CONCRETE,
                    PURPLE_CONCRETE,
                    PINK_CONCRETE,
                    BROWN_CONCRETE,
                    BLACK_CONCRETE,
                    GRAY_CONCRETE,
                    LIGHT_GRAY_CONCRETE,
                    WHITE_CONCRETE
            );
            LOGGER.error("Unable to load allowed blocks. Will default to list of concrete blocks, creating a map will not work.");
            COLOR_MAP = Map.of();
        } else {
            configMaterials.add(0, DEFAULT_BLOCK.registry().material());
            ALLOWED_BLOCKS = Collections.unmodifiableList(configMaterials);
            COLOR_MAP = Collections.unmodifiableMap(materialToColorMap);
        }
    }

    private PlaceBlocks() {
    }

}
