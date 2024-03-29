package net.minecraft.client.sound;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

@Environment(EnvType.CLIENT)
public class EntityTrackingSoundInstance extends MovingSoundInstance {
   private final Entity entity;

   public EntityTrackingSoundInstance(SoundEvent sound, SoundCategory soundCategory, Entity entity) {
      this(sound, soundCategory, 1.0F, 1.0F, entity);
   }

   public EntityTrackingSoundInstance(SoundEvent sound, SoundCategory category, float volume, float pitch, Entity entity) {
      super(sound, category);
      this.volume = volume;
      this.pitch = pitch;
      this.entity = entity;
      this.x = (double)((float)this.entity.getX());
      this.y = (double)((float)this.entity.getY());
      this.z = (double)((float)this.entity.getZ());
   }

   public boolean canPlay() {
      return !this.entity.isSilent();
   }

   public void tick() {
      if (this.entity.removed) {
         this.setDone();
      } else {
         this.x = (double)((float)this.entity.getX());
         this.y = (double)((float)this.entity.getY());
         this.z = (double)((float)this.entity.getZ());
      }
   }
}
