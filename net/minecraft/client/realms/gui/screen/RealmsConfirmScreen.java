package net.minecraft.client.realms.gui.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class RealmsConfirmScreen extends RealmsScreen {
   protected BooleanConsumer field_22692;
   private final Text title1;
   private final Text title2;
   private int delayTicker;

   public RealmsConfirmScreen(BooleanConsumer booleanConsumer, Text title1, Text title2) {
      this.field_22692 = booleanConsumer;
      this.title1 = title1;
      this.title2 = title2;
   }

   public void init() {
      this.addButton(new ButtonWidget(this.width / 2 - 105, row(9), 100, 20, ScreenTexts.YES, (buttonWidget) -> {
         this.field_22692.accept(true);
      }));
      this.addButton(new ButtonWidget(this.width / 2 + 5, row(9), 100, 20, ScreenTexts.NO, (buttonWidget) -> {
         this.field_22692.accept(false);
      }));
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, this.title1, this.width / 2, row(3), 16777215);
      drawCenteredText(matrices, this.textRenderer, this.title2, this.width / 2, row(5), 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public void tick() {
      super.tick();
      ClickableWidget clickableWidget;
      if (--this.delayTicker == 0) {
         for(Iterator var1 = this.buttons.iterator(); var1.hasNext(); clickableWidget.active = true) {
            clickableWidget = (ClickableWidget)var1.next();
         }
      }

   }
}
