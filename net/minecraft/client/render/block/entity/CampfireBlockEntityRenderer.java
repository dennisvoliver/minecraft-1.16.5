package net.minecraft.client.render.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class CampfireBlockEntityRenderer extends BlockEntityRenderer<CampfireBlockEntity> {
   public CampfireBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
      super(blockEntityRenderDispatcher);
   }

   public void render(CampfireBlockEntity campfireBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      Direction direction = (Direction)campfireBlockEntity.getCachedState().get(CampfireBlock.FACING);
      DefaultedList<ItemStack> defaultedList = campfireBlockEntity.getItemsBeingCooked();

      for(int k = 0; k < defaultedList.size(); ++k) {
         ItemStack itemStack = (ItemStack)defaultedList.get(k);
         if (itemStack != ItemStack.EMPTY) {
            matrixStack.push();
            matrixStack.translate(0.5D, 0.44921875D, 0.5D);
            Direction direction2 = Direction.fromHorizontal((k + direction.getHorizontal()) % 4);
            float g = -direction2.asRotation();
            matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(g));
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0F));
            matrixStack.translate(-0.3125D, -0.3125D, 0.0D);
            matrixStack.scale(0.375F, 0.375F, 0.375F);
            MinecraftClient.getInstance().getItemRenderer().renderItem(itemStack, ModelTransformation.Mode.FIXED, i, j, matrixStack, vertexConsumerProvider);
            matrixStack.pop();
         }
      }

   }
}
