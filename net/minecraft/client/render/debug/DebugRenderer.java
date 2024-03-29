package net.minecraft.client.render.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Optional;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class DebugRenderer {
   public final PathfindingDebugRenderer pathfindingDebugRenderer = new PathfindingDebugRenderer();
   public final DebugRenderer.Renderer waterDebugRenderer;
   public final DebugRenderer.Renderer chunkBorderDebugRenderer;
   public final DebugRenderer.Renderer heightmapDebugRenderer;
   public final DebugRenderer.Renderer collisionDebugRenderer;
   public final DebugRenderer.Renderer neighborUpdateDebugRenderer;
   public final CaveDebugRenderer caveDebugRenderer;
   public final StructureDebugRenderer structureDebugRenderer;
   public final DebugRenderer.Renderer skyLightDebugRenderer;
   public final DebugRenderer.Renderer worldGenAttemptDebugRenderer;
   public final DebugRenderer.Renderer blockOutlineDebugRenderer;
   public final DebugRenderer.Renderer chunkLoadingDebugRenderer;
   public final VillageDebugRenderer villageDebugRenderer;
   public final VillageSectionsDebugRenderer villageSectionsDebugRenderer;
   public final BeeDebugRenderer beeDebugRenderer;
   public final RaidCenterDebugRenderer raidCenterDebugRenderer;
   public final GoalSelectorDebugRenderer goalSelectorDebugRenderer;
   public final GameTestDebugRenderer gameTestDebugRenderer;
   private boolean showChunkBorder;

   public DebugRenderer(MinecraftClient client) {
      this.waterDebugRenderer = new WaterDebugRenderer(client);
      this.chunkBorderDebugRenderer = new ChunkBorderDebugRenderer(client);
      this.heightmapDebugRenderer = new HeightmapDebugRenderer(client);
      this.collisionDebugRenderer = new CollisionDebugRenderer(client);
      this.neighborUpdateDebugRenderer = new NeighborUpdateDebugRenderer(client);
      this.caveDebugRenderer = new CaveDebugRenderer();
      this.structureDebugRenderer = new StructureDebugRenderer(client);
      this.skyLightDebugRenderer = new SkyLightDebugRenderer(client);
      this.worldGenAttemptDebugRenderer = new WorldGenAttemptDebugRenderer();
      this.blockOutlineDebugRenderer = new BlockOutlineDebugRenderer(client);
      this.chunkLoadingDebugRenderer = new ChunkLoadingDebugRenderer(client);
      this.villageDebugRenderer = new VillageDebugRenderer(client);
      this.villageSectionsDebugRenderer = new VillageSectionsDebugRenderer();
      this.beeDebugRenderer = new BeeDebugRenderer(client);
      this.raidCenterDebugRenderer = new RaidCenterDebugRenderer(client);
      this.goalSelectorDebugRenderer = new GoalSelectorDebugRenderer(client);
      this.gameTestDebugRenderer = new GameTestDebugRenderer();
   }

   public void reset() {
      this.pathfindingDebugRenderer.clear();
      this.waterDebugRenderer.clear();
      this.chunkBorderDebugRenderer.clear();
      this.heightmapDebugRenderer.clear();
      this.collisionDebugRenderer.clear();
      this.neighborUpdateDebugRenderer.clear();
      this.caveDebugRenderer.clear();
      this.structureDebugRenderer.clear();
      this.skyLightDebugRenderer.clear();
      this.worldGenAttemptDebugRenderer.clear();
      this.blockOutlineDebugRenderer.clear();
      this.chunkLoadingDebugRenderer.clear();
      this.villageDebugRenderer.clear();
      this.villageSectionsDebugRenderer.clear();
      this.beeDebugRenderer.clear();
      this.raidCenterDebugRenderer.clear();
      this.goalSelectorDebugRenderer.clear();
      this.gameTestDebugRenderer.clear();
   }

   public boolean toggleShowChunkBorder() {
      this.showChunkBorder = !this.showChunkBorder;
      return this.showChunkBorder;
   }

   public void render(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, double cameraX, double cameraY, double cameraZ) {
      if (this.showChunkBorder && !MinecraftClient.getInstance().hasReducedDebugInfo()) {
         this.chunkBorderDebugRenderer.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
      }

      this.gameTestDebugRenderer.render(matrices, vertexConsumers, cameraX, cameraY, cameraZ);
   }

   public static Optional<Entity> getTargetedEntity(@Nullable Entity entity, int maxDistance) {
      if (entity == null) {
         return Optional.empty();
      } else {
         Vec3d vec3d = entity.getCameraPosVec(1.0F);
         Vec3d vec3d2 = entity.getRotationVec(1.0F).multiply((double)maxDistance);
         Vec3d vec3d3 = vec3d.add(vec3d2);
         Box box = entity.getBoundingBox().stretch(vec3d2).expand(1.0D);
         int i = maxDistance * maxDistance;
         Predicate<Entity> predicate = (entityx) -> {
            return !entityx.isSpectator() && entityx.collides();
         };
         EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, vec3d, vec3d3, box, predicate, (double)i);
         if (entityHitResult == null) {
            return Optional.empty();
         } else {
            return vec3d.squaredDistanceTo(entityHitResult.getPos()) > (double)i ? Optional.empty() : Optional.of(entityHitResult.getEntity());
         }
      }
   }

   public static void drawBox(BlockPos pos1, BlockPos pos2, float red, float green, float blue, float alpha) {
      Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
      if (camera.isReady()) {
         Vec3d vec3d = camera.getPos().negate();
         Box box = (new Box(pos1, pos2)).offset(vec3d);
         drawBox(box, red, green, blue, alpha);
      }
   }

   public static void drawBox(BlockPos pos, float expand, float red, float green, float blue, float alpha) {
      Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
      if (camera.isReady()) {
         Vec3d vec3d = camera.getPos().negate();
         Box box = (new Box(pos)).offset(vec3d).expand((double)expand);
         drawBox(box, red, green, blue, alpha);
      }
   }

   public static void drawBox(Box box, float red, float green, float blue, float alpha) {
      drawBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue, alpha);
   }

   public static void drawBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float red, float green, float blue, float alpha) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferBuilder = tessellator.getBuffer();
      bufferBuilder.begin(5, VertexFormats.POSITION_COLOR);
      WorldRenderer.drawBox(bufferBuilder, minX, minY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
      tessellator.draw();
   }

   public static void drawString(String string, int x, int y, int z, int color) {
      drawString(string, (double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, color);
   }

   public static void drawString(String string, double x, double y, double z, int color) {
      drawString(string, x, y, z, color, 0.02F);
   }

   public static void drawString(String string, double x, double y, double z, int color, float size) {
      drawString(string, x, y, z, color, size, true, 0.0F, false);
   }

   public static void drawString(String string, double x, double y, double z, int color, float size, boolean center, float offset, boolean visibleThroughObjects) {
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      Camera camera = minecraftClient.gameRenderer.getCamera();
      if (camera.isReady() && minecraftClient.getEntityRenderDispatcher().gameOptions != null) {
         TextRenderer textRenderer = minecraftClient.textRenderer;
         double d = camera.getPos().x;
         double e = camera.getPos().y;
         double f = camera.getPos().z;
         RenderSystem.pushMatrix();
         RenderSystem.translatef((float)(x - d), (float)(y - e) + 0.07F, (float)(z - f));
         RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
         RenderSystem.multMatrix(new Matrix4f(camera.getRotation()));
         RenderSystem.scalef(size, -size, size);
         RenderSystem.enableTexture();
         if (visibleThroughObjects) {
            RenderSystem.disableDepthTest();
         } else {
            RenderSystem.enableDepthTest();
         }

         RenderSystem.depthMask(true);
         RenderSystem.scalef(-1.0F, 1.0F, 1.0F);
         float g = center ? (float)(-textRenderer.getWidth(string)) / 2.0F : 0.0F;
         g -= offset / size;
         RenderSystem.enableAlphaTest();
         VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
         textRenderer.draw((String)string, g, 0.0F, color, false, AffineTransformation.identity().getMatrix(), immediate, visibleThroughObjects, 0, 15728880);
         immediate.draw();
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.enableDepthTest();
         RenderSystem.popMatrix();
      }
   }

   @Environment(EnvType.CLIENT)
   public interface Renderer {
      void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ);

      default void clear() {
      }
   }
}
