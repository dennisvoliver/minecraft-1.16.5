package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.enums.WallShape;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class WallBlock extends Block implements Waterloggable {
   public static final BooleanProperty UP;
   public static final EnumProperty<WallShape> EAST_SHAPE;
   public static final EnumProperty<WallShape> NORTH_SHAPE;
   public static final EnumProperty<WallShape> SOUTH_SHAPE;
   public static final EnumProperty<WallShape> WEST_SHAPE;
   public static final BooleanProperty WATERLOGGED;
   private final Map<BlockState, VoxelShape> shapeMap;
   private final Map<BlockState, VoxelShape> collisionShapeMap;
   private static final VoxelShape TALL_POST_SHAPE;
   private static final VoxelShape TALL_NORTH_SHAPE;
   private static final VoxelShape TALL_SOUTH_SHAPE;
   private static final VoxelShape TALL_WEST_SHAPE;
   private static final VoxelShape TALL_EAST_SHAPE;

   public WallBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(UP, true)).with(NORTH_SHAPE, WallShape.NONE)).with(EAST_SHAPE, WallShape.NONE)).with(SOUTH_SHAPE, WallShape.NONE)).with(WEST_SHAPE, WallShape.NONE)).with(WATERLOGGED, false));
      this.shapeMap = this.getShapeMap(4.0F, 3.0F, 16.0F, 0.0F, 14.0F, 16.0F);
      this.collisionShapeMap = this.getShapeMap(4.0F, 3.0F, 24.0F, 0.0F, 24.0F, 24.0F);
   }

   private static VoxelShape method_24426(VoxelShape voxelShape, WallShape wallShape, VoxelShape voxelShape2, VoxelShape voxelShape3) {
      if (wallShape == WallShape.TALL) {
         return VoxelShapes.union(voxelShape, voxelShape3);
      } else {
         return wallShape == WallShape.LOW ? VoxelShapes.union(voxelShape, voxelShape2) : voxelShape;
      }
   }

   private Map<BlockState, VoxelShape> getShapeMap(float f, float g, float h, float i, float j, float k) {
      float l = 8.0F - f;
      float m = 8.0F + f;
      float n = 8.0F - g;
      float o = 8.0F + g;
      VoxelShape voxelShape = Block.createCuboidShape((double)l, 0.0D, (double)l, (double)m, (double)h, (double)m);
      VoxelShape voxelShape2 = Block.createCuboidShape((double)n, (double)i, 0.0D, (double)o, (double)j, (double)o);
      VoxelShape voxelShape3 = Block.createCuboidShape((double)n, (double)i, (double)n, (double)o, (double)j, 16.0D);
      VoxelShape voxelShape4 = Block.createCuboidShape(0.0D, (double)i, (double)n, (double)o, (double)j, (double)o);
      VoxelShape voxelShape5 = Block.createCuboidShape((double)n, (double)i, (double)n, 16.0D, (double)j, (double)o);
      VoxelShape voxelShape6 = Block.createCuboidShape((double)n, (double)i, 0.0D, (double)o, (double)k, (double)o);
      VoxelShape voxelShape7 = Block.createCuboidShape((double)n, (double)i, (double)n, (double)o, (double)k, 16.0D);
      VoxelShape voxelShape8 = Block.createCuboidShape(0.0D, (double)i, (double)n, (double)o, (double)k, (double)o);
      VoxelShape voxelShape9 = Block.createCuboidShape((double)n, (double)i, (double)n, 16.0D, (double)k, (double)o);
      Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();
      Iterator var21 = UP.getValues().iterator();

      while(var21.hasNext()) {
         Boolean boolean_ = (Boolean)var21.next();
         Iterator var23 = EAST_SHAPE.getValues().iterator();

         while(var23.hasNext()) {
            WallShape wallShape = (WallShape)var23.next();
            Iterator var25 = NORTH_SHAPE.getValues().iterator();

            while(var25.hasNext()) {
               WallShape wallShape2 = (WallShape)var25.next();
               Iterator var27 = WEST_SHAPE.getValues().iterator();

               while(var27.hasNext()) {
                  WallShape wallShape3 = (WallShape)var27.next();
                  Iterator var29 = SOUTH_SHAPE.getValues().iterator();

                  while(var29.hasNext()) {
                     WallShape wallShape4 = (WallShape)var29.next();
                     VoxelShape voxelShape10 = VoxelShapes.empty();
                     voxelShape10 = method_24426(voxelShape10, wallShape, voxelShape5, voxelShape9);
                     voxelShape10 = method_24426(voxelShape10, wallShape3, voxelShape4, voxelShape8);
                     voxelShape10 = method_24426(voxelShape10, wallShape2, voxelShape2, voxelShape6);
                     voxelShape10 = method_24426(voxelShape10, wallShape4, voxelShape3, voxelShape7);
                     if (boolean_) {
                        voxelShape10 = VoxelShapes.union(voxelShape10, voxelShape);
                     }

                     BlockState blockState = (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(UP, boolean_)).with(EAST_SHAPE, wallShape)).with(WEST_SHAPE, wallShape3)).with(NORTH_SHAPE, wallShape2)).with(SOUTH_SHAPE, wallShape4);
                     builder.put(blockState.with(WATERLOGGED, false), voxelShape10);
                     builder.put(blockState.with(WATERLOGGED, true), voxelShape10);
                  }
               }
            }
         }
      }

      return builder.build();
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)this.shapeMap.get(state);
   }

   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return (VoxelShape)this.collisionShapeMap.get(state);
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   private boolean shouldConnectTo(BlockState state, boolean faceFullSquare, Direction side) {
      Block block = state.getBlock();
      boolean bl = block instanceof FenceGateBlock && FenceGateBlock.canWallConnect(state, side);
      return state.isIn(BlockTags.WALLS) || !cannotConnect(block) && faceFullSquare || block instanceof PaneBlock || bl;
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      WorldView worldView = ctx.getWorld();
      BlockPos blockPos = ctx.getBlockPos();
      FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
      BlockPos blockPos2 = blockPos.north();
      BlockPos blockPos3 = blockPos.east();
      BlockPos blockPos4 = blockPos.south();
      BlockPos blockPos5 = blockPos.west();
      BlockPos blockPos6 = blockPos.up();
      BlockState blockState = worldView.getBlockState(blockPos2);
      BlockState blockState2 = worldView.getBlockState(blockPos3);
      BlockState blockState3 = worldView.getBlockState(blockPos4);
      BlockState blockState4 = worldView.getBlockState(blockPos5);
      BlockState blockState5 = worldView.getBlockState(blockPos6);
      boolean bl = this.shouldConnectTo(blockState, blockState.isSideSolidFullSquare(worldView, blockPos2, Direction.SOUTH), Direction.SOUTH);
      boolean bl2 = this.shouldConnectTo(blockState2, blockState2.isSideSolidFullSquare(worldView, blockPos3, Direction.WEST), Direction.WEST);
      boolean bl3 = this.shouldConnectTo(blockState3, blockState3.isSideSolidFullSquare(worldView, blockPos4, Direction.NORTH), Direction.NORTH);
      boolean bl4 = this.shouldConnectTo(blockState4, blockState4.isSideSolidFullSquare(worldView, blockPos5, Direction.EAST), Direction.EAST);
      BlockState blockState6 = (BlockState)this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
      return this.method_24422(worldView, blockState6, blockPos6, blockState5, bl, bl2, bl3, bl4);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      if (direction == Direction.DOWN) {
         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      } else {
         return direction == Direction.UP ? this.method_24421(world, state, neighborPos, neighborState) : this.method_24423(world, pos, state, neighborPos, neighborState, direction);
      }
   }

   private static boolean method_24424(BlockState blockState, Property<WallShape> property) {
      return blockState.get(property) != WallShape.NONE;
   }

   private static boolean method_24427(VoxelShape voxelShape, VoxelShape voxelShape2) {
      return !VoxelShapes.matchesAnywhere(voxelShape2, voxelShape, BooleanBiFunction.ONLY_FIRST);
   }

   private BlockState method_24421(WorldView worldView, BlockState blockState, BlockPos blockPos, BlockState blockState2) {
      boolean bl = method_24424(blockState, NORTH_SHAPE);
      boolean bl2 = method_24424(blockState, EAST_SHAPE);
      boolean bl3 = method_24424(blockState, SOUTH_SHAPE);
      boolean bl4 = method_24424(blockState, WEST_SHAPE);
      return this.method_24422(worldView, blockState, blockPos, blockState2, bl, bl2, bl3, bl4);
   }

   private BlockState method_24423(WorldView worldView, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2, Direction direction) {
      Direction direction2 = direction.getOpposite();
      boolean bl = direction == Direction.NORTH ? this.shouldConnectTo(blockState2, blockState2.isSideSolidFullSquare(worldView, blockPos2, direction2), direction2) : method_24424(blockState, NORTH_SHAPE);
      boolean bl2 = direction == Direction.EAST ? this.shouldConnectTo(blockState2, blockState2.isSideSolidFullSquare(worldView, blockPos2, direction2), direction2) : method_24424(blockState, EAST_SHAPE);
      boolean bl3 = direction == Direction.SOUTH ? this.shouldConnectTo(blockState2, blockState2.isSideSolidFullSquare(worldView, blockPos2, direction2), direction2) : method_24424(blockState, SOUTH_SHAPE);
      boolean bl4 = direction == Direction.WEST ? this.shouldConnectTo(blockState2, blockState2.isSideSolidFullSquare(worldView, blockPos2, direction2), direction2) : method_24424(blockState, WEST_SHAPE);
      BlockPos blockPos3 = blockPos.up();
      BlockState blockState3 = worldView.getBlockState(blockPos3);
      return this.method_24422(worldView, blockState, blockPos3, blockState3, bl, bl2, bl3, bl4);
   }

   private BlockState method_24422(WorldView worldView, BlockState blockState, BlockPos blockPos, BlockState blockState2, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
      VoxelShape voxelShape = blockState2.getCollisionShape(worldView, blockPos).getFace(Direction.DOWN);
      BlockState blockState3 = this.method_24425(blockState, bl, bl2, bl3, bl4, voxelShape);
      return (BlockState)blockState3.with(UP, this.method_27092(blockState3, blockState2, voxelShape));
   }

   private boolean method_27092(BlockState blockState, BlockState blockState2, VoxelShape voxelShape) {
      boolean bl = blockState2.getBlock() instanceof WallBlock && (Boolean)blockState2.get(UP);
      if (bl) {
         return true;
      } else {
         WallShape wallShape = (WallShape)blockState.get(NORTH_SHAPE);
         WallShape wallShape2 = (WallShape)blockState.get(SOUTH_SHAPE);
         WallShape wallShape3 = (WallShape)blockState.get(EAST_SHAPE);
         WallShape wallShape4 = (WallShape)blockState.get(WEST_SHAPE);
         boolean bl2 = wallShape2 == WallShape.NONE;
         boolean bl3 = wallShape4 == WallShape.NONE;
         boolean bl4 = wallShape3 == WallShape.NONE;
         boolean bl5 = wallShape == WallShape.NONE;
         boolean bl6 = bl5 && bl2 && bl3 && bl4 || bl5 != bl2 || bl3 != bl4;
         if (bl6) {
            return true;
         } else {
            boolean bl7 = wallShape == WallShape.TALL && wallShape2 == WallShape.TALL || wallShape3 == WallShape.TALL && wallShape4 == WallShape.TALL;
            if (bl7) {
               return false;
            } else {
               return blockState2.getBlock().isIn(BlockTags.WALL_POST_OVERRIDE) || method_24427(voxelShape, TALL_POST_SHAPE);
            }
         }
      }
   }

   private BlockState method_24425(BlockState blockState, boolean bl, boolean bl2, boolean bl3, boolean bl4, VoxelShape voxelShape) {
      return (BlockState)((BlockState)((BlockState)((BlockState)blockState.with(NORTH_SHAPE, this.method_24428(bl, voxelShape, TALL_NORTH_SHAPE))).with(EAST_SHAPE, this.method_24428(bl2, voxelShape, TALL_EAST_SHAPE))).with(SOUTH_SHAPE, this.method_24428(bl3, voxelShape, TALL_SOUTH_SHAPE))).with(WEST_SHAPE, this.method_24428(bl4, voxelShape, TALL_WEST_SHAPE));
   }

   private WallShape method_24428(boolean bl, VoxelShape voxelShape, VoxelShape voxelShape2) {
      if (bl) {
         return method_24427(voxelShape, voxelShape2) ? WallShape.TALL : WallShape.LOW;
      } else {
         return WallShape.NONE;
      }
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
      return !(Boolean)state.get(WATERLOGGED);
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(UP, NORTH_SHAPE, EAST_SHAPE, WEST_SHAPE, SOUTH_SHAPE, WATERLOGGED);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      switch(rotation) {
      case CLOCKWISE_180:
         return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_SHAPE, state.get(SOUTH_SHAPE))).with(EAST_SHAPE, state.get(WEST_SHAPE))).with(SOUTH_SHAPE, state.get(NORTH_SHAPE))).with(WEST_SHAPE, state.get(EAST_SHAPE));
      case COUNTERCLOCKWISE_90:
         return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_SHAPE, state.get(EAST_SHAPE))).with(EAST_SHAPE, state.get(SOUTH_SHAPE))).with(SOUTH_SHAPE, state.get(WEST_SHAPE))).with(WEST_SHAPE, state.get(NORTH_SHAPE));
      case CLOCKWISE_90:
         return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_SHAPE, state.get(WEST_SHAPE))).with(EAST_SHAPE, state.get(NORTH_SHAPE))).with(SOUTH_SHAPE, state.get(EAST_SHAPE))).with(WEST_SHAPE, state.get(SOUTH_SHAPE));
      default:
         return state;
      }
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      switch(mirror) {
      case LEFT_RIGHT:
         return (BlockState)((BlockState)state.with(NORTH_SHAPE, state.get(SOUTH_SHAPE))).with(SOUTH_SHAPE, state.get(NORTH_SHAPE));
      case FRONT_BACK:
         return (BlockState)((BlockState)state.with(EAST_SHAPE, state.get(WEST_SHAPE))).with(WEST_SHAPE, state.get(EAST_SHAPE));
      default:
         return super.mirror(state, mirror);
      }
   }

   static {
      UP = Properties.UP;
      EAST_SHAPE = Properties.EAST_WALL_SHAPE;
      NORTH_SHAPE = Properties.NORTH_WALL_SHAPE;
      SOUTH_SHAPE = Properties.SOUTH_WALL_SHAPE;
      WEST_SHAPE = Properties.WEST_WALL_SHAPE;
      WATERLOGGED = Properties.WATERLOGGED;
      TALL_POST_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);
      TALL_NORTH_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 9.0D);
      TALL_SOUTH_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 16.0D);
      TALL_WEST_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);
      TALL_EAST_SHAPE = Block.createCuboidShape(7.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);
   }
}
