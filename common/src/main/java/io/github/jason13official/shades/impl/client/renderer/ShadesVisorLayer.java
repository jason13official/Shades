package io.github.jason13official.shades.impl.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jason13official.shades.Shades;
import io.github.jason13official.shades.ShadesClient;
import io.github.jason13official.shades.api.client.renderer.ShadesRenderStateExtension;
import io.github.jason13official.shades.impl.client.ShadesRenderPipelines;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import java.util.Map;
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

  /// which lens texture renders for which pair of glasses; lazy since ModItems' fields aren't set yet
  /// at class-init time
  private static Map<Item, Identifier> texturesByItem;

  private ShadesVisorModel model;

  public ShadesVisorLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
    super(parent);
    this.model = new ShadesVisorModel(ShadesVisorModel.createLayer().bakeRoot());
  }

  private static Map<Item, Identifier> texturesByItem() {

    if (texturesByItem == null) {
      texturesByItem = Map.ofEntries(
          Map.entry(ModItems.BASIC_SHADES, Shades.identifier("textures/models/armor/basic_shades_visor.png")),
          Map.entry(ModItems.CREEPER_SHADES, Shades.identifier("textures/models/armor/creeper_shades_visor.png")),
          Map.entry(ModItems.INVERT_SHADES, Shades.identifier("textures/models/armor/invert_shades_visor.png")),
          Map.entry(ModItems.SPIDER_SHADES, Shades.identifier("textures/models/armor/spider_shades_visor.png")),
          Map.entry(ModItems.BLUR_SHADES, Shades.identifier("textures/models/armor/blur_shades_visor.png")),
          Map.entry(ModItems.NIGHT_VISION_SHADES, Shades.identifier("textures/models/armor/night_vision_shades_visor.png")),
          Map.entry(ModItems.THERMAL_SHADES, Shades.identifier("textures/models/armor/thermal_shades_visor.png")),
          Map.entry(ModItems.MATRIX_SHADES, Shades.identifier("textures/models/armor/matrix_shades_visor.png")),
          Map.entry(ModItems.PRISM_SHADES, Shades.identifier("textures/models/armor/prism_shades_visor.png")),
          Map.entry(ModItems.RECEIPT_SHADES, Shades.identifier("textures/models/armor/receipt_shades_visor.png")),
          Map.entry(ModItems.HALFTONE_SHADES, Shades.identifier("textures/models/armor/halftone_shades_visor.png")),
          Map.entry(ModItems.LEGO_SHADES, Shades.identifier("textures/models/armor/lego_shades_visor.png")),
          Map.entry(ModItems.FLUTED_GLASS_SHADES, Shades.identifier("textures/models/armor/fluted_glass_shades_visor.png")),
          Map.entry(ModItems.CHROMATIC_SHADES, Shades.identifier("textures/models/armor/chromatic_shades_visor.png")),
          Map.entry(ModItems.XRAY_SHADES, Shades.identifier("textures/models/armor/xray_shades_visor.png")),
          Map.entry(ModItems.FISHEYE_SHADES, Shades.identifier("textures/models/armor/fisheye_shades_visor.png")),
          Map.entry(ModItems.STATIC_SHADES, Shades.identifier("textures/models/armor/static_shades_visor.png")),
          Map.entry(ModItems.SONAR_SHADES, Shades.identifier("textures/models/armor/sonar_shades_visor.png")),
          Map.entry(ModItems.GLITCH_SHADES, Shades.identifier("textures/models/armor/glitch_shades_visor.png")));
    }

    return texturesByItem;
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
      Identifier texture = texturesByItem().get(item);
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
