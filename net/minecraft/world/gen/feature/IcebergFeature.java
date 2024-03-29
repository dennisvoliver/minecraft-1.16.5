package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class IcebergFeature extends Feature<SingleStateFeatureConfig> {
   public IcebergFeature(Codec<SingleStateFeatureConfig> codec) {
      super(codec);
   }

   public boolean generate(StructureWorldAccess structureWorldAccess, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, SingleStateFeatureConfig singleStateFeatureConfig) {
      blockPos = new BlockPos(blockPos.getX(), chunkGenerator.getSeaLevel(), blockPos.getZ());
      boolean bl = random.nextDouble() > 0.7D;
      BlockState blockState = singleStateFeatureConfig.state;
      double d = random.nextDouble() * 2.0D * 3.141592653589793D;
      int i = 11 - random.nextInt(5);
      int j = 3 + random.nextInt(3);
      boolean bl2 = random.nextDouble() > 0.7D;
      int k = true;
      int l = bl2 ? random.nextInt(6) + 6 : random.nextInt(15) + 3;
      if (!bl2 && random.nextDouble() > 0.9D) {
         l += random.nextInt(19) + 7;
      }

      int m = Math.min(l + random.nextInt(11), 18);
      int n = Math.min(l + random.nextInt(7) - random.nextInt(5), 11);
      int o = bl2 ? i : 11;

      int t;
      int u;
      int v;
      int w;
      for(t = -o; t < o; ++t) {
         for(u = -o; u < o; ++u) {
            for(v = 0; v < l; ++v) {
               w = bl2 ? this.method_13417(v, l, n) : this.method_13419(random, v, l, n);
               if (bl2 || t < w) {
                  this.method_13426(structureWorldAccess, random, blockPos, l, t, v, u, w, o, bl2, j, d, bl, blockState);
               }
            }
         }
      }

      this.method_13418(structureWorldAccess, blockPos, n, l, bl2, i);

      for(t = -o; t < o; ++t) {
         for(u = -o; u < o; ++u) {
            for(v = -1; v > -m; --v) {
               w = bl2 ? MathHelper.ceil((float)o * (1.0F - (float)Math.pow((double)v, 2.0D) / ((float)m * 8.0F))) : o;
               int x = this.method_13427(random, -v, m, n);
               if (t < x) {
                  this.method_13426(structureWorldAccess, random, blockPos, m, t, v, u, x, w, bl2, j, d, bl, blockState);
               }
            }
         }
      }

      boolean bl3 = bl2 ? random.nextDouble() > 0.1D : random.nextDouble() > 0.7D;
      if (bl3) {
         this.method_13428(random, structureWorldAccess, n, l, blockPos, bl2, i, d, j);
      }

      return true;
   }

   private void method_13428(Random random, WorldAccess worldAccess, int i, int j, BlockPos blockPos, boolean bl, int k, double d, int l) {
      int m = random.nextBoolean() ? -1 : 1;
      int n = random.nextBoolean() ? -1 : 1;
      int o = random.nextInt(Math.max(i / 2 - 2, 1));
      if (random.nextBoolean()) {
         o = i / 2 + 1 - random.nextInt(Math.max(i - i / 2 - 1, 1));
      }

      int p = random.nextInt(Math.max(i / 2 - 2, 1));
      if (random.nextBoolean()) {
         p = i / 2 + 1 - random.nextInt(Math.max(i - i / 2 - 1, 1));
      }

      if (bl) {
         o = p = random.nextInt(Math.max(k - 5, 1));
      }

      BlockPos blockPos2 = new BlockPos(m * o, 0, n * p);
      double e = bl ? d + 1.5707963267948966D : random.nextDouble() * 2.0D * 3.141592653589793D;

      int s;
      int t;
      for(s = 0; s < j - 3; ++s) {
         t = this.method_13419(random, s, j, i);
         this.method_13415(t, s, blockPos, worldAccess, false, e, blockPos2, k, l);
      }

      for(s = -1; s > -j + random.nextInt(5); --s) {
         t = this.method_13427(random, -s, j, i);
         this.method_13415(t, s, blockPos, worldAccess, true, e, blockPos2, k, l);
      }

   }

   private void method_13415(int i, int j, BlockPos blockPos, WorldAccess worldAccess, boolean bl, double d, BlockPos blockPos2, int k, int l) {
      int m = i + 1 + k / 3;
      int n = Math.min(i - 3, 3) + l / 2 - 1;

      for(int o = -m; o < m; ++o) {
         for(int p = -m; p < m; ++p) {
            double e = this.method_13424(o, p, blockPos2, m, n, d);
            if (e < 0.0D) {
               BlockPos blockPos3 = blockPos.add(o, j, p);
               Block block = worldAccess.getBlockState(blockPos3).getBlock();
               if (this.isSnowyOrIcy(block) || block == Blocks.SNOW_BLOCK) {
                  if (bl) {
                     this.setBlockState(worldAccess, blockPos3, Blocks.WATER.getDefaultState());
                  } else {
                     this.setBlockState(worldAccess, blockPos3, Blocks.AIR.getDefaultState());
                     this.clearSnowAbove(worldAccess, blockPos3);
                  }
               }
            }
         }
      }

   }

   private void clearSnowAbove(WorldAccess world, BlockPos pos) {
      if (world.getBlockState(pos.up()).isOf(Blocks.SNOW)) {
         this.setBlockState(world, pos.up(), Blocks.AIR.getDefaultState());
      }

   }

   private void method_13426(WorldAccess worldAccess, Random random, BlockPos blockPos, int i, int j, int k, int l, int m, int n, boolean bl, int o, double d, boolean bl2, BlockState blockState) {
      double e = bl ? this.method_13424(j, l, BlockPos.ORIGIN, n, this.method_13416(k, i, o), d) : this.method_13421(j, l, BlockPos.ORIGIN, m, random);
      if (e < 0.0D) {
         BlockPos blockPos2 = blockPos.add(j, k, l);
         double f = bl ? -0.5D : (double)(-6 - random.nextInt(3));
         if (e > f && random.nextDouble() > 0.9D) {
            return;
         }

         this.method_13425(blockPos2, worldAccess, random, i - k, i, bl, bl2, blockState);
      }

   }

   private void method_13425(BlockPos blockPos, WorldAccess worldAccess, Random random, int i, int j, boolean bl, boolean bl2, BlockState blockState) {
      BlockState blockState2 = worldAccess.getBlockState(blockPos);
      if (blockState2.getMaterial() == Material.AIR || blockState2.isOf(Blocks.SNOW_BLOCK) || blockState2.isOf(Blocks.ICE) || blockState2.isOf(Blocks.WATER)) {
         boolean bl3 = !bl || random.nextDouble() > 0.05D;
         int k = bl ? 3 : 2;
         if (bl2 && !blockState2.isOf(Blocks.WATER) && (double)i <= (double)random.nextInt(Math.max(1, j / k)) + (double)j * 0.6D && bl3) {
            this.setBlockState(worldAccess, blockPos, Blocks.SNOW_BLOCK.getDefaultState());
         } else {
            this.setBlockState(worldAccess, blockPos, blockState);
         }
      }

   }

   private int method_13416(int i, int j, int k) {
      int l = k;
      if (i > 0 && j - i <= 3) {
         l = k - (4 - (j - i));
      }

      return l;
   }

   private double method_13421(int i, int j, BlockPos blockPos, int k, Random random) {
      float f = 10.0F * MathHelper.clamp(random.nextFloat(), 0.2F, 0.8F) / (float)k;
      return (double)f + Math.pow((double)(i - blockPos.getX()), 2.0D) + Math.pow((double)(j - blockPos.getZ()), 2.0D) - Math.pow((double)k, 2.0D);
   }

   private double method_13424(int i, int j, BlockPos blockPos, int k, int l, double d) {
      return Math.pow(((double)(i - blockPos.getX()) * Math.cos(d) - (double)(j - blockPos.getZ()) * Math.sin(d)) / (double)k, 2.0D) + Math.pow(((double)(i - blockPos.getX()) * Math.sin(d) + (double)(j - blockPos.getZ()) * Math.cos(d)) / (double)l, 2.0D) - 1.0D;
   }

   private int method_13419(Random random, int i, int j, int k) {
      float f = 3.5F - random.nextFloat();
      float g = (1.0F - (float)Math.pow((double)i, 2.0D) / ((float)j * f)) * (float)k;
      if (j > 15 + random.nextInt(5)) {
         int l = i < 3 + random.nextInt(6) ? i / 2 : i;
         g = (1.0F - (float)l / ((float)j * f * 0.4F)) * (float)k;
      }

      return MathHelper.ceil(g / 2.0F);
   }

   private int method_13417(int i, int j, int k) {
      float f = 1.0F;
      float g = (1.0F - (float)Math.pow((double)i, 2.0D) / ((float)j * 1.0F)) * (float)k;
      return MathHelper.ceil(g / 2.0F);
   }

   private int method_13427(Random random, int i, int j, int k) {
      float f = 1.0F + random.nextFloat() / 2.0F;
      float g = (1.0F - (float)i / ((float)j * f)) * (float)k;
      return MathHelper.ceil(g / 2.0F);
   }

   private boolean isSnowyOrIcy(Block block) {
      return block == Blocks.PACKED_ICE || block == Blocks.SNOW_BLOCK || block == Blocks.BLUE_ICE;
   }

   private boolean isAirBelow(BlockView world, BlockPos pos) {
      return world.getBlockState(pos.down()).getMaterial() == Material.AIR;
   }

   private void method_13418(WorldAccess world, BlockPos pos, int i, int height, boolean bl, int j) {
      int k = bl ? j : i / 2;

      for(int l = -k; l <= k; ++l) {
         for(int m = -k; m <= k; ++m) {
            for(int n = 0; n <= height; ++n) {
               BlockPos blockPos = pos.add(l, n, m);
               Block block = world.getBlockState(blockPos).getBlock();
               if (this.isSnowyOrIcy(block) || block == Blocks.SNOW) {
                  if (this.isAirBelow(world, blockPos)) {
                     this.setBlockState(world, blockPos, Blocks.AIR.getDefaultState());
                     this.setBlockState(world, blockPos.up(), Blocks.AIR.getDefaultState());
                  } else if (this.isSnowyOrIcy(block)) {
                     Block[] blocks = new Block[]{world.getBlockState(blockPos.west()).getBlock(), world.getBlockState(blockPos.east()).getBlock(), world.getBlockState(blockPos.north()).getBlock(), world.getBlockState(blockPos.south()).getBlock()};
                     int o = 0;
                     Block[] var15 = blocks;
                     int var16 = blocks.length;

                     for(int var17 = 0; var17 < var16; ++var17) {
                        Block block2 = var15[var17];
                        if (!this.isSnowyOrIcy(block2)) {
                           ++o;
                        }
                     }

                     if (o >= 3) {
                        this.setBlockState(world, blockPos, Blocks.AIR.getDefaultState());
                     }
                  }
               }
            }
         }
      }

   }
}
