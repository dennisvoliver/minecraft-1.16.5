package net.minecraft.world.gen.treedecorator;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;

public class CocoaBeansTreeDecorator extends TreeDecorator {
   public static final Codec<CocoaBeansTreeDecorator> CODEC = Codec.floatRange(0.0F, 1.0F).fieldOf("probability").xmap(CocoaBeansTreeDecorator::new, (cocoaBeansTreeDecorator) -> {
      return cocoaBeansTreeDecorator.probability;
   }).codec();
   private final float probability;

   public CocoaBeansTreeDecorator(float probability) {
      this.probability = probability;
   }

   protected TreeDecoratorType<?> getType() {
      return TreeDecoratorType.COCOA;
   }

   public void generate(StructureWorldAccess world, Random random, List<BlockPos> logPositions, List<BlockPos> leavesPositions, Set<BlockPos> placedStates, BlockBox box) {
      if (!(random.nextFloat() >= this.probability)) {
         int i = ((BlockPos)logPositions.get(0)).getY();
         logPositions.stream().filter((pos) -> {
            return pos.getY() - i <= 2;
         }).forEach((pos) -> {
            Iterator var6 = Direction.Type.HORIZONTAL.iterator();

            while(var6.hasNext()) {
               Direction direction = (Direction)var6.next();
               if (random.nextFloat() <= 0.25F) {
                  Direction direction2 = direction.getOpposite();
                  BlockPos blockPos = pos.add(direction2.getOffsetX(), 0, direction2.getOffsetZ());
                  if (Feature.isAir(world, blockPos)) {
                     BlockState blockState = (BlockState)((BlockState)Blocks.COCOA.getDefaultState().with(CocoaBlock.AGE, random.nextInt(3))).with(CocoaBlock.FACING, direction);
                     this.setBlockStateAndEncompassPosition(world, blockPos, blockState, placedStates, box);
                  }
               }
            }

         });
      }
   }
}
