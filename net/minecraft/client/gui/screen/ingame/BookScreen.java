package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BookScreen extends Screen {
   public static final BookScreen.Contents EMPTY_PROVIDER = new BookScreen.Contents() {
      public int getPageCount() {
         return 0;
      }

      public StringVisitable getPageUnchecked(int index) {
         return StringVisitable.EMPTY;
      }
   };
   public static final Identifier BOOK_TEXTURE = new Identifier("textures/gui/book.png");
   private BookScreen.Contents contents;
   private int pageIndex;
   private List<OrderedText> cachedPage;
   private int cachedPageIndex;
   private Text pageIndexText;
   private PageTurnWidget nextPageButton;
   private PageTurnWidget previousPageButton;
   private final boolean pageTurnSound;

   public BookScreen(BookScreen.Contents pageProvider) {
      this(pageProvider, true);
   }

   public BookScreen() {
      this(EMPTY_PROVIDER, false);
   }

   private BookScreen(BookScreen.Contents contents, boolean playPageTurnSound) {
      super(NarratorManager.EMPTY);
      this.cachedPage = Collections.emptyList();
      this.cachedPageIndex = -1;
      this.pageIndexText = LiteralText.EMPTY;
      this.contents = contents;
      this.pageTurnSound = playPageTurnSound;
   }

   public void setPageProvider(BookScreen.Contents pageProvider) {
      this.contents = pageProvider;
      this.pageIndex = MathHelper.clamp(this.pageIndex, 0, pageProvider.getPageCount());
      this.updatePageButtons();
      this.cachedPageIndex = -1;
   }

   public boolean setPage(int index) {
      int i = MathHelper.clamp(index, 0, this.contents.getPageCount() - 1);
      if (i != this.pageIndex) {
         this.pageIndex = i;
         this.updatePageButtons();
         this.cachedPageIndex = -1;
         return true;
      } else {
         return false;
      }
   }

   protected boolean jumpToPage(int page) {
      return this.setPage(page);
   }

   protected void init() {
      this.addCloseButton();
      this.addPageButtons();
   }

   protected void addCloseButton() {
      this.addButton(new ButtonWidget(this.width / 2 - 100, 196, 200, 20, ScreenTexts.DONE, (buttonWidget) -> {
         this.client.openScreen((Screen)null);
      }));
   }

   protected void addPageButtons() {
      int i = (this.width - 192) / 2;
      int j = true;
      this.nextPageButton = (PageTurnWidget)this.addButton(new PageTurnWidget(i + 116, 159, true, (buttonWidget) -> {
         this.goToNextPage();
      }, this.pageTurnSound));
      this.previousPageButton = (PageTurnWidget)this.addButton(new PageTurnWidget(i + 43, 159, false, (buttonWidget) -> {
         this.goToPreviousPage();
      }, this.pageTurnSound));
      this.updatePageButtons();
   }

   private int getPageCount() {
      return this.contents.getPageCount();
   }

   protected void goToPreviousPage() {
      if (this.pageIndex > 0) {
         --this.pageIndex;
      }

      this.updatePageButtons();
   }

   protected void goToNextPage() {
      if (this.pageIndex < this.getPageCount() - 1) {
         ++this.pageIndex;
      }

      this.updatePageButtons();
   }

   private void updatePageButtons() {
      this.nextPageButton.visible = this.pageIndex < this.getPageCount() - 1;
      this.previousPageButton.visible = this.pageIndex > 0;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (super.keyPressed(keyCode, scanCode, modifiers)) {
         return true;
      } else {
         switch(keyCode) {
         case 266:
            this.previousPageButton.onPress();
            return true;
         case 267:
            this.nextPageButton.onPress();
            return true;
         default:
            return false;
         }
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.client.getTextureManager().bindTexture(BOOK_TEXTURE);
      int i = (this.width - 192) / 2;
      int j = true;
      this.drawTexture(matrices, i, 2, 0, 0, 192, 192);
      if (this.cachedPageIndex != this.pageIndex) {
         StringVisitable stringVisitable = this.contents.getPage(this.pageIndex);
         this.cachedPage = this.textRenderer.wrapLines(stringVisitable, 114);
         this.pageIndexText = new TranslatableText("book.pageIndicator", new Object[]{this.pageIndex + 1, Math.max(this.getPageCount(), 1)});
      }

      this.cachedPageIndex = this.pageIndex;
      int k = this.textRenderer.getWidth((StringVisitable)this.pageIndexText);
      this.textRenderer.draw(matrices, (Text)this.pageIndexText, (float)(i - k + 192 - 44), 18.0F, 0);
      this.textRenderer.getClass();
      int l = Math.min(128 / 9, this.cachedPage.size());

      for(int m = 0; m < l; ++m) {
         OrderedText orderedText = (OrderedText)this.cachedPage.get(m);
         TextRenderer var10000 = this.textRenderer;
         float var10003 = (float)(i + 36);
         this.textRenderer.getClass();
         var10000.draw(matrices, (OrderedText)orderedText, var10003, (float)(32 + m * 9), 0);
      }

      Style style = this.getTextAt((double)mouseX, (double)mouseY);
      if (style != null) {
         this.renderTextHoverEffect(matrices, style, mouseX, mouseY);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         Style style = this.getTextAt(mouseX, mouseY);
         if (style != null && this.handleTextClick(style)) {
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean handleTextClick(Style style) {
      ClickEvent clickEvent = style.getClickEvent();
      if (clickEvent == null) {
         return false;
      } else if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
         String string = clickEvent.getValue();

         try {
            int i = Integer.parseInt(string) - 1;
            return this.jumpToPage(i);
         } catch (Exception var5) {
            return false;
         }
      } else {
         boolean bl = super.handleTextClick(style);
         if (bl && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
            this.client.openScreen((Screen)null);
         }

         return bl;
      }
   }

   @Nullable
   public Style getTextAt(double x, double y) {
      if (this.cachedPage.isEmpty()) {
         return null;
      } else {
         int i = MathHelper.floor(x - (double)((this.width - 192) / 2) - 36.0D);
         int j = MathHelper.floor(y - 2.0D - 30.0D);
         if (i >= 0 && j >= 0) {
            this.textRenderer.getClass();
            int k = Math.min(128 / 9, this.cachedPage.size());
            if (i <= 114) {
               this.client.textRenderer.getClass();
               if (j < 9 * k + k) {
                  this.client.textRenderer.getClass();
                  int l = j / 9;
                  if (l >= 0 && l < this.cachedPage.size()) {
                     OrderedText orderedText = (OrderedText)this.cachedPage.get(l);
                     return this.client.textRenderer.getTextHandler().getStyleAt(orderedText, i);
                  }

                  return null;
               }
            }

            return null;
         } else {
            return null;
         }
      }
   }

   public static List<String> readPages(NbtCompound nbt) {
      NbtList nbtList = nbt.getList("pages", 8).copy();
      Builder<String> builder = ImmutableList.builder();

      for(int i = 0; i < nbtList.size(); ++i) {
         builder.add((Object)nbtList.getString(i));
      }

      return builder.build();
   }

   @Environment(EnvType.CLIENT)
   public static class WritableBookContents implements BookScreen.Contents {
      private final List<String> pages;

      public WritableBookContents(ItemStack stack) {
         this.pages = getPages(stack);
      }

      private static List<String> getPages(ItemStack stack) {
         NbtCompound nbtCompound = stack.getTag();
         return (List)(nbtCompound != null ? BookScreen.readPages(nbtCompound) : ImmutableList.of());
      }

      public int getPageCount() {
         return this.pages.size();
      }

      public StringVisitable getPageUnchecked(int index) {
         return StringVisitable.plain((String)this.pages.get(index));
      }
   }

   @Environment(EnvType.CLIENT)
   public static class WrittenBookContents implements BookScreen.Contents {
      private final List<String> pages;

      public WrittenBookContents(ItemStack stack) {
         this.pages = getPages(stack);
      }

      private static List<String> getPages(ItemStack stack) {
         NbtCompound nbtCompound = stack.getTag();
         return (List)(nbtCompound != null && WrittenBookItem.isValid(nbtCompound) ? BookScreen.readPages(nbtCompound) : ImmutableList.of(Text.Serializer.toJson((new TranslatableText("book.invalid.tag")).formatted(Formatting.DARK_RED))));
      }

      public int getPageCount() {
         return this.pages.size();
      }

      public StringVisitable getPageUnchecked(int index) {
         String string = (String)this.pages.get(index);

         try {
            StringVisitable stringVisitable = Text.Serializer.fromJson(string);
            if (stringVisitable != null) {
               return stringVisitable;
            }
         } catch (Exception var4) {
         }

         return StringVisitable.plain(string);
      }
   }

   @Environment(EnvType.CLIENT)
   public interface Contents {
      int getPageCount();

      StringVisitable getPageUnchecked(int index);

      default StringVisitable getPage(int index) {
         return index >= 0 && index < this.getPageCount() ? this.getPageUnchecked(index) : StringVisitable.EMPTY;
      }

      static BookScreen.Contents create(ItemStack stack) {
         Item item = stack.getItem();
         if (item == Items.WRITTEN_BOOK) {
            return new BookScreen.WrittenBookContents(stack);
         } else {
            return (BookScreen.Contents)(item == Items.WRITABLE_BOOK ? new BookScreen.WritableBookContents(stack) : BookScreen.EMPTY_PROVIDER);
         }
      }
   }
}
