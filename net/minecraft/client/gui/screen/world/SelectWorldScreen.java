package net.minecraft.client.gui.screen.world;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class SelectWorldScreen extends Screen {
   protected final Screen parent;
   private List<OrderedText> tooltipText;
   private ButtonWidget deleteButton;
   private ButtonWidget selectButton;
   private ButtonWidget editButton;
   private ButtonWidget recreateButton;
   protected TextFieldWidget searchBox;
   private WorldListWidget levelList;

   public SelectWorldScreen(Screen parent) {
      super(new TranslatableText("selectWorld.title"));
      this.parent = parent;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return super.mouseScrolled(mouseX, mouseY, amount);
   }

   public void tick() {
      this.searchBox.tick();
   }

   protected void init() {
      this.client.keyboard.setRepeatEvents(true);
      this.searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 22, 200, 20, this.searchBox, new TranslatableText("selectWorld.search"));
      this.searchBox.setChangedListener((string) -> {
         this.levelList.filter(() -> {
            return string;
         }, false);
      });
      this.levelList = new WorldListWidget(this, this.client, this.width, this.height, 48, this.height - 64, 36, () -> {
         return this.searchBox.getText();
      }, this.levelList);
      this.children.add(this.searchBox);
      this.children.add(this.levelList);
      this.selectButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 52, 150, 20, new TranslatableText("selectWorld.select"), (buttonWidget) -> {
         this.levelList.getSelectedAsOptional().ifPresent(WorldListWidget.Entry::play);
      }));
      this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 52, 150, 20, new TranslatableText("selectWorld.create"), (buttonWidget) -> {
         this.client.openScreen(CreateWorldScreen.create(this));
      }));
      this.editButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 154, this.height - 28, 72, 20, new TranslatableText("selectWorld.edit"), (buttonWidget) -> {
         this.levelList.getSelectedAsOptional().ifPresent(WorldListWidget.Entry::edit);
      }));
      this.deleteButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 76, this.height - 28, 72, 20, new TranslatableText("selectWorld.delete"), (buttonWidget) -> {
         this.levelList.getSelectedAsOptional().ifPresent(WorldListWidget.Entry::delete);
      }));
      this.recreateButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 28, 72, 20, new TranslatableText("selectWorld.recreate"), (buttonWidget) -> {
         this.levelList.getSelectedAsOptional().ifPresent(WorldListWidget.Entry::recreate);
      }));
      this.addButton(new ButtonWidget(this.width / 2 + 82, this.height - 28, 72, 20, ScreenTexts.CANCEL, (buttonWidget) -> {
         this.client.openScreen(this.parent);
      }));
      this.worldSelected(false);
      this.setInitialFocus(this.searchBox);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return super.keyPressed(keyCode, scanCode, modifiers) ? true : this.searchBox.keyPressed(keyCode, scanCode, modifiers);
   }

   public void onClose() {
      this.client.openScreen(this.parent);
   }

   public boolean charTyped(char chr, int modifiers) {
      return this.searchBox.charTyped(chr, modifiers);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.tooltipText = null;
      this.levelList.render(matrices, mouseX, mouseY, delta);
      this.searchBox.render(matrices, mouseX, mouseY, delta);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
      if (this.tooltipText != null) {
         this.renderOrderedTooltip(matrices, this.tooltipText, mouseX, mouseY);
      }

   }

   public void setTooltip(List<OrderedText> tooltipText) {
      this.tooltipText = tooltipText;
   }

   public void worldSelected(boolean active) {
      this.selectButton.active = active;
      this.deleteButton.active = active;
      this.editButton.active = active;
      this.recreateButton.active = active;
   }

   public void removed() {
      if (this.levelList != null) {
         this.levelList.children().forEach(WorldListWidget.Entry::close);
      }

   }
}
