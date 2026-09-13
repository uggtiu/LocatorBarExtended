package ru.uggtiu.extendedlocatorbar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("extendedlocatorbar");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("extendedlocatorbar")
            .resolve("config.json")
            .toFile();

    private static ModConfig config = new ModConfig();

    public static class EffectiveSettings {
        public RenderMode mode;
        public Integer iconColor;
        public int headRounding;
        public boolean highlight;
        public int highlightColor;
        public int nicknameColor;
        public float nicknameScale;
        public boolean cleanNicknames;
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
            if (loaded != null) {
                config = loaded;
                if (config.global == null) config.global = new GlobalConfig();
                if (config.players == null) config.players = new LinkedHashMap<>();
                if (config.servers == null) config.servers = new LinkedHashMap<>();
            }
        } catch (Exception e) {
            LOGGER.error("[Extended Locator Bar] Failed to load config, generating defaults", e);
            save();
        }
    }

    public static void save() {
        try {
            File parent = CONFIG_FILE.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) return;

            try (FileWriter writer = new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            LOGGER.error("[Extended Locator Bar] Failed to save config", e);
        }
    }

    public static ModConfig get() {
        return config;
    }

    public static String getCurrentServerKey() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return "global";
        if (client.isInSingleplayer()) return "singleplayer";

        ServerInfo serverEntry = client.getCurrentServerEntry();
        if (serverEntry != null && serverEntry.address != null) {
            return serverEntry.address.toLowerCase().trim();
        }

        return "unknown";
    }

    /**
     * Resolves individual settings with cascading priority:
     * Server(Player) -> Global(Player) -> GlobalConfig(Default)
     */
    public static EffectiveSettings getEffectiveSettings(String playerName, UUID uuid) {
        GlobalConfig global = config.global;
        String serverKey = getCurrentServerKey();

        PlayerConfig serverPlayer = getPlayerFromMap(config.servers.get(serverKey), playerName, uuid);
        PlayerConfig globalPlayer = getPlayerFromMap(config.players, playerName, uuid);

        EffectiveSettings s = new EffectiveSettings();

        s.mode = resolve(
            serverPlayer != null ? serverPlayer.renderMode : null,
            globalPlayer != null ? globalPlayer.renderMode : null,
            global.renderMode
        );

        s.iconColor = resolve(
            serverPlayer != null ? serverPlayer.iconColor : null,
            globalPlayer != null ? globalPlayer.iconColor : null,
            global.defaultIconColor
        );

        s.headRounding = resolve(
            serverPlayer != null ? serverPlayer.headRounding : null,
            globalPlayer != null ? globalPlayer.headRounding : null,
            global.headRounding
        );

        s.highlight = resolve(
            serverPlayer != null ? serverPlayer.highlight : null,
            globalPlayer != null ? globalPlayer.highlight : null,
            global.highlight
        );

        s.highlightColor = resolve(
            serverPlayer != null ? serverPlayer.highlightColor : null,
            globalPlayer != null ? globalPlayer.highlightColor : null,
            global.defaultHighlightColor
        );

        s.nicknameColor = resolve(
            serverPlayer != null ? serverPlayer.nicknameColor : null,
            globalPlayer != null ? globalPlayer.nicknameColor : null,
            0xFFFFFFFF
        );

        s.nicknameScale = resolve(
            serverPlayer != null ? serverPlayer.nicknameScale : null,
            globalPlayer != null ? globalPlayer.nicknameScale : null,
            global.nicknameScale
        );

        s.cleanNicknames = resolve(
            serverPlayer != null ? serverPlayer.cleanNicknames : null,
            globalPlayer != null ? globalPlayer.cleanNicknames : null,
            global.cleanNicknames
        );

        return s;
    }

    private static PlayerConfig getPlayerFromMap(Map<String, PlayerConfig> map, String name, UUID uuid) {
        if (map == null) return null;
        if (name != null) {
            PlayerConfig p = map.get(name.toLowerCase());
            if (p != null) return p;
        }
        if (uuid != null) {
            return map.get(uuid.toString().toLowerCase());
        }
        return null;
    }

    private static <T> T resolve(T serverVal, T globalVal, T defaultVal) {
        if (serverVal != null) return serverVal;
        if (globalVal != null) return globalVal;
        return defaultVal;
    }

    /**
     * Auto-remembers discovered player with null fields (inheriting global defaults).
     */
    public static void autoRemember(String name, UUID uuid) {
        if (!config.global.autoRememberPlayers || name == null) return;

        String lowerName = name.toLowerCase();
        if (!config.players.containsKey(lowerName)) {
            PlayerConfig newPlayer = new PlayerConfig(name, uuid != null ? uuid.toString() : null);
            config.players.put(lowerName, newPlayer);
            save();
        }
    }
}