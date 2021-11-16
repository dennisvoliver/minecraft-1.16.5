package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MobSpawnerBlockEntity extends BlockEntity implements Tickable {
   private final MobSpawnerLogic logic = new MobSpawnerLogic() {
      public void sendStatus(int status) {
         MobSpawnerBlockEntity.this.world.addSyncedBlockEvent(MobSpawnerBlockEntity.this.pos, Blocks.SPAWNER, status, 0);
      }

      public World getWorld() {
         return MobSpawnerBlockEntity.this.world;
      }

      public BlockPos getPos() {
         return MobSpawnerBlockEntity.this.pos;
      }

      public void setSpawnEntry(MobSpawnerEntry spawnEntry) {
         super.setSpawnEntry(spawnEntry);
         if (this.getWorld() != null) {
            BlockState blockState = this.getWorld().getBlockState(this.getPos());
            this.getWorld().updateListeners(MobSpawnerBlockEntity.this.pos, blockState, blockState, 4);
         }

      }
   };

   public MobSpawnerBlockEntity() {
      super(BlockEntityType.MOB_SPAWNER);
   }

   public void fromTag(BlockState state, NbtCompound tag) {
      super.fromTag(state, tag);
      this.logic.fromTag(tag);
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      this.logic.toTag(nbt);
      return nbt;
   }

   public void tick() {
      this.logic.update();
   }

   @Nullable
   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return new BlockEntityUpdateS2CPacket(this.pos, 1, this.toInitialChunkDataNbt());
   }

   public NbtCompound toInitialChunkDataNbt() {
      NbtCompound nbtCompound = this.writeNbt(new NbtCompound());
      nbtCompound.remove("SpawnPotentials");
      return nbtCompound;
   }

   public boolean onSyncedBlockEvent(int type, int data) {
      return this.logic.method_8275(type) ? true : super.onSyncedBlockEvent(type, data);
   }

   public boolean copyItemDataRequiresOperator() {
      return true;
   }

   public MobSpawnerLogic getLogic() {
      return this.logic;
   }
}
