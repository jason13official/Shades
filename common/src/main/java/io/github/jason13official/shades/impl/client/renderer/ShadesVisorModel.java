package io.github.jason13official.shades.impl.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/// extends HumanoidModel but only reuses `head`, so our model is a real child of it and inherits
/// the look-angle transform through the normal model hierarchy at render time, no pose copy needed
public class ShadesVisorModel extends HumanoidModel<AvatarRenderState> {

  public ShadesVisorModel(ModelPart root) {
    super(root, RenderTypes::entityTranslucent);
  }

  public static LayerDefinition createLayer() {

    MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
    PartDefinition root = mesh.getRoot().clearRecursively();
    PartDefinition head = root.getChild("head");

    // lens band sits proud of the head's own front face (z -4.0), spanning full face width at eye height
    // inflated with CubeDeformation to prevent Z-fighting
    head.addOrReplaceChild("shades_lens",
        CubeListBuilder.create().texOffs(0, 0)
            .addBox(-4.0F, -4.0F, -4.6F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.01f)), PartPose.ZERO);
    // temple arms hug the sides of the head, wrapping from the lens back toward the ears
    head.addOrReplaceChild("shades_arm_right",
        CubeListBuilder.create().texOffs(0, 4)
            .addBox(-5.0F, -4.0F, -3.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.01f)), PartPose.ZERO);
    head.addOrReplaceChild("shades_arm_left",
        CubeListBuilder.create().texOffs(9, 4)
            .addBox(4.0F, -4.0F, -3.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.01f)), PartPose.ZERO);

    return LayerDefinition.create(mesh, 32, 16);
  }
}
