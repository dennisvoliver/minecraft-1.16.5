package net.minecraft.entity.ai.pathing;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class Path {
   private final List<PathNode> nodes;
   private PathNode[] field_57 = new PathNode[0];
   private PathNode[] field_55 = new PathNode[0];
   @Environment(EnvType.CLIENT)
   private Set<TargetPathNode> field_20300;
   private int currentNodeIndex;
   private final BlockPos target;
   private final float manhattanDistanceFromTarget;
   private final boolean reachesTarget;

   public Path(List<PathNode> nodes, BlockPos target, boolean reachesTarget) {
      this.nodes = nodes;
      this.target = target;
      this.manhattanDistanceFromTarget = nodes.isEmpty() ? Float.MAX_VALUE : ((PathNode)this.nodes.get(this.nodes.size() - 1)).getManhattanDistance(this.target);
      this.reachesTarget = reachesTarget;
   }

   public void next() {
      ++this.currentNodeIndex;
   }

   public boolean method_30849() {
      return this.currentNodeIndex <= 0;
   }

   public boolean isFinished() {
      return this.currentNodeIndex >= this.nodes.size();
   }

   @Nullable
   public PathNode getEnd() {
      return !this.nodes.isEmpty() ? (PathNode)this.nodes.get(this.nodes.size() - 1) : null;
   }

   public PathNode getNode(int index) {
      return (PathNode)this.nodes.get(index);
   }

   public void setLength(int length) {
      if (this.nodes.size() > length) {
         this.nodes.subList(length, this.nodes.size()).clear();
      }

   }

   public void setNode(int index, PathNode node) {
      this.nodes.set(index, node);
   }

   public int getLength() {
      return this.nodes.size();
   }

   public int getCurrentNodeIndex() {
      return this.currentNodeIndex;
   }

   public void setCurrentNodeIndex(int index) {
      this.currentNodeIndex = index;
   }

   public Vec3d getNodePosition(Entity entity, int index) {
      PathNode pathNode = (PathNode)this.nodes.get(index);
      double d = (double)pathNode.x + (double)((int)(entity.getWidth() + 1.0F)) * 0.5D;
      double e = (double)pathNode.y;
      double f = (double)pathNode.z + (double)((int)(entity.getWidth() + 1.0F)) * 0.5D;
      return new Vec3d(d, e, f);
   }

   public BlockPos method_31031(int i) {
      return ((PathNode)this.nodes.get(i)).getBlockPos();
   }

   public Vec3d getNodePosition(Entity entity) {
      return this.getNodePosition(entity, this.currentNodeIndex);
   }

   public BlockPos method_31032() {
      return ((PathNode)this.nodes.get(this.currentNodeIndex)).getBlockPos();
   }

   public PathNode method_29301() {
      return (PathNode)this.nodes.get(this.currentNodeIndex);
   }

   @Nullable
   public PathNode method_30850() {
      return this.currentNodeIndex > 0 ? (PathNode)this.nodes.get(this.currentNodeIndex - 1) : null;
   }

   public boolean equalsPath(@Nullable Path o) {
      if (o == null) {
         return false;
      } else if (o.nodes.size() != this.nodes.size()) {
         return false;
      } else {
         for(int i = 0; i < this.nodes.size(); ++i) {
            PathNode pathNode = (PathNode)this.nodes.get(i);
            PathNode pathNode2 = (PathNode)o.nodes.get(i);
            if (pathNode.x != pathNode2.x || pathNode.y != pathNode2.y || pathNode.z != pathNode2.z) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean reachesTarget() {
      return this.reachesTarget;
   }

   @Environment(EnvType.CLIENT)
   public PathNode[] method_22880() {
      return this.field_57;
   }

   @Environment(EnvType.CLIENT)
   public PathNode[] method_22881() {
      return this.field_55;
   }

   @Environment(EnvType.CLIENT)
   public static Path fromBuffer(PacketByteBuf buffer) {
      boolean bl = buffer.readBoolean();
      int i = buffer.readInt();
      int j = buffer.readInt();
      Set<TargetPathNode> set = Sets.newHashSet();

      for(int k = 0; k < j; ++k) {
         set.add(TargetPathNode.fromBuffer(buffer));
      }

      BlockPos blockPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
      List<PathNode> list = Lists.newArrayList();
      int l = buffer.readInt();

      for(int m = 0; m < l; ++m) {
         list.add(PathNode.readBuf(buffer));
      }

      PathNode[] pathNodes = new PathNode[buffer.readInt()];

      for(int n = 0; n < pathNodes.length; ++n) {
         pathNodes[n] = PathNode.readBuf(buffer);
      }

      PathNode[] pathNodes2 = new PathNode[buffer.readInt()];

      for(int o = 0; o < pathNodes2.length; ++o) {
         pathNodes2[o] = PathNode.readBuf(buffer);
      }

      Path path = new Path(list, blockPos, bl);
      path.field_57 = pathNodes;
      path.field_55 = pathNodes2;
      path.field_20300 = set;
      path.currentNodeIndex = i;
      return path;
   }

   public String toString() {
      return "Path(length=" + this.nodes.size() + ")";
   }

   public BlockPos getTarget() {
      return this.target;
   }

   public float getManhattanDistanceFromTarget() {
      return this.manhattanDistanceFromTarget;
   }
}
