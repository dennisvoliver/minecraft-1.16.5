package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * A pressable widget has a press action. It is pressed when it is clicked. It is
 * also pressed when enter or space keys are pressed when it is selected.
 */
@Environment(EnvType.CLIENT)
public abstract class PressableWidget extends ClickableWidget {
   public PressableWidget(int i, int j, int k, int l, Text text) {
      super(i, j, k, l, text);
   }

   public abstract void onPress();

   public void onClick(double mouseX, double mouseY) {
      this.onPress();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.active && this.visible) {
         if (keyCode != 257 && keyCode != 32 && keyCode != 335) {
            return false;
         } else {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onPress();
            return true;
         }
      } else {
         return false;
      }
   }
}
