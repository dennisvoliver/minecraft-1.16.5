package net.minecraft.client.util;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.StringVisitable;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TextCollector {
   private final List<StringVisitable> field_25260 = Lists.newArrayList();

   public void add(StringVisitable text) {
      this.field_25260.add(text);
   }

   @Nullable
   public StringVisitable getRawCombined() {
      if (this.field_25260.isEmpty()) {
         return null;
      } else {
         return this.field_25260.size() == 1 ? (StringVisitable)this.field_25260.get(0) : StringVisitable.concat(this.field_25260);
      }
   }

   public StringVisitable getCombined() {
      StringVisitable stringVisitable = this.getRawCombined();
      return stringVisitable != null ? stringVisitable : StringVisitable.EMPTY;
   }
}
