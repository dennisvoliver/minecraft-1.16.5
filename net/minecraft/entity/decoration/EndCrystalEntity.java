package net.minecraft.entity.decoration;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class EndCrystalEntity extends Entity {
   private static final TrackedData<Optional<BlockPos>> BEAM_TARGET;
   private static final TrackedData<Boolean> SHOW_BOTTOM;
   public int endCrystalAge;

   public EndCrystalEntity(EntityType<? extends EndCrystalEntity> entityType, World world) {
      super(entityType, world);
      this.inanimate = true;
      this.endCrystalAge = this.random.nextInt(100000);
   }

   public EndCrystalEntity(World world, double x, double y, double z) {
      this(EntityType.END_CRYSTAL, world);
      this.setPosition(x, y, z);
   }

   protected boolean canClimb() {
      return false;
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(BEAM_TARGET, Optional.empty());
      this.getDataTracker().startTracking(SHOW_BOTTOM, true);
   }

   public void tick() {
      ++this.endCrystalAge;
      if (this.world instanceof ServerWorld) {
         BlockPos blockPos = this.getBlockPos();
         if (((ServerWorld)this.world).getEnderDragonFight() != null && this.world.getBlockState(blockPos).isAir()) {
            this.world.setBlockState(blockPos, AbstractFireBlock.getState(this.world, blockPos));
         }
      }

   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      if (this.getBeamTarget() != null) {
         nbt.put("BeamTarget", NbtHelper.fromBlockPos(this.getBeamTarget()));
      }

      nbt.putBoolean("ShowBottom", this.shouldShowBottom());
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      if (nbt.contains("BeamTarget", 10)) {
         this.setBeamTarget(NbtHelper.toBlockPos(nbt.getCompound("BeamTarget")));
      }

      if (nbt.contains("ShowBottom", 1)) {
         this.setShowBottom(nbt.getBoolean("ShowBottom"));
      }

   }

   public boolean collides() {
      return true;
   }

   public boolean damage(DamageSource source, float amount) {
      if (this.isInvulnerableTo(source)) {
         return false;
      } else if (source.getAttacker() instanceof EnderDragonEntity) {
         return false;
      } else {
         if (!this.removed && !this.world.isClient) {
            this.remove();
            if (!source.isExplosive()) {
               this.world.createExplosion((Entity)null, this.getX(), this.getY(), this.getZ(), 6.0F, Explosion.DestructionType.DESTROY);
            }

            this.crystalDestroyed(source);
         }

         return true;
      }
   }

   public void kill() {
      this.crystalDestroyed(DamageSource.GENERIC);
      super.kill();
   }

   private void crystalDestroyed(DamageSource source) {
      if (this.world instanceof ServerWorld) {
         EnderDragonFight enderDragonFight = ((ServerWorld)this.world).getEnderDragonFight();
         if (enderDragonFight != null) {
            enderDragonFight.crystalDestroyed(this, source);
         }
      }

   }

   public void setBeamTarget(@Nullable BlockPos beamTarget) {
      this.getDataTracker().set(BEAM_TARGET, Optional.ofNullable(beamTarget));
   }

   @Nullable
   public BlockPos getBeamTarget() {
      return (BlockPos)((Optional)this.getDataTracker().get(BEAM_TARGET)).orElse((Object)null);
   }

   public void setShowBottom(boolean showBottom) {
      this.getDataTracker().set(SHOW_BOTTOM, showBottom);
   }

   public boolean shouldShowBottom() {
      return (Boolean)this.getDataTracker().get(SHOW_BOTTOM);
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      return super.shouldRender(distance) || this.getBeamTarget() != null;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }

   static {
      BEAM_TARGET = DataTracker.registerData(EndCrystalEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
      SHOW_BOTTOM = DataTracker.registerData(EndCrystalEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
