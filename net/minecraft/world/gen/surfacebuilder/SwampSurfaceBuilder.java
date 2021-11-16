package net.minecraft.world.gen.surfacebuilder;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

public class SwampSurfaceBuilder extends SurfaceBuilder<TernarySurfaceConfig> {
   public SwampSurfaceBuilder(Codec<TernarySurfaceConfig> codec) {
      super(codec);
   }

   public void generate(Random random, Chunk chunk, Biome biome, int i, int j, int k, double d, BlockState blockState, BlockState blockState2, int l, long m, TernarySurfaceConfig ternarySurfaceConfig) {
      double e = Biome.FOLIAGE_NOISE.sample((double)i * 0.25D, (double)j * 0.25D, false);
      if (e > 0.0D) {
         int n = i & 15;
         int o = j & 15;
         BlockPos.Mutable mutable = new BlockPos.Mutable();

         for(int p = k; p >= 0; --p) {
            mutable.set(n, p, o);
            if (!chunk.getBlockState(mutable).isAir()) {
               if (p == 62 && !chunk.getBlockState(mutable).isOf(blockState2.getBlock())) {
                  chunk.setBlockState(mutable, blockState2, false);
               }
               break;
            }
         }
      }

      SurfaceBuilder.DEFAULT.generate(random, chunk, biome, i, j, k, d, blockState, blockState2, l, m, ternarySurfaceConfig);
   }
}
