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

/// sunglasses overlay riding the player's head whenever a shades item is worn; those items carry
/// no `Equippable` asset, so vanilla's HumanoidArmorLayer never renders anything for them. Gated
/// on [ShadesRenderStateExtension] rather than `state.headEquipment`, which is always empty for us
/// @see ShadesRenderStateExtension
public class ShadesVisorLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

  private ShadesVisorModel model;

  public ShadesVisorLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
    super(parent);
    this.model = new ShadesVisorModel(ShadesVisorModel.createLayer().bakeRoot());
  }

  /// every item's visor texture lives at `{id}_visor.png`, except plasma_shades, whose lens is
  /// the live shader itself and has no static texture
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

      // the lens itself is the shader here, no texture fed in; safe to feed ModelPart-baked
      // geometry into a POSITION_TEX_COLOR-only pipeline since Cube#compile()'s full addVertex
      // overload just chains individual setters, and a reduced-format buffer drops the ones it
      // has no room for
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
