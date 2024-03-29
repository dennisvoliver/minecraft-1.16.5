package net.minecraft.client.gui.screen.ingame;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;

@Environment(EnvType.CLIENT)
public class LecternScreen extends BookScreen implements ScreenHandlerProvider<LecternScreenHandler> {
   private final LecternScreenHandler handler;
   private final ScreenHandlerListener listener = new ScreenHandlerListener() {
      public void onHandlerRegistered(ScreenHandler handler, DefaultedList<ItemStack> stacks) {
         LecternScreen.this.updatePageProvider();
      }

      public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
         LecternScreen.this.updatePageProvider();
      }

      public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
         if (property == 0) {
            LecternScreen.this.updatePage();
         }

      }
   };

   public LecternScreen(LecternScreenHandler handler, PlayerInventory inventory, Text title) {
      this.handler = handler;
   }

   public LecternScreenHandler getScreenHandler() {
      return this.handler;
   }

   protected void init() {
      super.init();
      this.handler.addListener(this.listener);
   }

   public void onClose() {
      this.client.player.closeHandledScreen();
      super.onClose();
   }

   public void removed() {
      super.removed();
      this.handler.removeListener(this.listener);
   }

   protected void addCloseButton() {
      if (this.client.player.canModifyBlocks()) {
         this.addButton(new ButtonWidget(this.width / 2 - 100, 196, 98, 20, ScreenTexts.DONE, (buttonWidget) -> {
            this.client.openScreen((Screen)null);
         }));
         this.addButton(new ButtonWidget(this.width / 2 + 2, 196, 98, 20, new TranslatableText("lectern.take_book"), (buttonWidget) -> {
            this.sendButtonPressPacket(3);
         }));
      } else {
         super.addCloseButton();
      }

   }

   protected void goToPreviousPage() {
      this.sendButtonPressPacket(1);
   }

   protected void goToNextPage() {
      this.sendButtonPressPacket(2);
   }

   protected boolean jumpToPage(int page) {
      if (page != this.handler.getPage()) {
         this.sendButtonPressPacket(100 + page);
         return true;
      } else {
         return false;
      }
   }

   private void sendButtonPressPacket(int id) {
      this.client.interactionManager.clickButton(this.handler.syncId, id);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void updatePageProvider() {
      ItemStack itemStack = this.handler.getBookItem();
      this.setPageProvider(BookScreen.Contents.create(itemStack));
   }

   private void updatePage() {
      this.setPage(this.handler.getPage());
   }
}
