package net.minecraft.client.render.chunk;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.level.ColorResolver;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChunkRendererRegion implements BlockRenderView {
   protected final int chunkXOffset;
   protected final int chunkZOffset;
   protected final BlockPos offset;
   protected final int sizeX;
   protected final int sizeY;
   protected final int sizeZ;
   protected final WorldChunk[][] chunks;
   protected final BlockState[] blockStates;
   protected final FluidState[] fluidStates;
   protected final World world;

   @Nullable
   public static ChunkRendererRegion create(World world, BlockPos startPos, BlockPos endPos, int chunkRadius) {
      int i = startPos.getX() - chunkRadius >> 4;
      int j = startPos.getZ() - chunkRadius >> 4;
      int k = endPos.getX() + chunkRadius >> 4;
      int l = endPos.getZ() + chunkRadius >> 4;
      WorldChunk[][] worldChunks = new WorldChunk[k - i + 1][l - j + 1];

      for(int m = i; m <= k; ++m) {
         for(int n = j; n <= l; ++n) {
            worldChunks[m - i][n - j] = world.getChunk(m, n);
         }
      }

      if (method_30000(startPos, endPos, i, j, worldChunks)) {
         return null;
      } else {
         int o = true;
         BlockPos blockPos = startPos.add(-1, -1, -1);
         BlockPos blockPos2 = endPos.add(1, 1, 1);
         return new ChunkRendererRegion(world, i, j, worldChunks, blockPos, blockPos2);
      }
   }

   public static boolean method_30000(BlockPos blockPos, BlockPos blockPos2, int i, int j, WorldChunk[][] worldChunks) {
      for(int k = blockPos.getX() >> 4; k <= blockPos2.getX() >> 4; ++k) {
         for(int l = blockPos.getZ() >> 4; l <= blockPos2.getZ() >> 4; ++l) {
            WorldChunk worldChunk = worldChunks[k - i][l - j];
            if (!worldChunk.areSectionsEmptyBetween(blockPos.getY(), blockPos2.getY())) {
               return false;
            }
         }
      }

      return true;
   }

   public ChunkRendererRegion(World world, int chunkX, int chunkZ, WorldChunk[][] chunks, BlockPos startPos, BlockPos endPos) {
      this.world = world;
      this.chunkXOffset = chunkX;
      this.chunkZOffset = chunkZ;
      this.chunks = chunks;
      this.offset = startPos;
      this.sizeX = endPos.getX() - startPos.getX() + 1;
      this.sizeY = endPos.getY() - startPos.getY() + 1;
      this.sizeZ = endPos.getZ() - startPos.getZ() + 1;
      this.blockStates = new BlockState[this.sizeX * this.sizeY * this.sizeZ];
      this.fluidStates = new FluidState[this.sizeX * this.sizeY * this.sizeZ];

      BlockPos blockPos;
      WorldChunk worldChunk;
      int k;
      for(Iterator var7 = BlockPos.iterate(startPos, endPos).iterator(); var7.hasNext(); this.fluidStates[k] = worldChunk.getFluidState(blockPos)) {
         blockPos = (BlockPos)var7.next();
         int i = (blockPos.getX() >> 4) - chunkX;
         int j = (blockPos.getZ() >> 4) - chunkZ;
         worldChunk = chunks[i][j];
         k = this.getIndex(blockPos);
         this.blockStates[k] = worldChunk.getBlockState(blockPos);
      }

   }

   protected final int getIndex(BlockPos pos) {
      return this.getIndex(pos.getX(), pos.getY(), pos.getZ());
   }

   protected int getIndex(int x, int y, int z) {
      int i = x - this.offset.getX();
      int j = y - this.offset.getY();
      int k = z - this.offset.getZ();
      return k * this.sizeX * this.sizeY + j * this.sizeX + i;
   }

   public BlockState getBlockState(BlockPos pos) {
      return this.blockStates[this.getIndex(pos)];
   }

   public FluidState getFluidState(BlockPos pos) {
      return this.fluidStates[this.getIndex(pos)];
   }

   public float getBrightness(Direction direction, boolean shaded) {
      return this.world.getBrightness(direction, shaded);
   }

   public LightingProvider getLightingProvider() {
      return this.world.getLightingProvider();
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos) {
      return this.getBlockEntity(pos, WorldChunk.CreationType.IMMEDIATE);
   }

   @Nullable
   public BlockEntity getBlockEntity(BlockPos pos, WorldChunk.CreationType creationType) {
      int i = (pos.getX() >> 4) - this.chunkXOffset;
      int j = (pos.getZ() >> 4) - this.chunkZOffset;
      return this.chunks[i][j].getBlockEntity(pos, creationType);
   }

   public int getColor(BlockPos pos, ColorResolver colorResolver) {
      return this.world.getColor(pos, colorResolver);
   }
}
