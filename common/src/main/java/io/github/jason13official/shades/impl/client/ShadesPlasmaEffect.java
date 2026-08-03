package io.github.jason13official.shades.impl.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jason13official.shades.ShadesClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;

/// a translucent, GameTime-animated quad covering the camera's own near clip plane whenever
/// plasma_shades is worn, or prism_shades is worn and cycled to the Plasma option (see
/// ShadesClient#isPlasmaSelected); this is the "cheat" for a full-screen-looking effect the
/// `post_effect` system structurally can't give us.
///
/// Anchored to the CAMERA itself (via Camera#getNearPlane,
/// sized to the player's actual FOV setting) rather than the player's eyes,
/// so it fills the view the same way in first person, third-person back, AND third-person
/// front/selfie mode; same as every other pair of glasses' real post-processing effect does in
/// every camera mode, just achieved through real world-space geometry instead of a post pass.
///
/// `ShadesVisorLayer` still separately renders the actual worn lens model too (using this same
/// `ShadesRenderPipelines.plasma()` RenderType) - that's what OTHER players see on us; this quad
/// only ever exists relative to OUR OWN camera, so it's invisible from anyone else's viewpoint.
///
/// Local player only; extending this to other visible players wouldn't make sense here anyway -
/// you only ever look through your own camera
/// @see ShadesRenderPipelines
public class ShadesPlasmaEffect {

  // slightly past the true near plane so the quad doesn't sit exactly on the clip boundary;
  // scaling forward/left/up together like this keeps the same angular size (still fills the
  // exact same portion of the screen), just a bit further from the camera
  private static final float NEAR_PLANE_PUSH = 1.15F;

  public static void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {

    Minecraft mc = Minecraft.getInstance();
    LocalPlayer player = mc.player;
    if (player == null || !ShadesClient.isPlasmaSelected(player.getItemBySlot(EquipmentSlot.HEAD).getItem())) {
      return;
    }

    Camera camera = mc.gameRenderer.getMainCamera();
    Camera.NearPlane nearPlane = camera.getNearPlane(mc.options.fov().get());

    // these are camera-relative offsets already (not world positions) - exactly the space
    // level-render geometry is submitted in, no further translation needed
    Vec3 topLeft = nearPlane.getTopLeft().scale(NEAR_PLANE_PUSH);
    Vec3 topRight = nearPlane.getTopRight().scale(NEAR_PLANE_PUSH);
    Vec3 bottomLeft = nearPlane.getBottomLeft().scale(NEAR_PLANE_PUSH);
    Vec3 bottomRight = nearPlane.getBottomRight().scale(NEAR_PLANE_PUSH);

    submitNodeCollector.submitCustomGeometry(poseStack, ShadesRenderPipelines.plasma(), (pose, buffer) -> {
      buffer.addVertex(pose, (float) topLeft.x, (float) topLeft.y, (float) topLeft.z)
          .setUv(0.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
      buffer.addVertex(pose, (float) bottomLeft.x, (float) bottomLeft.y, (float) bottomLeft.z)
          .setUv(0.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
      buffer.addVertex(pose, (float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z)
          .setUv(1.0F, 1.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
      buffer.addVertex(pose, (float) topRight.x, (float) topRight.y, (float) topRight.z)
          .setUv(1.0F, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
    });
  }
}
