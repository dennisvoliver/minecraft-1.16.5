package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.MineshaftFeature;
import org.jetbrains.annotations.Nullable;

public class MineshaftGenerator {
   private static MineshaftGenerator.MineshaftPart pickPiece(List<StructurePiece> pieces, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength, MineshaftFeature.Type type) {
      int i = random.nextInt(100);
      BlockBox blockBox2;
      if (i >= 80) {
         blockBox2 = MineshaftGenerator.MineshaftCrossing.getBoundingBox(pieces, random, x, y, z, orientation);
         if (blockBox2 != null) {
            return new MineshaftGenerator.MineshaftCrossing(chainLength, blockBox2, orientation, type);
         }
      } else if (i >= 70) {
         blockBox2 = MineshaftGenerator.MineshaftStairs.getBoundingBox(pieces, random, x, y, z, orientation);
         if (blockBox2 != null) {
            return new MineshaftGenerator.MineshaftStairs(chainLength, blockBox2, orientation, type);
         }
      } else {
         blockBox2 = MineshaftGenerator.MineshaftCorridor.getBoundingBox(pieces, random, x, y, z, orientation);
         if (blockBox2 != null) {
            return new MineshaftGenerator.MineshaftCorridor(chainLength, random, blockBox2, orientation, type);
         }
      }

      return null;
   }

   private static MineshaftGenerator.MineshaftPart pieceGenerator(StructurePiece start, List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
      if (chainLength > 8) {
         return null;
      } else if (Math.abs(x - start.getBoundingBox().minX) <= 80 && Math.abs(z - start.getBoundingBox().minZ) <= 80) {
         MineshaftFeature.Type type = ((MineshaftGenerator.MineshaftPart)start).mineshaftType;
         MineshaftGenerator.MineshaftPart mineshaftPart = pickPiece(pieces, random, x, y, z, orientation, chainLength + 1, type);
         if (mineshaftPart != null) {
            pieces.add(mineshaftPart);
            mineshaftPart.fillOpenings(start, pieces, random);
         }

         return mineshaftPart;
      } else {
         return null;
      }
   }

   public static class MineshaftStairs extends MineshaftGenerator.MineshaftPart {
      public MineshaftStairs(int chainLength, BlockBox boundingBox, Direction orientation, MineshaftFeature.Type type) {
         super(StructurePieceType.MINESHAFT_STAIRS, chainLength, type);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public MineshaftStairs(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.MINESHAFT_STAIRS, nbtCompound);
      }

      public static BlockBox getBoundingBox(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation) {
         BlockBox blockBox = new BlockBox(x, y - 5, z, x, y + 3 - 1, z);
         switch(orientation) {
         case NORTH:
         default:
            blockBox.maxX = x + 3 - 1;
            blockBox.minZ = z - 8;
            break;
         case SOUTH:
            blockBox.maxX = x + 3 - 1;
            blockBox.maxZ = z + 8;
            break;
         case WEST:
            blockBox.minX = x - 8;
            blockBox.maxZ = z + 3 - 1;
            break;
         case EAST:
            blockBox.maxX = x + 8;
            blockBox.maxZ = z + 3 - 1;
         }

         return StructurePiece.getOverlappingPiece(pieces, blockBox) != null ? null : blockBox;
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         int i = this.getChainLength();
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
            default:
               MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, Direction.NORTH, i);
               break;
            case SOUTH:
               MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.maxZ + 1, Direction.SOUTH, i);
               break;
            case WEST:
               MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ, Direction.WEST, i);
               break;
            case EAST:
               MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, Direction.EAST, i);
            }
         }

      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         if (this.isTouchingLiquid(world, boundingBox)) {
            return false;
         } else {
            this.fillWithOutline(world, boundingBox, 0, 5, 0, 2, 7, 1, AIR, AIR, false);
            this.fillWithOutline(world, boundingBox, 0, 0, 7, 2, 2, 8, AIR, AIR, false);

            for(int i = 0; i < 5; ++i) {
               this.fillWithOutline(world, boundingBox, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, AIR, AIR, false);
            }

            return true;
         }
      }
   }

   public static class MineshaftCrossing extends MineshaftGenerator.MineshaftPart {
      private final Direction direction;
      private final boolean twoFloors;

      public MineshaftCrossing(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.MINESHAFT_CROSSING, nbtCompound);
         this.twoFloors = nbtCompound.getBoolean("tf");
         this.direction = Direction.fromHorizontal(nbtCompound.getInt("D"));
      }

      protected void toNbt(NbtCompound tag) {
         super.toNbt(tag);
         tag.putBoolean("tf", this.twoFloors);
         tag.putInt("D", this.direction.getHorizontal());
      }

      public MineshaftCrossing(int chainLength, BlockBox boundingBox, @Nullable Direction orientation, MineshaftFeature.Type type) {
         super(StructurePieceType.MINESHAFT_CROSSING, chainLength, type);
         this.direction = orientation;
         this.boundingBox = boundingBox;
         this.twoFloors = boundingBox.getBlockCountY() > 3;
      }

      public static BlockBox getBoundingBox(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation) {
         BlockBox blockBox = new BlockBox(x, y, z, x, y + 3 - 1, z);
         if (random.nextInt(4) == 0) {
            blockBox.maxY += 4;
         }

         switch(orientation) {
         case NORTH:
         default:
            blockBox.minX = x - 1;
            blockBox.maxX = x + 3;
            blockBox.minZ = z - 4;
            break;
         case SOUTH:
            blockBox.minX = x - 1;
            blockBox.maxX = x + 3;
            blockBox.maxZ = z + 3 + 1;
            break;
         case WEST:
            blockBox.minX = x - 4;
            blockBox.minZ = z - 1;
            blockBox.maxZ = z + 3;
            break;
         case EAST:
            blockBox.maxX = x + 3 + 1;
            blockBox.minZ = z - 1;
            blockBox.maxZ = z + 3;
         }

         return StructurePiece.getOverlappingPiece(pieces, blockBox) != null ? null : blockBox;
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         int i = this.getChainLength();
         switch(this.direction) {
         case NORTH:
         default:
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, Direction.NORTH, i);
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, Direction.WEST, i);
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, Direction.EAST, i);
            break;
         case SOUTH:
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, Direction.SOUTH, i);
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, Direction.WEST, i);
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, Direction.EAST, i);
            break;
         case WEST:
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, Direction.NORTH, i);
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, Direction.SOUTH, i);
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, Direction.WEST, i);
            break;
         case EAST:
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, Direction.NORTH, i);
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, Direction.SOUTH, i);
            MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, Direction.EAST, i);
         }

         if (this.twoFloors) {
            if (random.nextBoolean()) {
               MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ - 1, Direction.NORTH, i);
            }

            if (random.nextBoolean()) {
               MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ + 1, Direction.WEST, i);
            }

            if (random.nextBoolean()) {
               MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ + 1, Direction.EAST, i);
            }

            if (random.nextBoolean()) {
               MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.maxZ + 1, Direction.SOUTH, i);
            }
         }

      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         if (this.isTouchingLiquid(world, boundingBox)) {
            return false;
         } else {
            BlockState blockState = this.getPlanksType();
            if (this.twoFloors) {
               this.fillWithOutline(world, boundingBox, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.minY + 3 - 1, this.boundingBox.maxZ, AIR, AIR, false);
               this.fillWithOutline(world, boundingBox, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.minY + 3 - 1, this.boundingBox.maxZ - 1, AIR, AIR, false);
               this.fillWithOutline(world, boundingBox, this.boundingBox.minX + 1, this.boundingBox.maxY - 2, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.maxZ, AIR, AIR, false);
               this.fillWithOutline(world, boundingBox, this.boundingBox.minX, this.boundingBox.maxY - 2, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ - 1, AIR, AIR, false);
               this.fillWithOutline(world, boundingBox, this.boundingBox.minX + 1, this.boundingBox.minY + 3, this.boundingBox.minZ + 1, this.boundingBox.maxX - 1, this.boundingBox.minY + 3, this.boundingBox.maxZ - 1, AIR, AIR, false);
            } else {
               this.fillWithOutline(world, boundingBox, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.maxZ, AIR, AIR, false);
               this.fillWithOutline(world, boundingBox, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ - 1, AIR, AIR, false);
            }

            this.generateCrossingPilliar(world, boundingBox, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxY);
            this.generateCrossingPilliar(world, boundingBox, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ - 1, this.boundingBox.maxY);
            this.generateCrossingPilliar(world, boundingBox, this.boundingBox.maxX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxY);
            this.generateCrossingPilliar(world, boundingBox, this.boundingBox.maxX - 1, this.boundingBox.minY, this.boundingBox.maxZ - 1, this.boundingBox.maxY);

            for(int i = this.boundingBox.minX; i <= this.boundingBox.maxX; ++i) {
               for(int j = this.boundingBox.minZ; j <= this.boundingBox.maxZ; ++j) {
                  if (this.getBlockAt(world, i, this.boundingBox.minY - 1, j, boundingBox).isAir() && this.isUnderSeaLevel(world, i, this.boundingBox.minY - 1, j, boundingBox)) {
                     this.addBlock(world, blockState, i, this.boundingBox.minY - 1, j, boundingBox);
                  }
               }
            }

            return true;
         }
      }

      private void generateCrossingPilliar(StructureWorldAccess structureWorldAccess, BlockBox boundingBox, int x, int minY, int z, int maxY) {
         if (!this.getBlockAt(structureWorldAccess, x, maxY + 1, z, boundingBox).isAir()) {
            this.fillWithOutline(structureWorldAccess, boundingBox, x, minY, z, x, maxY, z, this.getPlanksType(), AIR, false);
         }

      }
   }

   public static class MineshaftCorridor extends MineshaftGenerator.MineshaftPart {
      private final boolean hasRails;
      private final boolean hasCobwebs;
      private boolean hasSpawner;
      private final int length;

      public MineshaftCorridor(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.MINESHAFT_CORRIDOR, nbtCompound);
         this.hasRails = nbtCompound.getBoolean("hr");
         this.hasCobwebs = nbtCompound.getBoolean("sc");
         this.hasSpawner = nbtCompound.getBoolean("hps");
         this.length = nbtCompound.getInt("Num");
      }

      protected void toNbt(NbtCompound tag) {
         super.toNbt(tag);
         tag.putBoolean("hr", this.hasRails);
         tag.putBoolean("sc", this.hasCobwebs);
         tag.putBoolean("hps", this.hasSpawner);
         tag.putInt("Num", this.length);
      }

      public MineshaftCorridor(int chainLength, Random random, BlockBox boundingBox, Direction orientation, MineshaftFeature.Type type) {
         super(StructurePieceType.MINESHAFT_CORRIDOR, chainLength, type);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
         this.hasRails = random.nextInt(3) == 0;
         this.hasCobwebs = !this.hasRails && random.nextInt(23) == 0;
         if (this.getFacing().getAxis() == Direction.Axis.Z) {
            this.length = boundingBox.getBlockCountZ() / 5;
         } else {
            this.length = boundingBox.getBlockCountX() / 5;
         }

      }

      public static BlockBox getBoundingBox(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation) {
         BlockBox blockBox = new BlockBox(x, y, z, x, y + 3 - 1, z);

         int i;
         for(i = random.nextInt(3) + 2; i > 0; --i) {
            int j = i * 5;
            switch(orientation) {
            case NORTH:
            default:
               blockBox.maxX = x + 3 - 1;
               blockBox.minZ = z - (j - 1);
               break;
            case SOUTH:
               blockBox.maxX = x + 3 - 1;
               blockBox.maxZ = z + j - 1;
               break;
            case WEST:
               blockBox.minX = x - (j - 1);
               blockBox.maxZ = z + 3 - 1;
               break;
            case EAST:
               blockBox.maxX = x + j - 1;
               blockBox.maxZ = z + 3 - 1;
            }

            if (StructurePiece.getOverlappingPiece(pieces, blockBox) == null) {
               break;
            }
         }

         return i > 0 ? blockBox : null;
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         int i = this.getChainLength();
         int j = random.nextInt(4);
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
            default:
               if (j <= 1) {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.minZ - 1, direction, i);
               } else if (j == 2) {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.minZ, Direction.WEST, i);
               } else {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.minZ, Direction.EAST, i);
               }
               break;
            case SOUTH:
               if (j <= 1) {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.maxZ + 1, direction, i);
               } else if (j == 2) {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.maxZ - 3, Direction.WEST, i);
               } else {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.maxZ - 3, Direction.EAST, i);
               }
               break;
            case WEST:
               if (j <= 1) {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.minZ, direction, i);
               } else if (j == 2) {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.minZ - 1, Direction.NORTH, i);
               } else {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.maxZ + 1, Direction.SOUTH, i);
               }
               break;
            case EAST:
               if (j <= 1) {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.minZ, direction, i);
               } else if (j == 2) {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX - 3, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.minZ - 1, Direction.NORTH, i);
               } else {
                  MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX - 3, this.boundingBox.minY - 1 + random.nextInt(3), this.boundingBox.maxZ + 1, Direction.SOUTH, i);
               }
            }
         }

         if (i < 8) {
            int k;
            int l;
            if (direction != Direction.NORTH && direction != Direction.SOUTH) {
               for(k = this.boundingBox.minX + 3; k + 3 <= this.boundingBox.maxX; k += 5) {
                  l = random.nextInt(5);
                  if (l == 0) {
                     MineshaftGenerator.pieceGenerator(start, pieces, random, k, this.boundingBox.minY, this.boundingBox.minZ - 1, Direction.NORTH, i + 1);
                  } else if (l == 1) {
                     MineshaftGenerator.pieceGenerator(start, pieces, random, k, this.boundingBox.minY, this.boundingBox.maxZ + 1, Direction.SOUTH, i + 1);
                  }
               }
            } else {
               for(k = this.boundingBox.minZ + 3; k + 3 <= this.boundingBox.maxZ; k += 5) {
                  l = random.nextInt(5);
                  if (l == 0) {
                     MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY, k, Direction.WEST, i + 1);
                  } else if (l == 1) {
                     MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY, k, Direction.EAST, i + 1);
                  }
               }
            }
         }

      }

      protected boolean addChest(StructureWorldAccess world, BlockBox boundingBox, Random random, int x, int y, int z, Identifier lootTableId) {
         BlockPos blockPos = new BlockPos(this.applyXTransform(x, z), this.applyYTransform(y), this.applyZTransform(x, z));
         if (boundingBox.contains(blockPos) && world.getBlockState(blockPos).isAir() && !world.getBlockState(blockPos.down()).isAir()) {
            BlockState blockState = (BlockState)Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, random.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
            this.addBlock(world, blockState, x, y, z, boundingBox);
            ChestMinecartEntity chestMinecartEntity = new ChestMinecartEntity(world.toServerWorld(), (double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D);
            chestMinecartEntity.setLootTable(lootTableId, random.nextLong());
            world.spawnEntity(chestMinecartEntity);
            return true;
         } else {
            return false;
         }
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         if (this.isTouchingLiquid(world, boundingBox)) {
            return false;
         } else {
            int i = false;
            int j = true;
            int k = false;
            int l = true;
            int m = this.length * 5 - 1;
            BlockState blockState = this.getPlanksType();
            this.fillWithOutline(world, boundingBox, 0, 0, 0, 2, 1, m, AIR, AIR, false);
            this.fillWithOutlineUnderSeaLevel(world, boundingBox, random, 0.8F, 0, 2, 0, 2, 2, m, AIR, AIR, false, false);
            if (this.hasCobwebs) {
               this.fillWithOutlineUnderSeaLevel(world, boundingBox, random, 0.6F, 0, 0, 0, 2, 1, m, Blocks.COBWEB.getDefaultState(), AIR, false, true);
            }

            int t;
            int x;
            for(t = 0; t < this.length; ++t) {
               x = 2 + t * 5;
               this.generateSupports(world, boundingBox, 0, 0, x, 2, 2, random);
               this.addCobwebsUnderground(world, boundingBox, random, 0.1F, 0, 2, x - 1);
               this.addCobwebsUnderground(world, boundingBox, random, 0.1F, 2, 2, x - 1);
               this.addCobwebsUnderground(world, boundingBox, random, 0.1F, 0, 2, x + 1);
               this.addCobwebsUnderground(world, boundingBox, random, 0.1F, 2, 2, x + 1);
               this.addCobwebsUnderground(world, boundingBox, random, 0.05F, 0, 2, x - 2);
               this.addCobwebsUnderground(world, boundingBox, random, 0.05F, 2, 2, x - 2);
               this.addCobwebsUnderground(world, boundingBox, random, 0.05F, 0, 2, x + 2);
               this.addCobwebsUnderground(world, boundingBox, random, 0.05F, 2, 2, x + 2);
               if (random.nextInt(100) == 0) {
                  this.addChest(world, boundingBox, random, 2, 0, x - 1, LootTables.ABANDONED_MINESHAFT_CHEST);
               }

               if (random.nextInt(100) == 0) {
                  this.addChest(world, boundingBox, random, 0, 0, x + 1, LootTables.ABANDONED_MINESHAFT_CHEST);
               }

               if (this.hasCobwebs && !this.hasSpawner) {
                  int p = this.applyYTransform(0);
                  int q = x - 1 + random.nextInt(3);
                  int r = this.applyXTransform(1, q);
                  int s = this.applyZTransform(1, q);
                  BlockPos blockPos = new BlockPos(r, p, s);
                  if (boundingBox.contains(blockPos) && this.isUnderSeaLevel(world, 1, 0, q, boundingBox)) {
                     this.hasSpawner = true;
                     world.setBlockState(blockPos, Blocks.SPAWNER.getDefaultState(), 2);
                     BlockEntity blockEntity = world.getBlockEntity(blockPos);
                     if (blockEntity instanceof MobSpawnerBlockEntity) {
                        ((MobSpawnerBlockEntity)blockEntity).getLogic().setEntityId(EntityType.CAVE_SPIDER);
                     }
                  }
               }
            }

            for(t = 0; t <= 2; ++t) {
               for(x = 0; x <= m; ++x) {
                  int v = true;
                  BlockState blockState2 = this.getBlockAt(world, t, -1, x, boundingBox);
                  if (blockState2.isAir() && this.isUnderSeaLevel(world, t, -1, x, boundingBox)) {
                     int w = true;
                     this.addBlock(world, blockState, t, -1, x, boundingBox);
                  }
               }
            }

            if (this.hasRails) {
               BlockState blockState3 = (BlockState)Blocks.RAIL.getDefaultState().with(RailBlock.SHAPE, RailShape.NORTH_SOUTH);

               for(x = 0; x <= m; ++x) {
                  BlockState blockState4 = this.getBlockAt(world, 1, -1, x, boundingBox);
                  if (!blockState4.isAir() && blockState4.isOpaqueFullCube(world, new BlockPos(this.applyXTransform(1, x), this.applyYTransform(-1), this.applyZTransform(1, x)))) {
                     float f = this.isUnderSeaLevel(world, 1, 0, x, boundingBox) ? 0.7F : 0.9F;
                     this.addBlockWithRandomThreshold(world, boundingBox, random, f, 1, 0, x, blockState3);
                  }
               }
            }

            return true;
         }
      }

      private void generateSupports(StructureWorldAccess structureWorldAccess, BlockBox boundingBox, int minX, int minY, int z, int maxY, int maxX, Random random) {
         if (this.isSolidCeiling(structureWorldAccess, boundingBox, minX, maxX, maxY, z)) {
            BlockState blockState = this.getPlanksType();
            BlockState blockState2 = this.getFenceType();
            this.fillWithOutline(structureWorldAccess, boundingBox, minX, minY, z, minX, maxY - 1, z, (BlockState)blockState2.with(FenceBlock.WEST, true), AIR, false);
            this.fillWithOutline(structureWorldAccess, boundingBox, maxX, minY, z, maxX, maxY - 1, z, (BlockState)blockState2.with(FenceBlock.EAST, true), AIR, false);
            if (random.nextInt(4) == 0) {
               this.fillWithOutline(structureWorldAccess, boundingBox, minX, maxY, z, minX, maxY, z, blockState, AIR, false);
               this.fillWithOutline(structureWorldAccess, boundingBox, maxX, maxY, z, maxX, maxY, z, blockState, AIR, false);
            } else {
               this.fillWithOutline(structureWorldAccess, boundingBox, minX, maxY, z, maxX, maxY, z, blockState, AIR, false);
               this.addBlockWithRandomThreshold(structureWorldAccess, boundingBox, random, 0.05F, minX + 1, maxY, z - 1, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.NORTH));
               this.addBlockWithRandomThreshold(structureWorldAccess, boundingBox, random, 0.05F, minX + 1, maxY, z + 1, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.SOUTH));
            }

         }
      }

      private void addCobwebsUnderground(StructureWorldAccess structureWorldAccess, BlockBox boundingBox, Random random, float threshold, int x, int y, int z) {
         if (this.isUnderSeaLevel(structureWorldAccess, x, y, z, boundingBox)) {
            this.addBlockWithRandomThreshold(structureWorldAccess, boundingBox, random, threshold, x, y, z, Blocks.COBWEB.getDefaultState());
         }

      }
   }

   public static class MineshaftRoom extends MineshaftGenerator.MineshaftPart {
      private final List<BlockBox> entrances = Lists.newLinkedList();

      public MineshaftRoom(int chainLength, Random random, int x, int z, MineshaftFeature.Type type) {
         super(StructurePieceType.MINESHAFT_ROOM, chainLength, type);
         this.mineshaftType = type;
         this.boundingBox = new BlockBox(x, 50, z, x + 7 + random.nextInt(6), 54 + random.nextInt(6), z + 7 + random.nextInt(6));
      }

      public MineshaftRoom(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.MINESHAFT_ROOM, nbtCompound);
         NbtList nbtList = nbtCompound.getList("Entrances", 11);

         for(int i = 0; i < nbtList.size(); ++i) {
            this.entrances.add(new BlockBox(nbtList.getIntArray(i)));
         }

      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         int i = this.getChainLength();
         int j = this.boundingBox.getBlockCountY() - 3 - 1;
         if (j <= 0) {
            j = 1;
         }

         int k;
         MineshaftGenerator.MineshaftPart structurePiece;
         BlockBox blockBox4;
         for(k = 0; k < this.boundingBox.getBlockCountX(); k += 4) {
            k += random.nextInt(this.boundingBox.getBlockCountX());
            if (k + 3 > this.boundingBox.getBlockCountX()) {
               break;
            }

            structurePiece = MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + k, this.boundingBox.minY + random.nextInt(j) + 1, this.boundingBox.minZ - 1, Direction.NORTH, i);
            if (structurePiece != null) {
               blockBox4 = structurePiece.getBoundingBox();
               this.entrances.add(new BlockBox(blockBox4.minX, blockBox4.minY, this.boundingBox.minZ, blockBox4.maxX, blockBox4.maxY, this.boundingBox.minZ + 1));
            }
         }

         for(k = 0; k < this.boundingBox.getBlockCountX(); k += 4) {
            k += random.nextInt(this.boundingBox.getBlockCountX());
            if (k + 3 > this.boundingBox.getBlockCountX()) {
               break;
            }

            structurePiece = MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + k, this.boundingBox.minY + random.nextInt(j) + 1, this.boundingBox.maxZ + 1, Direction.SOUTH, i);
            if (structurePiece != null) {
               blockBox4 = structurePiece.getBoundingBox();
               this.entrances.add(new BlockBox(blockBox4.minX, blockBox4.minY, this.boundingBox.maxZ - 1, blockBox4.maxX, blockBox4.maxY, this.boundingBox.maxZ));
            }
         }

         for(k = 0; k < this.boundingBox.getBlockCountZ(); k += 4) {
            k += random.nextInt(this.boundingBox.getBlockCountZ());
            if (k + 3 > this.boundingBox.getBlockCountZ()) {
               break;
            }

            structurePiece = MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY + random.nextInt(j) + 1, this.boundingBox.minZ + k, Direction.WEST, i);
            if (structurePiece != null) {
               blockBox4 = structurePiece.getBoundingBox();
               this.entrances.add(new BlockBox(this.boundingBox.minX, blockBox4.minY, blockBox4.minZ, this.boundingBox.minX + 1, blockBox4.maxY, blockBox4.maxZ));
            }
         }

         for(k = 0; k < this.boundingBox.getBlockCountZ(); k += 4) {
            k += random.nextInt(this.boundingBox.getBlockCountZ());
            if (k + 3 > this.boundingBox.getBlockCountZ()) {
               break;
            }

            structurePiece = MineshaftGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY + random.nextInt(j) + 1, this.boundingBox.minZ + k, Direction.EAST, i);
            if (structurePiece != null) {
               blockBox4 = structurePiece.getBoundingBox();
               this.entrances.add(new BlockBox(this.boundingBox.maxX - 1, blockBox4.minY, blockBox4.minZ, this.boundingBox.maxX, blockBox4.maxY, blockBox4.maxZ));
            }
         }

      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         if (this.isTouchingLiquid(world, boundingBox)) {
            return false;
         } else {
            this.fillWithOutline(world, boundingBox, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX, this.boundingBox.minY, this.boundingBox.maxZ, Blocks.DIRT.getDefaultState(), AIR, true);
            this.fillWithOutline(world, boundingBox, this.boundingBox.minX, this.boundingBox.minY + 1, this.boundingBox.minZ, this.boundingBox.maxX, Math.min(this.boundingBox.minY + 3, this.boundingBox.maxY), this.boundingBox.maxZ, AIR, AIR, false);
            Iterator var8 = this.entrances.iterator();

            while(var8.hasNext()) {
               BlockBox blockBox = (BlockBox)var8.next();
               this.fillWithOutline(world, boundingBox, blockBox.minX, blockBox.maxY - 2, blockBox.minZ, blockBox.maxX, blockBox.maxY, blockBox.maxZ, AIR, AIR, false);
            }

            this.fillHalfEllipsoid(world, boundingBox, this.boundingBox.minX, this.boundingBox.minY + 4, this.boundingBox.minZ, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ, AIR, false);
            return true;
         }
      }

      public void translate(int x, int y, int z) {
         super.translate(x, y, z);
         Iterator var4 = this.entrances.iterator();

         while(var4.hasNext()) {
            BlockBox blockBox = (BlockBox)var4.next();
            blockBox.move(x, y, z);
         }

      }

      protected void toNbt(NbtCompound tag) {
         super.toNbt(tag);
         NbtList nbtList = new NbtList();
         Iterator var3 = this.entrances.iterator();

         while(var3.hasNext()) {
            BlockBox blockBox = (BlockBox)var3.next();
            nbtList.add(blockBox.toNbt());
         }

         tag.put("Entrances", nbtList);
      }
   }

   abstract static class MineshaftPart extends StructurePiece {
      protected MineshaftFeature.Type mineshaftType;

      public MineshaftPart(StructurePieceType structurePieceType, int chainLength, MineshaftFeature.Type type) {
         super(structurePieceType, chainLength);
         this.mineshaftType = type;
      }

      public MineshaftPart(StructurePieceType structurePieceType, NbtCompound nbtCompound) {
         super(structurePieceType, nbtCompound);
         this.mineshaftType = MineshaftFeature.Type.byIndex(nbtCompound.getInt("MST"));
      }

      protected void toNbt(NbtCompound tag) {
         tag.putInt("MST", this.mineshaftType.ordinal());
      }

      protected BlockState getPlanksType() {
         switch(this.mineshaftType) {
         case NORMAL:
         default:
            return Blocks.OAK_PLANKS.getDefaultState();
         case MESA:
            return Blocks.DARK_OAK_PLANKS.getDefaultState();
         }
      }

      protected BlockState getFenceType() {
         switch(this.mineshaftType) {
         case NORMAL:
         default:
            return Blocks.OAK_FENCE.getDefaultState();
         case MESA:
            return Blocks.DARK_OAK_FENCE.getDefaultState();
         }
      }

      protected boolean isSolidCeiling(BlockView blockView, BlockBox boundingBox, int minX, int maxX, int y, int z) {
         for(int i = minX; i <= maxX; ++i) {
            if (this.getBlockAt(blockView, i, y + 1, z, boundingBox).isAir()) {
               return false;
            }
         }

         return true;
      }
   }
}
