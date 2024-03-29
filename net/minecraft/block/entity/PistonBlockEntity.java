package net.minecraft.block.entity;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.enums.PistonType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Boxes;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

/**
 * A piston block entity represents the block being pushed by a piston.
 */
public class PistonBlockEntity extends BlockEntity implements Tickable {
   private BlockState pushedBlock;
   private Direction facing;
   private boolean extending;
   private boolean source;
   private static final ThreadLocal<Direction> field_12205 = ThreadLocal.withInitial(() -> {
      return null;
   });
   private float progress;
   private float lastProgress;
   private long savedWorldTime;
   private int field_26705;

   public PistonBlockEntity() {
      super(BlockEntityType.PISTON);
   }

   public PistonBlockEntity(BlockState pushedBlock, Direction facing, boolean extending, boolean source) {
      this();
      this.pushedBlock = pushedBlock;
      this.facing = facing;
      this.extending = extending;
      this.source = source;
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.writeNbt(new NbtCompound());
   }

   public boolean isExtending() {
      return this.extending;
   }

   public Direction getFacing() {
      return this.facing;
   }

   public boolean isSource() {
      return this.source;
   }

   public float getProgress(float tickDelta) {
      if (tickDelta > 1.0F) {
         tickDelta = 1.0F;
      }

      return MathHelper.lerp(tickDelta, this.lastProgress, this.progress);
   }

   @Environment(EnvType.CLIENT)
   public float getRenderOffsetX(float tickDelta) {
      return (float)this.facing.getOffsetX() * this.getAmountExtended(this.getProgress(tickDelta));
   }

   @Environment(EnvType.CLIENT)
   public float getRenderOffsetY(float tickDelta) {
      return (float)this.facing.getOffsetY() * this.getAmountExtended(this.getProgress(tickDelta));
   }

   @Environment(EnvType.CLIENT)
   public float getRenderOffsetZ(float tickDelta) {
      return (float)this.facing.getOffsetZ() * this.getAmountExtended(this.getProgress(tickDelta));
   }

   private float getAmountExtended(float progress) {
      return this.extending ? progress - 1.0F : 1.0F - progress;
   }

   private BlockState getHeadBlockState() {
      return !this.isExtending() && this.isSource() && this.pushedBlock.getBlock() instanceof PistonBlock ? (BlockState)((BlockState)((BlockState)Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.SHORT, this.progress > 0.25F)).with(PistonHeadBlock.TYPE, this.pushedBlock.isOf(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT)).with(PistonHeadBlock.FACING, this.pushedBlock.get(PistonBlock.FACING)) : this.pushedBlock;
   }

   private void pushEntities(float nextProgress) {
      Direction direction = this.getMovementDirection();
      double d = (double)(nextProgress - this.progress);
      VoxelShape voxelShape = this.getHeadBlockState().getCollisionShape(this.world, this.getPos());
      if (!voxelShape.isEmpty()) {
         Box box = this.offsetHeadBox(voxelShape.getBoundingBox());
         List<Entity> list = this.world.getOtherEntities((Entity)null, Boxes.stretch(box, direction, d).union(box));
         if (!list.isEmpty()) {
            List<Box> list2 = voxelShape.getBoundingBoxes();
            boolean bl = this.pushedBlock.isOf(Blocks.SLIME_BLOCK);
            Iterator var10 = list.iterator();

            while(true) {
               Entity entity;
               while(true) {
                  do {
                     if (!var10.hasNext()) {
                        return;
                     }

                     entity = (Entity)var10.next();
                  } while(entity.getPistonBehavior() == PistonBehavior.IGNORE);

                  if (!bl) {
                     break;
                  }

                  if (!(entity instanceof ServerPlayerEntity)) {
                     Vec3d vec3d = entity.getVelocity();
                     double e = vec3d.x;
                     double f = vec3d.y;
                     double g = vec3d.z;
                     switch(direction.getAxis()) {
                     case X:
                        e = (double)direction.getOffsetX();
                        break;
                     case Y:
                        f = (double)direction.getOffsetY();
                        break;
                     case Z:
                        g = (double)direction.getOffsetZ();
                     }

                     entity.setVelocity(e, f, g);
                     break;
                  }
               }

               double h = 0.0D;
               Iterator var14 = list2.iterator();

               while(var14.hasNext()) {
                  Box box2 = (Box)var14.next();
                  Box box3 = Boxes.stretch(this.offsetHeadBox(box2), direction, d);
                  Box box4 = entity.getBoundingBox();
                  if (box3.intersects(box4)) {
                     h = Math.max(h, getIntersectionSize(box3, direction, box4));
                     if (h >= d) {
                        break;
                     }
                  }
               }

               if (!(h <= 0.0D)) {
                  h = Math.min(h, d) + 0.01D;
                  method_23672(direction, entity, h, direction);
                  if (!this.extending && this.source) {
                     this.push(entity, direction, d);
                  }
               }
            }
         }
      }
   }

   private static void method_23672(Direction direction, Entity entity, double d, Direction direction2) {
      field_12205.set(direction);
      entity.move(MovementType.PISTON, new Vec3d(d * (double)direction2.getOffsetX(), d * (double)direction2.getOffsetY(), d * (double)direction2.getOffsetZ()));
      field_12205.set((Object)null);
   }

   private void method_23674(float f) {
      if (this.isPushingHoneyBlock()) {
         Direction direction = this.getMovementDirection();
         if (direction.getAxis().isHorizontal()) {
            double d = this.pushedBlock.getCollisionShape(this.world, this.pos).getMax(Direction.Axis.Y);
            Box box = this.offsetHeadBox(new Box(0.0D, d, 0.0D, 1.0D, 1.5000000999999998D, 1.0D));
            double e = (double)(f - this.progress);
            List<Entity> list = this.world.getOtherEntities((Entity)null, box, (entityx) -> {
               return method_23671(box, entityx);
            });
            Iterator var9 = list.iterator();

            while(var9.hasNext()) {
               Entity entity = (Entity)var9.next();
               method_23672(direction, entity, e, direction);
            }

         }
      }
   }

   private static boolean method_23671(Box box, Entity entity) {
      return entity.getPistonBehavior() == PistonBehavior.NORMAL && entity.isOnGround() && entity.getX() >= box.minX && entity.getX() <= box.maxX && entity.getZ() >= box.minZ && entity.getZ() <= box.maxZ;
   }

   private boolean isPushingHoneyBlock() {
      return this.pushedBlock.isOf(Blocks.HONEY_BLOCK);
   }

   public Direction getMovementDirection() {
      return this.extending ? this.facing : this.facing.getOpposite();
   }

   private static double getIntersectionSize(Box box, Direction direction, Box box2) {
      switch(direction) {
      case EAST:
         return box.maxX - box2.minX;
      case WEST:
         return box2.maxX - box.minX;
      case UP:
      default:
         return box.maxY - box2.minY;
      case DOWN:
         return box2.maxY - box.minY;
      case SOUTH:
         return box.maxZ - box2.minZ;
      case NORTH:
         return box2.maxZ - box.minZ;
      }
   }

   private Box offsetHeadBox(Box box) {
      double d = (double)this.getAmountExtended(this.progress);
      return box.offset((double)this.pos.getX() + d * (double)this.facing.getOffsetX(), (double)this.pos.getY() + d * (double)this.facing.getOffsetY(), (double)this.pos.getZ() + d * (double)this.facing.getOffsetZ());
   }

   private void push(Entity entity, Direction direction, double amount) {
      Box box = entity.getBoundingBox();
      Box box2 = VoxelShapes.fullCube().getBoundingBox().offset(this.pos);
      if (box.intersects(box2)) {
         Direction direction2 = direction.getOpposite();
         double d = getIntersectionSize(box2, direction2, box) + 0.01D;
         double e = getIntersectionSize(box2, direction2, box.intersection(box2)) + 0.01D;
         if (Math.abs(d - e) < 0.01D) {
            d = Math.min(d, amount) + 0.01D;
            method_23672(direction, entity, d, direction2);
         }
      }

   }

   public BlockState getPushedBlock() {
      return this.pushedBlock;
   }

   public void finish() {
      if (this.world != null && (this.lastProgress < 1.0F || this.world.isClient)) {
         this.progress = 1.0F;
         this.lastProgress = this.progress;
         this.world.removeBlockEntity(this.pos);
         this.markRemoved();
         if (this.world.getBlockState(this.pos).isOf(Blocks.MOVING_PISTON)) {
            BlockState blockState2;
            if (this.source) {
               blockState2 = Blocks.AIR.getDefaultState();
            } else {
               blockState2 = Block.postProcessState(this.pushedBlock, this.world, this.pos);
            }

            this.world.setBlockState(this.pos, blockState2, 3);
            this.world.updateNeighbor(this.pos, blockState2.getBlock(), this.pos);
         }
      }

   }

   public void tick() {
      this.savedWorldTime = this.world.getTime();
      this.lastProgress = this.progress;
      if (this.lastProgress >= 1.0F) {
         if (this.world.isClient && this.field_26705 < 5) {
            ++this.field_26705;
         } else {
            this.world.removeBlockEntity(this.pos);
            this.markRemoved();
            if (this.pushedBlock != null && this.world.getBlockState(this.pos).isOf(Blocks.MOVING_PISTON)) {
               BlockState blockState = Block.postProcessState(this.pushedBlock, this.world, this.pos);
               if (blockState.isAir()) {
                  this.world.setBlockState(this.pos, this.pushedBlock, 84);
                  Block.replace(this.pushedBlock, blockState, this.world, this.pos, 3);
               } else {
                  if (blockState.contains(Properties.WATERLOGGED) && (Boolean)blockState.get(Properties.WATERLOGGED)) {
                     blockState = (BlockState)blockState.with(Properties.WATERLOGGED, false);
                  }

                  this.world.setBlockState(this.pos, blockState, 67);
                  this.world.updateNeighbor(this.pos, blockState.getBlock(), this.pos);
               }
            }

         }
      } else {
         float f = this.progress + 0.5F;
         this.pushEntities(f);
         this.method_23674(f);
         this.progress = f;
         if (this.progress >= 1.0F) {
            this.progress = 1.0F;
         }

      }
   }

   public void fromTag(BlockState state, NbtCompound tag) {
      super.fromTag(state, tag);
      this.pushedBlock = NbtHelper.toBlockState(tag.getCompound("blockState"));
      this.facing = Direction.byId(tag.getInt("facing"));
      this.progress = tag.getFloat("progress");
      this.lastProgress = this.progress;
      this.extending = tag.getBoolean("extending");
      this.source = tag.getBoolean("source");
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.put("blockState", NbtHelper.fromBlockState(this.pushedBlock));
      nbt.putInt("facing", this.facing.getId());
      nbt.putFloat("progress", this.lastProgress);
      nbt.putBoolean("extending", this.extending);
      nbt.putBoolean("source", this.source);
      return nbt;
   }

   public VoxelShape getCollisionShape(BlockView world, BlockPos pos) {
      VoxelShape voxelShape2;
      if (!this.extending && this.source) {
         voxelShape2 = ((BlockState)this.pushedBlock.with(PistonBlock.EXTENDED, true)).getCollisionShape(world, pos);
      } else {
         voxelShape2 = VoxelShapes.empty();
      }

      Direction direction = (Direction)field_12205.get();
      if ((double)this.progress < 1.0D && direction == this.getMovementDirection()) {
         return voxelShape2;
      } else {
         BlockState blockState2;
         if (this.isSource()) {
            blockState2 = (BlockState)((BlockState)Blocks.PISTON_HEAD.getDefaultState().with(PistonHeadBlock.FACING, this.facing)).with(PistonHeadBlock.SHORT, this.extending != 1.0F - this.progress < 0.25F);
         } else {
            blockState2 = this.pushedBlock;
         }

         float f = this.getAmountExtended(this.progress);
         double d = (double)((float)this.facing.getOffsetX() * f);
         double e = (double)((float)this.facing.getOffsetY() * f);
         double g = (double)((float)this.facing.getOffsetZ() * f);
         return VoxelShapes.union(voxelShape2, blockState2.getCollisionShape(world, pos).offset(d, e, g));
      }
   }

   public long getSavedWorldTime() {
      return this.savedWorldTime;
   }

   @Environment(EnvType.CLIENT)
   public double getRenderDistance() {
      return 68.0D;
   }
}
