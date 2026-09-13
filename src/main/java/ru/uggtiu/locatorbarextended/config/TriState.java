package ru.uggtiu.locatorbarextended.config;

public enum TriState {
    DEFAULT("ble.tristate.default"),
    ENABLED("ble.tristate.enabled"),
    DISABLED("ble.tristate.disabled");

    private final String translationKey;

    TriState(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}