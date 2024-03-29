package net.minecraft.entity;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.AutomaticItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class FallingBlockEntity extends Entity {
   private BlockState block;
   public int timeFalling;
   public boolean dropItem;
   private boolean destroyedOnLanding;
   private boolean hurtEntities;
   private int fallHurtMax;
   private float fallHurtAmount;
   public NbtCompound blockEntityData;
   protected static final TrackedData<BlockPos> BLOCK_POS;

   public FallingBlockEntity(EntityType<? extends FallingBlockEntity> entityType, World world) {
      super(entityType, world);
      this.block = Blocks.SAND.getDefaultState();
      this.dropItem = true;
      this.fallHurtMax = 40;
      this.fallHurtAmount = 2.0F;
   }

   public FallingBlockEntity(World world, double x, double y, double z, BlockState block) {
      this(EntityType.FALLING_BLOCK, world);
      this.block = block;
      this.inanimate = true;
      this.setPosition(x, y + (double)((1.0F - this.getHeight()) / 2.0F), z);
      this.setVelocity(Vec3d.ZERO);
      this.prevX = x;
      this.prevY = y;
      this.prevZ = z;
      this.setFallingBlockPos(this.getBlockPos());
   }

   public boolean isAttackable() {
      return false;
   }

   public void setFallingBlockPos(BlockPos pos) {
      this.dataTracker.set(BLOCK_POS, pos);
   }

   @Environment(EnvType.CLIENT)
   public BlockPos getFallingBlockPos() {
      return (BlockPos)this.dataTracker.get(BLOCK_POS);
   }

   protected boolean canClimb() {
      return false;
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(BLOCK_POS, BlockPos.ORIGIN);
   }

   public boolean collides() {
      return !this.removed;
   }

   public void tick() {
      if (this.block.isAir()) {
         this.remove();
      } else {
         Block block = this.block.getBlock();
         BlockPos blockPos2;
         if (this.timeFalling++ == 0) {
            blockPos2 = this.getBlockPos();
            if (this.world.getBlockState(blockPos2).isOf(block)) {
               this.world.removeBlock(blockPos2, false);
            } else if (!this.world.isClient) {
               this.remove();
               return;
            }
         }

         if (!this.hasNoGravity()) {
            this.setVelocity(this.getVelocity().add(0.0D, -0.04D, 0.0D));
         }

         this.move(MovementType.SELF, this.getVelocity());
         if (!this.world.isClient) {
            blockPos2 = this.getBlockPos();
            boolean bl = this.block.getBlock() instanceof ConcretePowderBlock;
            boolean bl2 = bl && this.world.getFluidState(blockPos2).isIn(FluidTags.WATER);
            double d = this.getVelocity().lengthSquared();
            if (bl && d > 1.0D) {
               BlockHitResult blockHitResult = this.world.raycast(new RaycastContext(new Vec3d(this.prevX, this.prevY, this.prevZ), this.getPos(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.SOURCE_ONLY, this));
               if (blockHitResult.getType() != HitResult.Type.MISS && this.world.getFluidState(blockHitResult.getBlockPos()).isIn(FluidTags.WATER)) {
                  blockPos2 = blockHitResult.getBlockPos();
                  bl2 = true;
               }
            }

            if (!this.onGround && !bl2) {
               if (!this.world.isClient && (this.timeFalling > 100 && (blockPos2.getY() < 1 || blockPos2.getY() > 256) || this.timeFalling > 600)) {
                  if (this.dropItem && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                     this.dropItem(block);
                  }

                  this.remove();
               }
            } else {
               BlockState blockState = this.world.getBlockState(blockPos2);
               this.setVelocity(this.getVelocity().multiply(0.7D, -0.5D, 0.7D));
               if (!blockState.isOf(Blocks.MOVING_PISTON)) {
                  this.remove();
                  if (!this.destroyedOnLanding) {
                     boolean bl3 = blockState.canReplace(new AutomaticItemPlacementContext(this.world, blockPos2, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                     boolean bl4 = FallingBlock.canFallThrough(this.world.getBlockState(blockPos2.down())) && (!bl || !bl2);
                     boolean bl5 = this.block.canPlaceAt(this.world, blockPos2) && !bl4;
                     if (bl3 && bl5) {
                        if (this.block.contains(Properties.WATERLOGGED) && this.world.getFluidState(blockPos2).getFluid() == Fluids.WATER) {
                           this.block = (BlockState)this.block.with(Properties.WATERLOGGED, true);
                        }

                        if (this.world.setBlockState(blockPos2, this.block, 3)) {
                           if (block instanceof FallingBlock) {
                              ((FallingBlock)block).onLanding(this.world, blockPos2, this.block, blockState, this);
                           }

                           if (this.blockEntityData != null && block instanceof BlockEntityProvider) {
                              BlockEntity blockEntity = this.world.getBlockEntity(blockPos2);
                              if (blockEntity != null) {
                                 NbtCompound nbtCompound = blockEntity.writeNbt(new NbtCompound());
                                 Iterator var13 = this.blockEntityData.getKeys().iterator();

                                 while(var13.hasNext()) {
                                    String string = (String)var13.next();
                                    NbtElement nbtElement = this.blockEntityData.get(string);
                                    if (!"x".equals(string) && !"y".equals(string) && !"z".equals(string)) {
                                       nbtCompound.put(string, nbtElement.copy());
                                    }
                                 }

                                 blockEntity.fromTag(this.block, nbtCompound);
                                 blockEntity.markDirty();
                              }
                           }
                        } else if (this.dropItem && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                           this.dropItem(block);
                        }
                     } else if (this.dropItem && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                        this.dropItem(block);
                     }
                  } else if (block instanceof FallingBlock) {
                     ((FallingBlock)block).onDestroyedOnLanding(this.world, blockPos2, this);
                  }
               }
            }
         }

         this.setVelocity(this.getVelocity().multiply(0.98D));
      }
   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
      if (this.hurtEntities) {
         int i = MathHelper.ceil(fallDistance - 1.0F);
         if (i > 0) {
            List<Entity> list = Lists.newArrayList((Iterable)this.world.getOtherEntities(this, this.getBoundingBox()));
            boolean bl = this.block.isIn(BlockTags.ANVIL);
            DamageSource damageSource = bl ? DamageSource.ANVIL : DamageSource.FALLING_BLOCK;
            Iterator var7 = list.iterator();

            while(var7.hasNext()) {
               Entity entity = (Entity)var7.next();
               entity.damage(damageSource, (float)Math.min(MathHelper.floor((float)i * this.fallHurtAmount), this.fallHurtMax));
            }

            if (bl && (double)this.random.nextFloat() < 0.05000000074505806D + (double)i * 0.05D) {
               BlockState blockState = AnvilBlock.getLandingState(this.block);
               if (blockState == null) {
                  this.destroyedOnLanding = true;
               } else {
                  this.block = blockState;
               }
            }
         }
      }

      return false;
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.put("BlockState", NbtHelper.fromBlockState(this.block));
      nbt.putInt("Time", this.timeFalling);
      nbt.putBoolean("DropItem", this.dropItem);
      nbt.putBoolean("HurtEntities", this.hurtEntities);
      nbt.putFloat("FallHurtAmount", this.fallHurtAmount);
      nbt.putInt("FallHurtMax", this.fallHurtMax);
      if (this.blockEntityData != null) {
         nbt.put("TileEntityData", this.blockEntityData);
      }

   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      this.block = NbtHelper.toBlockState(nbt.getCompound("BlockState"));
      this.timeFalling = nbt.getInt("Time");
      if (nbt.contains("HurtEntities", 99)) {
         this.hurtEntities = nbt.getBoolean("HurtEntities");
         this.fallHurtAmount = nbt.getFloat("FallHurtAmount");
         this.fallHurtMax = nbt.getInt("FallHurtMax");
      } else if (this.block.isIn(BlockTags.ANVIL)) {
         this.hurtEntities = true;
      }

      if (nbt.contains("DropItem", 99)) {
         this.dropItem = nbt.getBoolean("DropItem");
      }

      if (nbt.contains("TileEntityData", 10)) {
         this.blockEntityData = nbt.getCompound("TileEntityData");
      }

      if (this.block.isAir()) {
         this.block = Blocks.SAND.getDefaultState();
      }

   }

   @Environment(EnvType.CLIENT)
   public World getWorldClient() {
      return this.world;
   }

   public void setHurtEntities(boolean hurtEntities) {
      this.hurtEntities = hurtEntities;
   }

   @Environment(EnvType.CLIENT)
   public boolean doesRenderOnFire() {
      return false;
   }

   public void populateCrashReport(CrashReportSection section) {
      super.populateCrashReport(section);
      section.add("Immitating BlockState", (Object)this.block.toString());
   }

   public BlockState getBlockState() {
      return this.block;
   }

   public boolean entityDataRequiresOperator() {
      return true;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this, Block.getRawIdFromState(this.getBlockState()));
   }

   static {
      BLOCK_POS = DataTracker.registerData(FallingBlockEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
   }
}
