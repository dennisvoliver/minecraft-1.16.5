package net.minecraft.entity.projectile.thrown;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class ThrownEntity extends ProjectileEntity {
   protected ThrownEntity(EntityType<? extends ThrownEntity> entityType, World world) {
      super(entityType, world);
   }

   protected ThrownEntity(EntityType<? extends ThrownEntity> type, double x, double y, double z, World world) {
      this(type, world);
      this.setPosition(x, y, z);
   }

   protected ThrownEntity(EntityType<? extends ThrownEntity> type, LivingEntity owner, World world) {
      this(type, owner.getX(), owner.getEyeY() - 0.10000000149011612D, owner.getZ(), world);
      this.setOwner(owner);
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldRender(double distance) {
      double d = this.getBoundingBox().getAverageSideLength() * 4.0D;
      if (Double.isNaN(d)) {
         d = 4.0D;
      }

      d *= 64.0D;
      return distance < d * d;
   }

   public void tick() {
      super.tick();
      HitResult hitResult = ProjectileUtil.getCollision(this, this::method_26958);
      boolean bl = false;
      if (hitResult.getType() == HitResult.Type.BLOCK) {
         BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
         BlockState blockState = this.world.getBlockState(blockPos);
         if (blockState.isOf(Blocks.NETHER_PORTAL)) {
            this.setInNetherPortal(blockPos);
            bl = true;
         } else if (blockState.isOf(Blocks.END_GATEWAY)) {
            BlockEntity blockEntity = this.world.getBlockEntity(blockPos);
            if (blockEntity instanceof EndGatewayBlockEntity && EndGatewayBlockEntity.method_30276(this)) {
               ((EndGatewayBlockEntity)blockEntity).tryTeleportingEntity(this);
            }

            bl = true;
         }
      }

      if (hitResult.getType() != HitResult.Type.MISS && !bl) {
         this.onCollision(hitResult);
      }

      this.checkBlockCollision();
      Vec3d vec3d = this.getVelocity();
      double d = this.getX() + vec3d.x;
      double e = this.getY() + vec3d.y;
      double f = this.getZ() + vec3d.z;
      this.method_26962();
      float j;
      if (this.isTouchingWater()) {
         for(int i = 0; i < 4; ++i) {
            float g = 0.25F;
            this.world.addParticle(ParticleTypes.BUBBLE, d - vec3d.x * 0.25D, e - vec3d.y * 0.25D, f - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
         }

         j = 0.8F;
      } else {
         j = 0.99F;
      }

      this.setVelocity(vec3d.multiply((double)j));
      if (!this.hasNoGravity()) {
         Vec3d vec3d2 = this.getVelocity();
         this.setVelocity(vec3d2.x, vec3d2.y - (double)this.getGravity(), vec3d2.z);
      }

      this.setPosition(d, e, f);
   }

   protected float getGravity() {
      return 0.03F;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }
}
