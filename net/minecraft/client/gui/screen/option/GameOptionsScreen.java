package net.minecraft.client.gui.screen.option;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.OrderableTooltip;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class GameOptionsScreen extends Screen {
   protected final Screen parent;
   protected final GameOptions gameOptions;

   public GameOptionsScreen(Screen parent, GameOptions gameOptions, Text title) {
      super(title);
      this.parent = parent;
      this.gameOptions = gameOptions;
   }

   public void removed() {
      this.client.options.write();
   }

   public void onClose() {
      this.client.openScreen(this.parent);
   }

   @Nullable
   public static List<OrderedText> getHoveredButtonTooltip(ButtonListWidget buttonList, int mouseX, int mouseY) {
      Optional<ClickableWidget> optional = buttonList.getHoveredButton((double)mouseX, (double)mouseY);
      if (optional.isPresent() && optional.get() instanceof OrderableTooltip) {
         Optional<List<OrderedText>> optional2 = ((OrderableTooltip)optional.get()).getOrderedTooltip();
         return (List)optional2.orElse((Object)null);
      } else {
         return null;
      }
   }
}
