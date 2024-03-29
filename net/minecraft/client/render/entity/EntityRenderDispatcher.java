package net.minecraft.client.render.entity;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class EntityRenderDispatcher {
   private static final RenderLayer SHADOW_LAYER = RenderLayer.getEntityShadow(new Identifier("textures/misc/shadow.png"));
   private final Map<EntityType<?>, EntityRenderer<?>> renderers = Maps.newHashMap();
   private final Map<String, PlayerEntityRenderer> modelRenderers = Maps.newHashMap();
   private final PlayerEntityRenderer playerRenderer;
   private final TextRenderer textRenderer;
   public final TextureManager textureManager;
   private World world;
   public Camera camera;
   private Quaternion rotation;
   public Entity targetedEntity;
   public final GameOptions gameOptions;
   private boolean renderShadows = true;
   private boolean renderHitboxes;

   public <E extends Entity> int getLight(E entity, float tickDelta) {
      return this.getRenderer(entity).getLight(entity, tickDelta);
   }

   private <T extends Entity> void register(EntityType<T> entityType, EntityRenderer<? super T> entityRenderer) {
      this.renderers.put(entityType, entityRenderer);
   }

   private void registerRenderers(ItemRenderer itemRenderer, ReloadableResourceManager reloadableResourceManager) {
      this.register(EntityType.AREA_EFFECT_CLOUD, new AreaEffectCloudEntityRenderer(this));
      this.register(EntityType.ARMOR_STAND, new ArmorStandEntityRenderer(this));
      this.register(EntityType.ARROW, new ArrowEntityRenderer(this));
      this.register(EntityType.BAT, new BatEntityRenderer(this));
      this.register(EntityType.BEE, new BeeEntityRenderer(this));
      this.register(EntityType.BLAZE, new BlazeEntityRenderer(this));
      this.register(EntityType.BOAT, new BoatEntityRenderer(this));
      this.register(EntityType.CAT, new CatEntityRenderer(this));
      this.register(EntityType.CAVE_SPIDER, new CaveSpiderEntityRenderer(this));
      this.register(EntityType.CHEST_MINECART, new MinecartEntityRenderer(this));
      this.register(EntityType.CHICKEN, new ChickenEntityRenderer(this));
      this.register(EntityType.COD, new CodEntityRenderer(this));
      this.register(EntityType.COMMAND_BLOCK_MINECART, new MinecartEntityRenderer(this));
      this.register(EntityType.COW, new CowEntityRenderer(this));
      this.register(EntityType.CREEPER, new CreeperEntityRenderer(this));
      this.register(EntityType.DOLPHIN, new DolphinEntityRenderer(this));
      this.register(EntityType.DONKEY, new DonkeyEntityRenderer(this, 0.87F));
      this.register(EntityType.DRAGON_FIREBALL, new DragonFireballEntityRenderer(this));
      this.register(EntityType.DROWNED, new DrownedEntityRenderer(this));
      this.register(EntityType.EGG, new FlyingItemEntityRenderer(this, itemRenderer));
      this.register(EntityType.ELDER_GUARDIAN, new ElderGuardianEntityRenderer(this));
      this.register(EntityType.END_CRYSTAL, new EndCrystalEntityRenderer(this));
      this.register(EntityType.ENDER_DRAGON, new EnderDragonEntityRenderer(this));
      this.register(EntityType.ENDERMAN, new EndermanEntityRenderer(this));
      this.register(EntityType.ENDERMITE, new EndermiteEntityRenderer(this));
      this.register(EntityType.ENDER_PEARL, new FlyingItemEntityRenderer(this, itemRenderer));
      this.register(EntityType.EVOKER_FANGS, new EvokerFangsEntityRenderer(this));
      this.register(EntityType.EVOKER, new EvokerEntityRenderer(this));
      this.register(EntityType.EXPERIENCE_BOTTLE, new FlyingItemEntityRenderer(this, itemRenderer));
      this.register(EntityType.EXPERIENCE_ORB, new ExperienceOrbEntityRenderer(this));
      this.register(EntityType.EYE_OF_ENDER, new FlyingItemEntityRenderer(this, itemRenderer, 1.0F, true));
      this.register(EntityType.FALLING_BLOCK, new FallingBlockEntityRenderer(this));
      this.register(EntityType.FIREBALL, new FlyingItemEntityRenderer(this, itemRenderer, 3.0F, true));
      this.register(EntityType.FIREWORK_ROCKET, new FireworkEntityRenderer(this, itemRenderer));
      this.register(EntityType.FISHING_BOBBER, new FishingBobberEntityRenderer(this));
      this.register(EntityType.FOX, new FoxEntityRenderer(this));
      this.register(EntityType.FURNACE_MINECART, new MinecartEntityRenderer(this));
      this.register(EntityType.GHAST, new GhastEntityRenderer(this));
      this.register(EntityType.GIANT, new GiantEntityRenderer(this, 6.0F));
      this.register(EntityType.GUARDIAN, new GuardianEntityRenderer(this));
      this.register(EntityType.HOGLIN, new HoglinEntityRenderer(this));
      this.register(EntityType.HOPPER_MINECART, new MinecartEntityRenderer(this));
      this.register(EntityType.HORSE, new HorseEntityRenderer(this));
      this.register(EntityType.HUSK, new HuskEntityRenderer(this));
      this.register(EntityType.ILLUSIONER, new IllusionerEntityRenderer(this));
      this.register(EntityType.IRON_GOLEM, new IronGolemEntityRenderer(this));
      this.register(EntityType.ITEM, new ItemEntityRenderer(this, itemRenderer));
      this.register(EntityType.ITEM_FRAME, new ItemFrameEntityRenderer(this, itemRenderer));
      this.register(EntityType.LEASH_KNOT, new LeashKnotEntityRenderer(this));
      this.register(EntityType.LIGHTNING_BOLT, new LightningEntityRenderer(this));
      this.register(EntityType.LLAMA, new LlamaEntityRenderer(this));
      this.register(EntityType.LLAMA_SPIT, new LlamaSpitEntityRenderer(this));
      this.register(EntityType.MAGMA_CUBE, new MagmaCubeEntityRenderer(this));
      this.register(EntityType.MINECART, new MinecartEntityRenderer(this));
      this.register(EntityType.MOOSHROOM, new MooshroomEntityRenderer(this));
      this.register(EntityType.MULE, new DonkeyEntityRenderer(this, 0.92F));
      this.register(EntityType.OCELOT, new OcelotEntityRenderer(this));
      this.register(EntityType.PAINTING, new PaintingEntityRenderer(this));
      this.register(EntityType.PANDA, new PandaEntityRenderer(this));
      this.register(EntityType.PARROT, new ParrotEntityRenderer(this));
      this.register(EntityType.PHANTOM, new PhantomEntityRenderer(this));
      this.register(EntityType.PIG, new PigEntityRenderer(this));
      this.register(EntityType.PIGLIN, new PiglinEntityRenderer(this, false));
      this.register(EntityType.PIGLIN_BRUTE, new PiglinEntityRenderer(this, false));
      this.register(EntityType.PILLAGER, new PillagerEntityRenderer(this));
      this.register(EntityType.POLAR_BEAR, new PolarBearEntityRenderer(this));
      this.register(EntityType.POTION, new FlyingItemEntityRenderer(this, itemRenderer));
      this.register(EntityType.PUFFERFISH, new PufferfishEntityRenderer(this));
      this.register(EntityType.RABBIT, new RabbitEntityRenderer(this));
      this.register(EntityType.RAVAGER, new RavagerEntityRenderer(this));
      this.register(EntityType.SALMON, new SalmonEntityRenderer(this));
      this.register(EntityType.SHEEP, new SheepEntityRenderer(this));
      this.register(EntityType.SHULKER_BULLET, new ShulkerBulletEntityRenderer(this));
      this.register(EntityType.SHULKER, new ShulkerEntityRenderer(this));
      this.register(EntityType.SILVERFISH, new SilverfishEntityRenderer(this));
      this.register(EntityType.SKELETON_HORSE, new ZombieHorseEntityRenderer(this));
      this.register(EntityType.SKELETON, new SkeletonEntityRenderer(this));
      this.register(EntityType.SLIME, new SlimeEntityRenderer(this));
      this.register(EntityType.SMALL_FIREBALL, new FlyingItemEntityRenderer(this, itemRenderer, 0.75F, true));
      this.register(EntityType.SNOWBALL, new FlyingItemEntityRenderer(this, itemRenderer));
      this.register(EntityType.SNOW_GOLEM, new SnowGolemEntityRenderer(this));
      this.register(EntityType.SPAWNER_MINECART, new MinecartEntityRenderer(this));
      this.register(EntityType.SPECTRAL_ARROW, new SpectralArrowEntityRenderer(this));
      this.register(EntityType.SPIDER, new SpiderEntityRenderer(this));
      this.register(EntityType.SQUID, new SquidEntityRenderer(this));
      this.register(EntityType.STRAY, new StrayEntityRenderer(this));
      this.register(EntityType.TNT_MINECART, new TntMinecartEntityRenderer(this));
      this.register(EntityType.TNT, new TntEntityRenderer(this));
      this.register(EntityType.TRADER_LLAMA, new LlamaEntityRenderer(this));
      this.register(EntityType.TRIDENT, new TridentEntityRenderer(this));
      this.register(EntityType.TROPICAL_FISH, new TropicalFishEntityRenderer(this));
      this.register(EntityType.TURTLE, new TurtleEntityRenderer(this));
      this.register(EntityType.VEX, new VexEntityRenderer(this));
      this.register(EntityType.VILLAGER, new VillagerEntityRenderer(this, reloadableResourceManager));
      this.register(EntityType.VINDICATOR, new VindicatorEntityRenderer(this));
      this.register(EntityType.WANDERING_TRADER, new WanderingTraderEntityRenderer(this));
      this.register(EntityType.WITCH, new WitchEntityRenderer(this));
      this.register(EntityType.WITHER, new WitherEntityRenderer(this));
      this.register(EntityType.WITHER_SKELETON, new WitherSkeletonEntityRenderer(this));
      this.register(EntityType.WITHER_SKULL, new WitherSkullEntityRenderer(this));
      this.register(EntityType.WOLF, new WolfEntityRenderer(this));
      this.register(EntityType.ZOGLIN, new ZoglinEntityRenderer(this));
      this.register(EntityType.ZOMBIE_HORSE, new ZombieHorseEntityRenderer(this));
      this.register(EntityType.ZOMBIE, new ZombieEntityRenderer(this));
      this.register(EntityType.ZOMBIFIED_PIGLIN, new PiglinEntityRenderer(this, true));
      this.register(EntityType.ZOMBIE_VILLAGER, new ZombieVillagerEntityRenderer(this, reloadableResourceManager));
      this.register(EntityType.STRIDER, new StriderEntityRenderer(this));
   }

   public EntityRenderDispatcher(TextureManager textureManager, ItemRenderer itemRenderer, ReloadableResourceManager reloadableResourceManager, TextRenderer textRenderer, GameOptions gameOptions) {
      this.textureManager = textureManager;
      this.textRenderer = textRenderer;
      this.gameOptions = gameOptions;
      this.registerRenderers(itemRenderer, reloadableResourceManager);
      this.playerRenderer = new PlayerEntityRenderer(this);
      this.modelRenderers.put("default", this.playerRenderer);
      this.modelRenderers.put("slim", new PlayerEntityRenderer(this, true));
      Iterator var6 = Registry.ENTITY_TYPE.iterator();

      EntityType entityType;
      do {
         if (!var6.hasNext()) {
            return;
         }

         entityType = (EntityType)var6.next();
      } while(entityType == EntityType.PLAYER || this.renderers.containsKey(entityType));

      throw new IllegalStateException("No renderer registered for " + Registry.ENTITY_TYPE.getId(entityType));
   }

   public <T extends Entity> EntityRenderer<? super T> getRenderer(T entity) {
      if (entity instanceof AbstractClientPlayerEntity) {
         String string = ((AbstractClientPlayerEntity)entity).getModel();
         PlayerEntityRenderer playerEntityRenderer = (PlayerEntityRenderer)this.modelRenderers.get(string);
         return playerEntityRenderer != null ? playerEntityRenderer : this.playerRenderer;
      } else {
         return (EntityRenderer)this.renderers.get(entity.getType());
      }
   }

   public void configure(World world, Camera camera, Entity target) {
      this.world = world;
      this.camera = camera;
      this.rotation = camera.getRotation();
      this.targetedEntity = target;
   }

   public void setRotation(Quaternion rotation) {
      this.rotation = rotation;
   }

   public void setRenderShadows(boolean value) {
      this.renderShadows = value;
   }

   public void setRenderHitboxes(boolean value) {
      this.renderHitboxes = value;
   }

   public boolean shouldRenderHitboxes() {
      return this.renderHitboxes;
   }

   public <E extends Entity> boolean shouldRender(E entity, Frustum frustum, double x, double y, double z) {
      EntityRenderer<? super E> entityRenderer = this.getRenderer(entity);
      return entityRenderer.shouldRender(entity, frustum, x, y, z);
   }

   public <E extends Entity> void render(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      EntityRenderer entityRenderer = this.getRenderer(entity);

      try {
         Vec3d vec3d = entityRenderer.getPositionOffset(entity, tickDelta);
         double d = x + vec3d.getX();
         double e = y + vec3d.getY();
         double f = z + vec3d.getZ();
         matrices.push();
         matrices.translate(d, e, f);
         entityRenderer.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
         if (entity.doesRenderOnFire()) {
            this.renderFire(matrices, vertexConsumers, entity);
         }

         matrices.translate(-vec3d.getX(), -vec3d.getY(), -vec3d.getZ());
         if (this.gameOptions.entityShadows && this.renderShadows && entityRenderer.shadowRadius > 0.0F && !entity.isInvisible()) {
            double g = this.getSquaredDistanceToCamera(entity.getX(), entity.getY(), entity.getZ());
            float h = (float)((1.0D - g / 256.0D) * (double)entityRenderer.shadowOpacity);
            if (h > 0.0F) {
               renderShadow(matrices, vertexConsumers, entity, h, tickDelta, this.world, entityRenderer.shadowRadius);
            }
         }

         if (this.renderHitboxes && !entity.isInvisible() && !MinecraftClient.getInstance().hasReducedDebugInfo()) {
            this.renderHitbox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), entity, tickDelta);
         }

         matrices.pop();
      } catch (Throwable var24) {
         CrashReport crashReport = CrashReport.create(var24, "Rendering entity in world");
         CrashReportSection crashReportSection = crashReport.addElement("Entity being rendered");
         entity.populateCrashReport(crashReportSection);
         CrashReportSection crashReportSection2 = crashReport.addElement("Renderer details");
         crashReportSection2.add("Assigned renderer", (Object)entityRenderer);
         crashReportSection2.add("Location", (Object)CrashReportSection.createPositionString(x, y, z));
         crashReportSection2.add("Rotation", (Object)yaw);
         crashReportSection2.add("Delta", (Object)tickDelta);
         throw new CrashException(crashReport);
      }
   }

   private void renderHitbox(MatrixStack matrices, VertexConsumer vertices, Entity entity, float tickDelta) {
      float f = entity.getWidth() / 2.0F;
      this.drawBox(matrices, vertices, entity, 1.0F, 1.0F, 1.0F);
      if (entity instanceof EnderDragonEntity) {
         double d = -MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
         double e = -MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
         double g = -MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
         EnderDragonPart[] var12 = ((EnderDragonEntity)entity).getBodyParts();
         int var13 = var12.length;

         for(int var14 = 0; var14 < var13; ++var14) {
            EnderDragonPart enderDragonPart = var12[var14];
            matrices.push();
            double h = d + MathHelper.lerp((double)tickDelta, enderDragonPart.lastRenderX, enderDragonPart.getX());
            double i = e + MathHelper.lerp((double)tickDelta, enderDragonPart.lastRenderY, enderDragonPart.getY());
            double j = g + MathHelper.lerp((double)tickDelta, enderDragonPart.lastRenderZ, enderDragonPart.getZ());
            matrices.translate(h, i, j);
            this.drawBox(matrices, vertices, enderDragonPart, 0.25F, 1.0F, 0.0F);
            matrices.pop();
         }
      }

      if (entity instanceof LivingEntity) {
         float k = 0.01F;
         WorldRenderer.drawBox(matrices, vertices, (double)(-f), (double)(entity.getStandingEyeHeight() - 0.01F), (double)(-f), (double)f, (double)(entity.getStandingEyeHeight() + 0.01F), (double)f, 1.0F, 0.0F, 0.0F, 1.0F);
      }

      Vec3d vec3d = entity.getRotationVec(tickDelta);
      Matrix4f matrix4f = matrices.peek().getModel();
      vertices.vertex(matrix4f, 0.0F, entity.getStandingEyeHeight(), 0.0F).color(0, 0, 255, 255).next();
      vertices.vertex(matrix4f, (float)(vec3d.x * 2.0D), (float)((double)entity.getStandingEyeHeight() + vec3d.y * 2.0D), (float)(vec3d.z * 2.0D)).color(0, 0, 255, 255).next();
   }

   private void drawBox(MatrixStack matrix, VertexConsumer vertices, Entity entity, float red, float green, float blue) {
      Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());
      WorldRenderer.drawBox(matrix, vertices, box, red, green, blue, 1.0F);
   }

   private void renderFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity) {
      Sprite sprite = ModelLoader.FIRE_0.getSprite();
      Sprite sprite2 = ModelLoader.FIRE_1.getSprite();
      matrices.push();
      float f = entity.getWidth() * 1.4F;
      matrices.scale(f, f, f);
      float g = 0.5F;
      float h = 0.0F;
      float i = entity.getHeight() / f;
      float j = 0.0F;
      matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-this.camera.getYaw()));
      matrices.translate(0.0D, 0.0D, (double)(-0.3F + (float)((int)i) * 0.02F));
      float k = 0.0F;
      int l = 0;
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());

      for(MatrixStack.Entry entry = matrices.peek(); i > 0.0F; ++l) {
         Sprite sprite3 = l % 2 == 0 ? sprite : sprite2;
         float m = sprite3.getMinU();
         float n = sprite3.getMinV();
         float o = sprite3.getMaxU();
         float p = sprite3.getMaxV();
         if (l / 2 % 2 == 0) {
            float q = o;
            o = m;
            m = q;
         }

         drawFireVertex(entry, vertexConsumer, g - 0.0F, 0.0F - j, k, o, p);
         drawFireVertex(entry, vertexConsumer, -g - 0.0F, 0.0F - j, k, m, p);
         drawFireVertex(entry, vertexConsumer, -g - 0.0F, 1.4F - j, k, m, n);
         drawFireVertex(entry, vertexConsumer, g - 0.0F, 1.4F - j, k, o, n);
         i -= 0.45F;
         j -= 0.45F;
         g *= 0.9F;
         k += 0.03F;
      }

      matrices.pop();
   }

   private static void drawFireVertex(MatrixStack.Entry entry, VertexConsumer vertices, float x, float y, float z, float u, float v) {
      vertices.vertex(entry.getModel(), x, y, z).color(255, 255, 255, 255).texture(u, v).overlay(0, 10).light(240).normal(entry.getNormal(), 0.0F, 1.0F, 0.0F).next();
   }

   private static void renderShadow(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Entity entity, float opacity, float tickDelta, WorldView world, float radius) {
      float f = radius;
      if (entity instanceof MobEntity) {
         MobEntity mobEntity = (MobEntity)entity;
         if (mobEntity.isBaby()) {
            f = radius * 0.5F;
         }
      }

      double d = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
      double e = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY());
      double g = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
      int i = MathHelper.floor(d - (double)f);
      int j = MathHelper.floor(d + (double)f);
      int k = MathHelper.floor(e - (double)f);
      int l = MathHelper.floor(e);
      int m = MathHelper.floor(g - (double)f);
      int n = MathHelper.floor(g + (double)f);
      MatrixStack.Entry entry = matrices.peek();
      VertexConsumer vertexConsumer = vertexConsumers.getBuffer(SHADOW_LAYER);
      Iterator var22 = BlockPos.iterate(new BlockPos(i, k, m), new BlockPos(j, l, n)).iterator();

      while(var22.hasNext()) {
         BlockPos blockPos = (BlockPos)var22.next();
         renderShadowPart(entry, vertexConsumer, world, blockPos, d, e, g, f, opacity);
      }

   }

   private static void renderShadowPart(MatrixStack.Entry entry, VertexConsumer vertices, WorldView world, BlockPos pos, double x, double y, double z, float radius, float opacity) {
      BlockPos blockPos = pos.down();
      BlockState blockState = world.getBlockState(blockPos);
      if (blockState.getRenderType() != BlockRenderType.INVISIBLE && world.getLightLevel(pos) > 3) {
         if (blockState.isFullCube(world, blockPos)) {
            VoxelShape voxelShape = blockState.getOutlineShape(world, pos.down());
            if (!voxelShape.isEmpty()) {
               float f = (float)(((double)opacity - (y - (double)pos.getY()) / 2.0D) * 0.5D * (double)world.getBrightness(pos));
               if (f >= 0.0F) {
                  if (f > 1.0F) {
                     f = 1.0F;
                  }

                  Box box = voxelShape.getBoundingBox();
                  double d = (double)pos.getX() + box.minX;
                  double e = (double)pos.getX() + box.maxX;
                  double g = (double)pos.getY() + box.minY;
                  double h = (double)pos.getZ() + box.minZ;
                  double i = (double)pos.getZ() + box.maxZ;
                  float j = (float)(d - x);
                  float k = (float)(e - x);
                  float l = (float)(g - y);
                  float m = (float)(h - z);
                  float n = (float)(i - z);
                  float o = -j / 2.0F / radius + 0.5F;
                  float p = -k / 2.0F / radius + 0.5F;
                  float q = -m / 2.0F / radius + 0.5F;
                  float r = -n / 2.0F / radius + 0.5F;
                  drawShadowVertex(entry, vertices, f, j, l, m, o, q);
                  drawShadowVertex(entry, vertices, f, j, l, n, o, r);
                  drawShadowVertex(entry, vertices, f, k, l, n, p, r);
                  drawShadowVertex(entry, vertices, f, k, l, m, p, q);
               }

            }
         }
      }
   }

   private static void drawShadowVertex(MatrixStack.Entry entry, VertexConsumer vertices, float alpha, float x, float y, float z, float u, float v) {
      vertices.vertex(entry.getModel(), x, y, z).color(1.0F, 1.0F, 1.0F, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(entry.getNormal(), 0.0F, 1.0F, 0.0F).next();
   }

   public void setWorld(@Nullable World world) {
      this.world = world;
      if (world == null) {
         this.camera = null;
      }

   }

   public double getSquaredDistanceToCamera(Entity entity) {
      return this.camera.getPos().squaredDistanceTo(entity.getPos());
   }

   public double getSquaredDistanceToCamera(double x, double y, double z) {
      return this.camera.getPos().squaredDistanceTo(x, y, z);
   }

   public Quaternion getRotation() {
      return this.rotation;
   }

   public TextRenderer getTextRenderer() {
      return this.textRenderer;
   }
}
