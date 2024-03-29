package net.minecraft.entity.vehicle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class TntMinecartEntity extends AbstractMinecartEntity {
   private int fuseTicks = -1;

   public TntMinecartEntity(EntityType<? extends TntMinecartEntity> entityType, World world) {
      super(entityType, world);
   }

   public TntMinecartEntity(World world, double x, double y, double z) {
      super(EntityType.TNT_MINECART, world, x, y, z);
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.TNT;
   }

   public BlockState getDefaultContainedBlock() {
      return Blocks.TNT.getDefaultState();
   }

   public void tick() {
      super.tick();
      if (this.fuseTicks > 0) {
         --this.fuseTicks;
         this.world.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
      } else if (this.fuseTicks == 0) {
         this.explode(squaredHorizontalLength(this.getVelocity()));
      }

      if (this.horizontalCollision) {
         double d = squaredHorizontalLength(this.getVelocity());
         if (d >= 0.009999999776482582D) {
            this.explode(d);
         }
      }

   }

   public boolean damage(DamageSource source, float amount) {
      Entity entity = source.getSource();
      if (entity instanceof PersistentProjectileEntity) {
         PersistentProjectileEntity persistentProjectileEntity = (PersistentProjectileEntity)entity;
         if (persistentProjectileEntity.isOnFire()) {
            this.explode(persistentProjectileEntity.getVelocity().lengthSquared());
         }
      }

      return super.damage(source, amount);
   }

   public void dropItems(DamageSource damageSource) {
      double d = squaredHorizontalLength(this.getVelocity());
      if (!damageSource.isFire() && !damageSource.isExplosive() && !(d >= 0.009999999776482582D)) {
         super.dropItems(damageSource);
         if (!damageSource.isExplosive() && this.world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            this.dropItem(Blocks.TNT);
         }

      } else {
         if (this.fuseTicks < 0) {
            this.prime();
            this.fuseTicks = this.random.nextInt(20) + this.random.nextInt(20);
         }

      }
   }

   protected void explode(double velocity) {
      if (!this.world.isClient) {
         double d = Math.sqrt(velocity);
         if (d > 5.0D) {
            d = 5.0D;
         }

         this.world.createExplosion(this, this.getX(), this.getY(), this.getZ(), (float)(4.0D + this.random.nextDouble() * 1.5D * d), Explosion.DestructionType.BREAK);
         this.remove();
      }

   }

   public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
      if (fallDistance >= 3.0F) {
         float f = fallDistance / 10.0F;
         this.explode((double)(f * f));
      }

      return super.handleFallDamage(fallDistance, damageMultiplier);
   }

   public void onActivatorRail(int x, int y, int z, boolean powered) {
      if (powered && this.fuseTicks < 0) {
         this.prime();
      }

   }

   @Environment(EnvType.CLIENT)
   public void handleStatus(byte status) {
      if (status == 10) {
         this.prime();
      } else {
         super.handleStatus(status);
      }

   }

   public void prime() {
      this.fuseTicks = 80;
      if (!this.world.isClient) {
         this.world.sendEntityStatus(this, (byte)10);
         if (!this.isSilent()) {
            this.world.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }
      }

   }

   @Environment(EnvType.CLIENT)
   public int getFuseTicks() {
      return this.fuseTicks;
   }

   public boolean isPrimed() {
      return this.fuseTicks > -1;
   }

   public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
      return !this.isPrimed() || !blockState.isIn(BlockTags.RAILS) && !world.getBlockState(pos.up()).isIn(BlockTags.RAILS) ? super.getEffectiveExplosionResistance(explosion, world, pos, blockState, fluidState, max) : 0.0F;
   }

   public boolean canExplosionDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float explosionPower) {
      return !this.isPrimed() || !state.isIn(BlockTags.RAILS) && !world.getBlockState(pos.up()).isIn(BlockTags.RAILS) ? super.canExplosionDestroyBlock(explosion, world, pos, state, explosionPower) : false;
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("TNTFuse", 99)) {
         this.fuseTicks = nbt.getInt("TNTFuse");
      }

   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("TNTFuse", this.fuseTicks);
   }
}
