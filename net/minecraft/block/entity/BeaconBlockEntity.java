package net.minecraft.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

public class BeaconBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Tickable {
   public static final StatusEffect[][] EFFECTS_BY_LEVEL;
   private static final Set<StatusEffect> EFFECTS;
   private List<BeaconBlockEntity.BeamSegment> beamSegments = Lists.newArrayList();
   private List<BeaconBlockEntity.BeamSegment> field_19178 = Lists.newArrayList();
   private int level;
   private int field_19179 = -1;
   @Nullable
   private StatusEffect primary;
   @Nullable
   private StatusEffect secondary;
   @Nullable
   private Text customName;
   private ContainerLock lock;
   private final PropertyDelegate propertyDelegate;

   public BeaconBlockEntity() {
      super(BlockEntityType.BEACON);
      this.lock = ContainerLock.EMPTY;
      this.propertyDelegate = new PropertyDelegate() {
         public int get(int index) {
            switch(index) {
            case 0:
               return BeaconBlockEntity.this.level;
            case 1:
               return StatusEffect.getRawId(BeaconBlockEntity.this.primary);
            case 2:
               return StatusEffect.getRawId(BeaconBlockEntity.this.secondary);
            default:
               return 0;
            }
         }

         public void set(int index, int value) {
            switch(index) {
            case 0:
               BeaconBlockEntity.this.level = value;
               break;
            case 1:
               if (!BeaconBlockEntity.this.world.isClient && !BeaconBlockEntity.this.beamSegments.isEmpty()) {
                  BeaconBlockEntity.this.playSound(SoundEvents.BLOCK_BEACON_POWER_SELECT);
               }

               BeaconBlockEntity.this.primary = BeaconBlockEntity.getPotionEffectById(value);
               break;
            case 2:
               BeaconBlockEntity.this.secondary = BeaconBlockEntity.getPotionEffectById(value);
            }

         }

         public int size() {
            return 3;
         }
      };
   }

   public void tick() {
      int i = this.pos.getX();
      int j = this.pos.getY();
      int k = this.pos.getZ();
      BlockPos blockPos2;
      if (this.field_19179 < j) {
         blockPos2 = this.pos;
         this.field_19178 = Lists.newArrayList();
         this.field_19179 = blockPos2.getY() - 1;
      } else {
         blockPos2 = new BlockPos(i, this.field_19179 + 1, k);
      }

      BeaconBlockEntity.BeamSegment beamSegment = this.field_19178.isEmpty() ? null : (BeaconBlockEntity.BeamSegment)this.field_19178.get(this.field_19178.size() - 1);
      int l = this.world.getTopY(Heightmap.Type.WORLD_SURFACE, i, k);

      int n;
      for(n = 0; n < 10 && blockPos2.getY() <= l; ++n) {
         BlockState blockState = this.world.getBlockState(blockPos2);
         Block block = blockState.getBlock();
         if (block instanceof Stainable) {
            float[] fs = ((Stainable)block).getColor().getColorComponents();
            if (this.field_19178.size() <= 1) {
               beamSegment = new BeaconBlockEntity.BeamSegment(fs);
               this.field_19178.add(beamSegment);
            } else if (beamSegment != null) {
               if (Arrays.equals(fs, beamSegment.color)) {
                  beamSegment.increaseHeight();
               } else {
                  beamSegment = new BeaconBlockEntity.BeamSegment(new float[]{(beamSegment.color[0] + fs[0]) / 2.0F, (beamSegment.color[1] + fs[1]) / 2.0F, (beamSegment.color[2] + fs[2]) / 2.0F});
                  this.field_19178.add(beamSegment);
               }
            }
         } else {
            if (beamSegment == null || blockState.getOpacity(this.world, blockPos2) >= 15 && block != Blocks.BEDROCK) {
               this.field_19178.clear();
               this.field_19179 = l;
               break;
            }

            beamSegment.increaseHeight();
         }

         blockPos2 = blockPos2.up();
         ++this.field_19179;
      }

      n = this.level;
      if (this.world.getTime() % 80L == 0L) {
         if (!this.beamSegments.isEmpty()) {
            this.updateLevel(i, j, k);
         }

         if (this.level > 0 && !this.beamSegments.isEmpty()) {
            this.applyPlayerEffects();
            this.playSound(SoundEvents.BLOCK_BEACON_AMBIENT);
         }
      }

      if (this.field_19179 >= l) {
         this.field_19179 = -1;
         boolean bl = n > 0;
         this.beamSegments = this.field_19178;
         if (!this.world.isClient) {
            boolean bl2 = this.level > 0;
            if (!bl && bl2) {
               this.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE);
               Iterator var14 = this.world.getNonSpectatingEntities(ServerPlayerEntity.class, (new Box((double)i, (double)j, (double)k, (double)i, (double)(j - 4), (double)k)).expand(10.0D, 5.0D, 10.0D)).iterator();

               while(var14.hasNext()) {
                  ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var14.next();
                  Criteria.CONSTRUCT_BEACON.trigger(serverPlayerEntity, this);
               }
            } else if (bl && !bl2) {
               this.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE);
            }
         }
      }

   }

   private void updateLevel(int x, int y, int z) {
      this.level = 0;

      for(int i = 1; i <= 4; this.level = i++) {
         int j = y - i;
         if (j < 0) {
            break;
         }

         boolean bl = true;

         for(int k = x - i; k <= x + i && bl; ++k) {
            for(int l = z - i; l <= z + i; ++l) {
               if (!this.world.getBlockState(new BlockPos(k, j, l)).isIn(BlockTags.BEACON_BASE_BLOCKS)) {
                  bl = false;
                  break;
               }
            }
         }

         if (!bl) {
            break;
         }
      }

   }

   public void markRemoved() {
      this.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE);
      super.markRemoved();
   }

   private void applyPlayerEffects() {
      if (!this.world.isClient && this.primary != null) {
         double d = (double)(this.level * 10 + 10);
         int i = 0;
         if (this.level >= 4 && this.primary == this.secondary) {
            i = 1;
         }

         int j = (9 + this.level * 2) * 20;
         Box box = (new Box(this.pos)).expand(d).stretch(0.0D, (double)this.world.getHeight(), 0.0D);
         List<PlayerEntity> list = this.world.getNonSpectatingEntities(PlayerEntity.class, box);
         Iterator var7 = list.iterator();

         PlayerEntity playerEntity2;
         while(var7.hasNext()) {
            playerEntity2 = (PlayerEntity)var7.next();
            playerEntity2.addStatusEffect(new StatusEffectInstance(this.primary, j, i, true, true));
         }

         if (this.level >= 4 && this.primary != this.secondary && this.secondary != null) {
            var7 = list.iterator();

            while(var7.hasNext()) {
               playerEntity2 = (PlayerEntity)var7.next();
               playerEntity2.addStatusEffect(new StatusEffectInstance(this.secondary, j, 0, true, true));
            }
         }

      }
   }

   public void playSound(SoundEvent soundEvent) {
      this.world.playSound((PlayerEntity)null, this.pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
   }

   @Environment(EnvType.CLIENT)
   public List<BeaconBlockEntity.BeamSegment> getBeamSegments() {
      return (List)(this.level == 0 ? ImmutableList.of() : this.beamSegments);
   }

   public int getLevel() {
      return this.level;
   }

   @Nullable
   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return new BlockEntityUpdateS2CPacket(this.pos, 3, this.toInitialChunkDataNbt());
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.writeNbt(new NbtCompound());
   }

   @Environment(EnvType.CLIENT)
   public double getRenderDistance() {
      return 256.0D;
   }

   @Nullable
   private static StatusEffect getPotionEffectById(int id) {
      StatusEffect statusEffect = StatusEffect.byRawId(id);
      return EFFECTS.contains(statusEffect) ? statusEffect : null;
   }

   public void fromTag(BlockState state, NbtCompound tag) {
      super.fromTag(state, tag);
      this.primary = getPotionEffectById(tag.getInt("Primary"));
      this.secondary = getPotionEffectById(tag.getInt("Secondary"));
      if (tag.contains("CustomName", 8)) {
         this.customName = Text.Serializer.fromJson(tag.getString("CustomName"));
      }

      this.lock = ContainerLock.fromNbt(tag);
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      nbt.putInt("Primary", StatusEffect.getRawId(this.primary));
      nbt.putInt("Secondary", StatusEffect.getRawId(this.secondary));
      nbt.putInt("Levels", this.level);
      if (this.customName != null) {
         nbt.putString("CustomName", Text.Serializer.toJson(this.customName));
      }

      this.lock.writeNbt(nbt);
      return nbt;
   }

   public void setCustomName(@Nullable Text customName) {
      this.customName = customName;
   }

   @Nullable
   public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
      return LockableContainerBlockEntity.checkUnlocked(playerEntity, this.lock, this.getDisplayName()) ? new BeaconScreenHandler(i, playerInventory, this.propertyDelegate, ScreenHandlerContext.create(this.world, this.getPos())) : null;
   }

   public Text getDisplayName() {
      return (Text)(this.customName != null ? this.customName : new TranslatableText("container.beacon"));
   }

   static {
      EFFECTS_BY_LEVEL = new StatusEffect[][]{{StatusEffects.SPEED, StatusEffects.HASTE}, {StatusEffects.RESISTANCE, StatusEffects.JUMP_BOOST}, {StatusEffects.STRENGTH}, {StatusEffects.REGENERATION}};
      EFFECTS = (Set)Arrays.stream(EFFECTS_BY_LEVEL).flatMap(Arrays::stream).collect(Collectors.toSet());
   }

   public static class BeamSegment {
      private final float[] color;
      private int height;

      public BeamSegment(float[] color) {
         this.color = color;
         this.height = 1;
      }

      protected void increaseHeight() {
         ++this.height;
      }

      @Environment(EnvType.CLIENT)
      public float[] getColor() {
         return this.color;
      }

      @Environment(EnvType.CLIENT)
      public int getHeight() {
         return this.height;
      }
   }
}
