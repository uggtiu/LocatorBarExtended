package ru.uggtiu.locatorbarextended.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.waypoint.EntityTickProgress;
import net.minecraft.world.waypoint.TrackedWaypoint;
import org.joml.Matrix3x2fStack;
import ru.uggtiu.locatorbarextended.config.ConfigManager;
import ru.uggtiu.locatorbarextended.config.GlobalConfig;
import ru.uggtiu.locatorbarextended.config.RenderMode;

import java.awt.Color;
import java.util.Optional;
import java.util.UUID;

public class PlayerMarkerRenderer {

    public static boolean renderWaypointMarker(
            DrawContext context,
            RenderPipeline pipeline,
            Identifier texture,
            int x,
            int y,
            int width,
            int height,
            int color,
            TrackedWaypoint waypoint
    ) {
        if (waypoint == null) return false;

        Optional<UUID> playerUuidOpt = waypoint.getSource().left();
        if (playerUuidOpt.isEmpty()) return false;

        UUID playerUuid = playerUuidOpt.get();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null) return false;

        PlayerListEntry playerEntry = networkHandler.getPlayerListEntry(playerUuid);
        if (playerEntry == null) return false;

        GlobalConfig global = ConfigManager.get().global;

        // In-world projection check: hide from locator bar if projected onto screen
        if (global.inWorldOverlayEnabled && global.hideFromBarWhenOverlayed && client.world != null) {
            Entity cameraEntity = client.getCameraEntity();
            World world = cameraEntity != null ? cameraEntity.getEntityWorld() : null;
            if (cameraEntity != null && world != null) {
                EntityTickProgress tickProgress = (e) -> 1.0f;
                if (InWorldOverlayRenderer.shouldShowWaypointInWorld(waypoint, cameraEntity, world, tickProgress)) {
                    return true; // Skips rendering on locator bar
                }
            }
        }

        String playerName = playerEntry.getProfile().name();
        ConfigManager.autoRemember(playerName, playerUuid);
        ConfigManager.EffectiveSettings settings = ConfigManager.getEffectiveSettings(playerName, playerUuid);

        int centerX = x + width / 2;
        int centerY = y + height / 2;

        if (settings.mode == RenderMode.HEAD) {
            SkinTextures skin = playerEntry.getSkinTextures();
            if (skin != null) {
                int iconSize = Math.max(1, Math.round(global.headSize));
                int headX = centerX - iconSize / 2;
                int headY = centerY - iconSize / 2;

                PlayerSkinDrawer.draw(context, skin, headX, headY, iconSize);
                renderHeadFrameAndRounding(context, headX, headY, iconSize, settings.headRounding, settings.highlight, settings.highlightColor);

                if (global.showNicknames) {
                    renderNickname(context, client.textRenderer, playerEntry, headX, headY, iconSize,
                            global.enhancedNickname, settings.nicknameColor, settings.nicknameScale,
                            settings.highlight, settings.highlightColor, settings.cleanNicknames);
                }

                return true;
            }
        }

        if (global.showNicknames) {
            renderNickname(context, client.textRenderer, playerEntry, x, y, width,
                    global.enhancedNickname, settings.nicknameColor, settings.nicknameScale,
                    settings.highlight, settings.highlightColor, settings.cleanNicknames);
        }

        return false;
    }

    public static int getEffectiveIconColor(TrackedWaypoint waypoint, int vanillaColor) {
        GlobalConfig global = ConfigManager.get().global;

        // 1. Rainbow icons: applies strictly to waypoint icons
        if (global.rainbowIcons) {
            int seed = waypoint != null ? waypoint.hashCode() : vanillaColor;
            if (waypoint != null && waypoint.getSource().left().isPresent()) {
                seed = waypoint.getSource().left().get().hashCode();
            }
            return getRainbowColor(seed, global.rainbowSpeed);
        }

        // 2. Universal single color for all icons
        if (global.universalIconColorEnabled) {
            return global.universalIconColor;
        }

        if (waypoint == null) return vanillaColor;
        Optional<UUID> playerUuidOpt = waypoint.getSource().left();
        if (playerUuidOpt.isEmpty()) return vanillaColor;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null) return vanillaColor;

        PlayerListEntry playerEntry = client.getNetworkHandler().getPlayerListEntry(playerUuidOpt.get());
        if (playerEntry == null) return vanillaColor;

        ConfigManager.EffectiveSettings settings = ConfigManager.getEffectiveSettings(playerEntry.getProfile().name(), playerUuidOpt.get());

        if (settings.iconColor != null) {
            return settings.iconColor;
        }

        if (settings.highlight) {
            return settings.highlightColor;
        }

        return vanillaColor;
    }

/**
     * Calculates smooth animated rainbow wave color based on time and player/waypoint seed.
     * Fixed: uses long modulo first to prevent float precision truncation.
     */
    public static int getRainbowColor(int seed, float speed) {
        // Base cycle: 3000 milliseconds (3 seconds) for a full rainbow turn at 1.0x speed
        long period = (long) (3000.0f / Math.max(0.05f, speed));
        long timeInCycle = System.currentTimeMillis() % period;

        // Smooth progress from 0.0 to 1.0 with single-millisecond precision
        float progress = (float) timeInCycle / (float) period;

        // Wave offset per waypoint / player seed
        float offset = ((seed & 0xFFFF) % 360) / 360.0f;
        float hue = (progress + offset) % 1.0f;
        if (hue < 0) hue += 1.0f;

        // HSB to ARGB with 100% alpha
        return 0xFF000000 | (Color.HSBtoRGB(hue, 0.85f, 1.0f) & 0x00FFFFFF);
    }

    private static void renderHeadFrameAndRounding(DrawContext context, int x, int y, int size, int rounding, boolean highlight, int highlightColor) {
        if (!highlight && rounding <= 0) return;
        int frameColor = highlight ? highlightColor : 0xAA000000;

        if (rounding <= 0) {
            renderBoxOutline(context, x - 1, y - 1, size + 2, size + 2, frameColor);
        } else if (rounding < 50) {
            context.fill(x, y - 1, x + size, y, frameColor);
            context.fill(x, y + size, x + size, y + size + 1, frameColor);
            context.fill(x - 1, y, x, y + size, frameColor);
            context.fill(x + size, y, x + size + 1, y + size, frameColor);
        } else {
            int cut = Math.max(1, size / 4);
            context.fill(x + cut, y - 1, x + size - cut, y, frameColor);
            context.fill(x + cut, y + size, x + size - cut, y + size + 1, frameColor);
            context.fill(x - 1, y + cut, x, y + size - cut, frameColor);
            context.fill(x + size, y + cut, x + size + 1, y + size - cut, frameColor);
            context.fill(x, y, x + cut, y + 1, frameColor);
            context.fill(x + size - cut, y, x + size, y + 1, frameColor);
            context.fill(x, y + size - 1, x + cut, y + size, frameColor);
            context.fill(x + size - cut, y + size - 1, x + size, y + size, frameColor);
        }
    }

    private static void renderNickname(DrawContext context, TextRenderer textRenderer, PlayerListEntry entry, int anchorX, int anchorY, int anchorWidth, boolean enhanced, int textColor, float scale, boolean highlight, int highlightColor, boolean cleanNicknames) {
        Text nameText = cleanNicknames ? Text.literal(entry.getProfile().name()) : (entry.getDisplayName() != null ? entry.getDisplayName() : Text.literal(entry.getProfile().name()));

        int textWidth = textRenderer.getWidth(nameText);
        int centerX = anchorX + anchorWidth / 2;

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        if (scale != 1.0f && scale > 0.0f) {
            matrices.translate((float) centerX, (float) anchorY);
            matrices.scale(scale, scale);
            matrices.translate((float) -centerX, (float) -anchorY);
        }

        if (enhanced) {
            int padX = 3;
            int padY = 2;
            int badgeW = textWidth + padX * 2;
            int badgeH = textRenderer.fontHeight + padY * 2;

            int badgeX = centerX - badgeW / 2;
            int badgeY = anchorY - badgeH - 4;

            int borderColor = highlight ? highlightColor : 0x80FFFFFF;
            int bgColor = 0xDD111111;

            context.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, bgColor);
            renderBoxOutline(context, badgeX, badgeY, badgeW, badgeH, borderColor);

            int arrowY = badgeY + badgeH;
            context.fill(centerX - 2, arrowY, centerX + 3, arrowY + 1, borderColor);
            context.fill(centerX - 1, arrowY + 1, centerX + 2, arrowY + 2, borderColor);
            context.fill(centerX, arrowY + 2, centerX + 1, arrowY + 3, borderColor);

            context.drawText(textRenderer, nameText, badgeX + padX, badgeY + padY + 1, textColor, true);
        } else {
            int textX = centerX - textWidth / 2;
            int textY = anchorY - textRenderer.fontHeight - 2;
            context.drawText(textRenderer, nameText, textX, textY, textColor, true);
        }

        matrices.popMatrix();
    }

    private static void renderBoxOutline(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }
}