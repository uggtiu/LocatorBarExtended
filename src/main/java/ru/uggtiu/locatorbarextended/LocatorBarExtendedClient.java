package ru.uggtiu.locatorbarextended;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uggtiu.locatorbarextended.command.BleCommand;
import ru.uggtiu.locatorbarextended.config.ConfigManager;
import ru.uggtiu.locatorbarextended.render.InWorldOverlayRenderer;




@Environment(EnvType.CLIENT)
public class LocatorBarExtendedClient implements ClientModInitializer {
    public static final String MOD_ID = "locatorbarextended";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        BleCommand.register();
        HudElementRegistry.addLast(
            Identifier.of(MOD_ID, "in_world_overlay"),
            InWorldOverlayRenderer::render
        );
        LOGGER.info("[Locator Bar Extended] Initialized successfully for Minecraft 1.21.11");
    }
}