package net.minecraft.entity.ai.pathing;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import java.util.Iterator;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class LandPathNodeMaker extends PathNodeMaker {
   protected float waterPathNodeTypeWeight;
   private final Long2ObjectMap<PathNodeType> field_25190 = new Long2ObjectOpenHashMap();
   private final Object2BooleanMap<Box> field_25191 = new Object2BooleanOpenHashMap();

   public void init(ChunkCache cachedWorld, MobEntity entity) {
      super.init(cachedWorld, entity);
      this.waterPathNodeTypeWeight = entity.getPathfindingPenalty(PathNodeType.WATER);
   }

   public void clear() {
      this.entity.setPathfindingPenalty(PathNodeType.WATER, this.waterPathNodeTypeWeight);
      this.field_25190.clear();
      this.field_25191.clear();
      super.clear();
   }

   public PathNode getStart() {
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      int i = MathHelper.floor(this.entity.getY());
      BlockState blockState = this.cachedWorld.getBlockState(mutable.set(this.entity.getX(), (double)i, this.entity.getZ()));
      BlockPos blockPos;
      if (!this.entity.canWalkOnFluid(blockState.getFluidState().getFluid())) {
         if (this.canSwim() && this.entity.isTouchingWater()) {
            while(true) {
               if (blockState.getBlock() != Blocks.WATER && blockState.getFluidState() != Fluids.WATER.getStill(false)) {
                  --i;
                  break;
               }

               ++i;
               blockState = this.cachedWorld.getBlockState(mutable.set(this.entity.getX(), (double)i, this.entity.getZ()));
            }
         } else if (this.entity.isOnGround()) {
            i = MathHelper.floor(this.entity.getY() + 0.5D);
         } else {
            for(blockPos = this.entity.getBlockPos(); (this.cachedWorld.getBlockState(blockPos).isAir() || this.cachedWorld.getBlockState(blockPos).canPathfindThrough(this.cachedWorld, blockPos, NavigationType.LAND)) && blockPos.getY() > 0; blockPos = blockPos.down()) {
            }

            i = blockPos.up().getY();
         }
      } else {
         while(true) {
            if (!this.entity.canWalkOnFluid(blockState.getFluidState().getFluid())) {
               --i;
               break;
            }

            ++i;
            blockState = this.cachedWorld.getBlockState(mutable.set(this.entity.getX(), (double)i, this.entity.getZ()));
         }
      }

      blockPos = this.entity.getBlockPos();
      PathNodeType pathNodeType = this.method_29303(this.entity, blockPos.getX(), i, blockPos.getZ());
      if (this.entity.getPathfindingPenalty(pathNodeType) < 0.0F) {
         Box box = this.entity.getBoundingBox();
         if (this.method_27139(mutable.set(box.minX, (double)i, box.minZ)) || this.method_27139(mutable.set(box.minX, (double)i, box.maxZ)) || this.method_27139(mutable.set(box.maxX, (double)i, box.minZ)) || this.method_27139(mutable.set(box.maxX, (double)i, box.maxZ))) {
            PathNode pathNode = this.method_27137(mutable);
            pathNode.type = this.getNodeType(this.entity, pathNode.getBlockPos());
            pathNode.penalty = this.entity.getPathfindingPenalty(pathNode.type);
            return pathNode;
         }
      }

      PathNode pathNode2 = this.getNode(blockPos.getX(), i, blockPos.getZ());
      pathNode2.type = this.getNodeType(this.entity, pathNode2.getBlockPos());
      pathNode2.penalty = this.entity.getPathfindingPenalty(pathNode2.type);
      return pathNode2;
   }

   private boolean method_27139(BlockPos blockPos) {
      PathNodeType pathNodeType = this.getNodeType(this.entity, blockPos);
      return this.entity.getPathfindingPenalty(pathNodeType) >= 0.0F;
   }

   public TargetPathNode getNode(double x, double y, double z) {
      return new TargetPathNode(this.getNode(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z)));
   }

   public int getSuccessors(PathNode[] successors, PathNode node) {
      int i = 0;
      int j = 0;
      PathNodeType pathNodeType = this.method_29303(this.entity, node.x, node.y + 1, node.z);
      PathNodeType pathNodeType2 = this.method_29303(this.entity, node.x, node.y, node.z);
      if (this.entity.getPathfindingPenalty(pathNodeType) >= 0.0F && pathNodeType2 != PathNodeType.STICKY_HONEY) {
         j = MathHelper.floor(Math.max(1.0F, this.entity.stepHeight));
      }

      double d = getFeetY(this.cachedWorld, new BlockPos(node.x, node.y, node.z));
      PathNode pathNode = this.getPathNode(node.x, node.y, node.z + 1, j, d, Direction.SOUTH, pathNodeType2);
      if (this.isValidAdjacentSuccessor(pathNode, node)) {
         successors[i++] = pathNode;
      }

      PathNode pathNode2 = this.getPathNode(node.x - 1, node.y, node.z, j, d, Direction.WEST, pathNodeType2);
      if (this.isValidAdjacentSuccessor(pathNode2, node)) {
         successors[i++] = pathNode2;
      }

      PathNode pathNode3 = this.getPathNode(node.x + 1, node.y, node.z, j, d, Direction.EAST, pathNodeType2);
      if (this.isValidAdjacentSuccessor(pathNode3, node)) {
         successors[i++] = pathNode3;
      }

      PathNode pathNode4 = this.getPathNode(node.x, node.y, node.z - 1, j, d, Direction.NORTH, pathNodeType2);
      if (this.isValidAdjacentSuccessor(pathNode4, node)) {
         successors[i++] = pathNode4;
      }

      PathNode pathNode5 = this.getPathNode(node.x - 1, node.y, node.z - 1, j, d, Direction.NORTH, pathNodeType2);
      if (this.method_29579(node, pathNode2, pathNode4, pathNode5)) {
         successors[i++] = pathNode5;
      }

      PathNode pathNode6 = this.getPathNode(node.x + 1, node.y, node.z - 1, j, d, Direction.NORTH, pathNodeType2);
      if (this.method_29579(node, pathNode3, pathNode4, pathNode6)) {
         successors[i++] = pathNode6;
      }

      PathNode pathNode7 = this.getPathNode(node.x - 1, node.y, node.z + 1, j, d, Direction.SOUTH, pathNodeType2);
      if (this.method_29579(node, pathNode2, pathNode, pathNode7)) {
         successors[i++] = pathNode7;
      }

      PathNode pathNode8 = this.getPathNode(node.x + 1, node.y, node.z + 1, j, d, Direction.SOUTH, pathNodeType2);
      if (this.method_29579(node, pathNode3, pathNode, pathNode8)) {
         successors[i++] = pathNode8;
      }

      return i;
   }

   private boolean isValidAdjacentSuccessor(PathNode node, PathNode successor1) {
      return node != null && !node.visited && (node.penalty >= 0.0F || successor1.penalty < 0.0F);
   }

   private boolean method_29579(PathNode pathNode, @Nullable PathNode pathNode2, @Nullable PathNode pathNode3, @Nullable PathNode pathNode4) {
      if (pathNode4 != null && pathNode3 != null && pathNode2 != null) {
         if (pathNode4.visited) {
            return false;
         } else if (pathNode3.y <= pathNode.y && pathNode2.y <= pathNode.y) {
            if (pathNode2.type != PathNodeType.WALKABLE_DOOR && pathNode3.type != PathNodeType.WALKABLE_DOOR && pathNode4.type != PathNodeType.WALKABLE_DOOR) {
               boolean bl = pathNode3.type == PathNodeType.FENCE && pathNode2.type == PathNodeType.FENCE && (double)this.entity.getWidth() < 0.5D;
               return pathNode4.penalty >= 0.0F && (pathNode3.y < pathNode.y || pathNode3.penalty >= 0.0F || bl) && (pathNode2.y < pathNode.y || pathNode2.penalty >= 0.0F || bl);
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean method_29578(PathNode pathNode) {
      Vec3d vec3d = new Vec3d((double)pathNode.x - this.entity.getX(), (double)pathNode.y - this.entity.getY(), (double)pathNode.z - this.entity.getZ());
      Box box = this.entity.getBoundingBox();
      int i = MathHelper.ceil(vec3d.length() / box.getAverageSideLength());
      vec3d = vec3d.multiply((double)(1.0F / (float)i));

      for(int j = 1; j <= i; ++j) {
         box = box.offset(vec3d);
         if (this.method_29304(box)) {
            return false;
         }
      }

      return true;
   }

   public static double getFeetY(BlockView world, BlockPos pos) {
      BlockPos blockPos = pos.down();
      VoxelShape voxelShape = world.getBlockState(blockPos).getCollisionShape(world, blockPos);
      return (double)blockPos.getY() + (voxelShape.isEmpty() ? 0.0D : voxelShape.getMax(Direction.Axis.Y));
   }

   @Nullable
   private PathNode getPathNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType) {
      PathNode pathNode = null;
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      double d = getFeetY(this.cachedWorld, mutable.set(x, y, z));
      if (d - prevFeetY > 1.125D) {
         return null;
      } else {
         PathNodeType pathNodeType = this.method_29303(this.entity, x, y, z);
         float f = this.entity.getPathfindingPenalty(pathNodeType);
         double e = (double)this.entity.getWidth() / 2.0D;
         if (f >= 0.0F) {
            pathNode = this.getNode(x, y, z);
            pathNode.type = pathNodeType;
            pathNode.penalty = Math.max(pathNode.penalty, f);
         }

         if (nodeType == PathNodeType.FENCE && pathNode != null && pathNode.penalty >= 0.0F && !this.method_29578(pathNode)) {
            pathNode = null;
         }

         if (pathNodeType == PathNodeType.WALKABLE) {
            return pathNode;
         } else {
            if ((pathNode == null || pathNode.penalty < 0.0F) && maxYStep > 0 && pathNodeType != PathNodeType.FENCE && pathNodeType != PathNodeType.UNPASSABLE_RAIL && pathNodeType != PathNodeType.TRAPDOOR) {
               pathNode = this.getPathNode(x, y + 1, z, maxYStep - 1, prevFeetY, direction, nodeType);
               if (pathNode != null && (pathNode.type == PathNodeType.OPEN || pathNode.type == PathNodeType.WALKABLE) && this.entity.getWidth() < 1.0F) {
                  double g = (double)(x - direction.getOffsetX()) + 0.5D;
                  double h = (double)(z - direction.getOffsetZ()) + 0.5D;
                  Box box = new Box(g - e, getFeetY(this.cachedWorld, mutable.set(g, (double)(y + 1), h)) + 0.001D, h - e, g + e, (double)this.entity.getHeight() + getFeetY(this.cachedWorld, mutable.set((double)pathNode.x, (double)pathNode.y, (double)pathNode.z)) - 0.002D, h + e);
                  if (this.method_29304(box)) {
                     pathNode = null;
                  }
               }
            }

            if (pathNodeType == PathNodeType.WATER && !this.canSwim()) {
               if (this.method_29303(this.entity, x, y - 1, z) != PathNodeType.WATER) {
                  return pathNode;
               }

               while(y > 0) {
                  --y;
                  pathNodeType = this.method_29303(this.entity, x, y, z);
                  if (pathNodeType != PathNodeType.WATER) {
                     return pathNode;
                  }

                  pathNode = this.getNode(x, y, z);
                  pathNode.type = pathNodeType;
                  pathNode.penalty = Math.max(pathNode.penalty, this.entity.getPathfindingPenalty(pathNodeType));
               }
            }

            if (pathNodeType == PathNodeType.OPEN) {
               int i = 0;
               int j = y;

               while(pathNodeType == PathNodeType.OPEN) {
                  --y;
                  PathNode pathNode4;
                  if (y < 0) {
                     pathNode4 = this.getNode(x, j, z);
                     pathNode4.type = PathNodeType.BLOCKED;
                     pathNode4.penalty = -1.0F;
                     return pathNode4;
                  }

                  if (i++ >= this.entity.getSafeFallDistance()) {
                     pathNode4 = this.getNode(x, y, z);
                     pathNode4.type = PathNodeType.BLOCKED;
                     pathNode4.penalty = -1.0F;
                     return pathNode4;
                  }

                  pathNodeType = this.method_29303(this.entity, x, y, z);
                  f = this.entity.getPathfindingPenalty(pathNodeType);
                  if (pathNodeType != PathNodeType.OPEN && f >= 0.0F) {
                     pathNode = this.getNode(x, y, z);
                     pathNode.type = pathNodeType;
                     pathNode.penalty = Math.max(pathNode.penalty, f);
                     break;
                  }

                  if (f < 0.0F) {
                     pathNode4 = this.getNode(x, y, z);
                     pathNode4.type = PathNodeType.BLOCKED;
                     pathNode4.penalty = -1.0F;
                     return pathNode4;
                  }
               }
            }

            if (pathNodeType == PathNodeType.FENCE) {
               pathNode = this.getNode(x, y, z);
               pathNode.visited = true;
               pathNode.type = pathNodeType;
               pathNode.penalty = pathNodeType.getDefaultPenalty();
            }

            return pathNode;
         }
      }
   }

   private boolean method_29304(Box box) {
      return (Boolean)this.field_25191.computeIfAbsent(box, (box2) -> {
         return !this.cachedWorld.isSpaceEmpty(this.entity, box);
      });
   }

   public PathNodeType getNodeType(BlockView world, int x, int y, int z, MobEntity mob, int sizeX, int sizeY, int sizeZ, boolean canOpenDoors, boolean canEnterOpenDoors) {
      EnumSet<PathNodeType> enumSet = EnumSet.noneOf(PathNodeType.class);
      PathNodeType pathNodeType = PathNodeType.BLOCKED;
      BlockPos blockPos = mob.getBlockPos();
      pathNodeType = this.findNearbyNodeTypes(world, x, y, z, sizeX, sizeY, sizeZ, canOpenDoors, canEnterOpenDoors, enumSet, pathNodeType, blockPos);
      if (enumSet.contains(PathNodeType.FENCE)) {
         return PathNodeType.FENCE;
      } else if (enumSet.contains(PathNodeType.UNPASSABLE_RAIL)) {
         return PathNodeType.UNPASSABLE_RAIL;
      } else {
         PathNodeType pathNodeType2 = PathNodeType.BLOCKED;
         Iterator var15 = enumSet.iterator();

         while(var15.hasNext()) {
            PathNodeType pathNodeType3 = (PathNodeType)var15.next();
            if (mob.getPathfindingPenalty(pathNodeType3) < 0.0F) {
               return pathNodeType3;
            }

            if (mob.getPathfindingPenalty(pathNodeType3) >= mob.getPathfindingPenalty(pathNodeType2)) {
               pathNodeType2 = pathNodeType3;
            }
         }

         if (pathNodeType == PathNodeType.OPEN && mob.getPathfindingPenalty(pathNodeType2) == 0.0F && sizeX <= 1) {
            return PathNodeType.OPEN;
         } else {
            return pathNodeType2;
         }
      }
   }

   /**
    * Adds the node types in the box with the given size to the input EnumSet.
    * @return The node type at the least coordinates of the input box.
    */
   public PathNodeType findNearbyNodeTypes(BlockView world, int x, int y, int z, int sizeX, int sizeY, int sizeZ, boolean canOpenDoors, boolean canEnterOpenDoors, EnumSet<PathNodeType> nearbyTypes, PathNodeType type, BlockPos pos) {
      for(int i = 0; i < sizeX; ++i) {
         for(int j = 0; j < sizeY; ++j) {
            for(int k = 0; k < sizeZ; ++k) {
               int l = i + x;
               int m = j + y;
               int n = k + z;
               PathNodeType pathNodeType = this.getDefaultNodeType(world, l, m, n);
               pathNodeType = this.adjustNodeType(world, canOpenDoors, canEnterOpenDoors, pos, pathNodeType);
               if (i == 0 && j == 0 && k == 0) {
                  type = pathNodeType;
               }

               nearbyTypes.add(pathNodeType);
            }
         }
      }

      return type;
   }

   protected PathNodeType adjustNodeType(BlockView world, boolean canOpenDoors, boolean canEnterOpenDoors, BlockPos pos, PathNodeType type) {
      if (type == PathNodeType.DOOR_WOOD_CLOSED && canOpenDoors && canEnterOpenDoors) {
         type = PathNodeType.WALKABLE_DOOR;
      }

      if (type == PathNodeType.DOOR_OPEN && !canEnterOpenDoors) {
         type = PathNodeType.BLOCKED;
      }

      if (type == PathNodeType.RAIL && !(world.getBlockState(pos).getBlock() instanceof AbstractRailBlock) && !(world.getBlockState(pos.down()).getBlock() instanceof AbstractRailBlock)) {
         type = PathNodeType.UNPASSABLE_RAIL;
      }

      if (type == PathNodeType.LEAVES) {
         type = PathNodeType.BLOCKED;
      }

      return type;
   }

   private PathNodeType getNodeType(MobEntity entity, BlockPos pos) {
      return this.method_29303(entity, pos.getX(), pos.getY(), pos.getZ());
   }

   private PathNodeType method_29303(MobEntity mobEntity, int i, int j, int k) {
      return (PathNodeType)this.field_25190.computeIfAbsent(BlockPos.asLong(i, j, k), (l) -> {
         return this.getNodeType(this.cachedWorld, i, j, k, mobEntity, this.entityBlockXSize, this.entityBlockYSize, this.entityBlockZSize, this.canOpenDoors(), this.canEnterOpenDoors());
      });
   }

   public PathNodeType getDefaultNodeType(BlockView world, int x, int y, int z) {
      return getLandNodeType(world, new BlockPos.Mutable(x, y, z));
   }

   public static PathNodeType getLandNodeType(BlockView world, BlockPos.Mutable pos) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      PathNodeType pathNodeType = getCommonNodeType(world, pos);
      if (pathNodeType == PathNodeType.OPEN && j >= 1) {
         PathNodeType pathNodeType2 = getCommonNodeType(world, pos.set(i, j - 1, k));
         pathNodeType = pathNodeType2 != PathNodeType.WALKABLE && pathNodeType2 != PathNodeType.OPEN && pathNodeType2 != PathNodeType.WATER && pathNodeType2 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;
         if (pathNodeType2 == PathNodeType.DAMAGE_FIRE) {
            pathNodeType = PathNodeType.DAMAGE_FIRE;
         }

         if (pathNodeType2 == PathNodeType.DAMAGE_CACTUS) {
            pathNodeType = PathNodeType.DAMAGE_CACTUS;
         }

         if (pathNodeType2 == PathNodeType.DAMAGE_OTHER) {
            pathNodeType = PathNodeType.DAMAGE_OTHER;
         }

         if (pathNodeType2 == PathNodeType.STICKY_HONEY) {
            pathNodeType = PathNodeType.STICKY_HONEY;
         }
      }

      if (pathNodeType == PathNodeType.WALKABLE) {
         pathNodeType = getNodeTypeFromNeighbors(world, pos.set(i, j, k), pathNodeType);
      }

      return pathNodeType;
   }

   public static PathNodeType getNodeTypeFromNeighbors(BlockView world, BlockPos.Mutable pos, PathNodeType nodeType) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();

      for(int l = -1; l <= 1; ++l) {
         for(int m = -1; m <= 1; ++m) {
            for(int n = -1; n <= 1; ++n) {
               if (l != 0 || n != 0) {
                  pos.set(i + l, j + m, k + n);
                  BlockState blockState = world.getBlockState(pos);
                  if (blockState.isOf(Blocks.CACTUS)) {
                     return PathNodeType.DANGER_CACTUS;
                  }

                  if (blockState.isOf(Blocks.SWEET_BERRY_BUSH)) {
                     return PathNodeType.DANGER_OTHER;
                  }

                  if (method_27138(blockState)) {
                     return PathNodeType.DANGER_FIRE;
                  }

                  if (world.getFluidState(pos).isIn(FluidTags.WATER)) {
                     return PathNodeType.WATER_BORDER;
                  }
               }
            }
         }
      }

      return nodeType;
   }

   protected static PathNodeType getCommonNodeType(BlockView world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos);
      Block block = blockState.getBlock();
      Material material = blockState.getMaterial();
      if (blockState.isAir()) {
         return PathNodeType.OPEN;
      } else if (!blockState.isIn(BlockTags.TRAPDOORS) && !blockState.isOf(Blocks.LILY_PAD)) {
         if (blockState.isOf(Blocks.CACTUS)) {
            return PathNodeType.DAMAGE_CACTUS;
         } else if (blockState.isOf(Blocks.SWEET_BERRY_BUSH)) {
            return PathNodeType.DAMAGE_OTHER;
         } else if (blockState.isOf(Blocks.HONEY_BLOCK)) {
            return PathNodeType.STICKY_HONEY;
         } else if (blockState.isOf(Blocks.COCOA)) {
            return PathNodeType.COCOA;
         } else {
            FluidState fluidState = world.getFluidState(pos);
            if (fluidState.isIn(FluidTags.WATER)) {
               return PathNodeType.WATER;
            } else if (fluidState.isIn(FluidTags.LAVA)) {
               return PathNodeType.LAVA;
            } else if (method_27138(blockState)) {
               return PathNodeType.DAMAGE_FIRE;
            } else if (DoorBlock.isWoodenDoor(blockState) && !(Boolean)blockState.get(DoorBlock.OPEN)) {
               return PathNodeType.DOOR_WOOD_CLOSED;
            } else if (block instanceof DoorBlock && material == Material.METAL && !(Boolean)blockState.get(DoorBlock.OPEN)) {
               return PathNodeType.DOOR_IRON_CLOSED;
            } else if (block instanceof DoorBlock && (Boolean)blockState.get(DoorBlock.OPEN)) {
               return PathNodeType.DOOR_OPEN;
            } else if (block instanceof AbstractRailBlock) {
               return PathNodeType.RAIL;
            } else if (block instanceof LeavesBlock) {
               return PathNodeType.LEAVES;
            } else if (!block.isIn(BlockTags.FENCES) && !block.isIn(BlockTags.WALLS) && (!(block instanceof FenceGateBlock) || (Boolean)blockState.get(FenceGateBlock.OPEN))) {
               return !blockState.canPathfindThrough(world, pos, NavigationType.LAND) ? PathNodeType.BLOCKED : PathNodeType.OPEN;
            } else {
               return PathNodeType.FENCE;
            }
         }
      } else {
         return PathNodeType.TRAPDOOR;
      }
   }

   private static boolean method_27138(BlockState blockState) {
      return blockState.isIn(BlockTags.FIRE) || blockState.isOf(Blocks.LAVA) || blockState.isOf(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState);
   }
}
