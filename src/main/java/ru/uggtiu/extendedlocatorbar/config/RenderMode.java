package ru.uggtiu.extendedlocatorbar.config;

public enum RenderMode {
    DEFAULT("elb.mode.default"),
    HEAD("elb.mode.head"),
    ICON("elb.mode.icon");

    private final String translationKey;

    RenderMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}