package net.minecraft.world;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

/**
 * A world view or {@link World}'s superinterface that exposes access to
 * a registry manager.
 * 
 * @see #getRegistryManager()
 */
public interface RegistryWorldView extends EntityView, WorldView, ModifiableTestableWorld {
   default Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box, Predicate<Entity> predicate) {
      return EntityView.super.getEntityCollisions(entity, box, predicate);
   }

   default boolean intersectsEntities(@Nullable Entity except, VoxelShape shape) {
      return EntityView.super.intersectsEntities(except, shape);
   }

   default BlockPos getTopPosition(Heightmap.Type type, BlockPos pos) {
      return WorldView.super.getTopPosition(type, pos);
   }

   DynamicRegistryManager getRegistryManager();

   default Optional<RegistryKey<Biome>> getBiomeKey(BlockPos pos) {
      return this.getRegistryManager().get(Registry.BIOME_KEY).getKey(this.getBiome(pos));
   }
}
