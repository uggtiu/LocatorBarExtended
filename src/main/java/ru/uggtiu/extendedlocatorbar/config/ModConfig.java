package ru.uggtiu.extendedlocatorbar.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModConfig {
    public GlobalConfig global = new GlobalConfig();

    // Global per-player overrides (key is lowercase nickname or uuid string)
    public Map<String, PlayerConfig> players = new LinkedHashMap<>();

    // Server-scoped player overrides: serverAddress -> (key -> PlayerConfig)
    public Map<String, Map<String, PlayerConfig>> servers = new LinkedHashMap<>();
}