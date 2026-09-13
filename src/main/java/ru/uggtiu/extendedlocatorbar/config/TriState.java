package ru.uggtiu.extendedlocatorbar.config;

public enum TriState {
    DEFAULT("elb.tristate.default"),
    ENABLED("elb.tristate.enabled"),
    DISABLED("elb.tristate.disabled");

    private final String translationKey;

    TriState(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}