package net.minecraft.block;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

public class BedBlock extends HorizontalFacingBlock implements BlockEntityProvider {
   public static final EnumProperty<BedPart> PART;
   public static final BooleanProperty OCCUPIED;
   protected static final VoxelShape TOP_SHAPE;
   protected static final VoxelShape LEG_1_SHAPE;
   protected static final VoxelShape LEG_2_SHAPE;
   protected static final VoxelShape LEG_3_SHAPE;
   protected static final VoxelShape LEG_4_SHAPE;
   protected static final VoxelShape NORTH_SHAPE;
   protected static final VoxelShape SOUTH_SHAPE;
   protected static final VoxelShape WEST_SHAPE;
   protected static final VoxelShape EAST_SHAPE;
   private final DyeColor color;

   public BedBlock(DyeColor color, AbstractBlock.Settings settings) {
      super(settings);
      this.color = color;
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(PART, BedPart.FOOT)).with(OCCUPIED, false));
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public static Direction getDirection(BlockView world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos);
      return blockState.getBlock() instanceof BedBlock ? (Direction)blockState.get(FACING) : null;
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         return ActionResult.CONSUME;
      } else {
         if (state.get(PART) != BedPart.HEAD) {
            pos = pos.offset((Direction)state.get(FACING));
            state = world.getBlockState(pos);
            if (!state.isOf(this)) {
               return ActionResult.CONSUME;
            }
         }

         if (!isOverworld(world)) {
            world.removeBlock(pos, false);
            BlockPos blockPos = pos.offset(((Direction)state.get(FACING)).getOpposite());
            if (world.getBlockState(blockPos).isOf(this)) {
               world.removeBlock(blockPos, false);
            }

            world.createExplosion((Entity)null, DamageSource.badRespawnPoint(), (ExplosionBehavior)null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, 5.0F, true, Explosion.DestructionType.DESTROY);
            return ActionResult.SUCCESS;
         } else if ((Boolean)state.get(OCCUPIED)) {
            if (!this.isFree(world, pos)) {
               player.sendMessage(new TranslatableText("block.minecraft.bed.occupied"), true);
            }

            return ActionResult.SUCCESS;
         } else {
            player.trySleep(pos).ifLeft((sleepFailureReason) -> {
               if (sleepFailureReason != null) {
                  player.sendMessage(sleepFailureReason.toText(), true);
               }

            });
            return ActionResult.SUCCESS;
         }
      }
   }

   public static boolean isOverworld(World world) {
      return world.getDimension().isBedWorking();
   }

   private boolean isFree(World world, BlockPos pos) {
      List<VillagerEntity> list = world.getEntitiesByClass(VillagerEntity.class, new Box(pos), LivingEntity::isSleeping);
      if (list.isEmpty()) {
         return false;
      } else {
         ((VillagerEntity)list.get(0)).wakeUp();
         return true;
      }
   }

   public void onLandedUpon(World world, BlockPos pos, Entity entity, float distance) {
      super.onLandedUpon(world, pos, entity, distance * 0.5F);
   }

   public void onEntityLand(BlockView world, Entity entity) {
      if (entity.bypassesLandingEffects()) {
         super.onEntityLand(world, entity);
      } else {
         this.bounceEntity(entity);
      }

   }

   private void bounceEntity(Entity entity) {
      Vec3d vec3d = entity.getVelocity();
      if (vec3d.y < 0.0D) {
         double d = entity instanceof LivingEntity ? 1.0D : 0.8D;
         entity.setVelocity(vec3d.x, -vec3d.y * 0.6600000262260437D * d, vec3d.z);
      }

   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (direction == getDirectionTowardsOtherPart((BedPart)state.get(PART), (Direction)state.get(FACING))) {
         return neighborState.isOf(this) && neighborState.get(PART) != state.get(PART) ? (BlockState)state.with(OCCUPIED, neighborState.get(OCCUPIED)) : Blocks.AIR.getDefaultState();
      } else {
         return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
      }
   }

   private static Direction getDirectionTowardsOtherPart(BedPart part, Direction direction) {
      return part == BedPart.FOOT ? direction : direction.getOpposite();
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient && player.isCreative()) {
         BedPart bedPart = (BedPart)state.get(PART);
         if (bedPart == BedPart.FOOT) {
            BlockPos blockPos = pos.offset(getDirectionTowardsOtherPart(bedPart, (Direction)state.get(FACING)));
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.getBlock() == this && blockState.get(PART) == BedPart.HEAD) {
               world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 35);
               world.syncWorldEvent(player, 2001, blockPos, Block.getRawIdFromState(blockState));
            }
         }
      }

      super.onBreak(world, pos, state, player);
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction direction = ctx.getPlayerFacing();
      BlockPos blockPos = ctx.getBlockPos();
      BlockPos blockPos2 = blockPos.offset(direction);
      return ctx.getWorld().getBlockState(blockPos2).canReplace(ctx) ? (BlockState)this.getDefaultState().with(FACING, direction) : null;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      Direction direction = getOppositePartDirection(state).getOpposite();
      switch(direction) {
      case NORTH:
         return NORTH_SHAPE;
      case SOUTH:
         return SOUTH_SHAPE;
      case WEST:
         return WEST_SHAPE;
      default:
         return EAST_SHAPE;
      }
   }

   public static Direction getOppositePartDirection(BlockState state) {
      Direction direction = (Direction)state.get(FACING);
      return state.get(PART) == BedPart.HEAD ? direction.getOpposite() : direction;
   }

   @Environment(EnvType.CLIENT)
   public static DoubleBlockProperties.Type getBedPart(BlockState state) {
      BedPart bedPart = (BedPart)state.get(PART);
      return bedPart == BedPart.HEAD ? DoubleBlockProperties.Type.FIRST : DoubleBlockProperties.Type.SECOND;
   }

   private static boolean method_30839(BlockView blockView, BlockPos blockPos) {
      return blockView.getBlockState(blockPos.down()).getBlock() instanceof BedBlock;
   }

   public static Optional<Vec3d> findWakeUpPosition(EntityType<?> type, CollisionView world, BlockPos pos, float f) {
      Direction direction = (Direction)world.getBlockState(pos).get(FACING);
      Direction direction2 = direction.rotateYClockwise();
      Direction direction3 = direction2.method_30928(f) ? direction2.getOpposite() : direction2;
      if (method_30839(world, pos)) {
         return method_30835(type, world, pos, direction, direction3);
      } else {
         int[][] is = method_30838(direction, direction3);
         Optional<Vec3d> optional = method_30836(type, world, pos, is, true);
         return optional.isPresent() ? optional : method_30836(type, world, pos, is, false);
      }
   }

   private static Optional<Vec3d> method_30835(EntityType<?> entityType, CollisionView collisionView, BlockPos blockPos, Direction direction, Direction direction2) {
      int[][] is = method_30840(direction, direction2);
      Optional<Vec3d> optional = method_30836(entityType, collisionView, blockPos, is, true);
      if (optional.isPresent()) {
         return optional;
      } else {
         BlockPos blockPos2 = blockPos.down();
         Optional<Vec3d> optional2 = method_30836(entityType, collisionView, blockPos2, is, true);
         if (optional2.isPresent()) {
            return optional2;
         } else {
            int[][] js = method_30837(direction);
            Optional<Vec3d> optional3 = method_30836(entityType, collisionView, blockPos, js, true);
            if (optional3.isPresent()) {
               return optional3;
            } else {
               Optional<Vec3d> optional4 = method_30836(entityType, collisionView, blockPos, is, false);
               if (optional4.isPresent()) {
                  return optional4;
               } else {
                  Optional<Vec3d> optional5 = method_30836(entityType, collisionView, blockPos2, is, false);
                  return optional5.isPresent() ? optional5 : method_30836(entityType, collisionView, blockPos, js, false);
               }
            }
         }
      }
   }

   private static Optional<Vec3d> method_30836(EntityType<?> entityType, CollisionView collisionView, BlockPos blockPos, int[][] is, boolean bl) {
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      int[][] var6 = is;
      int var7 = is.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         int[] js = var6[var8];
         mutable.set(blockPos.getX() + js[0], blockPos.getY(), blockPos.getZ() + js[1]);
         Vec3d vec3d = Dismounting.method_30769(entityType, collisionView, mutable, bl);
         if (vec3d != null) {
            return Optional.of(vec3d);
         }
      }

      return Optional.empty();
   }

   public PistonBehavior getPistonBehavior(BlockState state) {
      return PistonBehavior.DESTROY;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(FACING, PART, OCCUPIED);
   }

   public BlockEntity createBlockEntity(BlockView world) {
      return new BedBlockEntity(this.color);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      super.onPlaced(world, pos, state, placer, itemStack);
      if (!world.isClient) {
         BlockPos blockPos = pos.offset((Direction)state.get(FACING));
         world.setBlockState(blockPos, (BlockState)state.with(PART, BedPart.HEAD), 3);
         world.updateNeighbors(pos, Blocks.AIR);
         state.updateNeighbors(world, pos, 3);
      }

   }

   @Environment(EnvType.CLIENT)
   public DyeColor getColor() {
      return this.color;
   }

   @Environment(EnvType.CLIENT)
   public long getRenderingSeed(BlockState state, BlockPos pos) {
      BlockPos blockPos = pos.offset((Direction)state.get(FACING), state.get(PART) == BedPart.HEAD ? 0 : 1);
      return MathHelper.hashCode(blockPos.getX(), pos.getY(), blockPos.getZ());
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   private static int[][] method_30838(Direction direction, Direction direction2) {
      return (int[][])ArrayUtils.addAll((Object[])method_30840(direction, direction2), (Object[])method_30837(direction));
   }

   private static int[][] method_30840(Direction direction, Direction direction2) {
      return new int[][]{{direction2.getOffsetX(), direction2.getOffsetZ()}, {direction2.getOffsetX() - direction.getOffsetX(), direction2.getOffsetZ() - direction.getOffsetZ()}, {direction2.getOffsetX() - direction.getOffsetX() * 2, direction2.getOffsetZ() - direction.getOffsetZ() * 2}, {-direction.getOffsetX() * 2, -direction.getOffsetZ() * 2}, {-direction2.getOffsetX() - direction.getOffsetX() * 2, -direction2.getOffsetZ() - direction.getOffsetZ() * 2}, {-direction2.getOffsetX() - direction.getOffsetX(), -direction2.getOffsetZ() - direction.getOffsetZ()}, {-direction2.getOffsetX(), -direction2.getOffsetZ()}, {-direction2.getOffsetX() + direction.getOffsetX(), -direction2.getOffsetZ() + direction.getOffsetZ()}, {direction.getOffsetX(), direction.getOffsetZ()}, {direction2.getOffsetX() + direction.getOffsetX(), direction2.getOffsetZ() + direction.getOffsetZ()}};
   }

   private static int[][] method_30837(Direction direction) {
      return new int[][]{{0, 0}, {-direction.getOffsetX(), -direction.getOffsetZ()}};
   }

   static {
      PART = Properties.BED_PART;
      OCCUPIED = Properties.OCCUPIED;
      TOP_SHAPE = Block.createCuboidShape(0.0D, 3.0D, 0.0D, 16.0D, 9.0D, 16.0D);
      LEG_1_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 3.0D, 3.0D, 3.0D);
      LEG_2_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 13.0D, 3.0D, 3.0D, 16.0D);
      LEG_3_SHAPE = Block.createCuboidShape(13.0D, 0.0D, 0.0D, 16.0D, 3.0D, 3.0D);
      LEG_4_SHAPE = Block.createCuboidShape(13.0D, 0.0D, 13.0D, 16.0D, 3.0D, 16.0D);
      NORTH_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_1_SHAPE, LEG_3_SHAPE);
      SOUTH_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_2_SHAPE, LEG_4_SHAPE);
      WEST_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_1_SHAPE, LEG_2_SHAPE);
      EAST_SHAPE = VoxelShapes.union(TOP_SHAPE, LEG_3_SHAPE, LEG_4_SHAPE);
   }
}
