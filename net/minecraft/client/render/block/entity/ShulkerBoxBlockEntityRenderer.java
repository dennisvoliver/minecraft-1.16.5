package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class ShulkerBoxBlockEntityRenderer extends BlockEntityRenderer<ShulkerBoxBlockEntity> {
   private final ShulkerEntityModel<?> model;

   public ShulkerBoxBlockEntityRenderer(ShulkerEntityModel<?> shulkerEntityModel, BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
      super(blockEntityRenderDispatcher);
      this.model = shulkerEntityModel;
   }

   public void render(ShulkerBoxBlockEntity shulkerBoxBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      Direction direction = Direction.UP;
      if (shulkerBoxBlockEntity.hasWorld()) {
         BlockState blockState = shulkerBoxBlockEntity.getWorld().getBlockState(shulkerBoxBlockEntity.getPos());
         if (blockState.getBlock() instanceof ShulkerBoxBlock) {
            direction = (Direction)blockState.get(ShulkerBoxBlock.FACING);
         }
      }

      DyeColor dyeColor = shulkerBoxBlockEntity.getColor();
      SpriteIdentifier spriteIdentifier2;
      if (dyeColor == null) {
         spriteIdentifier2 = TexturedRenderLayers.SHULKER_TEXTURE_ID;
      } else {
         spriteIdentifier2 = (SpriteIdentifier)TexturedRenderLayers.COLORED_SHULKER_BOXES_TEXTURES.get(dyeColor.getId());
      }

      matrixStack.push();
      matrixStack.translate(0.5D, 0.5D, 0.5D);
      float g = 0.9995F;
      matrixStack.scale(0.9995F, 0.9995F, 0.9995F);
      matrixStack.multiply(direction.getRotationQuaternion());
      matrixStack.scale(1.0F, -1.0F, -1.0F);
      matrixStack.translate(0.0D, -1.0D, 0.0D);
      VertexConsumer vertexConsumer = spriteIdentifier2.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityCutoutNoCull);
      this.model.getBottomShell().render(matrixStack, vertexConsumer, i, j);
      matrixStack.translate(0.0D, (double)(-shulkerBoxBlockEntity.getAnimationProgress(f) * 0.5F), 0.0D);
      matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(270.0F * shulkerBoxBlockEntity.getAnimationProgress(f)));
      this.model.getTopShell().render(matrixStack, vertexConsumer, i, j);
      matrixStack.pop();
   }
}
