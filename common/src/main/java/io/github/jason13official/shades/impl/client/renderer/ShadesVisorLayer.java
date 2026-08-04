package io.github.jason13official.shades.impl.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jason13official.shades.Shades;
import io.github.jason13official.shades.ShadesClient;
import io.github.jason13official.shades.api.client.renderer.ShadesRenderStateExtension;
import io.github.jason13official.shades.impl.client.ShadesRenderPipelines;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/// sunglasses overlay riding the player's head whenever any of the shades items (see [ModItems]) is worn;
/// since those items carry no `Equippable` asset, vanilla's HumanoidArmorLayer never renders this
///
///
/// ( ^ plus [io.github.jason13official.shades.mixin.CustomHeadLayerMixin] suppressing
/// vanilla's generic decorative-head-item fallback) is the only thing drawn on the head for it
///
/// gated on [ShadesRenderStateExtension] rather than `state.headEquipment`;
/// headEquipment is `ItemStack.EMPTY` for us unconditionally
/// @see ShadesRenderStateExtension
public class ShadesVisorLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

  private ShadesVisorModel model;

  public ShadesVisorLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
    super(parent);
    this.model = new ShadesVisorModel(ShadesVisorModel.createLayer().bakeRoot());
  }

  /// every item's visor texture lives at `{id}_visor.png` (same pixel mask, only fill colors
  /// differ) except plasma_shades, which has no static texture at all since its
  /// lens is the live shader itself; derives the path from [ModItems#idOf] instead of keeping a
  /// duplicate 29-entry map in lockstep with ModItems' fields
  private static Identifier textureFor(Item item) {

    if (item == ModItems.PLASMA_SHADES) {
      return null;
    }

    String id = ModItems.idOf(item);
    if (id == null) {
      return null;
    }

    return Shades.identifier("textures/models/armor/" + id + "_visor.png");
  }

  @Override
  public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {

    // TODO enable for debugging only, otherwise treat model as "effectively final"
    if (true) {
      this.model = new ShadesVisorModel(ShadesVisorModel.createLayer().bakeRoot());
    }
    doSubmit(this, poseStack, submitNodeCollector, lightCoords, state);
  }

  private static void doSubmit(ShadesVisorLayer layer, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state) {

    ItemStack headItem = ((ShadesRenderStateExtension) state).shades$getHeadSlotItem();
    Item item = headItem.getItem();

    RenderType renderType;
    if (ShadesClient.isPlasmaSelected(headItem)) {

      // the lens itself is the shader here; no texture fed in just the live plasma pattern
      // we could possibly separate this into two submitModel calls to have the arms on the old static entityTranslucent path,
      // and only the lens on plasma. Safe to feed ModelPart-baked geometry into a POSITION_TEX_COLOR-only pipeline:
      // ModelPart.Cube#compile() always calls the full addVertex(pos, color, uv, overlay, light, normal) overload;
      // a default method that just chains the individual setters ->
      // a reduced-format buffer (ours has no overlay/light/normal slots) simply drops the ones
      // it has no room for, same as any RenderType built from a smaller vertex format
      renderType = ShadesRenderPipelines.plasma();
    } else {
      Identifier texture = textureFor(item);
      if (texture == null) {
        return;
      }
      renderType = RenderTypes.entityTranslucent(texture);
    }

    // note: no poseStack.translate() here; this runs before the head's root rotation is applied, and
    // will shift our model's pivot point as well -> makes the model rotate around a slightly different
    // point than the real head and looks misaligned when head pitch != 0

    int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
    submitNodeCollector.submitModel(
        layer.model, state, poseStack, renderType, lightCoords, overlayCoords, state.outlineColor, null);
  }
}
