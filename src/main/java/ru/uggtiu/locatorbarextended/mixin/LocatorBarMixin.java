package ru.uggtiu.locatorbarextended.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.bar.LocatorBar;
import net.minecraft.util.Identifier;
import net.minecraft.world.waypoint.TrackedWaypoint;
import ru.uggtiu.locatorbarextended.render.PlayerMarkerRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(LocatorBar.class)
public abstract class LocatorBarMixin {

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
        boolean customHeadRendered = PlayerMarkerRenderer.renderWaypointMarker(
                context, pipeline, texture, x, y, width, height, color, waypoint
        );

        if (!customHeadRendered) {
            int effectiveColor = PlayerMarkerRenderer.getEffectiveIconColor(waypoint, color);
            original.call(context, pipeline, texture, x, y, width, height, effectiveColor);
        }
    }
}