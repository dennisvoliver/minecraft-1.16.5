package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CommandSuggestor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.CommandBlockExecutor;

@Environment(EnvType.CLIENT)
public abstract class AbstractCommandBlockScreen extends Screen {
   private static final Text SET_COMMAND_TEXT = new TranslatableText("advMode.setCommand");
   private static final Text COMMAND_TEXT = new TranslatableText("advMode.command");
   private static final Text PREVIOUS_OUTPUT_TEXT = new TranslatableText("advMode.previousOutput");
   protected TextFieldWidget consoleCommandTextField;
   protected TextFieldWidget previousOutputTextField;
   protected ButtonWidget doneButton;
   protected ButtonWidget cancelButton;
   protected ButtonWidget toggleTrackingOutputButton;
   protected boolean trackingOutput;
   private CommandSuggestor commandSuggestor;

   public AbstractCommandBlockScreen() {
      super(NarratorManager.EMPTY);
   }

   public void tick() {
      this.consoleCommandTextField.tick();
   }

   abstract CommandBlockExecutor getCommandExecutor();

   abstract int getTrackOutputButtonHeight();

   protected void init() {
      this.client.keyboard.setRepeatEvents(true);
      this.doneButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, ScreenTexts.DONE, (buttonWidget) -> {
         this.commitAndClose();
      }));
      this.cancelButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20, ScreenTexts.CANCEL, (buttonWidget) -> {
         this.onClose();
      }));
      this.toggleTrackingOutputButton = (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 150 - 20, this.getTrackOutputButtonHeight(), 20, 20, new LiteralText("O"), (buttonWidget) -> {
         CommandBlockExecutor commandBlockExecutor = this.getCommandExecutor();
         commandBlockExecutor.setTrackingOutput(!commandBlockExecutor.isTrackingOutput());
         this.updateTrackedOutput();
      }));
      this.consoleCommandTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, 50, 300, 20, new TranslatableText("advMode.command")) {
         protected MutableText getNarrationMessage() {
            return super.getNarrationMessage().append(AbstractCommandBlockScreen.this.commandSuggestor.getNarration());
         }
      };
      this.consoleCommandTextField.setMaxLength(32500);
      this.consoleCommandTextField.setChangedListener(this::onCommandChanged);
      this.children.add(this.consoleCommandTextField);
      this.previousOutputTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, this.getTrackOutputButtonHeight(), 276, 20, new TranslatableText("advMode.previousOutput"));
      this.previousOutputTextField.setMaxLength(32500);
      this.previousOutputTextField.setEditable(false);
      this.previousOutputTextField.setText("-");
      this.children.add(this.previousOutputTextField);
      this.setInitialFocus(this.consoleCommandTextField);
      this.consoleCommandTextField.setTextFieldFocused(true);
      this.commandSuggestor = new CommandSuggestor(this.client, this, this.consoleCommandTextField, this.textRenderer, true, true, 0, 7, false, Integer.MIN_VALUE);
      this.commandSuggestor.setWindowActive(true);
      this.commandSuggestor.refresh();
   }

   public void resize(MinecraftClient client, int width, int height) {
      String string = this.consoleCommandTextField.getText();
      this.init(client, width, height);
      this.consoleCommandTextField.setText(string);
      this.commandSuggestor.refresh();
   }

   protected void updateTrackedOutput() {
      if (this.getCommandExecutor().isTrackingOutput()) {
         this.toggleTrackingOutputButton.setMessage(new LiteralText("O"));
         this.previousOutputTextField.setText(this.getCommandExecutor().getLastOutput().getString());
      } else {
         this.toggleTrackingOutputButton.setMessage(new LiteralText("X"));
         this.previousOutputTextField.setText("-");
      }

   }

   protected void commitAndClose() {
      CommandBlockExecutor commandBlockExecutor = this.getCommandExecutor();
      this.syncSettingsToServer(commandBlockExecutor);
      if (!commandBlockExecutor.isTrackingOutput()) {
         commandBlockExecutor.setLastOutput((Text)null);
      }

      this.client.openScreen((Screen)null);
   }

   public void removed() {
      this.client.keyboard.setRepeatEvents(false);
   }

   protected abstract void syncSettingsToServer(CommandBlockExecutor commandExecutor);

   public void onClose() {
      this.getCommandExecutor().setTrackingOutput(this.trackingOutput);
      this.client.openScreen((Screen)null);
   }

   private void onCommandChanged(String text) {
      this.commandSuggestor.refresh();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.commandSuggestor.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else if (keyCode != 257 && keyCode != 335) {
         return false;
      } else {
         this.commitAndClose();
         return true;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.commandSuggestor.mouseScrolled(amount) ? true : super.mouseScrolled(mouseX, mouseY, amount);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.commandSuggestor.mouseClicked(mouseX, mouseY, button) ? true : super.mouseClicked(mouseX, mouseY, button);
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, SET_COMMAND_TEXT, this.width / 2, 20, 16777215);
      drawTextWithShadow(matrices, this.textRenderer, COMMAND_TEXT, this.width / 2 - 150, 40, 10526880);
      this.consoleCommandTextField.render(matrices, mouseX, mouseY, delta);
      int i = 75;
      if (!this.previousOutputTextField.getText().isEmpty()) {
         this.textRenderer.getClass();
         int i = i + (5 * 9 + 1 + this.getTrackOutputButtonHeight() - 135);
         drawTextWithShadow(matrices, this.textRenderer, PREVIOUS_OUTPUT_TEXT, this.width / 2 - 150, i + 4, 10526880);
         this.previousOutputTextField.render(matrices, mouseX, mouseY, delta);
      }

      super.render(matrices, mouseX, mouseY, delta);
      this.commandSuggestor.render(matrices, mouseX, mouseY);
   }
}
