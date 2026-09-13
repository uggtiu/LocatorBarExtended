package ru.uggtiu.locatorbarextended.config;

public enum RenderMode {
    DEFAULT("ble.mode.default"),
    HEAD("ble.mode.head"),
    ICON("ble.mode.icon");

    private final String translationKey;

    RenderMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}