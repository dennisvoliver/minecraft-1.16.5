package net.minecraft.client.gui.screen;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class NoticeScreen extends Screen {
   private final Runnable actionHandler;
   protected final Text notice;
   private MultilineText noticeLines;
   protected final Text buttonText;
   private int field_2347;

   public NoticeScreen(Runnable actionHandler, Text title, Text notice) {
      this(actionHandler, title, notice, ScreenTexts.BACK);
   }

   public NoticeScreen(Runnable actionHandler, Text title, Text notice, Text buttonText) {
      super(title);
      this.noticeLines = MultilineText.EMPTY;
      this.actionHandler = actionHandler;
      this.notice = notice;
      this.buttonText = buttonText;
   }

   protected void init() {
      super.init();
      this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, this.buttonText, (buttonWidget) -> {
         this.actionHandler.run();
      }));
      this.noticeLines = MultilineText.create(this.textRenderer, this.notice, this.width - 50);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 70, 16777215);
      this.noticeLines.drawCenterWithShadow(matrices, this.width / 2, 90);
      super.render(matrices, mouseX, mouseY, delta);
   }

   public void tick() {
      super.tick();
      ClickableWidget clickableWidget;
      if (--this.field_2347 == 0) {
         for(Iterator var1 = this.buttons.iterator(); var1.hasNext(); clickableWidget.active = true) {
            clickableWidget = (ClickableWidget)var1.next();
         }
      }

   }
}
