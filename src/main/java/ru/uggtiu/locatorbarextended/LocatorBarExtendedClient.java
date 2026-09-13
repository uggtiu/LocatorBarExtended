package ru.uggtiu.locatorbarextended;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.uggtiu.locatorbarextended.command.BleCommand;
import ru.uggtiu.locatorbarextended.config.ConfigManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class LocatorBarExtendedClient implements ClientModInitializer {
    public static final String MOD_ID = "locatorbarextended";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        BleCommand.register();
        LOGGER.info("[Locator Bar Extended] Initialized successfully for Minecraft 1.21.11");
    }
}