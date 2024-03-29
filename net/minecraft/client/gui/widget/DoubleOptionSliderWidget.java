package net.minecraft.client.gui.widget;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.OrderableTooltip;
import net.minecraft.text.OrderedText;

@Environment(EnvType.CLIENT)
public class DoubleOptionSliderWidget extends OptionSliderWidget implements OrderableTooltip {
   private final DoubleOption option;

   public DoubleOptionSliderWidget(GameOptions gameOptions, int x, int y, int width, int height, DoubleOption option) {
      super(gameOptions, x, y, width, height, (double)((float)option.getRatio(option.get(gameOptions))));
      this.option = option;
      this.updateMessage();
   }

   protected void applyValue() {
      this.option.set(this.options, this.option.getValue(this.value));
      this.options.write();
   }

   protected void updateMessage() {
      this.setMessage(this.option.getDisplayString(this.options));
   }

   public Optional<List<OrderedText>> getOrderedTooltip() {
      return this.option.getTooltip();
   }
}
