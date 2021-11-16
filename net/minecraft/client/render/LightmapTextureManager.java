package net.minecraft.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class LightmapTextureManager implements AutoCloseable {
   private final NativeImageBackedTexture texture;
   private final NativeImage image;
   private final Identifier textureIdentifier;
   private boolean dirty;
   private float field_21528;
   private final GameRenderer renderer;
   private final MinecraftClient client;

   public LightmapTextureManager(GameRenderer renderer, MinecraftClient client) {
      this.renderer = renderer;
      this.client = client;
      this.texture = new NativeImageBackedTexture(16, 16, false);
      this.textureIdentifier = this.client.getTextureManager().registerDynamicTexture("light_map", this.texture);
      this.image = this.texture.getImage();

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            this.image.setPixelColor(j, i, -1);
         }
      }

      this.texture.upload();
   }

   public void close() {
      this.texture.close();
   }

   public void tick() {
      this.field_21528 = (float)((double)this.field_21528 + (Math.random() - Math.random()) * Math.random() * Math.random() * 0.1D);
      this.field_21528 = (float)((double)this.field_21528 * 0.9D);
      this.dirty = true;
   }

   public void disable() {
      RenderSystem.activeTexture(33986);
      RenderSystem.disableTexture();
      RenderSystem.activeTexture(33984);
   }

   public void enable() {
      RenderSystem.activeTexture(33986);
      RenderSystem.matrixMode(5890);
      RenderSystem.loadIdentity();
      float f = 0.00390625F;
      RenderSystem.scalef(0.00390625F, 0.00390625F, 0.00390625F);
      RenderSystem.translatef(8.0F, 8.0F, 8.0F);
      RenderSystem.matrixMode(5888);
      this.client.getTextureManager().bindTexture(this.textureIdentifier);
      RenderSystem.texParameter(3553, 10241, 9729);
      RenderSystem.texParameter(3553, 10240, 9729);
      RenderSystem.texParameter(3553, 10242, 10496);
      RenderSystem.texParameter(3553, 10243, 10496);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableTexture();
      RenderSystem.activeTexture(33984);
   }

   public void update(float delta) {
      if (this.dirty) {
         this.dirty = false;
         this.client.getProfiler().push("lightTex");
         ClientWorld clientWorld = this.client.world;
         if (clientWorld != null) {
            float f = clientWorld.method_23783(1.0F);
            float h;
            if (clientWorld.getLightningTicksLeft() > 0) {
               h = 1.0F;
            } else {
               h = f * 0.95F + 0.05F;
            }

            float i = this.client.player.getUnderwaterVisibility();
            float l;
            if (this.client.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
               l = GameRenderer.getNightVisionStrength(this.client.player, delta);
            } else if (i > 0.0F && this.client.player.hasStatusEffect(StatusEffects.CONDUIT_POWER)) {
               l = i;
            } else {
               l = 0.0F;
            }

            Vec3f vec3f = new Vec3f(f, f, 1.0F);
            vec3f.lerp(new Vec3f(1.0F, 1.0F, 1.0F), 0.35F);
            float m = this.field_21528 + 1.5F;
            Vec3f vec3f2 = new Vec3f();

            for(int n = 0; n < 16; ++n) {
               for(int o = 0; o < 16; ++o) {
                  float p = this.getBrightness(clientWorld, n) * h;
                  float q = this.getBrightness(clientWorld, o) * m;
                  float s = q * ((q * 0.6F + 0.4F) * 0.6F + 0.4F);
                  float t = q * (q * q * 0.6F + 0.4F);
                  vec3f2.set(q, s, t);
                  float w;
                  Vec3f vec3f5;
                  if (clientWorld.getSkyProperties().shouldBrightenLighting()) {
                     vec3f2.lerp(new Vec3f(0.99F, 1.12F, 1.0F), 0.25F);
                  } else {
                     Vec3f vec3f3 = vec3f.copy();
                     vec3f3.scale(p);
                     vec3f2.add(vec3f3);
                     vec3f2.lerp(new Vec3f(0.75F, 0.75F, 0.75F), 0.04F);
                     if (this.renderer.getSkyDarkness(delta) > 0.0F) {
                        w = this.renderer.getSkyDarkness(delta);
                        vec3f5 = vec3f2.copy();
                        vec3f5.multiplyComponentwise(0.7F, 0.6F, 0.6F);
                        vec3f2.lerp(vec3f5, w);
                     }
                  }

                  vec3f2.clamp(0.0F, 1.0F);
                  float v;
                  if (l > 0.0F) {
                     v = Math.max(vec3f2.getX(), Math.max(vec3f2.getY(), vec3f2.getZ()));
                     if (v < 1.0F) {
                        w = 1.0F / v;
                        vec3f5 = vec3f2.copy();
                        vec3f5.scale(w);
                        vec3f2.lerp(vec3f5, l);
                     }
                  }

                  v = (float)this.client.options.gamma;
                  Vec3f vec3f6 = vec3f2.copy();
                  vec3f6.modify(this::method_23795);
                  vec3f2.lerp(vec3f6, v);
                  vec3f2.lerp(new Vec3f(0.75F, 0.75F, 0.75F), 0.04F);
                  vec3f2.clamp(0.0F, 1.0F);
                  vec3f2.scale(255.0F);
                  int y = true;
                  int z = (int)vec3f2.getX();
                  int aa = (int)vec3f2.getY();
                  int ab = (int)vec3f2.getZ();
                  this.image.setPixelColor(o, n, -16777216 | ab << 16 | aa << 8 | z);
               }
            }

            this.texture.upload();
            this.client.getProfiler().pop();
         }
      }
   }

   private float method_23795(float f) {
      float g = 1.0F - f;
      return 1.0F - g * g * g * g;
   }

   private float getBrightness(World world, int i) {
      return world.getDimension().method_28516(i);
   }

   public static int pack(int block, int sky) {
      return block << 4 | sky << 20;
   }

   public static int getBlockLightCoordinates(int light) {
      return light >> 4 & '\uffff';
   }

   public static int getSkyLightCoordinates(int light) {
      return light >> 20 & '\uffff';
   }
}
