package net.minecraft.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.ArmorStandArmorEntityModel;
import net.minecraft.client.render.entity.model.ArmorStandEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ArmorStandEntityRenderer extends LivingEntityRenderer<ArmorStandEntity, ArmorStandArmorEntityModel> {
   public static final Identifier TEXTURE = new Identifier("textures/entity/armorstand/wood.png");

   public ArmorStandEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
      super(entityRenderDispatcher, new ArmorStandEntityModel(), 0.0F);
      this.addFeature(new ArmorFeatureRenderer(this, new ArmorStandArmorEntityModel(0.5F), new ArmorStandArmorEntityModel(1.0F)));
      this.addFeature(new HeldItemFeatureRenderer(this));
      this.addFeature(new ElytraFeatureRenderer(this));
      this.addFeature(new HeadFeatureRenderer(this));
   }

   public Identifier getTexture(ArmorStandEntity armorStandEntity) {
      return TEXTURE;
   }

   protected void setupTransforms(ArmorStandEntity armorStandEntity, MatrixStack matrixStack, float f, float g, float h) {
      matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F - g));
      float i = (float)(armorStandEntity.world.getTime() - armorStandEntity.lastHitTime) + h;
      if (i < 5.0F) {
         matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(MathHelper.sin(i / 1.5F * 3.1415927F) * 3.0F));
      }

   }

   protected boolean hasLabel(ArmorStandEntity armorStandEntity) {
      double d = this.dispatcher.getSquaredDistanceToCamera(armorStandEntity);
      float f = armorStandEntity.isInSneakingPose() ? 32.0F : 64.0F;
      return d >= (double)(f * f) ? false : armorStandEntity.isCustomNameVisible();
   }

   @Nullable
   protected RenderLayer getRenderLayer(ArmorStandEntity armorStandEntity, boolean bl, boolean bl2, boolean bl3) {
      if (!armorStandEntity.isMarker()) {
         return super.getRenderLayer(armorStandEntity, bl, bl2, bl3);
      } else {
         Identifier identifier = this.getTexture(armorStandEntity);
         if (bl2) {
            return RenderLayer.getEntityTranslucent(identifier, false);
         } else {
            return bl ? RenderLayer.getEntityCutoutNoCull(identifier, false) : null;
         }
      }
   }
}
