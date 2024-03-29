package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.CodEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class CodEntityRenderer extends MobEntityRenderer<CodEntity, CodEntityModel<CodEntity>> {
   private static final Identifier TEXTURE = new Identifier("textures/entity/fish/cod.png");

   public CodEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher, new CodEntityModel(), 0.3F);
   }

   public Identifier getTexture(CodEntity codEntity) {
      return TEXTURE;
   }

   protected void setupTransforms(CodEntity codEntity, MatrixStack matrixStack, float f, float g, float h) {
      super.setupTransforms(codEntity, matrixStack, f, g, h);
      float i = 4.3F * MathHelper.sin(0.6F * f);
      matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(i));
      if (!codEntity.isTouchingWater()) {
         matrixStack.translate(0.10000000149011612D, 0.10000000149011612D, -0.10000000149011612D);
         matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90.0F));
      }

   }
}
