package net.minecraft.world.gen.feature;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.tag.BlockTags;

public class OreFeatureConfig implements FeatureConfig {
   public static final Codec<OreFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(RuleTest.field_25012.fieldOf("target").forGetter((oreFeatureConfig) -> {
         return oreFeatureConfig.target;
      }), BlockState.CODEC.fieldOf("state").forGetter((oreFeatureConfig) -> {
         return oreFeatureConfig.state;
      }), Codec.intRange(0, 64).fieldOf("size").forGetter((oreFeatureConfig) -> {
         return oreFeatureConfig.size;
      })).apply(instance, (Function3)(OreFeatureConfig::new));
   });
   public final RuleTest target;
   public final int size;
   public final BlockState state;

   public OreFeatureConfig(RuleTest test, BlockState state, int size) {
      this.size = size;
      this.state = state;
      this.target = test;
   }

   public static final class Rules {
      public static final RuleTest BASE_STONE_OVERWORLD;
      public static final RuleTest NETHERRACK;
      public static final RuleTest BASE_STONE_NETHER;

      static {
         BASE_STONE_OVERWORLD = new TagMatchRuleTest(BlockTags.BASE_STONE_OVERWORLD);
         NETHERRACK = new BlockMatchRuleTest(Blocks.NETHERRACK);
         BASE_STONE_NETHER = new TagMatchRuleTest(BlockTags.BASE_STONE_NETHER);
      }
   }
}
