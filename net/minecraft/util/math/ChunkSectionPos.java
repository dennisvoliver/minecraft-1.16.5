package net.minecraft.util.math;

import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.entity.Entity;
import net.minecraft.util.CuboidBlockIterator;

public class ChunkSectionPos extends Vec3i {
   private ChunkSectionPos(int x, int y, int z) {
      super(x, y, z);
   }

   /**
    * Creates a chunk section position from its x-, y- and z-coordinates.
    */
   public static ChunkSectionPos from(int x, int y, int z) {
      return new ChunkSectionPos(x, y, z);
   }

   public static ChunkSectionPos from(BlockPos pos) {
      return new ChunkSectionPos(getSectionCoord(pos.getX()), getSectionCoord(pos.getY()), getSectionCoord(pos.getZ()));
   }

   /**
    * Creates a chunk section position from a chunk position and the y-coordinate of the vertical section.
    */
   public static ChunkSectionPos from(ChunkPos chunkPos, int y) {
      return new ChunkSectionPos(chunkPos.x, y, chunkPos.z);
   }

   public static ChunkSectionPos from(Entity entity) {
      return new ChunkSectionPos(getSectionCoord(MathHelper.floor(entity.getX())), getSectionCoord(MathHelper.floor(entity.getY())), getSectionCoord(MathHelper.floor(entity.getZ())));
   }

   /**
    * Creates a chunk section position from its packed representation.
    * @see #asLong
    */
   public static ChunkSectionPos from(long packed) {
      return new ChunkSectionPos(unpackX(packed), unpackY(packed), unpackZ(packed));
   }

   /**
    * Offsets a packed chunk section position in the given direction.
    * @see #asLong
    */
   public static long offset(long packed, Direction direction) {
      return offset(packed, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
   }

   /**
    * Offsets a packed chunk section position by the given offsets.
    * @see #asLong
    */
   public static long offset(long packed, int x, int y, int z) {
      return asLong(unpackX(packed) + x, unpackY(packed) + y, unpackZ(packed) + z);
   }

   /**
    * Converts a world coordinate to the corresponding chunk-section coordinate.
    * 
    * @implNote This implementation returns {@code coord / 16}.
    */
   public static int getSectionCoord(int coord) {
      return coord >> 4;
   }

   /**
    * Converts a world coordinate to the local coordinate system (0-15) of its corresponding chunk section.
    */
   public static int getLocalCoord(int coord) {
      return coord & 15;
   }

   /**
    * Returns the local position of the given block position relative to
    * its respective chunk section, packed into a short.
    */
   public static short packLocal(BlockPos pos) {
      int i = getLocalCoord(pos.getX());
      int j = getLocalCoord(pos.getY());
      int k = getLocalCoord(pos.getZ());
      return (short)(i << 8 | k << 4 | j << 0);
   }

   /**
    * Gets the local x-coordinate from the given packed local position.
    * @see #packLocal
    */
   public static int unpackLocalX(short packedLocalPos) {
      return packedLocalPos >>> 8 & 15;
   }

   /**
    * Gets the local y-coordinate from the given packed local position.
    * @see #packLocal
    */
   public static int unpackLocalY(short packedLocalPos) {
      return packedLocalPos >>> 0 & 15;
   }

   /**
    * Gets the local z-coordinate from the given packed local position.
    * @see #packLocal
    */
   public static int unpackLocalZ(short packedLocalPos) {
      return packedLocalPos >>> 4 & 15;
   }

   /**
    * Gets the world x-coordinate of the given local position within this chunk section.
    * @see #packLocal
    */
   public int unpackBlockX(short packedLocalPos) {
      return this.getMinX() + unpackLocalX(packedLocalPos);
   }

   /**
    * Gets the world y-coordinate of the given local position within this chunk section.
    * @see #packLocal
    */
   public int unpackBlockY(short packedLocalPos) {
      return this.getMinY() + unpackLocalY(packedLocalPos);
   }

   /**
    * Gets the world z-coordinate of the given local position within this chunk section.
    * @see #packLocal
    */
   public int unpackBlockZ(short packedLocalPos) {
      return this.getMinZ() + unpackLocalZ(packedLocalPos);
   }

   /**
    * Gets the world position of the given local position within this chunk section.
    * @see #packLocal
    */
   public BlockPos unpackBlockPos(short packedLocalPos) {
      return new BlockPos(this.unpackBlockX(packedLocalPos), this.unpackBlockY(packedLocalPos), this.unpackBlockZ(packedLocalPos));
   }

   /**
    * Converts the given chunk section coordinate to the world coordinate system.
    * The returned coordinate will always be at the origin of the chunk section in world space.
    */
   public static int getBlockCoord(int sectionCoord) {
      return sectionCoord << 4;
   }

   /**
    * Gets the chunk section x-coordinate from the given packed chunk section coordinate.
    * @see #asLong
    */
   public static int unpackX(long packed) {
      return (int)(packed << 0 >> 42);
   }

   /**
    * Gets the chunk section y-coordinate from the given packed chunk section coordinate.
    * @see #asLong
    */
   public static int unpackY(long packed) {
      return (int)(packed << 44 >> 44);
   }

   /**
    * Gets the chunk section z-coordinate from the given packed chunk section coordinate.
    * @see #asLong
    */
   public static int unpackZ(long packed) {
      return (int)(packed << 22 >> 42);
   }

   public int getSectionX() {
      return this.getX();
   }

   public int getSectionY() {
      return this.getY();
   }

   public int getSectionZ() {
      return this.getZ();
   }

   public int getMinX() {
      return this.getSectionX() << 4;
   }

   public int getMinY() {
      return this.getSectionY() << 4;
   }

   public int getMinZ() {
      return this.getSectionZ() << 4;
   }

   public int getMaxX() {
      return (this.getSectionX() << 4) + 15;
   }

   public int getMaxY() {
      return (this.getSectionY() << 4) + 15;
   }

   public int getMaxZ() {
      return (this.getSectionZ() << 4) + 15;
   }

   /**
    * Gets the packed chunk section coordinate for a given packed {@link BlockPos}.
    * @see #asLong
    * @see BlockPos#asLong
    */
   public static long fromBlockPos(long blockPos) {
      return asLong(getSectionCoord(BlockPos.unpackLongX(blockPos)), getSectionCoord(BlockPos.unpackLongY(blockPos)), getSectionCoord(BlockPos.unpackLongZ(blockPos)));
   }

   /**
    * Gets the packed chunk section coordinate at y=0 for the same chunk as
    * the given packed chunk section coordinate.
    * @see #asLong
    */
   public static long withZeroY(long pos) {
      return pos & -1048576L;
   }

   public BlockPos getMinPos() {
      return new BlockPos(getBlockCoord(this.getSectionX()), getBlockCoord(this.getSectionY()), getBlockCoord(this.getSectionZ()));
   }

   public BlockPos getCenterPos() {
      int i = true;
      return this.getMinPos().add(8, 8, 8);
   }

   public ChunkPos toChunkPos() {
      return new ChunkPos(this.getSectionX(), this.getSectionZ());
   }

   public static long asLong(int x, int y, int z) {
      long l = 0L;
      l |= ((long)x & 4194303L) << 42;
      l |= ((long)y & 1048575L) << 0;
      l |= ((long)z & 4194303L) << 20;
      return l;
   }

   public long asLong() {
      return asLong(this.getSectionX(), this.getSectionY(), this.getSectionZ());
   }

   public Stream<BlockPos> streamBlocks() {
      return BlockPos.stream(this.getMinX(), this.getMinY(), this.getMinZ(), this.getMaxX(), this.getMaxY(), this.getMaxZ());
   }

   public static Stream<ChunkSectionPos> stream(ChunkSectionPos center, int radius) {
      int i = center.getSectionX();
      int j = center.getSectionY();
      int k = center.getSectionZ();
      return stream(i - radius, j - radius, k - radius, i + radius, j + radius, k + radius);
   }

   public static Stream<ChunkSectionPos> stream(ChunkPos center, int radius) {
      int i = center.x;
      int j = center.z;
      return stream(i - radius, 0, j - radius, i + radius, 15, j + radius);
   }

   public static Stream<ChunkSectionPos> stream(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {
      return StreamSupport.stream(new AbstractSpliterator<ChunkSectionPos>((long)((maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)), 64) {
         final CuboidBlockIterator iterator = new CuboidBlockIterator(minX, minY, minZ, maxX, maxY, maxZ);

         public boolean tryAdvance(Consumer<? super ChunkSectionPos> consumer) {
            if (this.iterator.step()) {
               consumer.accept(new ChunkSectionPos(this.iterator.getX(), this.iterator.getY(), this.iterator.getZ()));
               return true;
            } else {
               return false;
            }
         }
      }, false);
   }
}
