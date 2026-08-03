package io.github.jason13official.shades.impl.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jason13official.shades.Shades;
import io.github.jason13official.shades.impl.common.registry.ModItems;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/// sunglasses overlay riding the player's head whenever [ModItems#BASIC_SHADES] is worn;
/// since that item carries no `Equippable` asset, vanilla's HumanoidArmorLayer never renders this
///
///
/// ( ^ plus [io.github.jason13official.shades.mixin.CustomHeadLayerMixin] suppressing
/// vanilla's generic decorative-head-item fallback) is the only thing drawn on the head for it
///
/// gated on [ShadesRenderStateExtension] rather than `state.headEquipment`;
/// headEquipment is `ItemStack.EMPTY` for us unconditionally
/// @see ShadesRenderStateExtension
public class ShadesVisorLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

  private static final Identifier TEXTURE = Shades.identifier("textures/models/armor/basic_shades_visor.png");

  private ShadesVisorModel model;

  public ShadesVisorLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
    super(parent);
    this.model = new ShadesVisorModel(ShadesVisorModel.createLayer().bakeRoot());
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
    if (!headItem.is(ModItems.BASIC_SHADES)) {
      return;
    }

    // note: no poseStack.translate() here;
    // this runs before the head's root rotation is applied, and
    // will shift our model's pivot point as well ->
    // makes the model rotate around a slightly different point than the real head
    // and looks misaligned when head pitch != 0

    int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
    submitNodeCollector.submitModel(
        layer.model, state, poseStack, RenderTypes.entityTranslucent(TEXTURE), lightCoords, overlayCoords, state.outlineColor, null);
  }
}
