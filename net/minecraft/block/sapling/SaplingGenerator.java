package net.minecraft.block.sapling;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.jetbrains.annotations.Nullable;

public abstract class SaplingGenerator {
   @Nullable
   protected abstract ConfiguredFeature<TreeFeatureConfig, ?> createTreeFeature(Random random, boolean bees);

   public boolean generate(ServerWorld world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random random) {
      ConfiguredFeature<TreeFeatureConfig, ?> configuredFeature = this.createTreeFeature(random, this.method_24282(world, pos));
      if (configuredFeature == null) {
         return false;
      } else {
         world.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);
         ((TreeFeatureConfig)configuredFeature.config).ignoreFluidCheck();
         if (configuredFeature.generate(world, chunkGenerator, random, pos)) {
            return true;
         } else {
            world.setBlockState(pos, state, 4);
            return false;
         }
      }
   }

   private boolean method_24282(WorldAccess worldAccess, BlockPos blockPos) {
      Iterator var3 = BlockPos.Mutable.iterate(blockPos.down().north(2).west(2), blockPos.up().south(2).east(2)).iterator();

      BlockPos blockPos2;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         blockPos2 = (BlockPos)var3.next();
      } while(!worldAccess.getBlockState(blockPos2).isIn(BlockTags.FLOWERS));

      return true;
   }
}
