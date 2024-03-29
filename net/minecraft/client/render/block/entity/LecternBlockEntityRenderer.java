package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class LecternBlockEntityRenderer extends BlockEntityRenderer<LecternBlockEntity> {
   private final BookModel book = new BookModel();

   public LecternBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
      super(blockEntityRenderDispatcher);
   }

   public void render(LecternBlockEntity lecternBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      BlockState blockState = lecternBlockEntity.getCachedState();
      if ((Boolean)blockState.get(LecternBlock.HAS_BOOK)) {
         matrixStack.push();
         matrixStack.translate(0.5D, 1.0625D, 0.5D);
         float g = ((Direction)blockState.get(LecternBlock.FACING)).rotateYClockwise().asRotation();
         matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-g));
         matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(67.5F));
         matrixStack.translate(0.0D, -0.125D, 0.0D);
         this.book.setPageAngles(0.0F, 0.1F, 0.9F, 1.2F);
         VertexConsumer vertexConsumer = EnchantingTableBlockEntityRenderer.BOOK_TEXTURE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntitySolid);
         this.book.renderBook(matrixStack, vertexConsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
         matrixStack.pop();
      }
   }
}
