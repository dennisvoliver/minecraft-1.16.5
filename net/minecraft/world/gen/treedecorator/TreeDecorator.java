package net.minecraft.world.gen.treedecorator;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ModifiableWorld;
import net.minecraft.world.StructureWorldAccess;

/**
 * Tree decorators can add additional blocks to trees, such as vines or beehives.
 */
public abstract class TreeDecorator {
   public static final Codec<TreeDecorator> TYPE_CODEC;

   protected abstract TreeDecoratorType<?> getType();

   public abstract void generate(StructureWorldAccess world, Random random, List<BlockPos> logPositions, List<BlockPos> leavesPositions, Set<BlockPos> placedStates, BlockBox box);

   protected void placeVine(ModifiableWorld world, BlockPos pos, BooleanProperty directionProperty, Set<BlockPos> placedStates, BlockBox box) {
      this.setBlockStateAndEncompassPosition(world, pos, (BlockState)Blocks.VINE.getDefaultState().with(directionProperty, true), placedStates, box);
   }

   protected void setBlockStateAndEncompassPosition(ModifiableWorld world, BlockPos pos, BlockState state, Set<BlockPos> placedStates, BlockBox box) {
      world.setBlockState(pos, state, 19);
      placedStates.add(pos);
      box.encompass(new BlockBox(pos, pos));
   }

   static {
      TYPE_CODEC = Registry.TREE_DECORATOR_TYPE.dispatch(TreeDecorator::getType, TreeDecoratorType::getCodec);
   }
}
