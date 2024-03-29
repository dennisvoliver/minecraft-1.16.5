package net.minecraft.structure;

import com.google.common.collect.ImmutableSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public abstract class StructurePiece {
   protected static final BlockState AIR;
   protected BlockBox boundingBox;
   @Nullable
   private Direction facing;
   private BlockMirror mirror;
   private BlockRotation rotation;
   protected int chainLength;
   private final StructurePieceType type;
   private static final Set<Block> BLOCKS_NEEDING_POST_PROCESSING;

   protected StructurePiece(StructurePieceType type, int length) {
      this.type = type;
      this.chainLength = length;
   }

   public StructurePiece(StructurePieceType type, NbtCompound nbt) {
      this(type, nbt.getInt("GD"));
      if (nbt.contains("BB")) {
         this.boundingBox = new BlockBox(nbt.getIntArray("BB"));
      }

      int i = nbt.getInt("O");
      this.setOrientation(i == -1 ? null : Direction.fromHorizontal(i));
   }

   public final NbtCompound getTag() {
      NbtCompound nbtCompound = new NbtCompound();
      nbtCompound.putString("id", Registry.STRUCTURE_PIECE.getId(this.getType()).toString());
      nbtCompound.put("BB", this.boundingBox.toNbt());
      Direction direction = this.getFacing();
      nbtCompound.putInt("O", direction == null ? -1 : direction.getHorizontal());
      nbtCompound.putInt("GD", this.chainLength);
      this.toNbt(nbtCompound);
      return nbtCompound;
   }

   protected abstract void toNbt(NbtCompound tag);

   public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
   }

   public abstract boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos);

   public BlockBox getBoundingBox() {
      return this.boundingBox;
   }

   public int getChainLength() {
      return this.chainLength;
   }

   public boolean intersectsChunk(ChunkPos pos, int offset) {
      int i = pos.x << 4;
      int j = pos.z << 4;
      return this.boundingBox.intersectsXZ(i - offset, j - offset, i + 15 + offset, j + 15 + offset);
   }

   public static StructurePiece getOverlappingPiece(List<StructurePiece> pieces, BlockBox blockBox) {
      Iterator var2 = pieces.iterator();

      StructurePiece structurePiece;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         structurePiece = (StructurePiece)var2.next();
      } while(structurePiece.getBoundingBox() == null || !structurePiece.getBoundingBox().intersects(blockBox));

      return structurePiece;
   }

   protected boolean isTouchingLiquid(BlockView blockView, BlockBox blockBox) {
      int i = Math.max(this.boundingBox.minX - 1, blockBox.minX);
      int j = Math.max(this.boundingBox.minY - 1, blockBox.minY);
      int k = Math.max(this.boundingBox.minZ - 1, blockBox.minZ);
      int l = Math.min(this.boundingBox.maxX + 1, blockBox.maxX);
      int m = Math.min(this.boundingBox.maxY + 1, blockBox.maxY);
      int n = Math.min(this.boundingBox.maxZ + 1, blockBox.maxZ);
      BlockPos.Mutable mutable = new BlockPos.Mutable();

      int s;
      int t;
      for(s = i; s <= l; ++s) {
         for(t = k; t <= n; ++t) {
            if (blockView.getBlockState(mutable.set(s, j, t)).getMaterial().isLiquid()) {
               return true;
            }

            if (blockView.getBlockState(mutable.set(s, m, t)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      for(s = i; s <= l; ++s) {
         for(t = j; t <= m; ++t) {
            if (blockView.getBlockState(mutable.set(s, t, k)).getMaterial().isLiquid()) {
               return true;
            }

            if (blockView.getBlockState(mutable.set(s, t, n)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      for(s = k; s <= n; ++s) {
         for(t = j; t <= m; ++t) {
            if (blockView.getBlockState(mutable.set(i, t, s)).getMaterial().isLiquid()) {
               return true;
            }

            if (blockView.getBlockState(mutable.set(l, t, s)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      return false;
   }

   protected int applyXTransform(int x, int z) {
      Direction direction = this.getFacing();
      if (direction == null) {
         return x;
      } else {
         switch(direction) {
         case NORTH:
         case SOUTH:
            return this.boundingBox.minX + x;
         case WEST:
            return this.boundingBox.maxX - z;
         case EAST:
            return this.boundingBox.minX + z;
         default:
            return x;
         }
      }
   }

   protected int applyYTransform(int y) {
      return this.getFacing() == null ? y : y + this.boundingBox.minY;
   }

   protected int applyZTransform(int x, int z) {
      Direction direction = this.getFacing();
      if (direction == null) {
         return z;
      } else {
         switch(direction) {
         case NORTH:
            return this.boundingBox.maxZ - z;
         case SOUTH:
            return this.boundingBox.minZ + z;
         case WEST:
         case EAST:
            return this.boundingBox.minZ + x;
         default:
            return z;
         }
      }
   }

   protected void addBlock(StructureWorldAccess world, BlockState block, int x, int i, int j, BlockBox box) {
      BlockPos blockPos = new BlockPos(this.applyXTransform(x, j), this.applyYTransform(i), this.applyZTransform(x, j));
      if (box.contains(blockPos)) {
         if (this.mirror != BlockMirror.NONE) {
            block = block.mirror(this.mirror);
         }

         if (this.rotation != BlockRotation.NONE) {
            block = block.rotate(this.rotation);
         }

         world.setBlockState(blockPos, block, 2);
         FluidState fluidState = world.getFluidState(blockPos);
         if (!fluidState.isEmpty()) {
            world.getFluidTickScheduler().schedule(blockPos, fluidState.getFluid(), 0);
         }

         if (BLOCKS_NEEDING_POST_PROCESSING.contains(block.getBlock())) {
            world.getChunk(blockPos).markBlockForPostProcessing(blockPos);
         }

      }
   }

   protected BlockState getBlockAt(BlockView world, int x, int i, int j, BlockBox box) {
      int k = this.applyXTransform(x, j);
      int l = this.applyYTransform(i);
      int m = this.applyZTransform(x, j);
      BlockPos blockPos = new BlockPos(k, l, m);
      return !box.contains(blockPos) ? Blocks.AIR.getDefaultState() : world.getBlockState(blockPos);
   }

   protected boolean isUnderSeaLevel(WorldView world, int x, int z, int y, BlockBox box) {
      int i = this.applyXTransform(x, y);
      int j = this.applyYTransform(z + 1);
      int k = this.applyZTransform(x, y);
      BlockPos blockPos = new BlockPos(i, j, k);
      if (!box.contains(blockPos)) {
         return false;
      } else {
         return j < world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, i, k);
      }
   }

   protected void fill(StructureWorldAccess world, BlockBox bounds, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      for(int i = minY; i <= maxY; ++i) {
         for(int j = minX; j <= maxX; ++j) {
            for(int k = minZ; k <= maxZ; ++k) {
               this.addBlock(world, Blocks.AIR.getDefaultState(), j, i, k, bounds);
            }
         }
      }

   }

   protected void fillWithOutline(StructureWorldAccess world, BlockBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState outline, BlockState inside, boolean cantReplaceAir) {
      for(int i = minY; i <= maxY; ++i) {
         for(int j = minX; j <= maxX; ++j) {
            for(int k = minZ; k <= maxZ; ++k) {
               if (!cantReplaceAir || !this.getBlockAt(world, j, i, k, box).isAir()) {
                  if (i != minY && i != maxY && j != minX && j != maxX && k != minZ && k != maxZ) {
                     this.addBlock(world, inside, j, i, k, box);
                  } else {
                     this.addBlock(world, outline, j, i, k, box);
                  }
               }
            }
         }
      }

   }

   protected void fillWithOutline(StructureWorldAccess world, BlockBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean cantReplaceAir, Random random, StructurePiece.BlockRandomizer randomizer) {
      for(int i = minY; i <= maxY; ++i) {
         for(int j = minX; j <= maxX; ++j) {
            for(int k = minZ; k <= maxZ; ++k) {
               if (!cantReplaceAir || !this.getBlockAt(world, j, i, k, box).isAir()) {
                  randomizer.setBlock(random, j, i, k, i == minY || i == maxY || j == minX || j == maxX || k == minZ || k == maxZ);
                  this.addBlock(world, randomizer.getBlock(), j, i, k, box);
               }
            }
         }
      }

   }

   protected void fillWithOutlineUnderSeaLevel(StructureWorldAccess world, BlockBox box, Random random, float blockChance, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState outline, BlockState inside, boolean cantReplaceAir, boolean stayBelowSeaLevel) {
      for(int i = minY; i <= maxY; ++i) {
         for(int j = minX; j <= maxX; ++j) {
            for(int k = minZ; k <= maxZ; ++k) {
               if (!(random.nextFloat() > blockChance) && (!cantReplaceAir || !this.getBlockAt(world, j, i, k, box).isAir()) && (!stayBelowSeaLevel || this.isUnderSeaLevel(world, j, i, k, box))) {
                  if (i != minY && i != maxY && j != minX && j != maxX && k != minZ && k != maxZ) {
                     this.addBlock(world, inside, j, i, k, box);
                  } else {
                     this.addBlock(world, outline, j, i, k, box);
                  }
               }
            }
         }
      }

   }

   protected void addBlockWithRandomThreshold(StructureWorldAccess world, BlockBox bounds, Random random, float threshold, int x, int y, int z, BlockState state) {
      if (random.nextFloat() < threshold) {
         this.addBlock(world, state, x, y, z, bounds);
      }

   }

   protected void fillHalfEllipsoid(StructureWorldAccess world, BlockBox bounds, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState block, boolean cantReplaceAir) {
      float f = (float)(maxX - minX + 1);
      float g = (float)(maxY - minY + 1);
      float h = (float)(maxZ - minZ + 1);
      float i = (float)minX + f / 2.0F;
      float j = (float)minZ + h / 2.0F;

      for(int k = minY; k <= maxY; ++k) {
         float l = (float)(k - minY) / g;

         for(int m = minX; m <= maxX; ++m) {
            float n = ((float)m - i) / (f * 0.5F);

            for(int o = minZ; o <= maxZ; ++o) {
               float p = ((float)o - j) / (h * 0.5F);
               if (!cantReplaceAir || !this.getBlockAt(world, m, k, o, bounds).isAir()) {
                  float q = n * n + l * l + p * p;
                  if (q <= 1.05F) {
                     this.addBlock(world, block, m, k, o, bounds);
                  }
               }
            }
         }
      }

   }

   protected void fillDownwards(StructureWorldAccess world, BlockState state, int x, int i, int j, BlockBox box) {
      int k = this.applyXTransform(x, j);
      int l = this.applyYTransform(i);
      int m = this.applyZTransform(x, j);
      if (box.contains(new BlockPos(k, l, m))) {
         while((world.isAir(new BlockPos(k, l, m)) || world.getBlockState(new BlockPos(k, l, m)).getMaterial().isLiquid()) && l > 1) {
            world.setBlockState(new BlockPos(k, l, m), state, 2);
            --l;
         }

      }
   }

   protected boolean addChest(StructureWorldAccess world, BlockBox boundingBox, Random random, int x, int y, int z, Identifier lootTableId) {
      BlockPos blockPos = new BlockPos(this.applyXTransform(x, z), this.applyYTransform(y), this.applyZTransform(x, z));
      return this.addChest(world, boundingBox, random, blockPos, lootTableId, (BlockState)null);
   }

   public static BlockState orientateChest(BlockView world, BlockPos pos, BlockState state) {
      Direction direction = null;
      Iterator var4 = Direction.Type.HORIZONTAL.iterator();

      while(var4.hasNext()) {
         Direction direction2 = (Direction)var4.next();
         BlockPos blockPos = pos.offset(direction2);
         BlockState blockState = world.getBlockState(blockPos);
         if (blockState.isOf(Blocks.CHEST)) {
            return state;
         }

         if (blockState.isOpaqueFullCube(world, blockPos)) {
            if (direction != null) {
               direction = null;
               break;
            }

            direction = direction2;
         }
      }

      if (direction != null) {
         return (BlockState)state.with(HorizontalFacingBlock.FACING, direction.getOpposite());
      } else {
         Direction direction3 = (Direction)state.get(HorizontalFacingBlock.FACING);
         BlockPos blockPos2 = pos.offset(direction3);
         if (world.getBlockState(blockPos2).isOpaqueFullCube(world, blockPos2)) {
            direction3 = direction3.getOpposite();
            blockPos2 = pos.offset(direction3);
         }

         if (world.getBlockState(blockPos2).isOpaqueFullCube(world, blockPos2)) {
            direction3 = direction3.rotateYClockwise();
            blockPos2 = pos.offset(direction3);
         }

         if (world.getBlockState(blockPos2).isOpaqueFullCube(world, blockPos2)) {
            direction3 = direction3.getOpposite();
            pos.offset(direction3);
         }

         return (BlockState)state.with(HorizontalFacingBlock.FACING, direction3);
      }
   }

   protected boolean addChest(ServerWorldAccess world, BlockBox boundingBox, Random random, BlockPos pos, Identifier lootTableId, @Nullable BlockState block) {
      if (boundingBox.contains(pos) && !world.getBlockState(pos).isOf(Blocks.CHEST)) {
         if (block == null) {
            block = orientateChest(world, pos, Blocks.CHEST.getDefaultState());
         }

         world.setBlockState(pos, block, 2);
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof ChestBlockEntity) {
            ((ChestBlockEntity)blockEntity).setLootTable(lootTableId, random.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   protected boolean addDispenser(StructureWorldAccess world, BlockBox boundingBox, Random random, int x, int y, int z, Direction facing, Identifier lootTableId) {
      BlockPos blockPos = new BlockPos(this.applyXTransform(x, z), this.applyYTransform(y), this.applyZTransform(x, z));
      if (boundingBox.contains(blockPos) && !world.getBlockState(blockPos).isOf(Blocks.DISPENSER)) {
         this.addBlock(world, (BlockState)Blocks.DISPENSER.getDefaultState().with(DispenserBlock.FACING, facing), x, y, z, boundingBox);
         BlockEntity blockEntity = world.getBlockEntity(blockPos);
         if (blockEntity instanceof DispenserBlockEntity) {
            ((DispenserBlockEntity)blockEntity).setLootTable(lootTableId, random.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   public void translate(int x, int y, int z) {
      this.boundingBox.move(x, y, z);
   }

   @Nullable
   public Direction getFacing() {
      return this.facing;
   }

   public void setOrientation(@Nullable Direction orientation) {
      this.facing = orientation;
      if (orientation == null) {
         this.rotation = BlockRotation.NONE;
         this.mirror = BlockMirror.NONE;
      } else {
         switch(orientation) {
         case SOUTH:
            this.mirror = BlockMirror.LEFT_RIGHT;
            this.rotation = BlockRotation.NONE;
            break;
         case WEST:
            this.mirror = BlockMirror.LEFT_RIGHT;
            this.rotation = BlockRotation.CLOCKWISE_90;
            break;
         case EAST:
            this.mirror = BlockMirror.NONE;
            this.rotation = BlockRotation.CLOCKWISE_90;
            break;
         default:
            this.mirror = BlockMirror.NONE;
            this.rotation = BlockRotation.NONE;
         }
      }

   }

   public BlockRotation getRotation() {
      return this.rotation;
   }

   public StructurePieceType getType() {
      return this.type;
   }

   static {
      AIR = Blocks.CAVE_AIR.getDefaultState();
      BLOCKS_NEEDING_POST_PROCESSING = ImmutableSet.builder().add((Object)Blocks.NETHER_BRICK_FENCE).add((Object)Blocks.TORCH).add((Object)Blocks.WALL_TORCH).add((Object)Blocks.OAK_FENCE).add((Object)Blocks.SPRUCE_FENCE).add((Object)Blocks.DARK_OAK_FENCE).add((Object)Blocks.ACACIA_FENCE).add((Object)Blocks.BIRCH_FENCE).add((Object)Blocks.JUNGLE_FENCE).add((Object)Blocks.LADDER).add((Object)Blocks.IRON_BARS).build();
   }

   public abstract static class BlockRandomizer {
      protected BlockState block;

      protected BlockRandomizer() {
         this.block = Blocks.AIR.getDefaultState();
      }

      public abstract void setBlock(Random random, int x, int y, int z, boolean placeBlock);

      public BlockState getBlock() {
         return this.block;
      }
   }
}
