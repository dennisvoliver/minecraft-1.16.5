package net.minecraft.client.render.block.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BlockEntityRenderDispatcher {
   private final Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = Maps.newHashMap();
   public static final BlockEntityRenderDispatcher INSTANCE = new BlockEntityRenderDispatcher();
   private final BufferBuilder bufferBuilder = new BufferBuilder(256);
   private TextRenderer textRenderer;
   public TextureManager textureManager;
   public World world;
   public Camera camera;
   public HitResult crosshairTarget;

   private BlockEntityRenderDispatcher() {
      this.register(BlockEntityType.SIGN, new SignBlockEntityRenderer(this));
      this.register(BlockEntityType.MOB_SPAWNER, new MobSpawnerBlockEntityRenderer(this));
      this.register(BlockEntityType.PISTON, new PistonBlockEntityRenderer(this));
      this.register(BlockEntityType.CHEST, new ChestBlockEntityRenderer(this));
      this.register(BlockEntityType.ENDER_CHEST, new ChestBlockEntityRenderer(this));
      this.register(BlockEntityType.TRAPPED_CHEST, new ChestBlockEntityRenderer(this));
      this.register(BlockEntityType.ENCHANTING_TABLE, new EnchantingTableBlockEntityRenderer(this));
      this.register(BlockEntityType.LECTERN, new LecternBlockEntityRenderer(this));
      this.register(BlockEntityType.END_PORTAL, new EndPortalBlockEntityRenderer(this));
      this.register(BlockEntityType.END_GATEWAY, new EndGatewayBlockEntityRenderer(this));
      this.register(BlockEntityType.BEACON, new BeaconBlockEntityRenderer(this));
      this.register(BlockEntityType.SKULL, new SkullBlockEntityRenderer(this));
      this.register(BlockEntityType.BANNER, new BannerBlockEntityRenderer(this));
      this.register(BlockEntityType.STRUCTURE_BLOCK, new StructureBlockBlockEntityRenderer(this));
      this.register(BlockEntityType.SHULKER_BOX, new ShulkerBoxBlockEntityRenderer(new ShulkerEntityModel(), this));
      this.register(BlockEntityType.BED, new BedBlockEntityRenderer(this));
      this.register(BlockEntityType.CONDUIT, new ConduitBlockEntityRenderer(this));
      this.register(BlockEntityType.BELL, new BellBlockEntityRenderer(this));
      this.register(BlockEntityType.CAMPFIRE, new CampfireBlockEntityRenderer(this));
   }

   private <E extends BlockEntity> void register(BlockEntityType<E> blockEntityType, BlockEntityRenderer<E> blockEntityRenderer) {
      this.renderers.put(blockEntityType, blockEntityRenderer);
   }

   @Nullable
   public <E extends BlockEntity> BlockEntityRenderer<E> get(E blockEntity) {
      return (BlockEntityRenderer)this.renderers.get(blockEntity.getType());
   }

   public void configure(World world, TextureManager textureManager, TextRenderer textRenderer, Camera camera, HitResult crosshairTarget) {
      if (this.world != world) {
         this.setWorld(world);
      }

      this.textureManager = textureManager;
      this.camera = camera;
      this.textRenderer = textRenderer;
      this.crosshairTarget = crosshairTarget;
   }

   public <E extends BlockEntity> void render(E blockEntity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider) {
      if (Vec3d.ofCenter(blockEntity.getPos()).isInRange(this.camera.getPos(), blockEntity.getRenderDistance())) {
         BlockEntityRenderer<E> blockEntityRenderer = this.get(blockEntity);
         if (blockEntityRenderer != null) {
            if (blockEntity.hasWorld() && blockEntity.getType().supports(blockEntity.getCachedState().getBlock())) {
               runReported(blockEntity, () -> {
                  render(blockEntityRenderer, blockEntity, tickDelta, matrix, vertexConsumerProvider);
               });
            }
         }
      }
   }

   private static <T extends BlockEntity> void render(BlockEntityRenderer<T> renderer, T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
      World world = blockEntity.getWorld();
      int j;
      if (world != null) {
         j = WorldRenderer.getLightmapCoordinates(world, blockEntity.getPos());
      } else {
         j = 15728880;
      }

      renderer.render(blockEntity, tickDelta, matrices, vertexConsumers, j, OverlayTexture.DEFAULT_UV);
   }

   public <E extends BlockEntity> boolean renderEntity(E entity, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
      BlockEntityRenderer<E> blockEntityRenderer = this.get(entity);
      if (blockEntityRenderer == null) {
         return true;
      } else {
         runReported(entity, () -> {
            blockEntityRenderer.render(entity, 0.0F, matrix, vertexConsumerProvider, light, overlay);
         });
         return false;
      }
   }

   private static void runReported(BlockEntity blockEntity, Runnable runnable) {
      try {
         runnable.run();
      } catch (Throwable var5) {
         CrashReport crashReport = CrashReport.create(var5, "Rendering Block Entity");
         CrashReportSection crashReportSection = crashReport.addElement("Block Entity Details");
         blockEntity.populateCrashReport(crashReportSection);
         throw new CrashException(crashReport);
      }
   }

   public void setWorld(@Nullable World world) {
      this.world = world;
      if (world == null) {
         this.camera = null;
      }

   }

   public TextRenderer getTextRenderer() {
      return this.textRenderer;
   }
}
