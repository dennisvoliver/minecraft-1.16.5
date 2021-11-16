package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class AbstractPressurePlateBlock extends Block {
   protected static final VoxelShape PRESSED_SHAPE = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 0.5D, 15.0D);
   protected static final VoxelShape DEFAULT_SHAPE = Block.createCuboidShape(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D);
   protected static final Box BOX = new Box(0.125D, 0.0D, 0.125D, 0.875D, 0.25D, 0.875D);

   protected AbstractPressurePlateBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.getRedstoneOutput(state) > 0 ? PRESSED_SHAPE : DEFAULT_SHAPE;
   }

   protected int getTickRate() {
      return 20;
   }

   public boolean canMobSpawnInside() {
      return true;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      return direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockPos blockPos = pos.down();
      return hasTopRim(world, blockPos) || sideCoversSmallSquare(world, blockPos, Direction.UP);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      int i = this.getRedstoneOutput(state);
      if (i > 0) {
         this.updatePlateState(world, pos, state, i);
      }

   }

   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      if (!world.isClient) {
         int i = this.getRedstoneOutput(state);
         if (i == 0) {
            this.updatePlateState(world, pos, state, i);
         }

      }
   }

   protected void updatePlateState(World world, BlockPos pos, BlockState state, int rsOut) {
      int i = this.getRedstoneOutput(world, pos);
      boolean bl = rsOut > 0;
      boolean bl2 = i > 0;
      if (rsOut != i) {
         BlockState blockState = this.setRedstoneOutput(state, i);
         world.setBlockState(pos, blockState, 2);
         this.updateNeighbors(world, pos);
         world.scheduleBlockRerenderIfNeeded(pos, state, blockState);
      }

      if (!bl2 && bl) {
         this.playDepressSound(world, pos);
      } else if (bl2 && !bl) {
         this.playPressSound(world, pos);
      }

      if (bl2) {
         world.getBlockTickScheduler().schedule(new BlockPos(pos), this, this.getTickRate());
      }

   }

   protected abstract void playPressSound(WorldAccess world, BlockPos pos);

   protected abstract void playDepressSound(WorldAccess world, BlockPos pos);

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!moved && !state.isOf(newState.getBlock())) {
         if (this.getRedstoneOutput(state) > 0) {
            this.updateNeighbors(world, pos);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   protected void updateNeighbors(World world, BlockPos pos) {
      world.updateNeighborsAlways(pos, this);
      world.updateNeighborsAlways(pos.down(), this);
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return this.getRedstoneOutput(state);
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return direction == Direction.UP ? this.getRedstoneOutput(state) : 0;
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public PistonBehavior getPistonBehavior(BlockState state) {
      return PistonBehavior.DESTROY;
   }

   protected abstract int getRedstoneOutput(World world, BlockPos pos);

   protected abstract int getRedstoneOutput(BlockState state);

   protected abstract BlockState setRedstoneOutput(BlockState state, int rsOut);
}
