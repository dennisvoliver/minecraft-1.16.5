package net.minecraft.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public class StrongholdGenerator {
   private static final StrongholdGenerator.PieceData[] ALL_PIECES = new StrongholdGenerator.PieceData[]{new StrongholdGenerator.PieceData(StrongholdGenerator.Corridor.class, 40, 0), new StrongholdGenerator.PieceData(StrongholdGenerator.PrisonHall.class, 5, 5), new StrongholdGenerator.PieceData(StrongholdGenerator.LeftTurn.class, 20, 0), new StrongholdGenerator.PieceData(StrongholdGenerator.RightTurn.class, 20, 0), new StrongholdGenerator.PieceData(StrongholdGenerator.SquareRoom.class, 10, 6), new StrongholdGenerator.PieceData(StrongholdGenerator.Stairs.class, 5, 5), new StrongholdGenerator.PieceData(StrongholdGenerator.SpiralStaircase.class, 5, 5), new StrongholdGenerator.PieceData(StrongholdGenerator.FiveWayCrossing.class, 5, 4), new StrongholdGenerator.PieceData(StrongholdGenerator.ChestCorridor.class, 5, 4), new StrongholdGenerator.PieceData(StrongholdGenerator.Library.class, 10, 2) {
      public boolean canGenerate(int chainLength) {
         return super.canGenerate(chainLength) && chainLength > 4;
      }
   }, new StrongholdGenerator.PieceData(StrongholdGenerator.PortalRoom.class, 20, 1) {
      public boolean canGenerate(int chainLength) {
         return super.canGenerate(chainLength) && chainLength > 5;
      }
   }};
   private static List<StrongholdGenerator.PieceData> possiblePieces;
   private static Class<? extends StrongholdGenerator.Piece> activePieceType;
   private static int totalWeight;
   private static final StrongholdGenerator.StoneBrickRandomizer STONE_BRICK_RANDOMIZER = new StrongholdGenerator.StoneBrickRandomizer();

   public static void init() {
      possiblePieces = Lists.newArrayList();
      StrongholdGenerator.PieceData[] var0 = ALL_PIECES;
      int var1 = var0.length;

      for(int var2 = 0; var2 < var1; ++var2) {
         StrongholdGenerator.PieceData pieceData = var0[var2];
         pieceData.generatedCount = 0;
         possiblePieces.add(pieceData);
      }

      activePieceType = null;
   }

   private static boolean checkRemainingPieces() {
      boolean bl = false;
      totalWeight = 0;

      StrongholdGenerator.PieceData pieceData;
      for(Iterator var1 = possiblePieces.iterator(); var1.hasNext(); totalWeight += pieceData.weight) {
         pieceData = (StrongholdGenerator.PieceData)var1.next();
         if (pieceData.limit > 0 && pieceData.generatedCount < pieceData.limit) {
            bl = true;
         }
      }

      return bl;
   }

   private static StrongholdGenerator.Piece createPiece(Class<? extends StrongholdGenerator.Piece> pieceType, List<StructurePiece> pieces, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength) {
      StrongholdGenerator.Piece piece = null;
      if (pieceType == StrongholdGenerator.Corridor.class) {
         piece = StrongholdGenerator.Corridor.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (pieceType == StrongholdGenerator.PrisonHall.class) {
         piece = StrongholdGenerator.PrisonHall.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (pieceType == StrongholdGenerator.LeftTurn.class) {
         piece = StrongholdGenerator.LeftTurn.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (pieceType == StrongholdGenerator.RightTurn.class) {
         piece = StrongholdGenerator.RightTurn.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (pieceType == StrongholdGenerator.SquareRoom.class) {
         piece = StrongholdGenerator.SquareRoom.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (pieceType == StrongholdGenerator.Stairs.class) {
         piece = StrongholdGenerator.Stairs.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (pieceType == StrongholdGenerator.SpiralStaircase.class) {
         piece = StrongholdGenerator.SpiralStaircase.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (pieceType == StrongholdGenerator.FiveWayCrossing.class) {
         piece = StrongholdGenerator.FiveWayCrossing.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (pieceType == StrongholdGenerator.ChestCorridor.class) {
         piece = StrongholdGenerator.ChestCorridor.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (pieceType == StrongholdGenerator.Library.class) {
         piece = StrongholdGenerator.Library.create(pieces, random, x, y, z, orientation, chainLength);
      } else if (pieceType == StrongholdGenerator.PortalRoom.class) {
         piece = StrongholdGenerator.PortalRoom.create(pieces, x, y, z, orientation, chainLength);
      }

      return (StrongholdGenerator.Piece)piece;
   }

   private static StrongholdGenerator.Piece pickPiece(StrongholdGenerator.Start start, List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
      if (!checkRemainingPieces()) {
         return null;
      } else {
         if (activePieceType != null) {
            StrongholdGenerator.Piece piece = createPiece(activePieceType, pieces, random, x, y, z, orientation, chainLength);
            activePieceType = null;
            if (piece != null) {
               return piece;
            }
         }

         int i = 0;

         while(i < 5) {
            ++i;
            int j = random.nextInt(totalWeight);
            Iterator var10 = possiblePieces.iterator();

            while(var10.hasNext()) {
               StrongholdGenerator.PieceData pieceData = (StrongholdGenerator.PieceData)var10.next();
               j -= pieceData.weight;
               if (j < 0) {
                  if (!pieceData.canGenerate(chainLength) || pieceData == start.lastPiece) {
                     break;
                  }

                  StrongholdGenerator.Piece piece2 = createPiece(pieceData.pieceType, pieces, random, x, y, z, orientation, chainLength);
                  if (piece2 != null) {
                     ++pieceData.generatedCount;
                     start.lastPiece = pieceData;
                     if (!pieceData.canGenerate()) {
                        possiblePieces.remove(pieceData);
                     }

                     return piece2;
                  }
               }
            }
         }

         BlockBox blockBox = StrongholdGenerator.SmallCorridor.create(pieces, random, x, y, z, orientation);
         if (blockBox != null && blockBox.minY > 1) {
            return new StrongholdGenerator.SmallCorridor(chainLength, blockBox, orientation);
         } else {
            return null;
         }
      }
   }

   private static StructurePiece pieceGenerator(StrongholdGenerator.Start start, List<StructurePiece> pieces, Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength) {
      if (chainLength > 50) {
         return null;
      } else if (Math.abs(x - start.getBoundingBox().minX) <= 112 && Math.abs(z - start.getBoundingBox().minZ) <= 112) {
         StructurePiece structurePiece = pickPiece(start, pieces, random, x, y, z, orientation, chainLength + 1);
         if (structurePiece != null) {
            pieces.add(structurePiece);
            start.pieces.add(structurePiece);
         }

         return structurePiece;
      } else {
         return null;
      }
   }

   static class StoneBrickRandomizer extends StructurePiece.BlockRandomizer {
      private StoneBrickRandomizer() {
      }

      public void setBlock(Random random, int x, int y, int z, boolean placeBlock) {
         if (placeBlock) {
            float f = random.nextFloat();
            if (f < 0.2F) {
               this.block = Blocks.CRACKED_STONE_BRICKS.getDefaultState();
            } else if (f < 0.5F) {
               this.block = Blocks.MOSSY_STONE_BRICKS.getDefaultState();
            } else if (f < 0.55F) {
               this.block = Blocks.INFESTED_STONE_BRICKS.getDefaultState();
            } else {
               this.block = Blocks.STONE_BRICKS.getDefaultState();
            }
         } else {
            this.block = Blocks.CAVE_AIR.getDefaultState();
         }

      }
   }

   public static class PortalRoom extends StrongholdGenerator.Piece {
      private boolean spawnerPlaced;

      public PortalRoom(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
      }

      public PortalRoom(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, nbtCompound);
         this.spawnerPlaced = nbtCompound.getBoolean("Mob");
      }

      protected void toNbt(NbtCompound tag) {
         super.toNbt(tag);
         tag.putBoolean("Mob", this.spawnerPlaced);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         if (start != null) {
            ((StrongholdGenerator.Start)start).portalRoom = this;
         }

      }

      public static StrongholdGenerator.PortalRoom create(List<StructurePiece> pieces, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -4, -1, 0, 11, 8, 16, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new StrongholdGenerator.PortalRoom(chainLength, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         this.fillWithOutline(world, boundingBox, 0, 0, 0, 10, 7, 15, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, boundingBox, StrongholdGenerator.Piece.EntranceType.GRATES, 4, 1, 0);
         int i = 6;
         this.fillWithOutline(world, boundingBox, 1, i, 1, 1, i, 14, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 9, i, 1, 9, i, 14, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 2, i, 1, 8, i, 2, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 2, i, 14, 8, i, 14, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 1, 1, 1, 2, 1, 4, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 8, 1, 1, 9, 1, 4, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 1, 1, 1, 1, 1, 3, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
         this.fillWithOutline(world, boundingBox, 9, 1, 1, 9, 1, 3, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
         this.fillWithOutline(world, boundingBox, 3, 1, 8, 7, 1, 12, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 4, 1, 9, 6, 1, 11, Blocks.LAVA.getDefaultState(), Blocks.LAVA.getDefaultState(), false);
         BlockState blockState = (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true);

         int k;
         for(k = 3; k < 14; k += 2) {
            this.fillWithOutline(world, boundingBox, 0, 3, k, 0, 4, k, blockState, blockState, false);
            this.fillWithOutline(world, boundingBox, 10, 3, k, 10, 4, k, blockState, blockState, false);
         }

         for(k = 2; k < 9; k += 2) {
            this.fillWithOutline(world, boundingBox, k, 3, 15, k, 4, 15, blockState2, blockState2, false);
         }

         BlockState blockState3 = (BlockState)Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.NORTH);
         this.fillWithOutline(world, boundingBox, 4, 1, 5, 6, 1, 7, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 4, 2, 6, 6, 2, 7, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 4, 3, 7, 6, 3, 7, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);

         for(int l = 4; l <= 6; ++l) {
            this.addBlock(world, blockState3, l, 1, 4, boundingBox);
            this.addBlock(world, blockState3, l, 2, 5, boundingBox);
            this.addBlock(world, blockState3, l, 3, 6, boundingBox);
         }

         BlockState blockState4 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.NORTH);
         BlockState blockState5 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.SOUTH);
         BlockState blockState6 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.EAST);
         BlockState blockState7 = (BlockState)Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.WEST);
         boolean bl = true;
         boolean[] bls = new boolean[12];

         for(int m = 0; m < bls.length; ++m) {
            bls[m] = random.nextFloat() > 0.9F;
            bl &= bls[m];
         }

         this.addBlock(world, (BlockState)blockState4.with(EndPortalFrameBlock.EYE, bls[0]), 4, 3, 8, boundingBox);
         this.addBlock(world, (BlockState)blockState4.with(EndPortalFrameBlock.EYE, bls[1]), 5, 3, 8, boundingBox);
         this.addBlock(world, (BlockState)blockState4.with(EndPortalFrameBlock.EYE, bls[2]), 6, 3, 8, boundingBox);
         this.addBlock(world, (BlockState)blockState5.with(EndPortalFrameBlock.EYE, bls[3]), 4, 3, 12, boundingBox);
         this.addBlock(world, (BlockState)blockState5.with(EndPortalFrameBlock.EYE, bls[4]), 5, 3, 12, boundingBox);
         this.addBlock(world, (BlockState)blockState5.with(EndPortalFrameBlock.EYE, bls[5]), 6, 3, 12, boundingBox);
         this.addBlock(world, (BlockState)blockState6.with(EndPortalFrameBlock.EYE, bls[6]), 3, 3, 9, boundingBox);
         this.addBlock(world, (BlockState)blockState6.with(EndPortalFrameBlock.EYE, bls[7]), 3, 3, 10, boundingBox);
         this.addBlock(world, (BlockState)blockState6.with(EndPortalFrameBlock.EYE, bls[8]), 3, 3, 11, boundingBox);
         this.addBlock(world, (BlockState)blockState7.with(EndPortalFrameBlock.EYE, bls[9]), 7, 3, 9, boundingBox);
         this.addBlock(world, (BlockState)blockState7.with(EndPortalFrameBlock.EYE, bls[10]), 7, 3, 10, boundingBox);
         this.addBlock(world, (BlockState)blockState7.with(EndPortalFrameBlock.EYE, bls[11]), 7, 3, 11, boundingBox);
         if (bl) {
            BlockState blockState8 = Blocks.END_PORTAL.getDefaultState();
            this.addBlock(world, blockState8, 4, 3, 9, boundingBox);
            this.addBlock(world, blockState8, 5, 3, 9, boundingBox);
            this.addBlock(world, blockState8, 6, 3, 9, boundingBox);
            this.addBlock(world, blockState8, 4, 3, 10, boundingBox);
            this.addBlock(world, blockState8, 5, 3, 10, boundingBox);
            this.addBlock(world, blockState8, 6, 3, 10, boundingBox);
            this.addBlock(world, blockState8, 4, 3, 11, boundingBox);
            this.addBlock(world, blockState8, 5, 3, 11, boundingBox);
            this.addBlock(world, blockState8, 6, 3, 11, boundingBox);
         }

         if (!this.spawnerPlaced) {
            int i = this.applyYTransform(3);
            BlockPos blockPos = new BlockPos(this.applyXTransform(5, 6), i, this.applyZTransform(5, 6));
            if (boundingBox.contains(blockPos)) {
               this.spawnerPlaced = true;
               world.setBlockState(blockPos, Blocks.SPAWNER.getDefaultState(), 2);
               BlockEntity blockEntity = world.getBlockEntity(blockPos);
               if (blockEntity instanceof MobSpawnerBlockEntity) {
                  ((MobSpawnerBlockEntity)blockEntity).getLogic().setEntityId(EntityType.SILVERFISH);
               }
            }
         }

         return true;
      }
   }

   public static class FiveWayCrossing extends StrongholdGenerator.Piece {
      private final boolean lowerLeftExists;
      private final boolean upperLeftExists;
      private final boolean lowerRightExists;
      private final boolean upperRightExists;

      public FiveWayCrossing(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_FIVE_WAY_CROSSING, chainLength);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = boundingBox;
         this.lowerLeftExists = random.nextBoolean();
         this.upperLeftExists = random.nextBoolean();
         this.lowerRightExists = random.nextBoolean();
         this.upperRightExists = random.nextInt(3) > 0;
      }

      public FiveWayCrossing(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_FIVE_WAY_CROSSING, nbtCompound);
         this.lowerLeftExists = nbtCompound.getBoolean("leftLow");
         this.upperLeftExists = nbtCompound.getBoolean("leftHigh");
         this.lowerRightExists = nbtCompound.getBoolean("rightLow");
         this.upperRightExists = nbtCompound.getBoolean("rightHigh");
      }

      protected void toNbt(NbtCompound tag) {
         super.toNbt(tag);
         tag.putBoolean("leftLow", this.lowerLeftExists);
         tag.putBoolean("leftHigh", this.upperLeftExists);
         tag.putBoolean("rightLow", this.lowerRightExists);
         tag.putBoolean("rightHigh", this.upperRightExists);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         int i = 3;
         int j = 5;
         Direction direction = this.getFacing();
         if (direction == Direction.WEST || direction == Direction.NORTH) {
            i = 8 - i;
            j = 8 - j;
         }

         this.fillForwardOpening((StrongholdGenerator.Start)start, pieces, random, 5, 1);
         if (this.lowerLeftExists) {
            this.fillNWOpening((StrongholdGenerator.Start)start, pieces, random, i, 1);
         }

         if (this.upperLeftExists) {
            this.fillNWOpening((StrongholdGenerator.Start)start, pieces, random, j, 7);
         }

         if (this.lowerRightExists) {
            this.fillSEOpening((StrongholdGenerator.Start)start, pieces, random, i, 1);
         }

         if (this.upperRightExists) {
            this.fillSEOpening((StrongholdGenerator.Start)start, pieces, random, j, 7);
         }

      }

      public static StrongholdGenerator.FiveWayCrossing create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -4, -3, 0, 10, 9, 11, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new StrongholdGenerator.FiveWayCrossing(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         this.fillWithOutline(world, boundingBox, 0, 0, 0, 9, 8, 10, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, boundingBox, this.entryDoor, 4, 3, 0);
         if (this.lowerLeftExists) {
            this.fillWithOutline(world, boundingBox, 0, 3, 1, 0, 5, 3, AIR, AIR, false);
         }

         if (this.lowerRightExists) {
            this.fillWithOutline(world, boundingBox, 9, 3, 1, 9, 5, 3, AIR, AIR, false);
         }

         if (this.upperLeftExists) {
            this.fillWithOutline(world, boundingBox, 0, 5, 7, 0, 7, 9, AIR, AIR, false);
         }

         if (this.upperRightExists) {
            this.fillWithOutline(world, boundingBox, 9, 5, 7, 9, 7, 9, AIR, AIR, false);
         }

         this.fillWithOutline(world, boundingBox, 5, 1, 10, 7, 3, 10, AIR, AIR, false);
         this.fillWithOutline(world, boundingBox, 1, 2, 1, 8, 2, 6, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 4, 1, 5, 4, 4, 9, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 8, 1, 5, 8, 4, 9, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 1, 4, 7, 3, 4, 9, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 1, 3, 5, 3, 3, 6, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 1, 3, 4, 3, 3, 4, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, boundingBox, 1, 4, 6, 3, 4, 6, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, boundingBox, 5, 1, 7, 7, 1, 8, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 5, 1, 9, 7, 1, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, boundingBox, 5, 2, 7, 7, 2, 7, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, boundingBox, 4, 5, 7, 4, 5, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, boundingBox, 8, 5, 7, 8, 5, 9, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), Blocks.SMOOTH_STONE_SLAB.getDefaultState(), false);
         this.fillWithOutline(world, boundingBox, 5, 5, 7, 7, 5, 9, (BlockState)Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE), (BlockState)Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE), false);
         this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, boundingBox);
         return true;
      }
   }

   public static class Library extends StrongholdGenerator.Piece {
      private final boolean tall;

      public Library(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_LIBRARY, chainLength);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = boundingBox;
         this.tall = boundingBox.getBlockCountY() > 6;
      }

      public Library(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_LIBRARY, nbtCompound);
         this.tall = nbtCompound.getBoolean("Tall");
      }

      protected void toNbt(NbtCompound tag) {
         super.toNbt(tag);
         tag.putBoolean("Tall", this.tall);
      }

      public static StrongholdGenerator.Library create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -4, -1, 0, 14, 11, 15, orientation);
         if (!isInbounds(blockBox) || StructurePiece.getOverlappingPiece(pieces, blockBox) != null) {
            blockBox = BlockBox.rotated(x, y, z, -4, -1, 0, 14, 6, 15, orientation);
            if (!isInbounds(blockBox) || StructurePiece.getOverlappingPiece(pieces, blockBox) != null) {
               return null;
            }
         }

         return new StrongholdGenerator.Library(chainLength, random, blockBox, orientation);
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         int i = 11;
         if (!this.tall) {
            i = 6;
         }

         this.fillWithOutline(world, boundingBox, 0, 0, 0, 13, i - 1, 14, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, boundingBox, this.entryDoor, 4, 1, 0);
         this.fillWithOutlineUnderSeaLevel(world, boundingBox, random, 0.07F, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.getDefaultState(), Blocks.COBWEB.getDefaultState(), false, false);
         int j = true;
         int k = true;

         int l;
         for(l = 1; l <= 13; ++l) {
            if ((l - 1) % 4 == 0) {
               this.fillWithOutline(world, boundingBox, 1, 1, l, 1, 4, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
               this.fillWithOutline(world, boundingBox, 12, 1, l, 12, 4, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
               this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST), 2, 3, l, boundingBox);
               this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST), 11, 3, l, boundingBox);
               if (this.tall) {
                  this.fillWithOutline(world, boundingBox, 1, 6, l, 1, 9, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
                  this.fillWithOutline(world, boundingBox, 12, 6, l, 12, 9, l, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
               }
            } else {
               this.fillWithOutline(world, boundingBox, 1, 1, l, 1, 4, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
               this.fillWithOutline(world, boundingBox, 12, 1, l, 12, 4, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
               if (this.tall) {
                  this.fillWithOutline(world, boundingBox, 1, 6, l, 1, 9, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                  this.fillWithOutline(world, boundingBox, 12, 6, l, 12, 9, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
               }
            }
         }

         for(l = 3; l < 12; l += 2) {
            this.fillWithOutline(world, boundingBox, 3, 1, l, 4, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 6, 1, l, 7, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 9, 1, l, 10, 3, l, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
         }

         if (this.tall) {
            this.fillWithOutline(world, boundingBox, 1, 5, 1, 3, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 10, 5, 1, 12, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 4, 5, 1, 9, 5, 2, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.fillWithOutline(world, boundingBox, 4, 5, 12, 9, 5, 13, Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_PLANKS.getDefaultState(), false);
            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 9, 5, 11, boundingBox);
            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 8, 5, 11, boundingBox);
            this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 9, 5, 10, boundingBox);
            BlockState blockState = (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.SOUTH, true);
            this.fillWithOutline(world, boundingBox, 3, 6, 3, 3, 6, 11, blockState2, blockState2, false);
            this.fillWithOutline(world, boundingBox, 10, 6, 3, 10, 6, 9, blockState2, blockState2, false);
            this.fillWithOutline(world, boundingBox, 4, 6, 2, 9, 6, 2, blockState, blockState, false);
            this.fillWithOutline(world, boundingBox, 4, 6, 12, 7, 6, 12, blockState, blockState, false);
            this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 3, 6, 2, boundingBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.EAST, true), 3, 6, 12, boundingBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.WEST, true), 10, 6, 2, boundingBox);

            for(int n = 0; n <= 2; ++n) {
               this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.SOUTH, true)).with(FenceBlock.WEST, true), 8 + n, 6, 12 - n, boundingBox);
               if (n != 2) {
                  this.addBlock(world, (BlockState)((BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.NORTH, true)).with(FenceBlock.EAST, true), 8 + n, 6, 11 - n, boundingBox);
               }
            }

            BlockState blockState3 = (BlockState)Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.SOUTH);
            this.addBlock(world, blockState3, 10, 1, 13, boundingBox);
            this.addBlock(world, blockState3, 10, 2, 13, boundingBox);
            this.addBlock(world, blockState3, 10, 3, 13, boundingBox);
            this.addBlock(world, blockState3, 10, 4, 13, boundingBox);
            this.addBlock(world, blockState3, 10, 5, 13, boundingBox);
            this.addBlock(world, blockState3, 10, 6, 13, boundingBox);
            this.addBlock(world, blockState3, 10, 7, 13, boundingBox);
            int o = true;
            int p = true;
            BlockState blockState4 = (BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.EAST, true);
            this.addBlock(world, blockState4, 6, 9, 7, boundingBox);
            BlockState blockState5 = (BlockState)Blocks.OAK_FENCE.getDefaultState().with(FenceBlock.WEST, true);
            this.addBlock(world, blockState5, 7, 9, 7, boundingBox);
            this.addBlock(world, blockState4, 6, 8, 7, boundingBox);
            this.addBlock(world, blockState5, 7, 8, 7, boundingBox);
            BlockState blockState6 = (BlockState)((BlockState)blockState2.with(FenceBlock.WEST, true)).with(FenceBlock.EAST, true);
            this.addBlock(world, blockState6, 6, 7, 7, boundingBox);
            this.addBlock(world, blockState6, 7, 7, 7, boundingBox);
            this.addBlock(world, blockState4, 5, 7, 7, boundingBox);
            this.addBlock(world, blockState5, 8, 7, 7, boundingBox);
            this.addBlock(world, (BlockState)blockState4.with(FenceBlock.NORTH, true), 6, 7, 6, boundingBox);
            this.addBlock(world, (BlockState)blockState4.with(FenceBlock.SOUTH, true), 6, 7, 8, boundingBox);
            this.addBlock(world, (BlockState)blockState5.with(FenceBlock.NORTH, true), 7, 7, 6, boundingBox);
            this.addBlock(world, (BlockState)blockState5.with(FenceBlock.SOUTH, true), 7, 7, 8, boundingBox);
            BlockState blockState7 = Blocks.TORCH.getDefaultState();
            this.addBlock(world, blockState7, 5, 8, 7, boundingBox);
            this.addBlock(world, blockState7, 8, 8, 7, boundingBox);
            this.addBlock(world, blockState7, 6, 8, 6, boundingBox);
            this.addBlock(world, blockState7, 6, 8, 8, boundingBox);
            this.addBlock(world, blockState7, 7, 8, 6, boundingBox);
            this.addBlock(world, blockState7, 7, 8, 8, boundingBox);
         }

         this.addChest(world, boundingBox, random, 3, 3, 5, LootTables.STRONGHOLD_LIBRARY_CHEST);
         if (this.tall) {
            this.addBlock(world, AIR, 12, 9, 1, boundingBox);
            this.addChest(world, boundingBox, random, 12, 8, 1, LootTables.STRONGHOLD_LIBRARY_CHEST);
         }

         return true;
      }
   }

   public static class PrisonHall extends StrongholdGenerator.Piece {
      public PrisonHall(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_PRISON_HALL, chainLength);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = boundingBox;
      }

      public PrisonHall(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_PRISON_HALL, nbtCompound);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((StrongholdGenerator.Start)start, pieces, random, 1, 1);
      }

      public static StrongholdGenerator.PrisonHall create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 9, 5, 11, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new StrongholdGenerator.PrisonHall(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         this.fillWithOutline(world, boundingBox, 0, 0, 0, 8, 4, 10, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 1, 0);
         this.fillWithOutline(world, boundingBox, 1, 1, 10, 3, 3, 10, AIR, AIR, false);
         this.fillWithOutline(world, boundingBox, 4, 1, 1, 4, 3, 1, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 4, 1, 3, 4, 3, 3, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 4, 1, 7, 4, 3, 7, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.fillWithOutline(world, boundingBox, 4, 1, 9, 4, 3, 9, false, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);

         for(int i = 1; i <= 3; ++i) {
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, i, 4, boundingBox);
            this.addBlock(world, (BlockState)((BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true)).with(PaneBlock.EAST, true), 4, i, 5, boundingBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, i, 6, boundingBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 5, i, 5, boundingBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 6, i, 5, boundingBox);
            this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true)).with(PaneBlock.EAST, true), 7, i, 5, boundingBox);
         }

         this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, 3, 2, boundingBox);
         this.addBlock(world, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, true)).with(PaneBlock.SOUTH, true), 4, 3, 8, boundingBox);
         BlockState blockState = (BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.FACING, Direction.WEST);
         BlockState blockState2 = (BlockState)((BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.FACING, Direction.WEST)).with(DoorBlock.HALF, DoubleBlockHalf.UPPER);
         this.addBlock(world, blockState, 4, 1, 2, boundingBox);
         this.addBlock(world, blockState2, 4, 2, 2, boundingBox);
         this.addBlock(world, blockState, 4, 1, 8, boundingBox);
         this.addBlock(world, blockState2, 4, 2, 8, boundingBox);
         return true;
      }
   }

   public static class SquareRoom extends StrongholdGenerator.Piece {
      protected final int roomType;

      public SquareRoom(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_SQUARE_ROOM, chainLength);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = boundingBox;
         this.roomType = random.nextInt(5);
      }

      public SquareRoom(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_SQUARE_ROOM, nbtCompound);
         this.roomType = nbtCompound.getInt("Type");
      }

      protected void toNbt(NbtCompound tag) {
         super.toNbt(tag);
         tag.putInt("Type", this.roomType);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((StrongholdGenerator.Start)start, pieces, random, 4, 1);
         this.fillNWOpening((StrongholdGenerator.Start)start, pieces, random, 1, 4);
         this.fillSEOpening((StrongholdGenerator.Start)start, pieces, random, 1, 4);
      }

      public static StrongholdGenerator.SquareRoom create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -4, -1, 0, 11, 7, 11, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new StrongholdGenerator.SquareRoom(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         this.fillWithOutline(world, boundingBox, 0, 0, 0, 10, 6, 10, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, boundingBox, this.entryDoor, 4, 1, 0);
         this.fillWithOutline(world, boundingBox, 4, 1, 10, 6, 3, 10, AIR, AIR, false);
         this.fillWithOutline(world, boundingBox, 0, 1, 4, 0, 3, 6, AIR, AIR, false);
         this.fillWithOutline(world, boundingBox, 10, 1, 4, 10, 3, 6, AIR, AIR, false);
         int m;
         switch(this.roomType) {
         case 0:
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 1, 5, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 2, 5, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 3, 5, boundingBox);
            this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST), 4, 3, 5, boundingBox);
            this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST), 6, 3, 5, boundingBox);
            this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.SOUTH), 5, 3, 4, boundingBox);
            this.addBlock(world, (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 6, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 4, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 5, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 4, 1, 6, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 4, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 5, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 6, 1, 6, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 5, 1, 4, boundingBox);
            this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 5, 1, 6, boundingBox);
            break;
         case 1:
            for(m = 0; m < 5; ++m) {
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 1, 3 + m, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 7, 1, 3 + m, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3 + m, 1, 3, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3 + m, 1, 7, boundingBox);
            }

            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 1, 5, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 2, 5, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 5, 3, 5, boundingBox);
            this.addBlock(world, Blocks.WATER.getDefaultState(), 5, 4, 5, boundingBox);
            break;
         case 2:
            for(m = 1; m <= 9; ++m) {
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 1, 3, m, boundingBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 9, 3, m, boundingBox);
            }

            for(m = 1; m <= 9; ++m) {
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), m, 3, 1, boundingBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), m, 3, 9, boundingBox);
            }

            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 1, 4, boundingBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 1, 6, boundingBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 3, 4, boundingBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 5, 3, 6, boundingBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, 1, 5, boundingBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, 1, 5, boundingBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, 3, 5, boundingBox);
            this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, 3, 5, boundingBox);

            for(m = 1; m <= 3; ++m) {
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, m, 4, boundingBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, m, 4, boundingBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 4, m, 6, boundingBox);
               this.addBlock(world, Blocks.COBBLESTONE.getDefaultState(), 6, m, 6, boundingBox);
            }

            this.addBlock(world, Blocks.TORCH.getDefaultState(), 5, 3, 5, boundingBox);

            for(m = 2; m <= 8; ++m) {
               this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 2, 3, m, boundingBox);
               this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 3, 3, m, boundingBox);
               if (m <= 3 || m >= 7) {
                  this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 4, 3, m, boundingBox);
                  this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 5, 3, m, boundingBox);
                  this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 6, 3, m, boundingBox);
               }

               this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 7, 3, m, boundingBox);
               this.addBlock(world, Blocks.OAK_PLANKS.getDefaultState(), 8, 3, m, boundingBox);
            }

            BlockState blockState = (BlockState)Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.WEST);
            this.addBlock(world, blockState, 9, 1, 3, boundingBox);
            this.addBlock(world, blockState, 9, 2, 3, boundingBox);
            this.addBlock(world, blockState, 9, 3, 3, boundingBox);
            this.addChest(world, boundingBox, random, 3, 4, 8, LootTables.STRONGHOLD_CROSSING_CHEST);
         }

         return true;
      }
   }

   public static class RightTurn extends StrongholdGenerator.Turn {
      public RightTurn(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_RIGHT_TURN, chainLength);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = boundingBox;
      }

      public RightTurn(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_RIGHT_TURN, nbtCompound);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         Direction direction = this.getFacing();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.fillNWOpening((StrongholdGenerator.Start)start, pieces, random, 1, 1);
         } else {
            this.fillSEOpening((StrongholdGenerator.Start)start, pieces, random, 1, 1);
         }

      }

      public static StrongholdGenerator.RightTurn create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 5, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new StrongholdGenerator.RightTurn(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 4, 4, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 1, 0);
         Direction direction = this.getFacing();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.fillWithOutline(world, boundingBox, 0, 1, 1, 0, 3, 3, AIR, AIR, false);
         } else {
            this.fillWithOutline(world, boundingBox, 4, 1, 1, 4, 3, 3, AIR, AIR, false);
         }

         return true;
      }
   }

   public static class LeftTurn extends StrongholdGenerator.Turn {
      public LeftTurn(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_LEFT_TURN, chainLength);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = boundingBox;
      }

      public LeftTurn(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_LEFT_TURN, nbtCompound);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         Direction direction = this.getFacing();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.fillSEOpening((StrongholdGenerator.Start)start, pieces, random, 1, 1);
         } else {
            this.fillNWOpening((StrongholdGenerator.Start)start, pieces, random, 1, 1);
         }

      }

      public static StrongholdGenerator.LeftTurn create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 5, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new StrongholdGenerator.LeftTurn(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 4, 4, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 1, 0);
         Direction direction = this.getFacing();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.fillWithOutline(world, boundingBox, 4, 1, 1, 4, 3, 3, AIR, AIR, false);
         } else {
            this.fillWithOutline(world, boundingBox, 0, 1, 1, 0, 3, 3, AIR, AIR, false);
         }

         return true;
      }
   }

   public abstract static class Turn extends StrongholdGenerator.Piece {
      protected Turn(StructurePieceType structurePieceType, int i) {
         super(structurePieceType, i);
      }

      public Turn(StructurePieceType structurePieceType, NbtCompound nbtCompound) {
         super(structurePieceType, nbtCompound);
      }
   }

   public static class Stairs extends StrongholdGenerator.Piece {
      public Stairs(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_STAIRS, chainLength);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = boundingBox;
      }

      public Stairs(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_STAIRS, nbtCompound);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((StrongholdGenerator.Start)start, pieces, random, 1, 1);
      }

      public static StrongholdGenerator.Stairs create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -7, 0, 5, 11, 8, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new StrongholdGenerator.Stairs(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 10, 7, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 7, 0);
         this.generateEntrance(world, random, boundingBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 7);
         BlockState blockState = (BlockState)Blocks.COBBLESTONE_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.SOUTH);

         for(int i = 0; i < 6; ++i) {
            this.addBlock(world, blockState, 1, 6 - i, 1 + i, boundingBox);
            this.addBlock(world, blockState, 2, 6 - i, 1 + i, boundingBox);
            this.addBlock(world, blockState, 3, 6 - i, 1 + i, boundingBox);
            if (i < 5) {
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5 - i, 1 + i, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 5 - i, 1 + i, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 5 - i, 1 + i, boundingBox);
            }
         }

         return true;
      }
   }

   public static class ChestCorridor extends StrongholdGenerator.Piece {
      private boolean chestGenerated;

      public ChestCorridor(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, chainLength);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = boundingBox;
      }

      public ChestCorridor(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, nbtCompound);
         this.chestGenerated = nbtCompound.getBoolean("Chest");
      }

      protected void toNbt(NbtCompound tag) {
         super.toNbt(tag);
         tag.putBoolean("Chest", this.chestGenerated);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((StrongholdGenerator.Start)start, pieces, random, 1, 1);
      }

      public static StrongholdGenerator.ChestCorridor create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainlength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 7, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new StrongholdGenerator.ChestCorridor(chainlength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 4, 6, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 1, 0);
         this.generateEntrance(world, random, boundingBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 6);
         this.fillWithOutline(world, boundingBox, 3, 1, 2, 3, 1, 4, Blocks.STONE_BRICKS.getDefaultState(), Blocks.STONE_BRICKS.getDefaultState(), false);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 1, 1, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 1, 5, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 2, 2, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 3, 2, 4, boundingBox);

         for(int i = 2; i <= 4; ++i) {
            this.addBlock(world, Blocks.STONE_BRICK_SLAB.getDefaultState(), 2, 1, i, boundingBox);
         }

         if (!this.chestGenerated && boundingBox.contains(new BlockPos(this.applyXTransform(3, 3), this.applyYTransform(2), this.applyZTransform(3, 3)))) {
            this.chestGenerated = true;
            this.addChest(world, boundingBox, random, 3, 2, 3, LootTables.STRONGHOLD_CORRIDOR_CHEST);
         }

         return true;
      }
   }

   public static class Corridor extends StrongholdGenerator.Piece {
      private final boolean leftExitExists;
      private final boolean rightExitExists;

      public Corridor(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_CORRIDOR, chainLength);
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = boundingBox;
         this.leftExitExists = random.nextInt(2) == 0;
         this.rightExitExists = random.nextInt(2) == 0;
      }

      public Corridor(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_CORRIDOR, nbtCompound);
         this.leftExitExists = nbtCompound.getBoolean("Left");
         this.rightExitExists = nbtCompound.getBoolean("Right");
      }

      protected void toNbt(NbtCompound tag) {
         super.toNbt(tag);
         tag.putBoolean("Left", this.leftExitExists);
         tag.putBoolean("Right", this.rightExitExists);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         this.fillForwardOpening((StrongholdGenerator.Start)start, pieces, random, 1, 1);
         if (this.leftExitExists) {
            this.fillNWOpening((StrongholdGenerator.Start)start, pieces, random, 1, 2);
         }

         if (this.rightExitExists) {
            this.fillSEOpening((StrongholdGenerator.Start)start, pieces, random, 1, 2);
         }

      }

      public static StrongholdGenerator.Corridor create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 7, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new StrongholdGenerator.Corridor(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 4, 6, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 1, 0);
         this.generateEntrance(world, random, boundingBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 6);
         BlockState blockState = (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.EAST);
         BlockState blockState2 = (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, Direction.WEST);
         this.addBlockWithRandomThreshold(world, boundingBox, random, 0.1F, 1, 2, 1, blockState);
         this.addBlockWithRandomThreshold(world, boundingBox, random, 0.1F, 3, 2, 1, blockState2);
         this.addBlockWithRandomThreshold(world, boundingBox, random, 0.1F, 1, 2, 5, blockState);
         this.addBlockWithRandomThreshold(world, boundingBox, random, 0.1F, 3, 2, 5, blockState2);
         if (this.leftExitExists) {
            this.fillWithOutline(world, boundingBox, 0, 1, 2, 0, 3, 4, AIR, AIR, false);
         }

         if (this.rightExitExists) {
            this.fillWithOutline(world, boundingBox, 4, 1, 2, 4, 3, 4, AIR, AIR, false);
         }

         return true;
      }
   }

   public static class Start extends StrongholdGenerator.SpiralStaircase {
      public StrongholdGenerator.PieceData lastPiece;
      @Nullable
      public StrongholdGenerator.PortalRoom portalRoom;
      public final List<StructurePiece> pieces = Lists.newArrayList();

      public Start(Random random, int i, int j) {
         super(StructurePieceType.STRONGHOLD_START, 0, random, i, j);
      }

      public Start(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_START, nbtCompound);
      }
   }

   public static class SpiralStaircase extends StrongholdGenerator.Piece {
      private final boolean isStructureStart;

      public SpiralStaircase(StructurePieceType structurePieceType, int chainLength, Random random, int x, int z) {
         super(structurePieceType, chainLength);
         this.isStructureStart = true;
         this.setOrientation(Direction.Type.HORIZONTAL.random(random));
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.OPENING;
         if (this.getFacing().getAxis() == Direction.Axis.Z) {
            this.boundingBox = new BlockBox(x, 64, z, x + 5 - 1, 74, z + 5 - 1);
         } else {
            this.boundingBox = new BlockBox(x, 64, z, x + 5 - 1, 74, z + 5 - 1);
         }

      }

      public SpiralStaircase(int chainLength, Random random, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_SPIRAL_STAIRCASE, chainLength);
         this.isStructureStart = false;
         this.setOrientation(orientation);
         this.entryDoor = this.getRandomEntrance(random);
         this.boundingBox = boundingBox;
      }

      public SpiralStaircase(StructurePieceType structurePieceType, NbtCompound nbtCompound) {
         super(structurePieceType, nbtCompound);
         this.isStructureStart = nbtCompound.getBoolean("Source");
      }

      public SpiralStaircase(StructureManager structureManager, NbtCompound nbtCompound) {
         this(StructurePieceType.STRONGHOLD_SPIRAL_STAIRCASE, nbtCompound);
      }

      protected void toNbt(NbtCompound tag) {
         super.toNbt(tag);
         tag.putBoolean("Source", this.isStructureStart);
      }

      public void fillOpenings(StructurePiece start, List<StructurePiece> pieces, Random random) {
         if (this.isStructureStart) {
            StrongholdGenerator.activePieceType = StrongholdGenerator.FiveWayCrossing.class;
         }

         this.fillForwardOpening((StrongholdGenerator.Start)start, pieces, random, 1, 1);
      }

      public static StrongholdGenerator.SpiralStaircase create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation, int chainLength) {
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -7, 0, 5, 11, 5, orientation);
         return isInbounds(blockBox) && StructurePiece.getOverlappingPiece(pieces, blockBox) == null ? new StrongholdGenerator.SpiralStaircase(chainLength, random, blockBox, orientation) : null;
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         this.fillWithOutline(world, boundingBox, 0, 0, 0, 4, 10, 4, true, random, StrongholdGenerator.STONE_BRICK_RANDOMIZER);
         this.generateEntrance(world, random, boundingBox, this.entryDoor, 1, 7, 0);
         this.generateEntrance(world, random, boundingBox, StrongholdGenerator.Piece.EntranceType.OPENING, 1, 1, 4);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 6, 1, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5, 1, boundingBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 6, 1, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 5, 2, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 4, 3, boundingBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 5, 3, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 4, 3, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 3, 3, boundingBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 3, 4, 3, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 3, 2, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 2, 1, boundingBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 3, 3, 1, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 2, 1, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 1, 1, boundingBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 2, 1, boundingBox);
         this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 1, 2, boundingBox);
         this.addBlock(world, Blocks.SMOOTH_STONE_SLAB.getDefaultState(), 1, 1, 3, boundingBox);
         return true;
      }
   }

   public static class SmallCorridor extends StrongholdGenerator.Piece {
      private final int length;

      public SmallCorridor(int chainLength, BlockBox boundingBox, Direction orientation) {
         super(StructurePieceType.STRONGHOLD_SMALL_CORRIDOR, chainLength);
         this.setOrientation(orientation);
         this.boundingBox = boundingBox;
         this.length = orientation != Direction.NORTH && orientation != Direction.SOUTH ? boundingBox.getBlockCountX() : boundingBox.getBlockCountZ();
      }

      public SmallCorridor(StructureManager structureManager, NbtCompound nbtCompound) {
         super(StructurePieceType.STRONGHOLD_SMALL_CORRIDOR, nbtCompound);
         this.length = nbtCompound.getInt("Steps");
      }

      protected void toNbt(NbtCompound tag) {
         super.toNbt(tag);
         tag.putInt("Steps", this.length);
      }

      public static BlockBox create(List<StructurePiece> pieces, Random random, int x, int y, int z, Direction orientation) {
         int i = true;
         BlockBox blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, 4, orientation);
         StructurePiece structurePiece = StructurePiece.getOverlappingPiece(pieces, blockBox);
         if (structurePiece == null) {
            return null;
         } else {
            if (structurePiece.getBoundingBox().minY == blockBox.minY) {
               for(int j = 3; j >= 1; --j) {
                  blockBox = BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, j - 1, orientation);
                  if (!structurePiece.getBoundingBox().intersects(blockBox)) {
                     return BlockBox.rotated(x, y, z, -1, -1, 0, 5, 5, j, orientation);
                  }
               }
            }

            return null;
         }
      }

      public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
         for(int i = 0; i < this.length; ++i) {
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, 0, i, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 0, i, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 0, i, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 0, i, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, 0, i, boundingBox);

            for(int j = 1; j <= 3; ++j) {
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, j, i, boundingBox);
               this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 1, j, i, boundingBox);
               this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 2, j, i, boundingBox);
               this.addBlock(world, Blocks.CAVE_AIR.getDefaultState(), 3, j, i, boundingBox);
               this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, j, i, boundingBox);
            }

            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 0, 4, i, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 1, 4, i, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 2, 4, i, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 3, 4, i, boundingBox);
            this.addBlock(world, Blocks.STONE_BRICKS.getDefaultState(), 4, 4, i, boundingBox);
         }

         return true;
      }
   }

   abstract static class Piece extends StructurePiece {
      protected StrongholdGenerator.Piece.EntranceType entryDoor;

      protected Piece(StructurePieceType structurePieceType, int i) {
         super(structurePieceType, i);
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.OPENING;
      }

      public Piece(StructurePieceType structurePieceType, NbtCompound nbtCompound) {
         super(structurePieceType, nbtCompound);
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.OPENING;
         this.entryDoor = StrongholdGenerator.Piece.EntranceType.valueOf(nbtCompound.getString("EntryDoor"));
      }

      protected void toNbt(NbtCompound tag) {
         tag.putString("EntryDoor", this.entryDoor.name());
      }

      protected void generateEntrance(StructureWorldAccess structureWorldAccess, Random random, BlockBox boundingBox, StrongholdGenerator.Piece.EntranceType type, int x, int y, int z) {
         switch(type) {
         case OPENING:
            this.fillWithOutline(structureWorldAccess, boundingBox, x, y, z, x + 3 - 1, y + 3 - 1, z, AIR, AIR, false);
            break;
         case WOOD_DOOR:
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x, y, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x, y + 1, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x, y + 2, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x + 1, y + 2, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 2, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 1, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.OAK_DOOR.getDefaultState(), x + 1, y, z, boundingBox);
            this.addBlock(structureWorldAccess, (BlockState)Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, boundingBox);
            break;
         case GRATES:
            this.addBlock(structureWorldAccess, Blocks.CAVE_AIR.getDefaultState(), x + 1, y, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.CAVE_AIR.getDefaultState(), x + 1, y + 1, z, boundingBox);
            this.addBlock(structureWorldAccess, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true), x, y, z, boundingBox);
            this.addBlock(structureWorldAccess, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.WEST, true), x, y + 1, z, boundingBox);
            this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x, y + 2, z, boundingBox);
            this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x + 1, y + 2, z, boundingBox);
            this.addBlock(structureWorldAccess, (BlockState)((BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true)).with(PaneBlock.WEST, true), x + 2, y + 2, z, boundingBox);
            this.addBlock(structureWorldAccess, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true), x + 2, y + 1, z, boundingBox);
            this.addBlock(structureWorldAccess, (BlockState)Blocks.IRON_BARS.getDefaultState().with(PaneBlock.EAST, true), x + 2, y, z, boundingBox);
            break;
         case IRON_DOOR:
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x, y, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x, y + 1, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x, y + 2, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x + 1, y + 2, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 2, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y + 1, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.STONE_BRICKS.getDefaultState(), x + 2, y, z, boundingBox);
            this.addBlock(structureWorldAccess, Blocks.IRON_DOOR.getDefaultState(), x + 1, y, z, boundingBox);
            this.addBlock(structureWorldAccess, (BlockState)Blocks.IRON_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER), x + 1, y + 1, z, boundingBox);
            this.addBlock(structureWorldAccess, (BlockState)Blocks.STONE_BUTTON.getDefaultState().with(AbstractButtonBlock.FACING, Direction.NORTH), x + 2, y + 1, z + 1, boundingBox);
            this.addBlock(structureWorldAccess, (BlockState)Blocks.STONE_BUTTON.getDefaultState().with(AbstractButtonBlock.FACING, Direction.SOUTH), x + 2, y + 1, z - 1, boundingBox);
         }

      }

      protected StrongholdGenerator.Piece.EntranceType getRandomEntrance(Random random) {
         int i = random.nextInt(5);
         switch(i) {
         case 0:
         case 1:
         default:
            return StrongholdGenerator.Piece.EntranceType.OPENING;
         case 2:
            return StrongholdGenerator.Piece.EntranceType.WOOD_DOOR;
         case 3:
            return StrongholdGenerator.Piece.EntranceType.GRATES;
         case 4:
            return StrongholdGenerator.Piece.EntranceType.IRON_DOOR;
         }
      }

      @Nullable
      protected StructurePiece fillForwardOpening(StrongholdGenerator.Start start, List<StructurePiece> pieces, Random random, int leftRightOffset, int heightOffset) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.minZ - 1, direction, this.getChainLength());
            case SOUTH:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.maxZ + 1, direction, this.getChainLength());
            case WEST:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, direction, this.getChainLength());
            case EAST:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, direction, this.getChainLength());
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece fillNWOpening(StrongholdGenerator.Start start, List<StructurePiece> pieces, Random random, int heightOffset, int leftRightOffset) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, Direction.WEST, this.getChainLength());
            case SOUTH:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX - 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, Direction.WEST, this.getChainLength());
            case WEST:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.minZ - 1, Direction.NORTH, this.getChainLength());
            case EAST:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.minZ - 1, Direction.NORTH, this.getChainLength());
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece fillSEOpening(StrongholdGenerator.Start start, List<StructurePiece> pieces, Random random, int heightOffset, int leftRightOffset) {
         Direction direction = this.getFacing();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, Direction.EAST, this.getChainLength());
            case SOUTH:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.maxX + 1, this.boundingBox.minY + heightOffset, this.boundingBox.minZ + leftRightOffset, Direction.EAST, this.getChainLength());
            case WEST:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.maxZ + 1, Direction.SOUTH, this.getChainLength());
            case EAST:
               return StrongholdGenerator.pieceGenerator(start, pieces, random, this.boundingBox.minX + leftRightOffset, this.boundingBox.minY + heightOffset, this.boundingBox.maxZ + 1, Direction.SOUTH, this.getChainLength());
            }
         }

         return null;
      }

      protected static boolean isInbounds(BlockBox boundingBox) {
         return boundingBox != null && boundingBox.minY > 10;
      }

      public static enum EntranceType {
         OPENING,
         WOOD_DOOR,
         GRATES,
         IRON_DOOR;
      }
   }

   static class PieceData {
      public final Class<? extends StrongholdGenerator.Piece> pieceType;
      public final int weight;
      public int generatedCount;
      public final int limit;

      public PieceData(Class<? extends StrongholdGenerator.Piece> pieceType, int weight, int limit) {
         this.pieceType = pieceType;
         this.weight = weight;
         this.limit = limit;
      }

      public boolean canGenerate(int chainLength) {
         return this.limit == 0 || this.generatedCount < this.limit;
      }

      public boolean canGenerate() {
         return this.limit == 0 || this.generatedCount < this.limit;
      }
   }
}
