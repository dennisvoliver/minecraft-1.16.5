package net.minecraft.block.entity;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public enum BannerPattern {
   BASE("base", "b", false),
   SQUARE_BOTTOM_LEFT("square_bottom_left", "bl"),
   SQUARE_BOTTOM_RIGHT("square_bottom_right", "br"),
   SQUARE_TOP_LEFT("square_top_left", "tl"),
   SQUARE_TOP_RIGHT("square_top_right", "tr"),
   STRIPE_BOTTOM("stripe_bottom", "bs"),
   STRIPE_TOP("stripe_top", "ts"),
   STRIPE_LEFT("stripe_left", "ls"),
   STRIPE_RIGHT("stripe_right", "rs"),
   STRIPE_CENTER("stripe_center", "cs"),
   STRIPE_MIDDLE("stripe_middle", "ms"),
   STRIPE_DOWNRIGHT("stripe_downright", "drs"),
   STRIPE_DOWNLEFT("stripe_downleft", "dls"),
   STRIPE_SMALL("small_stripes", "ss"),
   CROSS("cross", "cr"),
   STRAIGHT_CROSS("straight_cross", "sc"),
   TRIANGLE_BOTTOM("triangle_bottom", "bt"),
   TRIANGLE_TOP("triangle_top", "tt"),
   TRIANGLES_BOTTOM("triangles_bottom", "bts"),
   TRIANGLES_TOP("triangles_top", "tts"),
   DIAGONAL_LEFT("diagonal_left", "ld"),
   DIAGONAL_RIGHT("diagonal_up_right", "rd"),
   DIAGONAL_LEFT_MIRROR("diagonal_up_left", "lud"),
   DIAGONAL_RIGHT_MIRROR("diagonal_right", "rud"),
   CIRCLE_MIDDLE("circle", "mc"),
   RHOMBUS_MIDDLE("rhombus", "mr"),
   HALF_VERTICAL("half_vertical", "vh"),
   HALF_HORIZONTAL("half_horizontal", "hh"),
   HALF_VERTICAL_MIRROR("half_vertical_right", "vhr"),
   HALF_HORIZONTAL_MIRROR("half_horizontal_bottom", "hhb"),
   BORDER("border", "bo"),
   CURLY_BORDER("curly_border", "cbo"),
   GRADIENT("gradient", "gra"),
   GRADIENT_UP("gradient_up", "gru"),
   BRICKS("bricks", "bri"),
   GLOBE("globe", "glb", true),
   CREEPER("creeper", "cre", true),
   SKULL("skull", "sku", true),
   FLOWER("flower", "flo", true),
   MOJANG("mojang", "moj", true),
   PIGLIN("piglin", "pig", true);

   private static final BannerPattern[] VALUES = values();
   public static final int COUNT = VALUES.length;
   public static final int field_24417 = (int)Arrays.stream(VALUES).filter((bannerPattern) -> {
      return bannerPattern.field_24419;
   }).count();
   public static final int LOOM_APPLICABLE_COUNT = COUNT - field_24417 - 1;
   private final boolean field_24419;
   private final String name;
   private final String id;

   private BannerPattern(String name, String id) {
      this(name, id, false);
   }

   private BannerPattern(String name, String id, boolean hasPatternItem) {
      this.name = name;
      this.id = id;
      this.field_24419 = hasPatternItem;
   }

   @Environment(EnvType.CLIENT)
   public Identifier getSpriteId(boolean banner) {
      String string = banner ? "banner" : "shield";
      return new Identifier("entity/" + string + "/" + this.getName());
   }

   @Environment(EnvType.CLIENT)
   public String getName() {
      return this.name;
   }

   public String getId() {
      return this.id;
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public static BannerPattern byId(String id) {
      BannerPattern[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         BannerPattern bannerPattern = var1[var3];
         if (bannerPattern.id.equals(id)) {
            return bannerPattern;
         }
      }

      return null;
   }

   public static class Patterns {
      private final List<Pair<BannerPattern, DyeColor>> entries = Lists.newArrayList();

      public BannerPattern.Patterns add(BannerPattern pattern, DyeColor color) {
         this.entries.add(Pair.of(pattern, color));
         return this;
      }

      public NbtList toTag() {
         NbtList nbtList = new NbtList();
         Iterator var2 = this.entries.iterator();

         while(var2.hasNext()) {
            Pair<BannerPattern, DyeColor> pair = (Pair)var2.next();
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putString("Pattern", ((BannerPattern)pair.getLeft()).id);
            nbtCompound.putInt("Color", ((DyeColor)pair.getRight()).getId());
            nbtList.add(nbtCompound);
         }

         return nbtList;
      }
   }
}
