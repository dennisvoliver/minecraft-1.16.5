package net.minecraft.client.gui.screen.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class ChatOptionsScreen extends NarratorOptionsScreen {
   private static final Option[] OPTIONS;

   public ChatOptionsScreen(Screen parent, GameOptions options) {
      super(parent, options, new TranslatableText("options.chat.title"), OPTIONS);
   }

   static {
      OPTIONS = new Option[]{Option.VISIBILITY, Option.CHAT_COLOR, Option.CHAT_LINKS, Option.CHAT_LINKS_PROMPT, Option.CHAT_OPACITY, Option.TEXT_BACKGROUND_OPACITY, Option.CHAT_SCALE, Option.CHAT_LINE_SPACING, Option.CHAT_DELAY_INSTANT, Option.CHAT_WIDTH, Option.CHAT_HEIGHT_FOCUSED, Option.SATURATION, Option.NARRATOR, Option.AUTO_SUGGESTIONS, Option.field_26924, Option.REDUCED_DEBUG_INFO};
   }
}
