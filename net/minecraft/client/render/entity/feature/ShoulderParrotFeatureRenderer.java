package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ParrotEntityRenderer;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

@Environment(EnvType.CLIENT)
public class ShoulderParrotFeatureRenderer<T extends PlayerEntity> extends FeatureRenderer<T, PlayerEntityModel<T>> {
   private final ParrotEntityModel model = new ParrotEntityModel();

   public ShoulderParrotFeatureRenderer(FeatureRendererContext<T, PlayerEntityModel<T>> featureRendererContext) {
      super(featureRendererContext);
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T playerEntity, float f, float g, float h, float j, float k, float l) {
      this.renderShoulderParrot(matrixStack, vertexConsumerProvider, i, playerEntity, f, g, k, l, true);
      this.renderShoulderParrot(matrixStack, vertexConsumerProvider, i, playerEntity, f, g, k, l, false);
   }

   private void renderShoulderParrot(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T player, float limbAngle, float limbDistance, float headYaw, float headPitch, boolean leftShoulder) {
      NbtCompound nbtCompound = leftShoulder ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
      EntityType.get(nbtCompound.getString("id")).filter((entityType) -> {
         return entityType == EntityType.PARROT;
      }).ifPresent((entityType) -> {
         matrices.push();
         matrices.translate(leftShoulder ? 0.4000000059604645D : -0.4000000059604645D, player.isInSneakingPose() ? -1.2999999523162842D : -1.5D, 0.0D);
         VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(ParrotEntityRenderer.TEXTURES[nbtCompound.getInt("Variant")]));
         this.model.poseOnShoulder(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, limbAngle, limbDistance, headYaw, headPitch, player.age);
         matrices.pop();
      });
   }
}
