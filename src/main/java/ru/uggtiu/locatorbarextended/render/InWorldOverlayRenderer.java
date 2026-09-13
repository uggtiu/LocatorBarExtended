package ru.uggtiu.locatorbarextended.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.waypoint.EntityTickProgress;
import net.minecraft.world.waypoint.TrackedWaypoint;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3f;
import ru.uggtiu.locatorbarextended.config.ConfigManager;
import ru.uggtiu.locatorbarextended.config.GlobalConfig;

import java.util.Optional;
import java.util.UUID;

public class InWorldOverlayRenderer {

    public static boolean shouldShowWaypointInWorld(TrackedWaypoint waypoint, Entity cameraEntity, World world, EntityTickProgress tickProgress) {
        GlobalConfig global = ConfigManager.get().global;
        if (!global.inWorldOverlayEnabled || waypoint == null || cameraEntity == null) return false;

        Optional<UUID> uuidOpt = waypoint.getSource().left();
        if (uuidOpt.isEmpty()) return false;

        UUID uuid = uuidOpt.get();
        if (uuid.equals(cameraEntity.getUuid())) return false; // Ignore self

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return false;

        PlayerEntity targetPlayer = client.world.getPlayerByUuid(uuid);

        if (targetPlayer != null) {
            Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
            Vec3d targetPos = targetPlayer.getEyePos();
            double dist = cameraPos.distanceTo(targetPos);

            BlockHitResult hit = client.world.raycast(new RaycastContext(
                    cameraPos, targetPos,
                    RaycastContext.ShapeType.VISUAL,
                    RaycastContext.FluidHandling.NONE,
                    client.player
            ));

            boolean behindWall = (hit.getType() != HitResult.Type.MISS && hit.getPos().distanceTo(cameraPos) < dist - 0.5);
            return behindWall || dist >= global.inWorldDistanceThreshold;
        } else {
            float distance = MathHelper.sqrt((float) waypoint.squaredDistanceTo(cameraEntity));
            return distance >= global.inWorldDistanceThreshold;
        }
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        GlobalConfig global = ConfigManager.get().global;
        if (!global.inWorldOverlayEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        if (networkHandler == null || networkHandler.getWaypointHandler() == null) return;

        Entity cameraEntity = client.getCameraEntity();
        if (cameraEntity == null) return;

        World world = cameraEntity.getEntityWorld();
        EntityTickProgress tickProgress = (entityx) -> tickCounter.getTickProgress(!world.getTickManager().shouldSkipTick(entityx));

        Camera camera = client.gameRenderer.getCamera();
        int scaledWidth = context.getScaledWindowWidth();
        int scaledHeight = context.getScaledWindowHeight();
        float tickDelta = tickCounter.getTickProgress(true);

        float dynamicFov = ((ru.uggtiu.locatorbarextended.mixin.GameRendererAccessor) client.gameRenderer).callGetFov(camera, tickDelta, true);
        float aspect = (float) client.getWindow().getFramebufferWidth() / (float) client.getWindow().getFramebufferHeight();

        networkHandler.getWaypointHandler().forEachWaypoint(cameraEntity, (waypoint) -> {
            Optional<UUID> uuidOpt = waypoint.getSource().left();
            if (uuidOpt.isEmpty()) return;

            UUID uuid = uuidOpt.get();
            if (uuid.equals(cameraEntity.getUuid())) return;

            if (!shouldShowWaypointInWorld(waypoint, cameraEntity, world, tickProgress)) return;

            PlayerListEntry entry = networkHandler.getPlayerListEntry(uuid);
            if (entry == null) return;

            SkinTextures skin = entry.getSkinTextures();
            if (skin == null) return;

            PlayerEntity loadedPlayer = client.world.getPlayerByUuid(uuid);
            Vector2d screenPos;

            if (loadedPlayer != null) {
                // In render distance: exact 3D position
                Vec3d targetEyePos = loadedPlayer.getLerpedPos(tickDelta).add(0, loadedPlayer.getStandingEyeHeight() + 0.35, 0);
                screenPos = projectToScreen(targetEyePos, camera, scaledWidth, scaledHeight, dynamicFov, aspect);
            } else {
                // Outside render distance: project ray along waypoint direction
                double relYaw = waypoint.getRelativeYaw(world, camera, tickProgress);

                // Check front FOV sector (skip if behind camera)
                if (Math.abs(relYaw) > 85.0) return;

                // Build a 3D ray in world space based on camera yaw + relative yaw
                float worldYaw = (float) Math.toRadians(camera.getYaw() + relYaw);
                float worldPitch = 0.0f;

                TrackedWaypoint.Pitch pitch = waypoint.getPitch(world, client.gameRenderer, tickProgress);
                if (pitch == TrackedWaypoint.Pitch.UP) worldPitch = (float) Math.toRadians(-12.0);
                else if (pitch == TrackedWaypoint.Pitch.DOWN) worldPitch = (float) Math.toRadians(12.0);

                // Place virtual target 64 blocks ahead along the direction ray
                float rayDist = 64.0f;
                Vec3d rayDir = new Vec3d(
                        -MathHelper.sin(worldYaw) * MathHelper.cos(worldPitch),
                        -MathHelper.sin(worldPitch),
                        MathHelper.cos(worldYaw) * MathHelper.cos(worldPitch)
                );
                Vec3d virtualTargetPos = camera.getCameraPos().add(rayDir.multiply(rayDist));

                screenPos = projectToScreen(virtualTargetPos, camera, scaledWidth, scaledHeight, dynamicFov, aspect);
            }

            if (screenPos == null) return;

            int size = Math.round(global.inWorldHeadSize);
            int headX = (int) screenPos.x - size / 2;
            int headY = (int) screenPos.y - size / 2;

            // Draw Head
            PlayerSkinDrawer.draw(context, skin, headX, headY, size);

            // 1px Outline
            context.fill(headX - 1, headY - 1, headX + size + 1, headY, 0xDD000000);
            context.fill(headX - 1, headY + size, headX + size + 1, headY + size + 1, 0xDD000000);
            context.fill(headX - 1, headY, headX, headY + size, 0xDD000000);
            context.fill(headX + size, headY, headX + size + 1, headY + size, 0xDD000000);

            // Draw Nickname
            if (global.showNicknames) {
                String name = global.cleanNicknames ? entry.getProfile().name() : (entry.getDisplayName() != null ? entry.getDisplayName().getString() : entry.getProfile().name());
                Text text = Text.literal(name);
                int textWidth = client.textRenderer.getWidth(text);
                int textX = (int) screenPos.x - textWidth / 2;
                int textY = headY - client.textRenderer.fontHeight - 2;

                context.drawText(client.textRenderer, text, textX, textY, 0xFFFFFFFF, true);
            }
        });
    }

    /**
     * Projects any 3D world position to 2D screen coordinates using the camera's rotation quaternion.
     */
    private static Vector2d projectToScreen(Vec3d point, Camera camera, int screenWidth, int screenHeight, float fovDegrees, float aspect) {
        Vec3d camPos = camera.getCameraPos();
        Vec3d rel = point.subtract(camPos);

        // Transform world-space delta into camera space
        Vector3f viewPos = new Vector3f((float) rel.x, (float) rel.y, (float) rel.z);
        Quaternionf invCameraRot = new Quaternionf(camera.getRotation()).conjugate();
        viewPos.rotate(invCameraRot);

        // In OpenGL/Minecraft camera space, camera looks along -Z.
        // Points in front of camera must have Z < -0.2
        if (viewPos.z >= -0.2f) {
            return null; // Behind camera
        }

        float depth = -viewPos.z;

        double halfHeightTan = Math.tan(Math.toRadians(fovDegrees) / 2.0);
        double halfWidthTan = halfHeightTan * aspect;

        // Normalized Device Coordinates (-1.0 to 1.0)
        // Fixed: positive viewPos.y maps to positive NDC Y (top of view)
        double ndcX = viewPos.x / (depth * halfWidthTan);
        double ndcY = viewPos.y / (depth * halfHeightTan);

        // Cull points outside screen viewport
        if (ndcX < -1.15 || ndcX > 1.15 || ndcY < -1.15 || ndcY > 1.15) {
            return null;
        }

        // Screen mapping:
        // ndcX: -1 (left) -> 0, +1 (right) -> screenWidth
        // ndcY: +1 (top) -> 0, -1 (bottom) -> screenHeight
        double screenX = (screenWidth / 2.0) * (1.0 + ndcX);
        double screenY = (screenHeight / 2.0) * (1.0 - ndcY);

        return new Vector2d(screenX, screenY);
    }
}