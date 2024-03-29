package net.minecraft.client.item;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ModelPredicateProviderRegistry {
   private static final Map<Identifier, ModelPredicateProvider> GLOBAL = Maps.newHashMap();
   private static final Identifier DAMAGED_ID = new Identifier("damaged");
   private static final Identifier DAMAGE_ID = new Identifier("damage");
   private static final ModelPredicateProvider DAMAGED_PROVIDER = (itemStack, clientWorld, livingEntity) -> {
      return itemStack.isDamaged() ? 1.0F : 0.0F;
   };
   private static final ModelPredicateProvider DAMAGE_PROVIDER = (itemStack, clientWorld, livingEntity) -> {
      return MathHelper.clamp((float)itemStack.getDamage() / (float)itemStack.getMaxDamage(), 0.0F, 1.0F);
   };
   private static final Map<Item, Map<Identifier, ModelPredicateProvider>> ITEM_SPECIFIC = Maps.newHashMap();

   private static ModelPredicateProvider register(Identifier id, ModelPredicateProvider provider) {
      GLOBAL.put(id, provider);
      return provider;
   }

   private static void register(Item item, Identifier id, ModelPredicateProvider provider) {
      ((Map)ITEM_SPECIFIC.computeIfAbsent(item, (itemx) -> {
         return Maps.newHashMap();
      })).put(id, provider);
   }

   @Nullable
   public static ModelPredicateProvider get(Item item, Identifier id) {
      if (item.getMaxDamage() > 0) {
         if (DAMAGE_ID.equals(id)) {
            return DAMAGE_PROVIDER;
         }

         if (DAMAGED_ID.equals(id)) {
            return DAMAGED_PROVIDER;
         }
      }

      ModelPredicateProvider modelPredicateProvider = (ModelPredicateProvider)GLOBAL.get(id);
      if (modelPredicateProvider != null) {
         return modelPredicateProvider;
      } else {
         Map<Identifier, ModelPredicateProvider> map = (Map)ITEM_SPECIFIC.get(item);
         return map == null ? null : (ModelPredicateProvider)map.get(id);
      }
   }

   static {
      register(new Identifier("lefthanded"), (itemStack, clientWorld, livingEntity) -> {
         return livingEntity != null && livingEntity.getMainArm() != Arm.RIGHT ? 1.0F : 0.0F;
      });
      register(new Identifier("cooldown"), (itemStack, clientWorld, livingEntity) -> {
         return livingEntity instanceof PlayerEntity ? ((PlayerEntity)livingEntity).getItemCooldownManager().getCooldownProgress(itemStack.getItem(), 0.0F) : 0.0F;
      });
      register(new Identifier("custom_model_data"), (itemStack, clientWorld, livingEntity) -> {
         return itemStack.hasTag() ? (float)itemStack.getTag().getInt("CustomModelData") : 0.0F;
      });
      register(Items.BOW, new Identifier("pull"), (itemStack, clientWorld, livingEntity) -> {
         if (livingEntity == null) {
            return 0.0F;
         } else {
            return livingEntity.getActiveItem() != itemStack ? 0.0F : (float)(itemStack.getMaxUseTime() - livingEntity.getItemUseTimeLeft()) / 20.0F;
         }
      });
      register(Items.BOW, new Identifier("pulling"), (itemStack, clientWorld, livingEntity) -> {
         return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0F : 0.0F;
      });
      register(Items.CLOCK, new Identifier("time"), new ModelPredicateProvider() {
         private double time;
         private double step;
         private long lastTick;

         public float call(ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity) {
            Entity entity = livingEntity != null ? livingEntity : itemStack.getHolder();
            if (entity == null) {
               return 0.0F;
            } else {
               if (clientWorld == null && ((Entity)entity).world instanceof ClientWorld) {
                  clientWorld = (ClientWorld)((Entity)entity).world;
               }

               if (clientWorld == null) {
                  return 0.0F;
               } else {
                  double e;
                  if (clientWorld.getDimension().isNatural()) {
                     e = (double)clientWorld.getSkyAngle(1.0F);
                  } else {
                     e = Math.random();
                  }

                  e = this.getTime(clientWorld, e);
                  return (float)e;
               }
            }
         }

         private double getTime(World world, double skyAngle) {
            if (world.getTime() != this.lastTick) {
               this.lastTick = world.getTime();
               double d = skyAngle - this.time;
               d = MathHelper.floorMod(d + 0.5D, 1.0D) - 0.5D;
               this.step += d * 0.1D;
               this.step *= 0.9D;
               this.time = MathHelper.floorMod(this.time + this.step, 1.0D);
            }

            return this.time;
         }
      });
      register(Items.COMPASS, new Identifier("angle"), new ModelPredicateProvider() {
         private final ModelPredicateProviderRegistry.AngleInterpolator value = new ModelPredicateProviderRegistry.AngleInterpolator();
         private final ModelPredicateProviderRegistry.AngleInterpolator speed = new ModelPredicateProviderRegistry.AngleInterpolator();

         public float call(ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity) {
            Entity entity = livingEntity != null ? livingEntity : itemStack.getHolder();
            if (entity == null) {
               return 0.0F;
            } else {
               if (clientWorld == null && ((Entity)entity).world instanceof ClientWorld) {
                  clientWorld = (ClientWorld)((Entity)entity).world;
               }

               BlockPos blockPos = CompassItem.hasLodestone(itemStack) ? this.getLodestonePos(clientWorld, itemStack.getOrCreateTag()) : this.getSpawnPos(clientWorld);
               long l = clientWorld.getTime();
               if (blockPos != null && !(((Entity)entity).getPos().squaredDistanceTo((double)blockPos.getX() + 0.5D, ((Entity)entity).getPos().getY(), (double)blockPos.getZ() + 0.5D) < 9.999999747378752E-6D)) {
                  boolean bl = livingEntity instanceof PlayerEntity && ((PlayerEntity)livingEntity).isMainPlayer();
                  double e = 0.0D;
                  if (bl) {
                     e = (double)livingEntity.yaw;
                  } else if (entity instanceof ItemFrameEntity) {
                     e = this.getItemFrameAngleOffset((ItemFrameEntity)entity);
                  } else if (entity instanceof ItemEntity) {
                     e = (double)(180.0F - ((ItemEntity)entity).method_27314(0.5F) / 6.2831855F * 360.0F);
                  } else if (livingEntity != null) {
                     e = (double)livingEntity.bodyYaw;
                  }

                  e = MathHelper.floorMod(e / 360.0D, 1.0D);
                  double f = this.getAngleToPos(Vec3d.ofCenter(blockPos), (Entity)entity) / 6.2831854820251465D;
                  double h;
                  if (bl) {
                     if (this.value.shouldUpdate(l)) {
                        this.value.update(l, 0.5D - (e - 0.25D));
                     }

                     h = f + this.value.value;
                  } else {
                     h = 0.5D - (e - 0.25D - f);
                  }

                  return MathHelper.floorMod((float)h, 1.0F);
               } else {
                  if (this.speed.shouldUpdate(l)) {
                     this.speed.update(l, Math.random());
                  }

                  double d = this.speed.value + (double)((float)itemStack.hashCode() / 2.14748365E9F);
                  return MathHelper.floorMod((float)d, 1.0F);
               }
            }
         }

         @Nullable
         private BlockPos getSpawnPos(ClientWorld world) {
            return world.getDimension().isNatural() ? world.getSpawnPos() : null;
         }

         @Nullable
         private BlockPos getLodestonePos(World world, NbtCompound tag) {
            boolean bl = tag.contains("LodestonePos");
            boolean bl2 = tag.contains("LodestoneDimension");
            if (bl && bl2) {
               Optional<RegistryKey<World>> optional = CompassItem.getLodestoneDimension(tag);
               if (optional.isPresent() && world.getRegistryKey() == optional.get()) {
                  return NbtHelper.toBlockPos(tag.getCompound("LodestonePos"));
               }
            }

            return null;
         }

         private double getItemFrameAngleOffset(ItemFrameEntity itemFrame) {
            Direction direction = itemFrame.getHorizontalFacing();
            int i = direction.getAxis().isVertical() ? 90 * direction.getDirection().offset() : 0;
            return (double)MathHelper.wrapDegrees(180 + direction.getHorizontal() * 90 + itemFrame.getRotation() * 45 + i);
         }

         private double getAngleToPos(Vec3d pos, Entity entity) {
            return Math.atan2(pos.getZ() - entity.getZ(), pos.getX() - entity.getX());
         }
      });
      register(Items.CROSSBOW, new Identifier("pull"), (itemStack, clientWorld, livingEntity) -> {
         if (livingEntity == null) {
            return 0.0F;
         } else {
            return CrossbowItem.isCharged(itemStack) ? 0.0F : (float)(itemStack.getMaxUseTime() - livingEntity.getItemUseTimeLeft()) / (float)CrossbowItem.getPullTime(itemStack);
         }
      });
      register(Items.CROSSBOW, new Identifier("pulling"), (itemStack, clientWorld, livingEntity) -> {
         return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
      });
      register(Items.CROSSBOW, new Identifier("charged"), (itemStack, clientWorld, livingEntity) -> {
         return livingEntity != null && CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
      });
      register(Items.CROSSBOW, new Identifier("firework"), (itemStack, clientWorld, livingEntity) -> {
         return livingEntity != null && CrossbowItem.isCharged(itemStack) && CrossbowItem.hasProjectile(itemStack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
      });
      register(Items.ELYTRA, new Identifier("broken"), (itemStack, clientWorld, livingEntity) -> {
         return ElytraItem.isUsable(itemStack) ? 0.0F : 1.0F;
      });
      register(Items.FISHING_ROD, new Identifier("cast"), (itemStack, clientWorld, livingEntity) -> {
         if (livingEntity == null) {
            return 0.0F;
         } else {
            boolean bl = livingEntity.getMainHandStack() == itemStack;
            boolean bl2 = livingEntity.getOffHandStack() == itemStack;
            if (livingEntity.getMainHandStack().getItem() instanceof FishingRodItem) {
               bl2 = false;
            }

            return (bl || bl2) && livingEntity instanceof PlayerEntity && ((PlayerEntity)livingEntity).fishHook != null ? 1.0F : 0.0F;
         }
      });
      register(Items.SHIELD, new Identifier("blocking"), (itemStack, clientWorld, livingEntity) -> {
         return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0F : 0.0F;
      });
      register(Items.TRIDENT, new Identifier("throwing"), (itemStack, clientWorld, livingEntity) -> {
         return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0F : 0.0F;
      });
   }

   @Environment(EnvType.CLIENT)
   static class AngleInterpolator {
      private double value;
      private double speed;
      private long lastUpdateTime;

      private AngleInterpolator() {
      }

      private boolean shouldUpdate(long time) {
         return this.lastUpdateTime != time;
      }

      private void update(long time, double d) {
         this.lastUpdateTime = time;
         double e = d - this.value;
         e = MathHelper.floorMod(e + 0.5D, 1.0D) - 0.5D;
         this.speed += e * 0.1D;
         this.speed *= 0.8D;
         this.value = MathHelper.floorMod(this.value + this.speed, 1.0D);
      }
   }
}
