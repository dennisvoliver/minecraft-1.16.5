package net.minecraft.client.render;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.SignType;

@Environment(EnvType.CLIENT)
public class TexturedRenderLayers {
   public static final Identifier SHULKER_BOXES_ATLAS_TEXTURE = new Identifier("textures/atlas/shulker_boxes.png");
   public static final Identifier BEDS_ATLAS_TEXTURE = new Identifier("textures/atlas/beds.png");
   public static final Identifier BANNER_PATTERNS_ATLAS_TEXTURE = new Identifier("textures/atlas/banner_patterns.png");
   public static final Identifier SHIELD_PATTERNS_ATLAS_TEXTURE = new Identifier("textures/atlas/shield_patterns.png");
   public static final Identifier SIGNS_ATLAS_TEXTURE = new Identifier("textures/atlas/signs.png");
   public static final Identifier CHEST_ATLAS_TEXTURE = new Identifier("textures/atlas/chest.png");
   private static final RenderLayer SHULKER_BOXES_RENDER_LAYER;
   private static final RenderLayer BEDS_RENDER_LAYER;
   private static final RenderLayer BANNER_PATTERNS_RENDER_LAYER;
   private static final RenderLayer SHIELD_PATTERNS_RENDER_LAYER;
   private static final RenderLayer SIGN_RENDER_LAYER;
   private static final RenderLayer CHEST_RENDER_LAYER;
   private static final RenderLayer ENTITY_SOLID;
   private static final RenderLayer ENTITY_CUTOUT;
   private static final RenderLayer ITEM_ENTITY_TRANSLUCENT_CULL;
   private static final RenderLayer ENTITY_TRANSLUCENT_CULL;
   public static final SpriteIdentifier SHULKER_TEXTURE_ID;
   public static final List<SpriteIdentifier> COLORED_SHULKER_BOXES_TEXTURES;
   public static final Map<SignType, SpriteIdentifier> WOOD_TYPE_TEXTURES;
   public static final SpriteIdentifier[] BED_TEXTURES;
   public static final SpriteIdentifier TRAPPED;
   public static final SpriteIdentifier TRAPPED_LEFT;
   public static final SpriteIdentifier TRAPPED_RIGHT;
   public static final SpriteIdentifier CHRISTMAS;
   public static final SpriteIdentifier CHRISTMAS_LEFT;
   public static final SpriteIdentifier CHRISTMAS_RIGHT;
   public static final SpriteIdentifier NORMAL;
   public static final SpriteIdentifier NORMAL_LEFT;
   public static final SpriteIdentifier NORMAL_RIGHT;
   public static final SpriteIdentifier ENDER;

   public static RenderLayer getBannerPatterns() {
      return BANNER_PATTERNS_RENDER_LAYER;
   }

   public static RenderLayer getShieldPatterns() {
      return SHIELD_PATTERNS_RENDER_LAYER;
   }

   public static RenderLayer getBeds() {
      return BEDS_RENDER_LAYER;
   }

   public static RenderLayer getShulkerBoxes() {
      return SHULKER_BOXES_RENDER_LAYER;
   }

   public static RenderLayer getSign() {
      return SIGN_RENDER_LAYER;
   }

   public static RenderLayer getChest() {
      return CHEST_RENDER_LAYER;
   }

   public static RenderLayer getEntitySolid() {
      return ENTITY_SOLID;
   }

   public static RenderLayer getEntityCutout() {
      return ENTITY_CUTOUT;
   }

   public static RenderLayer getItemEntityTranslucentCull() {
      return ITEM_ENTITY_TRANSLUCENT_CULL;
   }

   public static RenderLayer getEntityTranslucentCull() {
      return ENTITY_TRANSLUCENT_CULL;
   }

   public static void addDefaultTextures(Consumer<SpriteIdentifier> adder) {
      adder.accept(SHULKER_TEXTURE_ID);
      COLORED_SHULKER_BOXES_TEXTURES.forEach(adder);
      BannerPattern[] var1 = BannerPattern.values();
      int var2 = var1.length;

      int var3;
      for(var3 = 0; var3 < var2; ++var3) {
         BannerPattern bannerPattern = var1[var3];
         adder.accept(new SpriteIdentifier(BANNER_PATTERNS_ATLAS_TEXTURE, bannerPattern.getSpriteId(true)));
         adder.accept(new SpriteIdentifier(SHIELD_PATTERNS_ATLAS_TEXTURE, bannerPattern.getSpriteId(false)));
      }

      WOOD_TYPE_TEXTURES.values().forEach(adder);
      SpriteIdentifier[] var5 = BED_TEXTURES;
      var2 = var5.length;

      for(var3 = 0; var3 < var2; ++var3) {
         SpriteIdentifier spriteIdentifier = var5[var3];
         adder.accept(spriteIdentifier);
      }

      adder.accept(TRAPPED);
      adder.accept(TRAPPED_LEFT);
      adder.accept(TRAPPED_RIGHT);
      adder.accept(CHRISTMAS);
      adder.accept(CHRISTMAS_LEFT);
      adder.accept(CHRISTMAS_RIGHT);
      adder.accept(NORMAL);
      adder.accept(NORMAL_LEFT);
      adder.accept(NORMAL_RIGHT);
      adder.accept(ENDER);
   }

   public static SpriteIdentifier createSignTextureId(SignType type) {
      return new SpriteIdentifier(SIGNS_ATLAS_TEXTURE, new Identifier("entity/signs/" + type.getName()));
   }

   private static SpriteIdentifier getChestTextureId(String variant) {
      return new SpriteIdentifier(CHEST_ATLAS_TEXTURE, new Identifier("entity/chest/" + variant));
   }

   public static SpriteIdentifier getChestTexture(BlockEntity blockEntity, ChestType type, boolean christmas) {
      if (christmas) {
         return getChestTexture(type, CHRISTMAS, CHRISTMAS_LEFT, CHRISTMAS_RIGHT);
      } else if (blockEntity instanceof TrappedChestBlockEntity) {
         return getChestTexture(type, TRAPPED, TRAPPED_LEFT, TRAPPED_RIGHT);
      } else {
         return blockEntity instanceof EnderChestBlockEntity ? ENDER : getChestTexture(type, NORMAL, NORMAL_LEFT, NORMAL_RIGHT);
      }
   }

   private static SpriteIdentifier getChestTexture(ChestType type, SpriteIdentifier single, SpriteIdentifier left, SpriteIdentifier right) {
      switch(type) {
      case LEFT:
         return left;
      case RIGHT:
         return right;
      case SINGLE:
      default:
         return single;
      }
   }

   static {
      SHULKER_BOXES_RENDER_LAYER = RenderLayer.getEntityCutoutNoCull(SHULKER_BOXES_ATLAS_TEXTURE);
      BEDS_RENDER_LAYER = RenderLayer.getEntitySolid(BEDS_ATLAS_TEXTURE);
      BANNER_PATTERNS_RENDER_LAYER = RenderLayer.getEntityNoOutline(BANNER_PATTERNS_ATLAS_TEXTURE);
      SHIELD_PATTERNS_RENDER_LAYER = RenderLayer.getEntityNoOutline(SHIELD_PATTERNS_ATLAS_TEXTURE);
      SIGN_RENDER_LAYER = RenderLayer.getEntityCutoutNoCull(SIGNS_ATLAS_TEXTURE);
      CHEST_RENDER_LAYER = RenderLayer.getEntityCutout(CHEST_ATLAS_TEXTURE);
      ENTITY_SOLID = RenderLayer.getEntitySolid(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
      ENTITY_CUTOUT = RenderLayer.getEntityCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
      ITEM_ENTITY_TRANSLUCENT_CULL = RenderLayer.getItemEntityTranslucentCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
      ENTITY_TRANSLUCENT_CULL = RenderLayer.getEntityTranslucentCull(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
      SHULKER_TEXTURE_ID = new SpriteIdentifier(SHULKER_BOXES_ATLAS_TEXTURE, new Identifier("entity/shulker/shulker"));
      COLORED_SHULKER_BOXES_TEXTURES = (List)Stream.of("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black").map((string) -> {
         return new SpriteIdentifier(SHULKER_BOXES_ATLAS_TEXTURE, new Identifier("entity/shulker/shulker_" + string));
      }).collect(ImmutableList.toImmutableList());
      WOOD_TYPE_TEXTURES = (Map)SignType.stream().collect(Collectors.toMap(Function.identity(), TexturedRenderLayers::createSignTextureId));
      BED_TEXTURES = (SpriteIdentifier[])Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map((dyeColor) -> {
         return new SpriteIdentifier(BEDS_ATLAS_TEXTURE, new Identifier("entity/bed/" + dyeColor.getName()));
      }).toArray((i) -> {
         return new SpriteIdentifier[i];
      });
      TRAPPED = getChestTextureId("trapped");
      TRAPPED_LEFT = getChestTextureId("trapped_left");
      TRAPPED_RIGHT = getChestTextureId("trapped_right");
      CHRISTMAS = getChestTextureId("christmas");
      CHRISTMAS_LEFT = getChestTextureId("christmas_left");
      CHRISTMAS_RIGHT = getChestTextureId("christmas_right");
      NORMAL = getChestTextureId("normal");
      NORMAL_LEFT = getChestTextureId("normal_left");
      NORMAL_RIGHT = getChestTextureId("normal_right");
      ENDER = getChestTextureId("ender");
   }
}
