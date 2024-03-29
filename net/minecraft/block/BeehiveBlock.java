package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class BeehiveBlock extends BlockWithEntity {
   private static final Direction[] GENERATE_DIRECTIONS;
   public static final DirectionProperty FACING;
   public static final IntProperty HONEY_LEVEL;

   public BeehiveBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HONEY_LEVEL, 0)).with(FACING, Direction.NORTH));
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return (Integer)state.get(HONEY_LEVEL);
   }

   public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
      super.afterBreak(world, player, pos, state, blockEntity, stack);
      if (!world.isClient && blockEntity instanceof BeehiveBlockEntity) {
         BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
         if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) == 0) {
            beehiveBlockEntity.angerBees(player, state, BeehiveBlockEntity.BeeState.EMERGENCY);
            world.updateComparators(pos, this);
            this.angerNearbyBees(world, pos);
         }

         Criteria.BEE_NEST_DESTROYED.test((ServerPlayerEntity)player, state.getBlock(), stack, beehiveBlockEntity.getBeeCount());
      }

   }

   private void angerNearbyBees(World world, BlockPos pos) {
      List<BeeEntity> list = world.getNonSpectatingEntities(BeeEntity.class, (new Box(pos)).expand(8.0D, 6.0D, 8.0D));
      if (!list.isEmpty()) {
         List<PlayerEntity> list2 = world.getNonSpectatingEntities(PlayerEntity.class, (new Box(pos)).expand(8.0D, 6.0D, 8.0D));
         int i = list2.size();
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            BeeEntity beeEntity = (BeeEntity)var6.next();
            if (beeEntity.getTarget() == null) {
               beeEntity.setTarget((LivingEntity)list2.get(world.random.nextInt(i)));
            }
         }
      }

   }

   public static void dropHoneycomb(World world, BlockPos pos) {
      dropStack(world, pos, new ItemStack(Items.HONEYCOMB, 3));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack itemStack = player.getStackInHand(hand);
      int i = (Integer)state.get(HONEY_LEVEL);
      boolean bl = false;
      if (i >= 5) {
         if (itemStack.getItem() == Items.SHEARS) {
            world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            dropHoneycomb(world, pos);
            itemStack.damage(1, (LivingEntity)player, (Consumer)((playerx) -> {
               playerx.sendToolBreakStatus(hand);
            }));
            bl = true;
         } else if (itemStack.getItem() == Items.GLASS_BOTTLE) {
            itemStack.decrement(1);
            world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            if (itemStack.isEmpty()) {
               player.setStackInHand(hand, new ItemStack(Items.HONEY_BOTTLE));
            } else if (!player.inventory.insertStack(new ItemStack(Items.HONEY_BOTTLE))) {
               player.dropItem(new ItemStack(Items.HONEY_BOTTLE), false);
            }

            bl = true;
         }
      }

      if (bl) {
         if (!CampfireBlock.isLitCampfireInRange(world, pos)) {
            if (this.hasBees(world, pos)) {
               this.angerNearbyBees(world, pos);
            }

            this.takeHoney(world, state, pos, player, BeehiveBlockEntity.BeeState.EMERGENCY);
         } else {
            this.takeHoney(world, state, pos);
         }

         return ActionResult.success(world.isClient);
      } else {
         return super.onUse(state, world, pos, player, hand, hit);
      }
   }

   private boolean hasBees(World world, BlockPos pos) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof BeehiveBlockEntity) {
         BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
         return !beehiveBlockEntity.hasNoBees();
      } else {
         return false;
      }
   }

   public void takeHoney(World world, BlockState state, BlockPos pos, @Nullable PlayerEntity player, BeehiveBlockEntity.BeeState beeState) {
      this.takeHoney(world, state, pos);
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof BeehiveBlockEntity) {
         BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
         beehiveBlockEntity.angerBees(player, state, beeState);
      }

   }

   public void takeHoney(World world, BlockState state, BlockPos pos) {
      world.setBlockState(pos, (BlockState)state.with(HONEY_LEVEL, 0), 3);
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Integer)state.get(HONEY_LEVEL) >= 5) {
         for(int i = 0; i < random.nextInt(1) + 1; ++i) {
            this.spawnHoneyParticles(world, pos, state);
         }
      }

   }

   @Environment(EnvType.CLIENT)
   private void spawnHoneyParticles(World world, BlockPos pos, BlockState state) {
      if (state.getFluidState().isEmpty() && !(world.random.nextFloat() < 0.3F)) {
         VoxelShape voxelShape = state.getCollisionShape(world, pos);
         double d = voxelShape.getMax(Direction.Axis.Y);
         if (d >= 1.0D && !state.isIn(BlockTags.IMPERMEABLE)) {
            double e = voxelShape.getMin(Direction.Axis.Y);
            if (e > 0.0D) {
               this.addHoneyParticle(world, pos, voxelShape, (double)pos.getY() + e - 0.05D);
            } else {
               BlockPos blockPos = pos.down();
               BlockState blockState = world.getBlockState(blockPos);
               VoxelShape voxelShape2 = blockState.getCollisionShape(world, blockPos);
               double f = voxelShape2.getMax(Direction.Axis.Y);
               if ((f < 1.0D || !blockState.isFullCube(world, blockPos)) && blockState.getFluidState().isEmpty()) {
                  this.addHoneyParticle(world, pos, voxelShape, (double)pos.getY() - 0.05D);
               }
            }
         }

      }
   }

   @Environment(EnvType.CLIENT)
   private void addHoneyParticle(World world, BlockPos pos, VoxelShape shape, double height) {
      this.addHoneyParticle(world, (double)pos.getX() + shape.getMin(Direction.Axis.X), (double)pos.getX() + shape.getMax(Direction.Axis.X), (double)pos.getZ() + shape.getMin(Direction.Axis.Z), (double)pos.getZ() + shape.getMax(Direction.Axis.Z), height);
   }

   @Environment(EnvType.CLIENT)
   private void addHoneyParticle(World world, double minX, double maxX, double minZ, double maxZ, double height) {
      world.addParticle(ParticleTypes.DRIPPING_HONEY, MathHelper.lerp(world.random.nextDouble(), minX, maxX), height, MathHelper.lerp(world.random.nextDouble(), minZ, maxZ), 0.0D, 0.0D, 0.0D);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(HONEY_LEVEL, FACING);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockView world) {
      return new BeehiveBlockEntity();
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient && player.isCreative() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
            ItemStack itemStack = new ItemStack(this);
            int i = (Integer)state.get(HONEY_LEVEL);
            boolean bl = !beehiveBlockEntity.hasNoBees();
            if (!bl && i == 0) {
               return;
            }

            NbtCompound nbtCompound2;
            if (bl) {
               nbtCompound2 = new NbtCompound();
               nbtCompound2.put("Bees", beehiveBlockEntity.getBees());
               itemStack.putSubTag("BlockEntityTag", nbtCompound2);
            }

            nbtCompound2 = new NbtCompound();
            nbtCompound2.putInt("honey_level", i);
            itemStack.putSubTag("BlockStateTag", nbtCompound2);
            ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), itemStack);
            itemEntity.setToDefaultPickupDelay();
            world.spawnEntity(itemEntity);
         }
      }

      super.onBreak(world, pos, state, player);
   }

   public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
      Entity entity = (Entity)builder.getNullable(LootContextParameters.THIS_ENTITY);
      if (entity instanceof TntEntity || entity instanceof CreeperEntity || entity instanceof WitherSkullEntity || entity instanceof WitherEntity || entity instanceof TntMinecartEntity) {
         BlockEntity blockEntity = (BlockEntity)builder.getNullable(LootContextParameters.BLOCK_ENTITY);
         if (blockEntity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
            beehiveBlockEntity.angerBees((PlayerEntity)null, state, BeehiveBlockEntity.BeeState.EMERGENCY);
         }
      }

      return super.getDroppedStacks(state, builder);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (world.getBlockState(neighborPos).getBlock() instanceof FireBlock) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
            beehiveBlockEntity.angerBees((PlayerEntity)null, state, BeehiveBlockEntity.BeeState.EMERGENCY);
         }
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public static Direction getRandomGenerationDirection(Random random) {
      return (Direction)Util.getRandom((Object[])GENERATE_DIRECTIONS, random);
   }

   static {
      GENERATE_DIRECTIONS = new Direction[]{Direction.WEST, Direction.EAST, Direction.SOUTH};
      FACING = HorizontalFacingBlock.FACING;
      HONEY_LEVEL = Properties.HONEY_LEVEL;
   }
}
