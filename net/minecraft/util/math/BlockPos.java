package net.minecraft.util.math;

import com.google.common.collect.AbstractIterator;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Represents the position of a block in a three-dimensional volume.
 * 
 * <p>The position is integer-valued.
 * 
 * <p>A block position may be mutable; hence, when using block positions
 * obtained from other places as map keys, etc., you should call {@link
 * #toImmutable()} to obtain an immutable block position.
 */
@Unmodifiable
public class BlockPos extends Vec3i {
   public static final Codec<BlockPos> CODEC;
   private static final Logger LOGGER;
   /**
    * The block position which x, y, and z values are all zero.
    */
   public static final BlockPos ORIGIN;
   private static final int SIZE_BITS_X;
   private static final int SIZE_BITS_Z;
   private static final int SIZE_BITS_Y;
   private static final long BITS_X;
   private static final long BITS_Y;
   private static final long BITS_Z;
   private static final int BIT_SHIFT_Z;
   private static final int BIT_SHIFT_X;

   public BlockPos(int i, int j, int k) {
      super(i, j, k);
   }

   public BlockPos(double d, double e, double f) {
      super(d, e, f);
   }

   public BlockPos(Vec3d pos) {
      this(pos.x, pos.y, pos.z);
   }

   public BlockPos(Position pos) {
      this(pos.getX(), pos.getY(), pos.getZ());
   }

   public BlockPos(Vec3i pos) {
      this(pos.getX(), pos.getY(), pos.getZ());
   }

   public static long offset(long value, Direction direction) {
      return add(value, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
   }

   public static long add(long value, int x, int y, int z) {
      return asLong(unpackLongX(value) + x, unpackLongY(value) + y, unpackLongZ(value) + z);
   }

   public static int unpackLongX(long packedPos) {
      return (int)(packedPos << 64 - BIT_SHIFT_X - SIZE_BITS_X >> 64 - SIZE_BITS_X);
   }

   public static int unpackLongY(long packedPos) {
      return (int)(packedPos << 64 - SIZE_BITS_Y >> 64 - SIZE_BITS_Y);
   }

   public static int unpackLongZ(long packedPos) {
      return (int)(packedPos << 64 - BIT_SHIFT_Z - SIZE_BITS_Z >> 64 - SIZE_BITS_Z);
   }

   public static BlockPos fromLong(long packedPos) {
      return new BlockPos(unpackLongX(packedPos), unpackLongY(packedPos), unpackLongZ(packedPos));
   }

   public long asLong() {
      return asLong(this.getX(), this.getY(), this.getZ());
   }

   public static long asLong(int x, int y, int z) {
      long l = 0L;
      l |= ((long)x & BITS_X) << BIT_SHIFT_X;
      l |= ((long)y & BITS_Y) << 0;
      l |= ((long)z & BITS_Z) << BIT_SHIFT_Z;
      return l;
   }

   public static long removeChunkSectionLocalY(long y) {
      return y & -16L;
   }

   public BlockPos add(double x, double y, double z) {
      return x == 0.0D && y == 0.0D && z == 0.0D ? this : new BlockPos((double)this.getX() + x, (double)this.getY() + y, (double)this.getZ() + z);
   }

   public BlockPos add(int x, int y, int z) {
      return x == 0 && y == 0 && z == 0 ? this : new BlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
   }

   public BlockPos add(Vec3i pos) {
      return this.add(pos.getX(), pos.getY(), pos.getZ());
   }

   public BlockPos subtract(Vec3i pos) {
      return this.add(-pos.getX(), -pos.getY(), -pos.getZ());
   }

   public BlockPos up() {
      return this.offset(Direction.UP);
   }

   public BlockPos up(int distance) {
      return this.offset(Direction.UP, distance);
   }

   public BlockPos down() {
      return this.offset(Direction.DOWN);
   }

   public BlockPos down(int i) {
      return this.offset(Direction.DOWN, i);
   }

   public BlockPos north() {
      return this.offset(Direction.NORTH);
   }

   public BlockPos north(int distance) {
      return this.offset(Direction.NORTH, distance);
   }

   public BlockPos south() {
      return this.offset(Direction.SOUTH);
   }

   public BlockPos south(int distance) {
      return this.offset(Direction.SOUTH, distance);
   }

   public BlockPos west() {
      return this.offset(Direction.WEST);
   }

   public BlockPos west(int distance) {
      return this.offset(Direction.WEST, distance);
   }

   public BlockPos east() {
      return this.offset(Direction.EAST);
   }

   public BlockPos east(int distance) {
      return this.offset(Direction.EAST, distance);
   }

   public BlockPos offset(Direction direction) {
      return new BlockPos(this.getX() + direction.getOffsetX(), this.getY() + direction.getOffsetY(), this.getZ() + direction.getOffsetZ());
   }

   public BlockPos offset(Direction direction, int i) {
      return i == 0 ? this : new BlockPos(this.getX() + direction.getOffsetX() * i, this.getY() + direction.getOffsetY() * i, this.getZ() + direction.getOffsetZ() * i);
   }

   public BlockPos offset(Direction.Axis axis, int distance) {
      if (distance == 0) {
         return this;
      } else {
         int i = axis == Direction.Axis.X ? distance : 0;
         int j = axis == Direction.Axis.Y ? distance : 0;
         int k = axis == Direction.Axis.Z ? distance : 0;
         return new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
      }
   }

   public BlockPos rotate(BlockRotation rotation) {
      switch(rotation) {
      case NONE:
      default:
         return this;
      case CLOCKWISE_90:
         return new BlockPos(-this.getZ(), this.getY(), this.getX());
      case CLOCKWISE_180:
         return new BlockPos(-this.getX(), this.getY(), -this.getZ());
      case COUNTERCLOCKWISE_90:
         return new BlockPos(this.getZ(), this.getY(), -this.getX());
      }
   }

   public BlockPos crossProduct(Vec3i pos) {
      return new BlockPos(this.getY() * pos.getZ() - this.getZ() * pos.getY(), this.getZ() * pos.getX() - this.getX() * pos.getZ(), this.getX() * pos.getY() - this.getY() * pos.getX());
   }

   /**
    * Returns an immutable block position with the same x, y, and z as this
    * position.
    * 
    * <p>This method should be called when a block position is used as map
    * keys as to prevent side effects of mutations of mutable block positions.
    */
   public BlockPos toImmutable() {
      return this;
   }

   /**
    * Returns a mutable copy of this block position.
    * 
    * <p>If this block position is a mutable one, mutation to this block
    * position won't affect the returned position.
    */
   public BlockPos.Mutable mutableCopy() {
      return new BlockPos.Mutable(this.getX(), this.getY(), this.getZ());
   }

   /**
    * Iterates through {@code count} random block positions in the given area.
    * 
    * <p>The iterator yields positions in no specific order. The same position
    * may be returned multiple times by the iterator.
    * 
    * @param random the {@link Random} object used to compute new positions
    * @param count the number of positions to iterate
    * @param minX the minimum x value for returned positions
    * @param minY the minimum y value for returned positions
    * @param minZ the minimum z value for returned positions
    * @param maxX the maximum x value for returned positions
    * @param maxY the maximum y value for returned positions
    * @param maxZ the maximum z value for returned positions
    */
   public static Iterable<BlockPos> iterateRandomly(Random random, int count, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      int i = maxX - minX + 1;
      int j = maxY - minY + 1;
      int k = maxZ - minZ + 1;
      return () -> {
         return new AbstractIterator<BlockPos>() {
            final BlockPos.Mutable pos = new BlockPos.Mutable();
            int remaining = i;

            protected BlockPos computeNext() {
               if (this.remaining <= 0) {
                  return (BlockPos)this.endOfData();
               } else {
                  BlockPos blockPos = this.pos.set(j + random.nextInt(k), l + random.nextInt(m), n + random.nextInt(o));
                  --this.remaining;
                  return blockPos;
               }
            }
         };
      };
   }

   /**
    * Iterates block positions around the {@code center}. The iteration order
    * is mainly based on the manhattan distance of the position from the
    * center.
    * 
    * <p>For the same manhattan distance, the positions are iterated by y
    * offset, from negative to positive. For the same y offset, the positions
    * are iterated by x offset, from negative to positive. For the two
    * positions with the same x and y offsets and the same manhattan distance,
    * the one with a positive z offset is visited first before the one with a
    * negative z offset.
    * 
    * @param center the center of iteration
    * @param rangeX the maximum x difference from the center
    * @param rangeY the maximum y difference from the center
    * @param rangeZ the maximum z difference from the center
    */
   public static Iterable<BlockPos> iterateOutwards(BlockPos center, int rangeX, int rangeY, int rangeZ) {
      int i = rangeX + rangeY + rangeZ;
      int j = center.getX();
      int k = center.getY();
      int l = center.getZ();
      return () -> {
         return new AbstractIterator<BlockPos>() {
            private final BlockPos.Mutable pos = new BlockPos.Mutable();
            private int manhattanDistance;
            private int limitX;
            private int limitY;
            private int dx;
            private int dy;
            private boolean field_23379;

            protected BlockPos computeNext() {
               if (this.field_23379) {
                  this.field_23379 = false;
                  this.pos.setZ(i - (this.pos.getZ() - i));
                  return this.pos;
               } else {
                  BlockPos.Mutable blockPos;
                  for(blockPos = null; blockPos == null; ++this.dy) {
                     if (this.dy > this.limitY) {
                        ++this.dx;
                        if (this.dx > this.limitX) {
                           ++this.manhattanDistance;
                           if (this.manhattanDistance > j) {
                              return (BlockPos)this.endOfData();
                           }

                           this.limitX = Math.min(k, this.manhattanDistance);
                           this.dx = -this.limitX;
                        }

                        this.limitY = Math.min(l, this.manhattanDistance - Math.abs(this.dx));
                        this.dy = -this.limitY;
                     }

                     int ix = this.dx;
                     int jx = this.dy;
                     int kx = this.manhattanDistance - Math.abs(ix) - Math.abs(jx);
                     if (kx <= m) {
                        this.field_23379 = kx != 0;
                        blockPos = this.pos.set(n + ix, o + jx, i + kx);
                     }
                  }

                  return blockPos;
               }
            }
         };
      };
   }

   public static Optional<BlockPos> findClosest(BlockPos pos, int horizontalRange, int verticalRange, Predicate<BlockPos> condition) {
      return streamOutwards(pos, horizontalRange, verticalRange, horizontalRange).filter(condition).findFirst();
   }

   public static Stream<BlockPos> streamOutwards(BlockPos center, int maxX, int maxY, int maxZ) {
      return StreamSupport.stream(iterateOutwards(center, maxX, maxY, maxZ).spliterator(), false);
   }

   public static Iterable<BlockPos> iterate(BlockPos start, BlockPos end) {
      return iterate(Math.min(start.getX(), end.getX()), Math.min(start.getY(), end.getY()), Math.min(start.getZ(), end.getZ()), Math.max(start.getX(), end.getX()), Math.max(start.getY(), end.getY()), Math.max(start.getZ(), end.getZ()));
   }

   public static Stream<BlockPos> stream(BlockPos start, BlockPos end) {
      return StreamSupport.stream(iterate(start, end).spliterator(), false);
   }

   public static Stream<BlockPos> stream(BlockBox box) {
      return stream(Math.min(box.minX, box.maxX), Math.min(box.minY, box.maxY), Math.min(box.minZ, box.maxZ), Math.max(box.minX, box.maxX), Math.max(box.minY, box.maxY), Math.max(box.minZ, box.maxZ));
   }

   public static Stream<BlockPos> stream(Box box) {
      return stream(MathHelper.floor(box.minX), MathHelper.floor(box.minY), MathHelper.floor(box.minZ), MathHelper.floor(box.maxX), MathHelper.floor(box.maxY), MathHelper.floor(box.maxZ));
   }

   public static Stream<BlockPos> stream(int startX, int startY, int startZ, int endX, int endY, int endZ) {
      return StreamSupport.stream(iterate(startX, startY, startZ, endX, endY, endZ).spliterator(), false);
   }

   public static Iterable<BlockPos> iterate(int startX, int startY, int startZ, int endX, int endY, int endZ) {
      int i = endX - startX + 1;
      int j = endY - startY + 1;
      int k = endZ - startZ + 1;
      int l = i * j * k;
      return () -> {
         return new AbstractIterator<BlockPos>() {
            private final BlockPos.Mutable pos = new BlockPos.Mutable();
            private int index;

            protected BlockPos computeNext() {
               if (this.index == i) {
                  return (BlockPos)this.endOfData();
               } else {
                  int ix = this.index % j;
                  int jx = this.index / j;
                  int kx = jx % k;
                  int lx = jx / k;
                  ++this.index;
                  return this.pos.set(l + ix, m + kx, n + lx);
               }
            }
         };
      };
   }

   public static Iterable<BlockPos.Mutable> method_30512(BlockPos blockPos, int i, Direction direction, Direction direction2) {
      Validate.validState(direction.getAxis() != direction2.getAxis(), "The two directions cannot be on the same axis");
      return () -> {
         return new AbstractIterator<BlockPos.Mutable>() {
            private final Direction[] directions = new Direction[]{direction, direction2, direction.getOpposite(), direction2.getOpposite()};
            private final BlockPos.Mutable pos = blockPos.mutableCopy().move(direction2);
            private final int field_25905 = 4 * i;
            private int field_25906 = -1;
            private int field_25907;
            private int field_25908;
            private int field_25909;
            private int field_25910;
            private int field_25911;

            {
               this.field_25909 = this.pos.getX();
               this.field_25910 = this.pos.getY();
               this.field_25911 = this.pos.getZ();
            }

            protected BlockPos.Mutable computeNext() {
               this.pos.set(this.field_25909, this.field_25910, this.field_25911).move(this.directions[(this.field_25906 + 4) % 4]);
               this.field_25909 = this.pos.getX();
               this.field_25910 = this.pos.getY();
               this.field_25911 = this.pos.getZ();
               if (this.field_25908 >= this.field_25907) {
                  if (this.field_25906 >= this.field_25905) {
                     return (BlockPos.Mutable)this.endOfData();
                  }

                  ++this.field_25906;
                  this.field_25908 = 0;
                  this.field_25907 = this.field_25906 / 2 + 1;
               }

               ++this.field_25908;
               return this.pos;
            }
         };
      };
   }

   static {
      CODEC = Codec.INT_STREAM.comapFlatMap((stream) -> {
         return Util.toArray(stream, 3).map((values) -> {
            return new BlockPos(values[0], values[1], values[2]);
         });
      }, (pos) -> {
         return IntStream.of(new int[]{pos.getX(), pos.getY(), pos.getZ()});
      }).stable();
      LOGGER = LogManager.getLogger();
      ORIGIN = new BlockPos(0, 0, 0);
      SIZE_BITS_X = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
      SIZE_BITS_Z = SIZE_BITS_X;
      SIZE_BITS_Y = 64 - SIZE_BITS_X - SIZE_BITS_Z;
      BITS_X = (1L << SIZE_BITS_X) - 1L;
      BITS_Y = (1L << SIZE_BITS_Y) - 1L;
      BITS_Z = (1L << SIZE_BITS_Z) - 1L;
      BIT_SHIFT_Z = SIZE_BITS_Y;
      BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_Z;
   }

   public static class Mutable extends BlockPos {
      public Mutable() {
         this(0, 0, 0);
      }

      public Mutable(int i, int j, int k) {
         super(i, j, k);
      }

      public Mutable(double d, double e, double f) {
         this(MathHelper.floor(d), MathHelper.floor(e), MathHelper.floor(f));
      }

      public BlockPos add(double x, double y, double z) {
         return super.add(x, y, z).toImmutable();
      }

      public BlockPos add(int x, int y, int z) {
         return super.add(x, y, z).toImmutable();
      }

      public BlockPos offset(Direction direction, int i) {
         return super.offset(direction, i).toImmutable();
      }

      public BlockPos offset(Direction.Axis axis, int distance) {
         return super.offset(axis, distance).toImmutable();
      }

      public BlockPos rotate(BlockRotation rotation) {
         return super.rotate(rotation).toImmutable();
      }

      /**
       * Sets the x, y, and z of this mutable block position.
       */
      public BlockPos.Mutable set(int x, int y, int z) {
         this.setX(x);
         this.setY(y);
         this.setZ(z);
         return this;
      }

      public BlockPos.Mutable set(double x, double y, double z) {
         return this.set(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
      }

      public BlockPos.Mutable set(Vec3i pos) {
         return this.set(pos.getX(), pos.getY(), pos.getZ());
      }

      public BlockPos.Mutable set(long pos) {
         return this.set(unpackLongX(pos), unpackLongY(pos), unpackLongZ(pos));
      }

      public BlockPos.Mutable set(AxisCycleDirection axis, int x, int y, int z) {
         return this.set(axis.choose(x, y, z, Direction.Axis.X), axis.choose(x, y, z, Direction.Axis.Y), axis.choose(x, y, z, Direction.Axis.Z));
      }

      /**
       * Sets this mutable block position to the offset position of the given
       * pos by the given direction.
       */
      public BlockPos.Mutable set(Vec3i pos, Direction direction) {
         return this.set(pos.getX() + direction.getOffsetX(), pos.getY() + direction.getOffsetY(), pos.getZ() + direction.getOffsetZ());
      }

      /**
       * Sets this mutable block position to the sum of the given position and the
       * given x, y, and z.
       */
      public BlockPos.Mutable set(Vec3i pos, int x, int y, int z) {
         return this.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
      }

      /**
       * Moves this mutable block position by 1 block in the given direction.
       */
      public BlockPos.Mutable move(Direction direction) {
         return this.move(direction, 1);
      }

      /**
       * Moves this mutable block position by the given distance in the given
       * direction.
       */
      public BlockPos.Mutable move(Direction direction, int distance) {
         return this.set(this.getX() + direction.getOffsetX() * distance, this.getY() + direction.getOffsetY() * distance, this.getZ() + direction.getOffsetZ() * distance);
      }

      /**
       * Moves the mutable block position by the delta x, y, and z provided.
       */
      public BlockPos.Mutable move(int dx, int dy, int dz) {
         return this.set(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
      }

      public BlockPos.Mutable move(Vec3i vec) {
         return this.set(this.getX() + vec.getX(), this.getY() + vec.getY(), this.getZ() + vec.getZ());
      }

      /**
       * Clamps the component corresponding to the given {@code axis} between {@code min} and {@code max}.
       */
      public BlockPos.Mutable clamp(Direction.Axis axis, int min, int max) {
         switch(axis) {
         case X:
            return this.set(MathHelper.clamp(this.getX(), min, max), this.getY(), this.getZ());
         case Y:
            return this.set(this.getX(), MathHelper.clamp(this.getY(), min, max), this.getZ());
         case Z:
            return this.set(this.getX(), this.getY(), MathHelper.clamp(this.getZ(), min, max));
         default:
            throw new IllegalStateException("Unable to clamp axis " + axis);
         }
      }

      public void setX(int x) {
         super.setX(x);
      }

      public void setY(int y) {
         super.setY(y);
      }

      public void setZ(int z) {
         super.setZ(z);
      }

      public BlockPos toImmutable() {
         return new BlockPos(this);
      }
   }
}
