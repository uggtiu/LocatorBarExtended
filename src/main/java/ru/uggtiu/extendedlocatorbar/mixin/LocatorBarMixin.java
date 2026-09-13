package ru.uggtiu.extendedlocatorbar.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.bar.LocatorBar;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.waypoint.TrackedWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.UUID;

@Environment(EnvType.CLIENT)
@Mixin(LocatorBar.class)
public abstract class LocatorBarMixin {

    /**
     * Intercepts the waypoint icon drawGuiTexture call inside method_70870.
     */
    @WrapOperation(
        method = "method_70870",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIII)V"
        )
    )
    private void wrapWaypointIconRender(
            DrawContext context,
            RenderPipeline pipeline,
            Identifier texture,
            int x,
            int y,
            int width,
            int height,
            int color,
            Operation<Void> original,
            @Local TrackedWaypoint waypoint
    ) {
        if (!renderCustomPlayerMarker(context, waypoint, x, y, width, height)) {
            // Fallback to vanilla colored diamond icon
            original.call(context, pipeline, texture, x, y, width, height, color);
        }
    }

    /**
     * Renders the player skin head and nickname above it.
     *
     * @return true if player skin rendering succeeded; false to fallback to vanilla icon.
     */
    private static boolean renderCustomPlayerMarker(
            DrawContext context,
            TrackedWaypoint waypoint,
            int x,
            int y,
            int width,
            int height
    ) {
        if (waypoint == null) {
            return false;
        }

        // Check if waypoint has an assigned player UUID
        Optional<UUID> playerUuidOpt = waypoint.getSource().left();
        if (playerUuidOpt.isEmpty()) {
            return false;
        }

        UUID playerUuid = playerUuidOpt.get();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null) {
            return false;
        }

        // Retrieve player's skin textures from cached PlayerListEntry
        PlayerListEntry playerEntry = networkHandler.getPlayerListEntry(playerUuid);
        if (playerEntry == null) {
            return false;
        }

        SkinTextures skinTextures = playerEntry.getSkinTextures();
        if (skinTextures == null) {
            return false;
        }

        // Draw the player's head (with hat layer) scaled to the waypoint icon size (9x9)
        int iconSize = Math.max(width, height);
        PlayerSkinDrawer.draw(context, skinTextures, x, y, iconSize);

        // Render the nickname centered above the head icon
        TextRenderer textRenderer = client.textRenderer;
        Text displayName = playerEntry.getDisplayName();
        Text nameText = displayName != null ? displayName : Text.literal(playerEntry.getProfile().name());

        int textWidth = textRenderer.getWidth(nameText);
        int textX = x + (iconSize - textWidth) / 2;
        int textY = y - textRenderer.fontHeight - 2;

        context.drawText(textRenderer, nameText, textX, textY, 0xFFFFFFFF, true);

        return true;
    }
}