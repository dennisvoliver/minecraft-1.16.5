package net.minecraft.world.border;

import com.google.common.collect.Lists;
import com.mojang.serialization.DynamicLike;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class WorldBorder {
   private final List<WorldBorderListener> listeners = Lists.newArrayList();
   private double damagePerBlock = 0.2D;
   private double safeZone = 5.0D;
   private int warningTime = 15;
   private int warningBlocks = 5;
   private double centerX;
   private double centerZ;
   private int maxRadius = 29999984;
   private WorldBorder.Area area = new WorldBorder.StaticArea(6.0E7D);
   public static final WorldBorder.Properties DEFAULT_BORDER = new WorldBorder.Properties(0.0D, 0.0D, 0.2D, 5.0D, 5, 15, 6.0E7D, 0L, 0.0D);

   public boolean contains(BlockPos pos) {
      return (double)(pos.getX() + 1) > this.getBoundWest() && (double)pos.getX() < this.getBoundEast() && (double)(pos.getZ() + 1) > this.getBoundNorth() && (double)pos.getZ() < this.getBoundSouth();
   }

   public boolean contains(ChunkPos pos) {
      return (double)pos.getEndX() > this.getBoundWest() && (double)pos.getStartX() < this.getBoundEast() && (double)pos.getEndZ() > this.getBoundNorth() && (double)pos.getStartZ() < this.getBoundSouth();
   }

   public boolean contains(Box box) {
      return box.maxX > this.getBoundWest() && box.minX < this.getBoundEast() && box.maxZ > this.getBoundNorth() && box.minZ < this.getBoundSouth();
   }

   public double getDistanceInsideBorder(Entity entity) {
      return this.getDistanceInsideBorder(entity.getX(), entity.getZ());
   }

   public VoxelShape asVoxelShape() {
      return this.area.asVoxelShape();
   }

   public double getDistanceInsideBorder(double x, double z) {
      double d = z - this.getBoundNorth();
      double e = this.getBoundSouth() - z;
      double f = x - this.getBoundWest();
      double g = this.getBoundEast() - x;
      double h = Math.min(f, g);
      h = Math.min(h, d);
      return Math.min(h, e);
   }

   @Environment(EnvType.CLIENT)
   public WorldBorderStage getStage() {
      return this.area.getStage();
   }

   public double getBoundWest() {
      return this.area.getBoundWest();
   }

   public double getBoundNorth() {
      return this.area.getBoundNorth();
   }

   public double getBoundEast() {
      return this.area.getBoundEast();
   }

   public double getBoundSouth() {
      return this.area.getBoundSouth();
   }

   public double getCenterX() {
      return this.centerX;
   }

   public double getCenterZ() {
      return this.centerZ;
   }

   /**
    * Sets the {@code x} and {@code z} coordinates of the center of this border,
    * and notifies its area and all listeners.
    */
   public void setCenter(double x, double z) {
      this.centerX = x;
      this.centerZ = z;
      this.area.onCenterChanged();
      Iterator var5 = this.getListeners().iterator();

      while(var5.hasNext()) {
         WorldBorderListener worldBorderListener = (WorldBorderListener)var5.next();
         worldBorderListener.onCenterChanged(this, x, z);
      }

   }

   public double getSize() {
      return this.area.getSize();
   }

   public long getSizeLerpTime() {
      return this.area.getTargetRemainingTime();
   }

   public double getSizeLerpTarget() {
      return this.area.getTargetSize();
   }

   /**
    * Sets the area of this border to a static area with the given {@code size},
    * and notifies all listeners.
    */
   public void setSize(double size) {
      this.area = new WorldBorder.StaticArea(size);
      Iterator var3 = this.getListeners().iterator();

      while(var3.hasNext()) {
         WorldBorderListener worldBorderListener = (WorldBorderListener)var3.next();
         worldBorderListener.onSizeChange(this, size);
      }

   }

   public void interpolateSize(double fromSize, double toSize, long time) {
      this.area = (WorldBorder.Area)(fromSize == toSize ? new WorldBorder.StaticArea(toSize) : new WorldBorder.MovingArea(fromSize, toSize, time));
      Iterator var7 = this.getListeners().iterator();

      while(var7.hasNext()) {
         WorldBorderListener worldBorderListener = (WorldBorderListener)var7.next();
         worldBorderListener.onInterpolateSize(this, fromSize, toSize, time);
      }

   }

   protected List<WorldBorderListener> getListeners() {
      return Lists.newArrayList((Iterable)this.listeners);
   }

   public void addListener(WorldBorderListener listener) {
      this.listeners.add(listener);
   }

   /**
    * Sets the maximum radius of this border and notifies its area.
    */
   public void setMaxRadius(int maxRadius) {
      this.maxRadius = maxRadius;
      this.area.onMaxWorldBorderRadiusChanged();
   }

   /**
    * Returns the maximum radius of this border, in blocks.
    * 
    * <p>The default value is 29999984.
    */
   public int getMaxRadius() {
      return this.maxRadius;
   }

   /**
    * Returns the safe zone of this border.
    * 
    * <p>The default value is 5.0.
    */
   public double getSafeZone() {
      return this.safeZone;
   }

   /**
    * Sets the safe zone of this border and notifies all listeners.
    */
   public void setSafeZone(double safeZone) {
      this.safeZone = safeZone;
      Iterator var3 = this.getListeners().iterator();

      while(var3.hasNext()) {
         WorldBorderListener worldBorderListener = (WorldBorderListener)var3.next();
         worldBorderListener.onSafeZoneChanged(this, safeZone);
      }

   }

   /**
    * Returns the damage increase per block beyond this border, in hearts.
    * <p>Once an entity goes beyond the border and the safe zone, damage will be
    * applied depending on the distance traveled multiplied by this damage increase.
    * 
    * <p>The default value is 0.2.
    * 
    * @see net.minecraft.entity.LivingEntity#baseTick()
    */
   public double getDamagePerBlock() {
      return this.damagePerBlock;
   }

   /**
    * Sets the damage per block of this border and notifies all listeners.
    */
   public void setDamagePerBlock(double damagePerBlock) {
      this.damagePerBlock = damagePerBlock;
      Iterator var3 = this.getListeners().iterator();

      while(var3.hasNext()) {
         WorldBorderListener worldBorderListener = (WorldBorderListener)var3.next();
         worldBorderListener.onDamagePerBlockChanged(this, damagePerBlock);
      }

   }

   @Environment(EnvType.CLIENT)
   public double getShrinkingSpeed() {
      return this.area.getShrinkingSpeed();
   }

   /**
    * Returns the warning time of this border, in ticks.
    * <p>Once a player goes beyond the border, this is the time before a message
    * is displayed to them.
    * 
    * <p>The default value is 15.
    */
   public int getWarningTime() {
      return this.warningTime;
   }

   /**
    * Sets the warning time of this border and notifies all listeners.
    */
   public void setWarningTime(int warningTime) {
      this.warningTime = warningTime;
      Iterator var2 = this.getListeners().iterator();

      while(var2.hasNext()) {
         WorldBorderListener worldBorderListener = (WorldBorderListener)var2.next();
         worldBorderListener.onWarningTimeChanged(this, warningTime);
      }

   }

   /**
    * Returns the warning distance of this border, in blocks.
    * <p>When an entity approaches the border, this is the distance from which
    * a warning will be displayed.
    * 
    * <p>The default value is 5.
    */
   public int getWarningBlocks() {
      return this.warningBlocks;
   }

   /**
    * Sets the warning blocks of this border and notifies all listeners.
    */
   public void setWarningBlocks(int warningBlocks) {
      this.warningBlocks = warningBlocks;
      Iterator var2 = this.getListeners().iterator();

      while(var2.hasNext()) {
         WorldBorderListener worldBorderListener = (WorldBorderListener)var2.next();
         worldBorderListener.onWarningBlocksChanged(this, warningBlocks);
      }

   }

   public void tick() {
      this.area = this.area.getAreaInstance();
   }

   public WorldBorder.Properties write() {
      return new WorldBorder.Properties(this);
   }

   public void load(WorldBorder.Properties properties) {
      this.setCenter(properties.getCenterX(), properties.getCenterZ());
      this.setDamagePerBlock(properties.getDamagePerBlock());
      this.setSafeZone(properties.getBuffer());
      this.setWarningBlocks(properties.getWarningBlocks());
      this.setWarningTime(properties.getWarningTime());
      if (properties.getTargetRemainingTime() > 0L) {
         this.interpolateSize(properties.getSize(), properties.getTargetSize(), properties.getTargetRemainingTime());
      } else {
         this.setSize(properties.getSize());
      }

   }

   public static class Properties {
      private final double centerX;
      private final double centerZ;
      private final double damagePerBlock;
      private final double buffer;
      private final int warningBlocks;
      private final int warningTime;
      private final double size;
      private final long targetRemainingTime;
      private final double targetSize;

      private Properties(double centerX, double centerZ, double damagePerBlock, double buffer, int warningBlocks, int warningTime, double size, long targetRemainingTime, double targetSize) {
         this.centerX = centerX;
         this.centerZ = centerZ;
         this.damagePerBlock = damagePerBlock;
         this.buffer = buffer;
         this.warningBlocks = warningBlocks;
         this.warningTime = warningTime;
         this.size = size;
         this.targetRemainingTime = targetRemainingTime;
         this.targetSize = targetSize;
      }

      private Properties(WorldBorder worldBorder) {
         this.centerX = worldBorder.getCenterX();
         this.centerZ = worldBorder.getCenterZ();
         this.damagePerBlock = worldBorder.getDamagePerBlock();
         this.buffer = worldBorder.getSafeZone();
         this.warningBlocks = worldBorder.getWarningBlocks();
         this.warningTime = worldBorder.getWarningTime();
         this.size = worldBorder.getSize();
         this.targetRemainingTime = worldBorder.getSizeLerpTime();
         this.targetSize = worldBorder.getSizeLerpTarget();
      }

      public double getCenterX() {
         return this.centerX;
      }

      public double getCenterZ() {
         return this.centerZ;
      }

      public double getDamagePerBlock() {
         return this.damagePerBlock;
      }

      public double getBuffer() {
         return this.buffer;
      }

      public int getWarningBlocks() {
         return this.warningBlocks;
      }

      public int getWarningTime() {
         return this.warningTime;
      }

      public double getSize() {
         return this.size;
      }

      public long getTargetRemainingTime() {
         return this.targetRemainingTime;
      }

      public double getTargetSize() {
         return this.targetSize;
      }

      public static WorldBorder.Properties fromDynamic(DynamicLike<?> dynamicLike, WorldBorder.Properties properties) {
         double d = dynamicLike.get("BorderCenterX").asDouble(properties.centerX);
         double e = dynamicLike.get("BorderCenterZ").asDouble(properties.centerZ);
         double f = dynamicLike.get("BorderSize").asDouble(properties.size);
         long l = dynamicLike.get("BorderSizeLerpTime").asLong(properties.targetRemainingTime);
         double g = dynamicLike.get("BorderSizeLerpTarget").asDouble(properties.targetSize);
         double h = dynamicLike.get("BorderSafeZone").asDouble(properties.buffer);
         double i = dynamicLike.get("BorderDamagePerBlock").asDouble(properties.damagePerBlock);
         int j = dynamicLike.get("BorderWarningBlocks").asInt(properties.warningBlocks);
         int k = dynamicLike.get("BorderWarningTime").asInt(properties.warningTime);
         return new WorldBorder.Properties(d, e, i, h, j, k, f, l, g);
      }

      public void toTag(NbtCompound tag) {
         tag.putDouble("BorderCenterX", this.centerX);
         tag.putDouble("BorderCenterZ", this.centerZ);
         tag.putDouble("BorderSize", this.size);
         tag.putLong("BorderSizeLerpTime", this.targetRemainingTime);
         tag.putDouble("BorderSafeZone", this.buffer);
         tag.putDouble("BorderDamagePerBlock", this.damagePerBlock);
         tag.putDouble("BorderSizeLerpTarget", this.targetSize);
         tag.putDouble("BorderWarningBlocks", (double)this.warningBlocks);
         tag.putDouble("BorderWarningTime", (double)this.warningTime);
      }
   }

   class StaticArea implements WorldBorder.Area {
      private final double size;
      private double boundWest;
      private double boundNorth;
      private double boundEast;
      private double boundSouth;
      private VoxelShape shape;

      public StaticArea(double d) {
         this.size = d;
         this.recalculateBounds();
      }

      public double getBoundWest() {
         return this.boundWest;
      }

      public double getBoundEast() {
         return this.boundEast;
      }

      public double getBoundNorth() {
         return this.boundNorth;
      }

      public double getBoundSouth() {
         return this.boundSouth;
      }

      public double getSize() {
         return this.size;
      }

      @Environment(EnvType.CLIENT)
      public WorldBorderStage getStage() {
         return WorldBorderStage.STATIONARY;
      }

      @Environment(EnvType.CLIENT)
      public double getShrinkingSpeed() {
         return 0.0D;
      }

      public long getTargetRemainingTime() {
         return 0L;
      }

      public double getTargetSize() {
         return this.size;
      }

      private void recalculateBounds() {
         this.boundWest = Math.max(WorldBorder.this.getCenterX() - this.size / 2.0D, (double)(-WorldBorder.this.maxRadius));
         this.boundNorth = Math.max(WorldBorder.this.getCenterZ() - this.size / 2.0D, (double)(-WorldBorder.this.maxRadius));
         this.boundEast = Math.min(WorldBorder.this.getCenterX() + this.size / 2.0D, (double)WorldBorder.this.maxRadius);
         this.boundSouth = Math.min(WorldBorder.this.getCenterZ() + this.size / 2.0D, (double)WorldBorder.this.maxRadius);
         this.shape = VoxelShapes.combineAndSimplify(VoxelShapes.UNBOUNDED, VoxelShapes.cuboid(Math.floor(this.getBoundWest()), Double.NEGATIVE_INFINITY, Math.floor(this.getBoundNorth()), Math.ceil(this.getBoundEast()), Double.POSITIVE_INFINITY, Math.ceil(this.getBoundSouth())), BooleanBiFunction.ONLY_FIRST);
      }

      public void onMaxWorldBorderRadiusChanged() {
         this.recalculateBounds();
      }

      public void onCenterChanged() {
         this.recalculateBounds();
      }

      public WorldBorder.Area getAreaInstance() {
         return this;
      }

      public VoxelShape asVoxelShape() {
         return this.shape;
      }
   }

   class MovingArea implements WorldBorder.Area {
      private final double oldSize;
      private final double newSize;
      private final long timeEnd;
      private final long timeStart;
      private final double timeDuration;

      private MovingArea(double oldSize, double newSize, long duration) {
         this.oldSize = oldSize;
         this.newSize = newSize;
         this.timeDuration = (double)duration;
         this.timeStart = Util.getMeasuringTimeMs();
         this.timeEnd = this.timeStart + duration;
      }

      public double getBoundWest() {
         return Math.max(WorldBorder.this.getCenterX() - this.getSize() / 2.0D, (double)(-WorldBorder.this.maxRadius));
      }

      public double getBoundNorth() {
         return Math.max(WorldBorder.this.getCenterZ() - this.getSize() / 2.0D, (double)(-WorldBorder.this.maxRadius));
      }

      public double getBoundEast() {
         return Math.min(WorldBorder.this.getCenterX() + this.getSize() / 2.0D, (double)WorldBorder.this.maxRadius);
      }

      public double getBoundSouth() {
         return Math.min(WorldBorder.this.getCenterZ() + this.getSize() / 2.0D, (double)WorldBorder.this.maxRadius);
      }

      public double getSize() {
         double d = (double)(Util.getMeasuringTimeMs() - this.timeStart) / this.timeDuration;
         return d < 1.0D ? MathHelper.lerp(d, this.oldSize, this.newSize) : this.newSize;
      }

      @Environment(EnvType.CLIENT)
      public double getShrinkingSpeed() {
         return Math.abs(this.oldSize - this.newSize) / (double)(this.timeEnd - this.timeStart);
      }

      public long getTargetRemainingTime() {
         return this.timeEnd - Util.getMeasuringTimeMs();
      }

      public double getTargetSize() {
         return this.newSize;
      }

      @Environment(EnvType.CLIENT)
      public WorldBorderStage getStage() {
         return this.newSize < this.oldSize ? WorldBorderStage.SHRINKING : WorldBorderStage.GROWING;
      }

      public void onCenterChanged() {
      }

      public void onMaxWorldBorderRadiusChanged() {
      }

      public WorldBorder.Area getAreaInstance() {
         return (WorldBorder.Area)(this.getTargetRemainingTime() <= 0L ? WorldBorder.this.new StaticArea(this.newSize) : this);
      }

      public VoxelShape asVoxelShape() {
         return VoxelShapes.combineAndSimplify(VoxelShapes.UNBOUNDED, VoxelShapes.cuboid(Math.floor(this.getBoundWest()), Double.NEGATIVE_INFINITY, Math.floor(this.getBoundNorth()), Math.ceil(this.getBoundEast()), Double.POSITIVE_INFINITY, Math.ceil(this.getBoundSouth())), BooleanBiFunction.ONLY_FIRST);
      }
   }

   interface Area {
      double getBoundWest();

      double getBoundEast();

      double getBoundNorth();

      double getBoundSouth();

      double getSize();

      @Environment(EnvType.CLIENT)
      double getShrinkingSpeed();

      long getTargetRemainingTime();

      double getTargetSize();

      @Environment(EnvType.CLIENT)
      WorldBorderStage getStage();

      void onMaxWorldBorderRadiusChanged();

      void onCenterChanged();

      WorldBorder.Area getAreaInstance();

      VoxelShape asVoxelShape();
   }
}
