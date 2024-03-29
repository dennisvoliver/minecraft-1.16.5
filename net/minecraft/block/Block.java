package net.minecraft.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.explosion.Explosion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class Block extends AbstractBlock implements ItemConvertible {
   protected static final Logger LOGGER = LogManager.getLogger();
   public static final IdList<BlockState> STATE_IDS = new IdList();
   private static final LoadingCache<VoxelShape, Boolean> FULL_CUBE_SHAPE_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build(new CacheLoader<VoxelShape, Boolean>() {
      public Boolean load(VoxelShape voxelShape) {
         return !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), voxelShape, BooleanBiFunction.NOT_SAME);
      }
   });
   protected final StateManager<Block, BlockState> stateManager;
   private BlockState defaultState;
   @Nullable
   private String translationKey;
   @Nullable
   private Item cachedItem;
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.NeighborGroup>> FACE_CULL_MAP = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap<Block.NeighborGroup> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<Block.NeighborGroup>(2048, 0.25F) {
         protected void rehash(int i) {
         }
      };
      object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
      return object2ByteLinkedOpenHashMap;
   });

   public static int getRawIdFromState(@Nullable BlockState state) {
      if (state == null) {
         return 0;
      } else {
         int i = STATE_IDS.getRawId(state);
         return i == -1 ? 0 : i;
      }
   }

   public static BlockState getStateFromRawId(int stateId) {
      BlockState blockState = (BlockState)STATE_IDS.get(stateId);
      return blockState == null ? Blocks.AIR.getDefaultState() : blockState;
   }

   public static Block getBlockFromItem(@Nullable Item item) {
      return item instanceof BlockItem ? ((BlockItem)item).getBlock() : Blocks.AIR;
   }

   public static BlockState pushEntitiesUpBeforeBlockChange(BlockState from, BlockState to, World world, BlockPos pos) {
      VoxelShape voxelShape = VoxelShapes.combine(from.getCollisionShape(world, pos), to.getCollisionShape(world, pos), BooleanBiFunction.ONLY_SECOND).offset((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
      List<Entity> list = world.getOtherEntities((Entity)null, voxelShape.getBoundingBox());
      Iterator var6 = list.iterator();

      while(var6.hasNext()) {
         Entity entity = (Entity)var6.next();
         double d = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, entity.getBoundingBox().offset(0.0D, 1.0D, 0.0D), Stream.of(voxelShape), -1.0D);
         entity.requestTeleport(entity.getX(), entity.getY() + 1.0D + d, entity.getZ());
      }

      return to;
   }

   public static VoxelShape createCuboidShape(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      return VoxelShapes.cuboid(minX / 16.0D, minY / 16.0D, minZ / 16.0D, maxX / 16.0D, maxY / 16.0D, maxZ / 16.0D);
   }

   public boolean isIn(Tag<Block> tag) {
      return tag.contains(this);
   }

   public boolean is(Block block) {
      return this == block;
   }

   public static BlockState postProcessState(BlockState state, WorldAccess world, BlockPos pos) {
      BlockState blockState = state;
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      Direction[] var5 = DIRECTIONS;
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction direction = var5[var7];
         mutable.set(pos, direction);
         blockState = blockState.getStateForNeighborUpdate(direction, world.getBlockState(mutable), world, pos, mutable);
      }

      return blockState;
   }

   /**
    * Replaces the {@code state} with the {@code newState} at the {@code pos}.
    * 
    * <p>If the two state objects are identical, this method does nothing.
    * 
    * <p>If the new state {@linkplain BlockState#isAir() is air},
    * breaks the block at the position instead.
    * 
    * @param state the existing block state
    * @param newState the new block state
    * @param world the world
    * @param pos the position of the replaced block state
    * @param flags the bitwise flags for {@link net.minecraft.world.ModifiableWorld#setBlockState(BlockPos, BlockState, int, int)}
    */
   public static void replace(BlockState state, BlockState newState, WorldAccess world, BlockPos pos, int flags) {
      replace(state, newState, world, pos, flags, 512);
   }

   /**
    * Replaces the {@code state} with the {@code newState} at the {@code pos}.
    * 
    * <p>If the two state objects are identical, this method does nothing.
    * 
    * <p>If the new state {@linkplain BlockState#isAir() is air},
    * breaks the block at the position instead.
    * 
    * @param state the existing block state
    * @param newState the new block state
    * @param world the world
    * @param pos the position of the replaced block state
    * @param flags the bitwise flags for {@link net.minecraft.world.ModifiableWorld#setBlockState(BlockPos, BlockState, int, int)}
    * @param maxUpdateDepth the limit for the cascading block updates
    */
   public static void replace(BlockState state, BlockState newState, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
      if (newState != state) {
         if (newState.isAir()) {
            if (!world.isClient()) {
               world.breakBlock(pos, (flags & 32) == 0, (Entity)null, maxUpdateDepth);
            }
         } else {
            world.setBlockState(pos, newState, flags & -33, maxUpdateDepth);
         }
      }

   }

   public Block(AbstractBlock.Settings settings) {
      super(settings);
      StateManager.Builder<Block, BlockState> builder = new StateManager.Builder(this);
      this.appendProperties(builder);
      this.stateManager = builder.build(Block::getDefaultState, BlockState::new);
      this.setDefaultState((BlockState)this.stateManager.getDefaultState());
   }

   public static boolean cannotConnect(Block block) {
      return block instanceof LeavesBlock || block == Blocks.BARRIER || block == Blocks.CARVED_PUMPKIN || block == Blocks.JACK_O_LANTERN || block == Blocks.MELON || block == Blocks.PUMPKIN || block.isIn(BlockTags.SHULKER_BOXES);
   }

   public boolean hasRandomTicks(BlockState state) {
      return this.randomTicks;
   }

   @Environment(EnvType.CLIENT)
   public static boolean shouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction facing) {
      BlockPos blockPos = pos.offset(facing);
      BlockState blockState = world.getBlockState(blockPos);
      if (state.isSideInvisible(blockState, facing)) {
         return false;
      } else if (blockState.isOpaque()) {
         Block.NeighborGroup neighborGroup = new Block.NeighborGroup(state, blockState, facing);
         Object2ByteLinkedOpenHashMap<Block.NeighborGroup> object2ByteLinkedOpenHashMap = (Object2ByteLinkedOpenHashMap)FACE_CULL_MAP.get();
         byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst(neighborGroup);
         if (b != 127) {
            return b != 0;
         } else {
            VoxelShape voxelShape = state.getCullingFace(world, pos, facing);
            VoxelShape voxelShape2 = blockState.getCullingFace(world, blockPos, facing.getOpposite());
            boolean bl = VoxelShapes.matchesAnywhere(voxelShape, voxelShape2, BooleanBiFunction.ONLY_FIRST);
            if (object2ByteLinkedOpenHashMap.size() == 2048) {
               object2ByteLinkedOpenHashMap.removeLastByte();
            }

            object2ByteLinkedOpenHashMap.putAndMoveToFirst(neighborGroup, (byte)(bl ? 1 : 0));
            return bl;
         }
      } else {
         return true;
      }
   }

   public static boolean hasTopRim(BlockView world, BlockPos pos) {
      return world.getBlockState(pos).isSideSolid(world, pos, Direction.UP, SideShapeType.RIGID);
   }

   public static boolean sideCoversSmallSquare(WorldView world, BlockPos pos, Direction side) {
      BlockState blockState = world.getBlockState(pos);
      return side == Direction.DOWN && blockState.isIn(BlockTags.UNSTABLE_BOTTOM_CENTER) ? false : blockState.isSideSolid(world, pos, side, SideShapeType.CENTER);
   }

   public static boolean isFaceFullSquare(VoxelShape shape, Direction side) {
      VoxelShape voxelShape = shape.getFace(side);
      return isShapeFullCube(voxelShape);
   }

   public static boolean isShapeFullCube(VoxelShape shape) {
      return (Boolean)FULL_CUBE_SHAPE_CACHE.getUnchecked(shape);
   }

   public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
      return !isShapeFullCube(state.getOutlineShape(world, pos)) && state.getFluidState().isEmpty();
   }

   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
   }

   public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
   }

   public static List<ItemStack> getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity) {
      LootContext.Builder builder = (new LootContext.Builder(world)).random(world.random).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).parameter(LootContextParameters.TOOL, ItemStack.EMPTY).optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity);
      return state.getDroppedStacks(builder);
   }

   public static List<ItemStack> getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack) {
      LootContext.Builder builder = (new LootContext.Builder(world)).random(world.random).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos)).parameter(LootContextParameters.TOOL, stack).optionalParameter(LootContextParameters.THIS_ENTITY, entity).optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity);
      return state.getDroppedStacks(builder);
   }

   public static void dropStacks(BlockState state, World world, BlockPos pos) {
      if (world instanceof ServerWorld) {
         getDroppedStacks(state, (ServerWorld)world, pos, (BlockEntity)null).forEach((stack) -> {
            dropStack(world, pos, stack);
         });
         state.onStacksDropped((ServerWorld)world, pos, ItemStack.EMPTY);
      }

   }

   public static void dropStacks(BlockState state, WorldAccess world, BlockPos pos, @Nullable BlockEntity blockEntity) {
      if (world instanceof ServerWorld) {
         getDroppedStacks(state, (ServerWorld)world, pos, blockEntity).forEach((stack) -> {
            dropStack((ServerWorld)world, pos, stack);
         });
         state.onStacksDropped((ServerWorld)world, pos, ItemStack.EMPTY);
      }

   }

   public static void dropStacks(BlockState state, World world, BlockPos pos, @Nullable BlockEntity blockEntity, Entity entity, ItemStack stack) {
      if (world instanceof ServerWorld) {
         getDroppedStacks(state, (ServerWorld)world, pos, blockEntity, entity, stack).forEach((itemStack) -> {
            dropStack(world, pos, itemStack);
         });
         state.onStacksDropped((ServerWorld)world, pos, stack);
      }

   }

   public static void dropStack(World world, BlockPos pos, ItemStack stack) {
      if (!world.isClient && !stack.isEmpty() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
         float f = 0.5F;
         double d = (double)(world.random.nextFloat() * 0.5F) + 0.25D;
         double e = (double)(world.random.nextFloat() * 0.5F) + 0.25D;
         double g = (double)(world.random.nextFloat() * 0.5F) + 0.25D;
         ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + d, (double)pos.getY() + e, (double)pos.getZ() + g, stack);
         itemEntity.setToDefaultPickupDelay();
         world.spawnEntity(itemEntity);
      }
   }

   protected void dropExperience(ServerWorld world, BlockPos pos, int size) {
      if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
         while(size > 0) {
            int i = ExperienceOrbEntity.roundToOrbSize(size);
            size -= i;
            world.spawnEntity(new ExperienceOrbEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, i));
         }
      }

   }

   public float getBlastResistance() {
      return this.resistance;
   }

   /**
    * Called when this block is destroyed by an explosion.
    */
   public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
   }

   /**
    * Called when an entity steps on this block.
    */
   public void onSteppedOn(World world, BlockPos pos, Entity entity) {
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return this.getDefaultState();
   }

   public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
      player.incrementStat(Stats.MINED.getOrCreateStat(this));
      player.addExhaustion(0.005F);
      dropStacks(state, world, pos, blockEntity, player, stack);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
   }

   public boolean canMobSpawnInside() {
      return !this.material.isSolid() && !this.material.isLiquid();
   }

   @Environment(EnvType.CLIENT)
   public MutableText getName() {
      return new TranslatableText(this.getTranslationKey());
   }

   public String getTranslationKey() {
      if (this.translationKey == null) {
         this.translationKey = Util.createTranslationKey("block", Registry.BLOCK.getId(this));
      }

      return this.translationKey;
   }

   public void onLandedUpon(World world, BlockPos pos, Entity entity, float distance) {
      entity.handleFallDamage(distance, 1.0F);
   }

   public void onEntityLand(BlockView world, Entity entity) {
      entity.setVelocity(entity.getVelocity().multiply(1.0D, 0.0D, 1.0D));
   }

   @Environment(EnvType.CLIENT)
   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return new ItemStack(this);
   }

   public void addStacksForDisplay(ItemGroup group, DefaultedList<ItemStack> list) {
      list.add(new ItemStack(this));
   }

   public float getSlipperiness() {
      return this.slipperiness;
   }

   public float getVelocityMultiplier() {
      return this.velocityMultiplier;
   }

   public float getJumpVelocityMultiplier() {
      return this.jumpVelocityMultiplier;
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      world.syncWorldEvent(player, 2001, pos, getRawIdFromState(state));
      if (this.isIn(BlockTags.GUARDED_BY_PIGLINS)) {
         PiglinBrain.onGuardedBlockInteracted(player, false);
      }

   }

   public void rainTick(World world, BlockPos pos) {
   }

   public boolean shouldDropItemsOnExplosion(Explosion explosion) {
      return true;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
   }

   public StateManager<Block, BlockState> getStateManager() {
      return this.stateManager;
   }

   protected final void setDefaultState(BlockState state) {
      this.defaultState = state;
   }

   public final BlockState getDefaultState() {
      return this.defaultState;
   }

   public BlockSoundGroup getSoundGroup(BlockState state) {
      return this.soundGroup;
   }

   public Item asItem() {
      if (this.cachedItem == null) {
         this.cachedItem = Item.fromBlock(this);
      }

      return this.cachedItem;
   }

   public boolean hasDynamicBounds() {
      return this.dynamicBounds;
   }

   public String toString() {
      return "Block{" + Registry.BLOCK.getId(this) + "}";
   }

   @Environment(EnvType.CLIENT)
   public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
   }

   protected Block asBlock() {
      return this;
   }

   public static final class NeighborGroup {
      private final BlockState self;
      private final BlockState other;
      private final Direction facing;

      public NeighborGroup(BlockState self, BlockState other, Direction facing) {
         this.self = self;
         this.other = other;
         this.facing = facing;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (!(o instanceof Block.NeighborGroup)) {
            return false;
         } else {
            Block.NeighborGroup neighborGroup = (Block.NeighborGroup)o;
            return this.self == neighborGroup.self && this.other == neighborGroup.other && this.facing == neighborGroup.facing;
         }
      }

      public int hashCode() {
         int i = this.self.hashCode();
         i = 31 * i + this.other.hashCode();
         i = 31 * i + this.facing.hashCode();
         return i;
      }
   }
}
