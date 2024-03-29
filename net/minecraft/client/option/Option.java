package net.minecraft.client.option;

import com.mojang.blaze3d.platform.GlStateManager;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.resource.VideoWarningManager;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.Window;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public abstract class Option {
   public static final DoubleOption BIOME_BLEND_RADIUS = new DoubleOption("options.biomeBlendRadius", 0.0D, 7.0D, 1.0F, (gameOptions) -> {
      return (double)gameOptions.biomeBlendRadius;
   }, (gameOptions, biomeBlendRadius) -> {
      gameOptions.biomeBlendRadius = MathHelper.clamp((int)biomeBlendRadius, 0, 7);
      MinecraftClient.getInstance().worldRenderer.reload();
   }, (gameOptions, option) -> {
      double d = option.get(gameOptions);
      int i = (int)d * 2 + 1;
      return option.getGenericLabel(new TranslatableText("options.biomeBlendRadius." + i));
   });
   public static final DoubleOption CHAT_HEIGHT_FOCUSED = new DoubleOption("options.chat.height.focused", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.chatHeightFocused;
   }, (gameOptions, chatHeightFocused) -> {
      gameOptions.chatHeightFocused = chatHeightFocused;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, option) -> {
      double d = option.getRatio(option.get(gameOptions));
      return option.getPixelLabel(ChatHud.getHeight(d));
   });
   public static final DoubleOption SATURATION = new DoubleOption("options.chat.height.unfocused", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.chatHeightUnfocused;
   }, (gameOptions, chatHeightUnfocused) -> {
      gameOptions.chatHeightUnfocused = chatHeightUnfocused;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, option) -> {
      double d = option.getRatio(option.get(gameOptions));
      return option.getPixelLabel(ChatHud.getHeight(d));
   });
   public static final DoubleOption CHAT_OPACITY = new DoubleOption("options.chat.opacity", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.chatOpacity;
   }, (gameOptions, chatOpacity) -> {
      gameOptions.chatOpacity = chatOpacity;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, option) -> {
      double d = option.getRatio(option.get(gameOptions));
      return option.getPercentLabel(d * 0.9D + 0.1D);
   });
   public static final DoubleOption CHAT_SCALE = new DoubleOption("options.chat.scale", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.chatScale;
   }, (gameOptions, chatScale) -> {
      gameOptions.chatScale = chatScale;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, option) -> {
      double d = option.getRatio(option.get(gameOptions));
      return (Text)(d == 0.0D ? ScreenTexts.composeToggleText(option.getDisplayPrefix(), false) : option.getPercentLabel(d));
   });
   public static final DoubleOption CHAT_WIDTH = new DoubleOption("options.chat.width", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.chatWidth;
   }, (gameOptions, chatWidth) -> {
      gameOptions.chatWidth = chatWidth;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, option) -> {
      double d = option.getRatio(option.get(gameOptions));
      return option.getPixelLabel(ChatHud.getWidth(d));
   });
   public static final DoubleOption CHAT_LINE_SPACING = new DoubleOption("options.chat.line_spacing", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.chatLineSpacing;
   }, (gameOptions, chatLineSpacing) -> {
      gameOptions.chatLineSpacing = chatLineSpacing;
   }, (gameOptions, option) -> {
      return option.getPercentLabel(option.getRatio(option.get(gameOptions)));
   });
   public static final DoubleOption CHAT_DELAY_INSTANT = new DoubleOption("options.chat.delay_instant", 0.0D, 6.0D, 0.1F, (gameOptions) -> {
      return gameOptions.chatDelay;
   }, (gameOptions, chatDelay) -> {
      gameOptions.chatDelay = chatDelay;
   }, (gameOptions, option) -> {
      double d = option.get(gameOptions);
      return d <= 0.0D ? new TranslatableText("options.chat.delay_none") : new TranslatableText("options.chat.delay", new Object[]{String.format("%.1f", d)});
   });
   public static final DoubleOption FOV = new DoubleOption("options.fov", 30.0D, 110.0D, 1.0F, (gameOptions) -> {
      return gameOptions.fov;
   }, (gameOptions, fov) -> {
      gameOptions.fov = fov;
   }, (gameOptions, option) -> {
      double d = option.get(gameOptions);
      if (d == 70.0D) {
         return option.getGenericLabel(new TranslatableText("options.fov.min"));
      } else {
         return d == option.getMax() ? option.getGenericLabel(new TranslatableText("options.fov.max")) : option.getGenericLabel((int)d);
      }
   });
   private static final Text FOV_EFFECT_SCALE_TOOLTIP = new TranslatableText("options.fovEffectScale.tooltip");
   public static final DoubleOption FOV_EFFECT_SCALE = new DoubleOption("options.fovEffectScale", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return Math.pow((double)gameOptions.fovEffectScale, 2.0D);
   }, (gameOptions, fovEffectScale) -> {
      gameOptions.fovEffectScale = MathHelper.sqrt(fovEffectScale);
   }, (gameOptions, option) -> {
      option.setTooltip(MinecraftClient.getInstance().textRenderer.wrapLines(FOV_EFFECT_SCALE_TOOLTIP, 200));
      double d = option.getRatio(option.get(gameOptions));
      return d == 0.0D ? option.getGenericLabel(new TranslatableText("options.fovEffectScale.off")) : option.getPercentLabel(d);
   });
   private static final Text DISTORTION_EFFECT_SCALE_TOOLTIP = new TranslatableText("options.screenEffectScale.tooltip");
   public static final DoubleOption DISTORTION_EFFECT_SCALE = new DoubleOption("options.screenEffectScale", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return (double)gameOptions.distortionEffectScale;
   }, (gameOptions, distortionEffectScale) -> {
      gameOptions.distortionEffectScale = distortionEffectScale.floatValue();
   }, (gameOptions, option) -> {
      option.setTooltip(MinecraftClient.getInstance().textRenderer.wrapLines(DISTORTION_EFFECT_SCALE_TOOLTIP, 200));
      double d = option.getRatio(option.get(gameOptions));
      return d == 0.0D ? option.getGenericLabel(new TranslatableText("options.screenEffectScale.off")) : option.getPercentLabel(d);
   });
   public static final DoubleOption FRAMERATE_LIMIT = new DoubleOption("options.framerateLimit", 10.0D, 260.0D, 10.0F, (gameOptions) -> {
      return (double)gameOptions.maxFps;
   }, (gameOptions, maxFps) -> {
      gameOptions.maxFps = (int)maxFps;
      MinecraftClient.getInstance().getWindow().setFramerateLimit(gameOptions.maxFps);
   }, (gameOptions, option) -> {
      double d = option.get(gameOptions);
      return d == option.getMax() ? option.getGenericLabel(new TranslatableText("options.framerateLimit.max")) : option.getGenericLabel(new TranslatableText("options.framerate", new Object[]{(int)d}));
   });
   public static final DoubleOption GAMMA = new DoubleOption("options.gamma", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.gamma;
   }, (gameOptions, gamma) -> {
      gameOptions.gamma = gamma;
   }, (gameOptions, option) -> {
      double d = option.getRatio(option.get(gameOptions));
      if (d == 0.0D) {
         return option.getGenericLabel(new TranslatableText("options.gamma.min"));
      } else {
         return d == 1.0D ? option.getGenericLabel(new TranslatableText("options.gamma.max")) : option.getPercentAdditionLabel((int)(d * 100.0D));
      }
   });
   public static final DoubleOption MIPMAP_LEVELS = new DoubleOption("options.mipmapLevels", 0.0D, 4.0D, 1.0F, (gameOptions) -> {
      return (double)gameOptions.mipmapLevels;
   }, (gameOptions, mipmapLevels) -> {
      gameOptions.mipmapLevels = (int)mipmapLevels;
   }, (gameOptions, option) -> {
      double d = option.get(gameOptions);
      return (Text)(d == 0.0D ? ScreenTexts.composeToggleText(option.getDisplayPrefix(), false) : option.getGenericLabel((int)d));
   });
   public static final DoubleOption MOUSE_WHEEL_SENSITIVITY = new LogarithmicOption("options.mouseWheelSensitivity", 0.01D, 10.0D, 0.01F, (gameOptions) -> {
      return gameOptions.mouseWheelSensitivity;
   }, (gameOptions, mouseWheelSensitivity) -> {
      gameOptions.mouseWheelSensitivity = mouseWheelSensitivity;
   }, (gameOptions, option) -> {
      double d = option.getRatio(option.get(gameOptions));
      return option.getGenericLabel(new LiteralText(String.format("%.2f", option.getValue(d))));
   });
   public static final BooleanOption RAW_MOUSE_INPUT = new BooleanOption("options.rawMouseInput", (gameOptions) -> {
      return gameOptions.rawMouseInput;
   }, (gameOptions, rawMouseInput) -> {
      gameOptions.rawMouseInput = rawMouseInput;
      Window window = MinecraftClient.getInstance().getWindow();
      if (window != null) {
         window.setRawMouseMotion(rawMouseInput);
      }

   });
   public static final DoubleOption RENDER_DISTANCE = new DoubleOption("options.renderDistance", 2.0D, 16.0D, 1.0F, (gameOptions) -> {
      return (double)gameOptions.viewDistance;
   }, (gameOptions, viewDistance) -> {
      gameOptions.viewDistance = (int)viewDistance;
      MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate();
   }, (gameOptions, option) -> {
      double d = option.get(gameOptions);
      return option.getGenericLabel(new TranslatableText("options.chunks", new Object[]{(int)d}));
   });
   public static final DoubleOption ENTITY_DISTANCE_SCALING = new DoubleOption("options.entityDistanceScaling", 0.5D, 5.0D, 0.25F, (gameOptions) -> {
      return (double)gameOptions.entityDistanceScaling;
   }, (gameOptions, entityDistanceScaling) -> {
      gameOptions.entityDistanceScaling = (float)entityDistanceScaling;
   }, (gameOptions, option) -> {
      double d = option.get(gameOptions);
      return option.getPercentLabel(d);
   });
   public static final DoubleOption SENSITIVITY = new DoubleOption("options.sensitivity", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.mouseSensitivity;
   }, (gameOptions, mouseSensitivity) -> {
      gameOptions.mouseSensitivity = mouseSensitivity;
   }, (gameOptions, option) -> {
      double d = option.getRatio(option.get(gameOptions));
      if (d == 0.0D) {
         return option.getGenericLabel(new TranslatableText("options.sensitivity.min"));
      } else {
         return d == 1.0D ? option.getGenericLabel(new TranslatableText("options.sensitivity.max")) : option.getPercentLabel(2.0D * d);
      }
   });
   public static final DoubleOption TEXT_BACKGROUND_OPACITY = new DoubleOption("options.accessibility.text_background_opacity", 0.0D, 1.0D, 0.0F, (gameOptions) -> {
      return gameOptions.textBackgroundOpacity;
   }, (gameOptions, textBackgroundOpacity) -> {
      gameOptions.textBackgroundOpacity = textBackgroundOpacity;
      MinecraftClient.getInstance().inGameHud.getChatHud().reset();
   }, (gameOptions, option) -> {
      return option.getPercentLabel(option.getRatio(option.get(gameOptions)));
   });
   public static final CyclingOption AO = new CyclingOption("options.ao", (gameOptions, amount) -> {
      gameOptions.ao = AoMode.byId(gameOptions.ao.getId() + amount);
      MinecraftClient.getInstance().worldRenderer.reload();
   }, (gameOptions, option) -> {
      return option.getGenericLabel(new TranslatableText(gameOptions.ao.getTranslationKey()));
   });
   public static final CyclingOption ATTACK_INDICATOR = new CyclingOption("options.attackIndicator", (gameOptions, amount) -> {
      gameOptions.attackIndicator = AttackIndicator.byId(gameOptions.attackIndicator.getId() + amount);
   }, (gameOptions, option) -> {
      return option.getGenericLabel(new TranslatableText(gameOptions.attackIndicator.getTranslationKey()));
   });
   public static final CyclingOption VISIBILITY = new CyclingOption("options.chat.visibility", (gameOptions, amount) -> {
      gameOptions.chatVisibility = ChatVisibility.byId((gameOptions.chatVisibility.getId() + amount) % 3);
   }, (gameOptions, option) -> {
      return option.getGenericLabel(new TranslatableText(gameOptions.chatVisibility.getTranslationKey()));
   });
   private static final Text FAST_GRAPHICS_TOOLTIP = new TranslatableText("options.graphics.fast.tooltip");
   private static final Text FABULOUS_GRAPHICS_TOOLTIP;
   private static final Text FANCY_GRAPHICS_TOOLTIP;
   public static final CyclingOption GRAPHICS;
   public static final CyclingOption GUI_SCALE;
   public static final CyclingOption MAIN_HAND;
   public static final CyclingOption NARRATOR;
   public static final CyclingOption PARTICLES;
   public static final CyclingOption CLOUDS;
   public static final CyclingOption TEXT_BACKGROUND;
   private static final Text field_26925;
   public static final BooleanOption AUTO_JUMP;
   public static final BooleanOption AUTO_SUGGESTIONS;
   public static final BooleanOption field_26924;
   public static final BooleanOption CHAT_COLOR;
   public static final BooleanOption CHAT_LINKS;
   public static final BooleanOption CHAT_LINKS_PROMPT;
   public static final BooleanOption DISCRETE_MOUSE_SCROLL;
   public static final BooleanOption VSYNC;
   public static final BooleanOption ENTITY_SHADOWS;
   public static final BooleanOption FORCE_UNICODE_FONT;
   public static final BooleanOption INVERT_MOUSE;
   public static final BooleanOption REALMS_NOTIFICATIONS;
   public static final BooleanOption REDUCED_DEBUG_INFO;
   public static final BooleanOption SUBTITLES;
   public static final BooleanOption SNOOPER;
   public static final CyclingOption SNEAK_TOGGLED;
   public static final CyclingOption SPRINT_TOGGLED;
   public static final BooleanOption TOUCHSCREEN;
   public static final BooleanOption FULLSCREEN;
   public static final BooleanOption VIEW_BOBBING;
   private final Text key;
   private Optional<List<OrderedText>> tooltip = Optional.empty();

   public Option(String key) {
      this.key = new TranslatableText(key);
   }

   public abstract ClickableWidget createButton(GameOptions options, int x, int y, int width);

   protected Text getDisplayPrefix() {
      return this.key;
   }

   public void setTooltip(List<OrderedText> tooltip) {
      this.tooltip = Optional.of(tooltip);
   }

   public Optional<List<OrderedText>> getTooltip() {
      return this.tooltip;
   }

   protected Text getPixelLabel(int pixel) {
      return new TranslatableText("options.pixel_value", new Object[]{this.getDisplayPrefix(), pixel});
   }

   protected Text getPercentLabel(double proportion) {
      return new TranslatableText("options.percent_value", new Object[]{this.getDisplayPrefix(), (int)(proportion * 100.0D)});
   }

   protected Text getPercentAdditionLabel(int percentage) {
      return new TranslatableText("options.percent_add_value", new Object[]{this.getDisplayPrefix(), percentage});
   }

   protected Text getGenericLabel(Text value) {
      return new TranslatableText("options.generic_value", new Object[]{this.getDisplayPrefix(), value});
   }

   protected Text getGenericLabel(int value) {
      return this.getGenericLabel(new LiteralText(Integer.toString(value)));
   }

   static {
      FABULOUS_GRAPHICS_TOOLTIP = new TranslatableText("options.graphics.fabulous.tooltip", new Object[]{(new TranslatableText("options.graphics.fabulous")).formatted(Formatting.ITALIC)});
      FANCY_GRAPHICS_TOOLTIP = new TranslatableText("options.graphics.fancy.tooltip");
      GRAPHICS = new CyclingOption("options.graphics", (gameOptions, amount) -> {
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         VideoWarningManager videoWarningManager = minecraftClient.getVideoWarningManager();
         if (gameOptions.graphicsMode == GraphicsMode.FANCY && videoWarningManager.canWarn()) {
            videoWarningManager.scheduleWarning();
         } else {
            gameOptions.graphicsMode = gameOptions.graphicsMode.next();
            if (gameOptions.graphicsMode == GraphicsMode.FABULOUS && (!GlStateManager.supportsGl30() || videoWarningManager.hasCancelledAfterWarning())) {
               gameOptions.graphicsMode = GraphicsMode.FAST;
            }

            minecraftClient.worldRenderer.reload();
         }
      }, (gameOptions, option) -> {
         switch(gameOptions.graphicsMode) {
         case FAST:
            option.setTooltip(MinecraftClient.getInstance().textRenderer.wrapLines(FAST_GRAPHICS_TOOLTIP, 200));
            break;
         case FANCY:
            option.setTooltip(MinecraftClient.getInstance().textRenderer.wrapLines(FANCY_GRAPHICS_TOOLTIP, 200));
            break;
         case FABULOUS:
            option.setTooltip(MinecraftClient.getInstance().textRenderer.wrapLines(FABULOUS_GRAPHICS_TOOLTIP, 200));
         }

         MutableText mutableText = new TranslatableText(gameOptions.graphicsMode.getTranslationKey());
         return gameOptions.graphicsMode == GraphicsMode.FABULOUS ? option.getGenericLabel(mutableText.formatted(Formatting.ITALIC)) : option.getGenericLabel(mutableText);
      });
      GUI_SCALE = new CyclingOption("options.guiScale", (gameOptions, amount) -> {
         gameOptions.guiScale = Integer.remainderUnsigned(gameOptions.guiScale + amount, MinecraftClient.getInstance().getWindow().calculateScaleFactor(0, MinecraftClient.getInstance().forcesUnicodeFont()) + 1);
      }, (gameOptions, option) -> {
         return gameOptions.guiScale == 0 ? option.getGenericLabel(new TranslatableText("options.guiScale.auto")) : option.getGenericLabel(gameOptions.guiScale);
      });
      MAIN_HAND = new CyclingOption("options.mainHand", (gameOptions, amount) -> {
         gameOptions.mainArm = gameOptions.mainArm.getOpposite();
      }, (gameOptions, option) -> {
         return option.getGenericLabel(gameOptions.mainArm.getOptionName());
      });
      NARRATOR = new CyclingOption("options.narrator", (gameOptions, amount) -> {
         if (NarratorManager.INSTANCE.isActive()) {
            gameOptions.narrator = NarratorMode.byId(gameOptions.narrator.getId() + amount);
         } else {
            gameOptions.narrator = NarratorMode.OFF;
         }

         NarratorManager.INSTANCE.addToast(gameOptions.narrator);
      }, (gameOptions, option) -> {
         return NarratorManager.INSTANCE.isActive() ? option.getGenericLabel(gameOptions.narrator.getName()) : option.getGenericLabel(new TranslatableText("options.narrator.notavailable"));
      });
      PARTICLES = new CyclingOption("options.particles", (gameOptions, amount) -> {
         gameOptions.particles = ParticlesMode.byId(gameOptions.particles.getId() + amount);
      }, (gameOptions, option) -> {
         return option.getGenericLabel(new TranslatableText(gameOptions.particles.getTranslationKey()));
      });
      CLOUDS = new CyclingOption("options.renderClouds", (gameOptions, amount) -> {
         gameOptions.cloudRenderMode = CloudRenderMode.byId(gameOptions.cloudRenderMode.getId() + amount);
         if (MinecraftClient.isFabulousGraphicsOrBetter()) {
            Framebuffer framebuffer = MinecraftClient.getInstance().worldRenderer.getCloudsFramebuffer();
            if (framebuffer != null) {
               framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
            }
         }

      }, (gameOptions, option) -> {
         return option.getGenericLabel(new TranslatableText(gameOptions.cloudRenderMode.getTranslationKey()));
      });
      TEXT_BACKGROUND = new CyclingOption("options.accessibility.text_background", (gameOptions, amount) -> {
         gameOptions.backgroundForChatOnly = !gameOptions.backgroundForChatOnly;
      }, (gameOptions, option) -> {
         return option.getGenericLabel(new TranslatableText(gameOptions.backgroundForChatOnly ? "options.accessibility.text_background.chat" : "options.accessibility.text_background.everywhere"));
      });
      field_26925 = new TranslatableText("options.hideMatchedNames.tooltip");
      AUTO_JUMP = new BooleanOption("options.autoJump", (gameOptions) -> {
         return gameOptions.autoJump;
      }, (gameOptions, autoJump) -> {
         gameOptions.autoJump = autoJump;
      });
      AUTO_SUGGESTIONS = new BooleanOption("options.autoSuggestCommands", (gameOptions) -> {
         return gameOptions.autoSuggestions;
      }, (gameOptions, autoSuggestions) -> {
         gameOptions.autoSuggestions = autoSuggestions;
      });
      field_26924 = new BooleanOption("options.hideMatchedNames", field_26925, (gameOptions) -> {
         return gameOptions.field_26926;
      }, (gameOptions, boolean_) -> {
         gameOptions.field_26926 = boolean_;
      });
      CHAT_COLOR = new BooleanOption("options.chat.color", (gameOptions) -> {
         return gameOptions.chatColors;
      }, (gameOptions, chatColors) -> {
         gameOptions.chatColors = chatColors;
      });
      CHAT_LINKS = new BooleanOption("options.chat.links", (gameOptions) -> {
         return gameOptions.chatLinks;
      }, (gameOptions, chatLinks) -> {
         gameOptions.chatLinks = chatLinks;
      });
      CHAT_LINKS_PROMPT = new BooleanOption("options.chat.links.prompt", (gameOptions) -> {
         return gameOptions.chatLinksPrompt;
      }, (gameOptions, chatLinksPrompt) -> {
         gameOptions.chatLinksPrompt = chatLinksPrompt;
      });
      DISCRETE_MOUSE_SCROLL = new BooleanOption("options.discrete_mouse_scroll", (gameOptions) -> {
         return gameOptions.discreteMouseScroll;
      }, (gameOptions, discreteMouseScroll) -> {
         gameOptions.discreteMouseScroll = discreteMouseScroll;
      });
      VSYNC = new BooleanOption("options.vsync", (gameOptions) -> {
         return gameOptions.enableVsync;
      }, (gameOptions, enableVsync) -> {
         gameOptions.enableVsync = enableVsync;
         if (MinecraftClient.getInstance().getWindow() != null) {
            MinecraftClient.getInstance().getWindow().setVsync(gameOptions.enableVsync);
         }

      });
      ENTITY_SHADOWS = new BooleanOption("options.entityShadows", (gameOptions) -> {
         return gameOptions.entityShadows;
      }, (gameOptions, entityShadows) -> {
         gameOptions.entityShadows = entityShadows;
      });
      FORCE_UNICODE_FONT = new BooleanOption("options.forceUnicodeFont", (gameOptions) -> {
         return gameOptions.forceUnicodeFont;
      }, (gameOptions, forceUnicodeFont) -> {
         gameOptions.forceUnicodeFont = forceUnicodeFont;
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         if (minecraftClient.getWindow() != null) {
            minecraftClient.initFont(forceUnicodeFont);
         }

      });
      INVERT_MOUSE = new BooleanOption("options.invertMouse", (gameOptions) -> {
         return gameOptions.invertYMouse;
      }, (gameOptions, invertYMouse) -> {
         gameOptions.invertYMouse = invertYMouse;
      });
      REALMS_NOTIFICATIONS = new BooleanOption("options.realmsNotifications", (gameOptions) -> {
         return gameOptions.realmsNotifications;
      }, (gameOptions, realmsNotifications) -> {
         gameOptions.realmsNotifications = realmsNotifications;
      });
      REDUCED_DEBUG_INFO = new BooleanOption("options.reducedDebugInfo", (gameOptions) -> {
         return gameOptions.reducedDebugInfo;
      }, (gameOptions, reducedDebugInfo) -> {
         gameOptions.reducedDebugInfo = reducedDebugInfo;
      });
      SUBTITLES = new BooleanOption("options.showSubtitles", (gameOptions) -> {
         return gameOptions.showSubtitles;
      }, (gameOptions, showSubtitles) -> {
         gameOptions.showSubtitles = showSubtitles;
      });
      SNOOPER = new BooleanOption("options.snooper", (gameOptions) -> {
         if (gameOptions.snooperEnabled) {
         }

         return false;
      }, (gameOptions, snooperEnabled) -> {
         gameOptions.snooperEnabled = snooperEnabled;
      });
      SNEAK_TOGGLED = new CyclingOption("key.sneak", (gameOptions, amount) -> {
         gameOptions.sneakToggled = !gameOptions.sneakToggled;
      }, (gameOptions, option) -> {
         return option.getGenericLabel(new TranslatableText(gameOptions.sneakToggled ? "options.key.toggle" : "options.key.hold"));
      });
      SPRINT_TOGGLED = new CyclingOption("key.sprint", (gameOptions, amount) -> {
         gameOptions.sprintToggled = !gameOptions.sprintToggled;
      }, (gameOptions, option) -> {
         return option.getGenericLabel(new TranslatableText(gameOptions.sprintToggled ? "options.key.toggle" : "options.key.hold"));
      });
      TOUCHSCREEN = new BooleanOption("options.touchscreen", (gameOptions) -> {
         return gameOptions.touchscreen;
      }, (gameOptions, touchscreen) -> {
         gameOptions.touchscreen = touchscreen;
      });
      FULLSCREEN = new BooleanOption("options.fullscreen", (gameOptions) -> {
         return gameOptions.fullscreen;
      }, (gameOptions, fullscreen) -> {
         gameOptions.fullscreen = fullscreen;
         MinecraftClient minecraftClient = MinecraftClient.getInstance();
         if (minecraftClient.getWindow() != null && minecraftClient.getWindow().isFullscreen() != gameOptions.fullscreen) {
            minecraftClient.getWindow().toggleFullscreen();
            gameOptions.fullscreen = minecraftClient.getWindow().isFullscreen();
         }

      });
      VIEW_BOBBING = new BooleanOption("options.viewBobbing", (gameOptions) -> {
         return gameOptions.bobView;
      }, (gameOptions, bobView) -> {
         gameOptions.bobView = bobView;
      });
   }
}
