package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public abstract class PosRuleTest {
   public static final Codec<PosRuleTest> field_25007;

   public abstract boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Random random);

   protected abstract PosRuleTestType<?> getType();

   static {
      field_25007 = Registry.POS_RULE_TEST.dispatch("predicate_type", PosRuleTest::getType, PosRuleTestType::codec);
   }
}
