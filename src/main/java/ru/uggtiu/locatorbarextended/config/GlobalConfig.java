package ru.uggtiu.locatorbarextended.config;

public class GlobalConfig {
    public RenderMode renderMode = RenderMode.HEAD;
    public Integer defaultIconColor = null;
    public float headSize = 9.0f;
    public int headRounding = 0;
    public boolean showNicknames = true;
    public boolean cleanNicknames = false;
    public float nicknameScale = 1.0f;
    public boolean enhancedNickname = true;
    public boolean highlight = false;
    public int defaultHighlightColor = 0xFFFF0000;
    public boolean autoRememberPlayers = true;

    // --- Danger / Fun settings ---
    // 1. Universal single icon color
    public boolean universalIconColorEnabled = false;
    public int universalIconColor = 0xFFFFFFFF;

    // 2. Rainbow icons
    public boolean rainbowIcons = false;
    public float rainbowSpeed = 1.0f;

    // 3. Locator Bar but not locator bar (In-World Screen Projection)
    public boolean inWorldOverlayEnabled = false;
    public double inWorldDistanceThreshold = 16.0;  // X blocks
    public boolean inWorldBehindWallsOnly = true;    // True = behind walls OR distance >= X
    public boolean hideFromBarWhenOverlayed = true;  // Remove from locator bar when projected on screen
    public float inWorldHeadSize = 14.0f;           // Size of head drawn in world space
}