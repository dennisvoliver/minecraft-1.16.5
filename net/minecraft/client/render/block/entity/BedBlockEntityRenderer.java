package net.minecraft.client.render.block.entity;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class BedBlockEntityRenderer extends BlockEntityRenderer<BedBlockEntity> {
   private final ModelPart field_20813 = new ModelPart(64, 64, 0, 0);
   private final ModelPart field_20814;
   private final ModelPart[] legs = new ModelPart[4];

   public BedBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
      super(blockEntityRenderDispatcher);
      this.field_20813.addCuboid(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F, 0.0F);
      this.field_20814 = new ModelPart(64, 64, 0, 22);
      this.field_20814.addCuboid(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F, 0.0F);
      this.legs[0] = new ModelPart(64, 64, 50, 0);
      this.legs[1] = new ModelPart(64, 64, 50, 6);
      this.legs[2] = new ModelPart(64, 64, 50, 12);
      this.legs[3] = new ModelPart(64, 64, 50, 18);
      this.legs[0].addCuboid(0.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F);
      this.legs[1].addCuboid(0.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F);
      this.legs[2].addCuboid(-16.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F);
      this.legs[3].addCuboid(-16.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F);
      this.legs[0].pitch = 1.5707964F;
      this.legs[1].pitch = 1.5707964F;
      this.legs[2].pitch = 1.5707964F;
      this.legs[3].pitch = 1.5707964F;
      this.legs[0].roll = 0.0F;
      this.legs[1].roll = 1.5707964F;
      this.legs[2].roll = 4.712389F;
      this.legs[3].roll = 3.1415927F;
   }

   public void render(BedBlockEntity bedBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      SpriteIdentifier spriteIdentifier = TexturedRenderLayers.BED_TEXTURES[bedBlockEntity.getColor().getId()];
      World world = bedBlockEntity.getWorld();
      if (world != null) {
         BlockState blockState = bedBlockEntity.getCachedState();
         DoubleBlockProperties.PropertySource<? extends BedBlockEntity> propertySource = DoubleBlockProperties.toPropertySource(BlockEntityType.BED, BedBlock::getBedPart, BedBlock::getOppositePartDirection, ChestBlock.FACING, blockState, world, bedBlockEntity.getPos(), (worldAccess, blockPos) -> {
            return false;
         });
         int k = ((Int2IntFunction)propertySource.apply(new LightmapCoordinatesRetriever())).get(i);
         this.method_3558(matrixStack, vertexConsumerProvider, blockState.get(BedBlock.PART) == BedPart.HEAD, (Direction)blockState.get(BedBlock.FACING), spriteIdentifier, k, j, false);
      } else {
         this.method_3558(matrixStack, vertexConsumerProvider, true, Direction.SOUTH, spriteIdentifier, i, j, false);
         this.method_3558(matrixStack, vertexConsumerProvider, false, Direction.SOUTH, spriteIdentifier, i, j, true);
      }

   }

   private void method_3558(MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, boolean bl, Direction direction, SpriteIdentifier spriteIdentifier, int light, int overlay, boolean bl2) {
      this.field_20813.visible = bl;
      this.field_20814.visible = !bl;
      this.legs[0].visible = !bl;
      this.legs[1].visible = bl;
      this.legs[2].visible = !bl;
      this.legs[3].visible = bl;
      matrix.push();
      matrix.translate(0.0D, 0.5625D, bl2 ? -1.0D : 0.0D);
      matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0F));
      matrix.translate(0.5D, 0.5D, 0.5D);
      matrix.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F + direction.asRotation()));
      matrix.translate(-0.5D, -0.5D, -0.5D);
      VertexConsumer vertexConsumer = spriteIdentifier.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntitySolid);
      this.field_20813.render(matrix, vertexConsumer, light, overlay);
      this.field_20814.render(matrix, vertexConsumer, light, overlay);
      this.legs[0].render(matrix, vertexConsumer, light, overlay);
      this.legs[1].render(matrix, vertexConsumer, light, overlay);
      this.legs[2].render(matrix, vertexConsumer, light, overlay);
      this.legs[3].render(matrix, vertexConsumer, light, overlay);
      matrix.pop();
   }
}
