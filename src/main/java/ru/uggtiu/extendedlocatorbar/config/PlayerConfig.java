package ru.uggtiu.extendedlocatorbar.config;

public class PlayerConfig {
    public String name;
    public String uuid;

    public RenderMode renderMode = null;
    public Integer iconColor = null;
    public Integer headRounding = null;
    public Boolean highlight = null;
    public Integer highlightColor = null;
    public Integer nicknameColor = null;
    public Float nicknameScale = null;
    public Boolean cleanNicknames = null;     // Nullable per-player override

    public PlayerConfig() {}

    public PlayerConfig(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }
}