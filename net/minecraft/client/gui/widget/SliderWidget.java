package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public abstract class SliderWidget extends ClickableWidget {
   protected double value;

   public SliderWidget(int x, int y, int width, int height, Text text, double value) {
      super(x, y, width, height, text);
      this.value = value;
   }

   protected int getYImage(boolean hovered) {
      return 0;
   }

   protected MutableText getNarrationMessage() {
      return new TranslatableText("gui.narrate.slider", new Object[]{this.getMessage()});
   }

   protected void renderBackground(MatrixStack matrices, MinecraftClient client, int mouseX, int mouseY) {
      client.getTextureManager().bindTexture(WIDGETS_TEXTURE);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      int i = (this.isHovered() ? 2 : 1) * 20;
      this.drawTexture(matrices, this.x + (int)(this.value * (double)(this.width - 8)), this.y, 0, 46 + i, 4, 20);
      this.drawTexture(matrices, this.x + (int)(this.value * (double)(this.width - 8)) + 4, this.y, 196, 46 + i, 4, 20);
   }

   public void onClick(double mouseX, double mouseY) {
      this.setValueFromMouse(mouseX);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      boolean bl = keyCode == 263;
      if (bl || keyCode == 262) {
         float f = bl ? -1.0F : 1.0F;
         this.setValue(this.value + (double)(f / (float)(this.width - 8)));
      }

      return false;
   }

   private void setValueFromMouse(double mouseX) {
      this.setValue((mouseX - (double)(this.x + 4)) / (double)(this.width - 8));
   }

   private void setValue(double mouseX) {
      double d = this.value;
      this.value = MathHelper.clamp(mouseX, 0.0D, 1.0D);
      if (d != this.value) {
         this.applyValue();
      }

      this.updateMessage();
   }

   protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
      this.setValueFromMouse(mouseX);
      super.onDrag(mouseX, mouseY, deltaX, deltaY);
   }

   public void playDownSound(SoundManager soundManager) {
   }

   public void onRelease(double mouseX, double mouseY) {
      super.playDownSound(MinecraftClient.getInstance().getSoundManager());
   }

   protected abstract void updateMessage();

   protected abstract void applyValue();
}
