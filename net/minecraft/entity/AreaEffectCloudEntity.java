package net.minecraft.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.command.argument.ParticleEffectArgumentType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class AreaEffectCloudEntity extends Entity {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final TrackedData<Float> RADIUS;
   private static final TrackedData<Integer> COLOR;
   private static final TrackedData<Boolean> WAITING;
   private static final TrackedData<ParticleEffect> PARTICLE_ID;
   private Potion potion;
   private final List<StatusEffectInstance> effects;
   private final Map<Entity, Integer> affectedEntities;
   private int duration;
   private int waitTime;
   private int reapplicationDelay;
   private boolean customColor;
   private int durationOnUse;
   private float radiusOnUse;
   private float radiusGrowth;
   private LivingEntity owner;
   private UUID ownerUuid;

   public AreaEffectCloudEntity(EntityType<? extends AreaEffectCloudEntity> entityType, World world) {
      super(entityType, world);
      this.potion = Potions.EMPTY;
      this.effects = Lists.newArrayList();
      this.affectedEntities = Maps.newHashMap();
      this.duration = 600;
      this.waitTime = 20;
      this.reapplicationDelay = 20;
      this.noClip = true;
      this.setRadius(3.0F);
   }

   public AreaEffectCloudEntity(World world, double x, double y, double z) {
      this(EntityType.AREA_EFFECT_CLOUD, world);
      this.setPosition(x, y, z);
   }

   protected void initDataTracker() {
      this.getDataTracker().startTracking(COLOR, 0);
      this.getDataTracker().startTracking(RADIUS, 0.5F);
      this.getDataTracker().startTracking(WAITING, false);
      this.getDataTracker().startTracking(PARTICLE_ID, ParticleTypes.ENTITY_EFFECT);
   }

   public void setRadius(float radius) {
      if (!this.world.isClient) {
         this.getDataTracker().set(RADIUS, radius);
      }

   }

   public void calculateDimensions() {
      double d = this.getX();
      double e = this.getY();
      double f = this.getZ();
      super.calculateDimensions();
      this.setPosition(d, e, f);
   }

   public float getRadius() {
      return (Float)this.getDataTracker().get(RADIUS);
   }

   public void setPotion(Potion potion) {
      this.potion = potion;
      if (!this.customColor) {
         this.updateColor();
      }

   }

   private void updateColor() {
      if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
         this.getDataTracker().set(COLOR, 0);
      } else {
         this.getDataTracker().set(COLOR, PotionUtil.getColor((Collection)PotionUtil.getPotionEffects(this.potion, this.effects)));
      }

   }

   public void addEffect(StatusEffectInstance effect) {
      this.effects.add(effect);
      if (!this.customColor) {
         this.updateColor();
      }

   }

   public int getColor() {
      return (Integer)this.getDataTracker().get(COLOR);
   }

   public void setColor(int rgb) {
      this.customColor = true;
      this.getDataTracker().set(COLOR, rgb);
   }

   public ParticleEffect getParticleType() {
      return (ParticleEffect)this.getDataTracker().get(PARTICLE_ID);
   }

   public void setParticleType(ParticleEffect particle) {
      this.getDataTracker().set(PARTICLE_ID, particle);
   }

   protected void setWaiting(boolean waiting) {
      this.getDataTracker().set(WAITING, waiting);
   }

   public boolean isWaiting() {
      return (Boolean)this.getDataTracker().get(WAITING);
   }

   public int getDuration() {
      return this.duration;
   }

   public void setDuration(int duration) {
      this.duration = duration;
   }

   public void tick() {
      super.tick();
      boolean bl = this.isWaiting();
      float f = this.getRadius();
      if (this.world.isClient) {
         ParticleEffect particleEffect = this.getParticleType();
         float h;
         float j;
         float k;
         int m;
         int n;
         int o;
         if (bl) {
            if (this.random.nextBoolean()) {
               for(int i = 0; i < 2; ++i) {
                  float g = this.random.nextFloat() * 6.2831855F;
                  h = MathHelper.sqrt(this.random.nextFloat()) * 0.2F;
                  j = MathHelper.cos(g) * h;
                  k = MathHelper.sin(g) * h;
                  if (particleEffect.getType() == ParticleTypes.ENTITY_EFFECT) {
                     int l = this.random.nextBoolean() ? 16777215 : this.getColor();
                     m = l >> 16 & 255;
                     n = l >> 8 & 255;
                     o = l & 255;
                     this.world.addImportantParticle(particleEffect, this.getX() + (double)j, this.getY(), this.getZ() + (double)k, (double)((float)m / 255.0F), (double)((float)n / 255.0F), (double)((float)o / 255.0F));
                  } else {
                     this.world.addImportantParticle(particleEffect, this.getX() + (double)j, this.getY(), this.getZ() + (double)k, 0.0D, 0.0D, 0.0D);
                  }
               }
            }
         } else {
            float p = 3.1415927F * f * f;

            for(int q = 0; (float)q < p; ++q) {
               h = this.random.nextFloat() * 6.2831855F;
               j = MathHelper.sqrt(this.random.nextFloat()) * f;
               k = MathHelper.cos(h) * j;
               float u = MathHelper.sin(h) * j;
               if (particleEffect.getType() == ParticleTypes.ENTITY_EFFECT) {
                  m = this.getColor();
                  n = m >> 16 & 255;
                  o = m >> 8 & 255;
                  int y = m & 255;
                  this.world.addImportantParticle(particleEffect, this.getX() + (double)k, this.getY(), this.getZ() + (double)u, (double)((float)n / 255.0F), (double)((float)o / 255.0F), (double)((float)y / 255.0F));
               } else {
                  this.world.addImportantParticle(particleEffect, this.getX() + (double)k, this.getY(), this.getZ() + (double)u, (0.5D - this.random.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - this.random.nextDouble()) * 0.15D);
               }
            }
         }
      } else {
         if (this.age >= this.waitTime + this.duration) {
            this.remove();
            return;
         }

         boolean bl2 = this.age < this.waitTime;
         if (bl != bl2) {
            this.setWaiting(bl2);
         }

         if (bl2) {
            return;
         }

         if (this.radiusGrowth != 0.0F) {
            f += this.radiusGrowth;
            if (f < 0.5F) {
               this.remove();
               return;
            }

            this.setRadius(f);
         }

         if (this.age % 5 == 0) {
            Iterator iterator = this.affectedEntities.entrySet().iterator();

            while(iterator.hasNext()) {
               Entry<Entity, Integer> entry = (Entry)iterator.next();
               if (this.age >= (Integer)entry.getValue()) {
                  iterator.remove();
               }
            }

            List<StatusEffectInstance> list = Lists.newArrayList();
            Iterator var22 = this.potion.getEffects().iterator();

            while(var22.hasNext()) {
               StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var22.next();
               list.add(new StatusEffectInstance(statusEffectInstance.getEffectType(), statusEffectInstance.getDuration() / 4, statusEffectInstance.getAmplifier(), statusEffectInstance.isAmbient(), statusEffectInstance.shouldShowParticles()));
            }

            list.addAll(this.effects);
            if (list.isEmpty()) {
               this.affectedEntities.clear();
            } else {
               List<LivingEntity> list2 = this.world.getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox());
               if (!list2.isEmpty()) {
                  Iterator var25 = list2.iterator();

                  while(true) {
                     LivingEntity livingEntity;
                     double z;
                     do {
                        do {
                           do {
                              if (!var25.hasNext()) {
                                 return;
                              }

                              livingEntity = (LivingEntity)var25.next();
                           } while(this.affectedEntities.containsKey(livingEntity));
                        } while(!livingEntity.isAffectedBySplashPotions());

                        double d = livingEntity.getX() - this.getX();
                        double e = livingEntity.getZ() - this.getZ();
                        z = d * d + e * e;
                     } while(!(z <= (double)(f * f)));

                     this.affectedEntities.put(livingEntity, this.age + this.reapplicationDelay);
                     Iterator var14 = list.iterator();

                     while(var14.hasNext()) {
                        StatusEffectInstance statusEffectInstance2 = (StatusEffectInstance)var14.next();
                        if (statusEffectInstance2.getEffectType().isInstant()) {
                           statusEffectInstance2.getEffectType().applyInstantEffect(this, this.getOwner(), livingEntity, statusEffectInstance2.getAmplifier(), 0.5D);
                        } else {
                           livingEntity.addStatusEffect(new StatusEffectInstance(statusEffectInstance2));
                        }
                     }

                     if (this.radiusOnUse != 0.0F) {
                        f += this.radiusOnUse;
                        if (f < 0.5F) {
                           this.remove();
                           return;
                        }

                        this.setRadius(f);
                     }

                     if (this.durationOnUse != 0) {
                        this.duration += this.durationOnUse;
                        if (this.duration <= 0) {
                           this.remove();
                           return;
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public void setRadiusOnUse(float radius) {
      this.radiusOnUse = radius;
   }

   public void setRadiusGrowth(float growth) {
      this.radiusGrowth = growth;
   }

   public void setWaitTime(int ticks) {
      this.waitTime = ticks;
   }

   public void setOwner(@Nullable LivingEntity owner) {
      this.owner = owner;
      this.ownerUuid = owner == null ? null : owner.getUuid();
   }

   @Nullable
   public LivingEntity getOwner() {
      if (this.owner == null && this.ownerUuid != null && this.world instanceof ServerWorld) {
         Entity entity = ((ServerWorld)this.world).getEntity(this.ownerUuid);
         if (entity instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
         }
      }

      return this.owner;
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      this.age = nbt.getInt("Age");
      this.duration = nbt.getInt("Duration");
      this.waitTime = nbt.getInt("WaitTime");
      this.reapplicationDelay = nbt.getInt("ReapplicationDelay");
      this.durationOnUse = nbt.getInt("DurationOnUse");
      this.radiusOnUse = nbt.getFloat("RadiusOnUse");
      this.radiusGrowth = nbt.getFloat("RadiusPerTick");
      this.setRadius(nbt.getFloat("Radius"));
      if (nbt.containsUuid("Owner")) {
         this.ownerUuid = nbt.getUuid("Owner");
      }

      if (nbt.contains("Particle", 8)) {
         try {
            this.setParticleType(ParticleEffectArgumentType.readParameters(new StringReader(nbt.getString("Particle"))));
         } catch (CommandSyntaxException var5) {
            LOGGER.warn((String)"Couldn't load custom particle {}", (Object)nbt.getString("Particle"), (Object)var5);
         }
      }

      if (nbt.contains("Color", 99)) {
         this.setColor(nbt.getInt("Color"));
      }

      if (nbt.contains("Potion", 8)) {
         this.setPotion(PotionUtil.getPotion(nbt));
      }

      if (nbt.contains("Effects", 9)) {
         NbtList nbtList = nbt.getList("Effects", 10);
         this.effects.clear();

         for(int i = 0; i < nbtList.size(); ++i) {
            StatusEffectInstance statusEffectInstance = StatusEffectInstance.fromNbt(nbtList.getCompound(i));
            if (statusEffectInstance != null) {
               this.addEffect(statusEffectInstance);
            }
         }
      }

   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.putInt("Age", this.age);
      nbt.putInt("Duration", this.duration);
      nbt.putInt("WaitTime", this.waitTime);
      nbt.putInt("ReapplicationDelay", this.reapplicationDelay);
      nbt.putInt("DurationOnUse", this.durationOnUse);
      nbt.putFloat("RadiusOnUse", this.radiusOnUse);
      nbt.putFloat("RadiusPerTick", this.radiusGrowth);
      nbt.putFloat("Radius", this.getRadius());
      nbt.putString("Particle", this.getParticleType().asString());
      if (this.ownerUuid != null) {
         nbt.putUuid("Owner", this.ownerUuid);
      }

      if (this.customColor) {
         nbt.putInt("Color", this.getColor());
      }

      if (this.potion != Potions.EMPTY && this.potion != null) {
         nbt.putString("Potion", Registry.POTION.getId(this.potion).toString());
      }

      if (!this.effects.isEmpty()) {
         NbtList nbtList = new NbtList();
         Iterator var3 = this.effects.iterator();

         while(var3.hasNext()) {
            StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var3.next();
            nbtList.add(statusEffectInstance.writeNbt(new NbtCompound()));
         }

         nbt.put("Effects", nbtList);
      }

   }

   public void onTrackedDataSet(TrackedData<?> data) {
      if (RADIUS.equals(data)) {
         this.calculateDimensions();
      }

      super.onTrackedDataSet(data);
   }

   public PistonBehavior getPistonBehavior() {
      return PistonBehavior.IGNORE;
   }

   public Packet<?> createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return EntityDimensions.changing(this.getRadius() * 2.0F, 0.5F);
   }

   static {
      RADIUS = DataTracker.registerData(AreaEffectCloudEntity.class, TrackedDataHandlerRegistry.FLOAT);
      COLOR = DataTracker.registerData(AreaEffectCloudEntity.class, TrackedDataHandlerRegistry.INTEGER);
      WAITING = DataTracker.registerData(AreaEffectCloudEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
      PARTICLE_ID = DataTracker.registerData(AreaEffectCloudEntity.class, TrackedDataHandlerRegistry.PARTICLE);
   }
}
