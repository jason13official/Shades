package io.github.jason13official.shades.impl.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

/// a translucent, GameTime-animated quad billboarded just in front of the local player's face
/// whenever plasma_shades is worn.
///
/// In first person it's close enough to fill most of the view,
/// the "cheat" for getting a full-screen-looking effect without the post_effect system's
/// baked-uniform limitation (see ShadesRenderPipelines).
///
/// In third person it just reads as a glowing translucent pane hovering in front
/// of the face, which is a fine look for "shades" too.
///
/// Local player only; extending this to other visible players wearing the item would
/// mean iterating level.entitiesForRendering() instead of just `Minecraft.getInstance().player`
public class ShadesPlasmaEffect {

  private static final float HALF_SIZE = 0.9F; // half-width/height of the quad, in blocks
  private static final float DISTANCE = 0.35F; // how far in front of the eyes to place it

  public static void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;
    if (player == null || player.getItemBySlot(EquipmentSlot.HEAD).getItem() != ModItems.PLASMA_SHADES) {
      return;
    }

    float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
    Vec3 eyePos = player.getEyePosition(partialTick);
    Vec3 lookVec = player.getViewVector(partialTick);
    Vec3 center = eyePos.add(lookVec.scale(DISTANCE));

    // billboard basis straight from the camera, same technique as Thaumatic's FXShatterStar -
    // guarantees the quad always faces the viewer, whether that's us in first person or someone
    // watching us in third person
    Camera camera = mc.gameRenderer.getMainCamera();
    Vec3 camPos = camera.position();
    Vector3fc left = camera.leftVector();
    Vector3fc up = camera.upVector();

    float rx = -left.x();
    float ry = -left.y();
    float rz = -left.z(); // right = -left
    float ux = up.x();
    float uy = up.y();
    float uz = up.z();

    // world position expressed relative to the camera - level-render geometry is always
    // submitted this way to avoid floating point precision loss far from the origin
    float cx = (float) (center.x - camPos.x);
    float cy = (float) (center.y - camPos.y);
    float cz = (float) (center.z - camPos.z);

    submitNodeCollector.submitCustomGeometry(poseStack, ShadesRenderPipelines.plasma(), (pose, buffer) -> {
      float hs = HALF_SIZE;
      buffer.addVertex(pose, cx - rx * hs + ux * hs, cy - ry * hs + uy * hs, cz - rz * hs + uz * hs)
          .setUv(0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
      buffer.addVertex(pose, cx - rx * hs - ux * hs, cy - ry * hs - uy * hs, cz - rz * hs - uz * hs)
          .setUv(0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
      buffer.addVertex(pose, cx + rx * hs - ux * hs, cy + ry * hs - uy * hs, cz + rz * hs - uz * hs)
          .setUv(1.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
      buffer.addVertex(pose, cx + rx * hs + ux * hs, cy + ry * hs + uy * hs, cz + rz * hs + uz * hs)
          .setUv(1.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
    });
  }
}
