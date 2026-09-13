package ru.uggtiu.extendedlocatorbar;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class ExtendedLocatorBarClient implements ClientModInitializer {
    public static final String MOD_ID = "extendedlocatorbar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Extended Locator Bar] Initialized successfully for Minecraft 1.21.11");
    }
}