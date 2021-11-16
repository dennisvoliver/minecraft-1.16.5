package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class OreFeature extends Feature<OreFeatureConfig> {
   public OreFeature(Codec<OreFeatureConfig> codec) {
      super(codec);
   }

   public boolean generate(StructureWorldAccess structureWorldAccess, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, OreFeatureConfig oreFeatureConfig) {
      float f = random.nextFloat() * 3.1415927F;
      float g = (float)oreFeatureConfig.size / 8.0F;
      int i = MathHelper.ceil(((float)oreFeatureConfig.size / 16.0F * 2.0F + 1.0F) / 2.0F);
      double d = (double)blockPos.getX() + Math.sin((double)f) * (double)g;
      double e = (double)blockPos.getX() - Math.sin((double)f) * (double)g;
      double h = (double)blockPos.getZ() + Math.cos((double)f) * (double)g;
      double j = (double)blockPos.getZ() - Math.cos((double)f) * (double)g;
      int k = true;
      double l = (double)(blockPos.getY() + random.nextInt(3) - 2);
      double m = (double)(blockPos.getY() + random.nextInt(3) - 2);
      int n = blockPos.getX() - MathHelper.ceil(g) - i;
      int o = blockPos.getY() - 2 - i;
      int p = blockPos.getZ() - MathHelper.ceil(g) - i;
      int q = 2 * (MathHelper.ceil(g) + i);
      int r = 2 * (2 + i);

      for(int s = n; s <= n + q; ++s) {
         for(int t = p; t <= p + q; ++t) {
            if (o <= structureWorldAccess.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, s, t)) {
               return this.generateVeinPart(structureWorldAccess, random, oreFeatureConfig, d, e, h, j, l, m, n, o, p, q, r);
            }
         }
      }

      return false;
   }

   protected boolean generateVeinPart(WorldAccess world, Random random, OreFeatureConfig config, double startX, double endX, double startZ, double endZ, double startY, double endY, int x, int y, int z, int horizontalSize, int verticalSize) {
      int i = 0;
      BitSet bitSet = new BitSet(horizontalSize * verticalSize * horizontalSize);
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      int j = config.size;
      double[] ds = new double[j * 4];

      int m;
      double o;
      double p;
      double q;
      double r;
      for(m = 0; m < j; ++m) {
         float f = (float)m / (float)j;
         o = MathHelper.lerp((double)f, startX, endX);
         p = MathHelper.lerp((double)f, startY, endY);
         q = MathHelper.lerp((double)f, startZ, endZ);
         r = random.nextDouble() * (double)j / 16.0D;
         double l = ((double)(MathHelper.sin(3.1415927F * f) + 1.0F) * r + 1.0D) / 2.0D;
         ds[m * 4 + 0] = o;
         ds[m * 4 + 1] = p;
         ds[m * 4 + 2] = q;
         ds[m * 4 + 3] = l;
      }

      for(m = 0; m < j - 1; ++m) {
         if (!(ds[m * 4 + 3] <= 0.0D)) {
            for(int n = m + 1; n < j; ++n) {
               if (!(ds[n * 4 + 3] <= 0.0D)) {
                  o = ds[m * 4 + 0] - ds[n * 4 + 0];
                  p = ds[m * 4 + 1] - ds[n * 4 + 1];
                  q = ds[m * 4 + 2] - ds[n * 4 + 2];
                  r = ds[m * 4 + 3] - ds[n * 4 + 3];
                  if (r * r > o * o + p * p + q * q) {
                     if (r > 0.0D) {
                        ds[n * 4 + 3] = -1.0D;
                     } else {
                        ds[m * 4 + 3] = -1.0D;
                     }
                  }
               }
            }
         }
      }

      for(m = 0; m < j; ++m) {
         double t = ds[m * 4 + 3];
         if (!(t < 0.0D)) {
            double u = ds[m * 4 + 0];
            double v = ds[m * 4 + 1];
            double w = ds[m * 4 + 2];
            int aa = Math.max(MathHelper.floor(u - t), x);
            int ab = Math.max(MathHelper.floor(v - t), y);
            int ac = Math.max(MathHelper.floor(w - t), z);
            int ad = Math.max(MathHelper.floor(u + t), aa);
            int ae = Math.max(MathHelper.floor(v + t), ab);
            int af = Math.max(MathHelper.floor(w + t), ac);

            for(int ag = aa; ag <= ad; ++ag) {
               double ah = ((double)ag + 0.5D - u) / t;
               if (ah * ah < 1.0D) {
                  for(int ai = ab; ai <= ae; ++ai) {
                     double aj = ((double)ai + 0.5D - v) / t;
                     if (ah * ah + aj * aj < 1.0D) {
                        for(int ak = ac; ak <= af; ++ak) {
                           double al = ((double)ak + 0.5D - w) / t;
                           if (ah * ah + aj * aj + al * al < 1.0D) {
                              int am = ag - x + (ai - y) * horizontalSize + (ak - z) * horizontalSize * verticalSize;
                              if (!bitSet.get(am)) {
                                 bitSet.set(am);
                                 mutable.set(ag, ai, ak);
                                 if (config.target.test(world.getBlockState(mutable), random)) {
                                    world.setBlockState(mutable, config.state, 2);
                                    ++i;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return i > 0;
   }
}
