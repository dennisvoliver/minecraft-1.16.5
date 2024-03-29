package net.minecraft.util.math.noise;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.world.gen.ChunkRandom;

public class OctaveSimplexNoiseSampler implements NoiseSampler {
   private final SimplexNoiseSampler[] octaveSamplers;
   private final double field_20661;
   private final double field_20662;

   public OctaveSimplexNoiseSampler(ChunkRandom chunkRandom, IntStream intStream) {
      this(chunkRandom, (List)intStream.boxed().collect(ImmutableList.toImmutableList()));
   }

   public OctaveSimplexNoiseSampler(ChunkRandom chunkRandom, List<Integer> list) {
      this(chunkRandom, (IntSortedSet)(new IntRBTreeSet(list)));
   }

   private OctaveSimplexNoiseSampler(ChunkRandom chunkRandom, IntSortedSet intSortedSet) {
      if (intSortedSet.isEmpty()) {
         throw new IllegalArgumentException("Need some octaves!");
      } else {
         int i = -intSortedSet.firstInt();
         int j = intSortedSet.lastInt();
         int k = i + j + 1;
         if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
         } else {
            SimplexNoiseSampler simplexNoiseSampler = new SimplexNoiseSampler(chunkRandom);
            int l = j;
            this.octaveSamplers = new SimplexNoiseSampler[k];
            if (j >= 0 && j < k && intSortedSet.contains(0)) {
               this.octaveSamplers[j] = simplexNoiseSampler;
            }

            for(int m = j + 1; m < k; ++m) {
               if (m >= 0 && intSortedSet.contains(l - m)) {
                  this.octaveSamplers[m] = new SimplexNoiseSampler(chunkRandom);
               } else {
                  chunkRandom.consume(262);
               }
            }

            if (j > 0) {
               long n = (long)(simplexNoiseSampler.method_22416(simplexNoiseSampler.originX, simplexNoiseSampler.originY, simplexNoiseSampler.originZ) * 9.223372036854776E18D);
               ChunkRandom chunkRandom2 = new ChunkRandom(n);

               for(int o = l - 1; o >= 0; --o) {
                  if (o < k && intSortedSet.contains(l - o)) {
                     this.octaveSamplers[o] = new SimplexNoiseSampler(chunkRandom2);
                  } else {
                     chunkRandom2.consume(262);
                  }
               }
            }

            this.field_20662 = Math.pow(2.0D, (double)j);
            this.field_20661 = 1.0D / (Math.pow(2.0D, (double)k) - 1.0D);
         }
      }
   }

   public double sample(double x, double y, boolean useOrigin) {
      double d = 0.0D;
      double e = this.field_20662;
      double f = this.field_20661;
      SimplexNoiseSampler[] var12 = this.octaveSamplers;
      int var13 = var12.length;

      for(int var14 = 0; var14 < var13; ++var14) {
         SimplexNoiseSampler simplexNoiseSampler = var12[var14];
         if (simplexNoiseSampler != null) {
            d += simplexNoiseSampler.sample(x * e + (useOrigin ? simplexNoiseSampler.originX : 0.0D), y * e + (useOrigin ? simplexNoiseSampler.originY : 0.0D)) * f;
         }

         e /= 2.0D;
         f *= 2.0D;
      }

      return d;
   }

   public double sample(double x, double y, double yScale, double yMax) {
      return this.sample(x, y, true) * 0.55D;
   }
}
