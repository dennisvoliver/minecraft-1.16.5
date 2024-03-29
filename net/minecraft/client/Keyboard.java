package net.minecraft.client;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.GameModeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.NarratorOptionsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Option;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotUtils;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class Keyboard {
   private final MinecraftClient client;
   private boolean repeatEvents;
   private final Clipboard clipboard = new Clipboard();
   private long debugCrashStartTime = -1L;
   private long debugCrashLastLogTime = -1L;
   private long debugCrashElapsedTime = -1L;
   private boolean switchF3State;

   public Keyboard(MinecraftClient client) {
      this.client = client;
   }

   private void debugWarn(String key, Object... args) {
      this.client.inGameHud.getChatHud().addMessage((new LiteralText("")).append((new TranslatableText("debug.prefix")).formatted(new Formatting[]{Formatting.YELLOW, Formatting.BOLD})).append(" ").append((Text)(new TranslatableText(key, args))));
   }

   private void debugError(String key, Object... args) {
      this.client.inGameHud.getChatHud().addMessage((new LiteralText("")).append((new TranslatableText("debug.prefix")).formatted(new Formatting[]{Formatting.RED, Formatting.BOLD})).append(" ").append((Text)(new TranslatableText(key, args))));
   }

   private boolean processF3(int key) {
      if (this.debugCrashStartTime > 0L && this.debugCrashStartTime < Util.getMeasuringTimeMs() - 100L) {
         return true;
      } else {
         switch(key) {
         case 65:
            this.client.worldRenderer.reload();
            this.debugWarn("debug.reload_chunks.message");
            return true;
         case 66:
            boolean bl = !this.client.getEntityRenderDispatcher().shouldRenderHitboxes();
            this.client.getEntityRenderDispatcher().setRenderHitboxes(bl);
            this.debugWarn(bl ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
            return true;
         case 67:
            if (this.client.player.hasReducedDebugInfo()) {
               return false;
            } else {
               ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.player.networkHandler;
               if (clientPlayNetworkHandler == null) {
                  return false;
               }

               this.debugWarn("debug.copy_location.message");
               this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.client.player.world.getRegistryKey().getValue(), this.client.player.getX(), this.client.player.getY(), this.client.player.getZ(), this.client.player.yaw, this.client.player.pitch));
               return true;
            }
         case 68:
            if (this.client.inGameHud != null) {
               this.client.inGameHud.getChatHud().clear(false);
            }

            return true;
         case 70:
            Option.RENDER_DISTANCE.set(this.client.options, MathHelper.clamp((double)(this.client.options.viewDistance + (Screen.hasShiftDown() ? -1 : 1)), Option.RENDER_DISTANCE.getMin(), Option.RENDER_DISTANCE.getMax()));
            this.debugWarn("debug.cycle_renderdistance.message", this.client.options.viewDistance);
            return true;
         case 71:
            boolean bl2 = this.client.debugRenderer.toggleShowChunkBorder();
            this.debugWarn(bl2 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
            return true;
         case 72:
            this.client.options.advancedItemTooltips = !this.client.options.advancedItemTooltips;
            this.debugWarn(this.client.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
            this.client.options.write();
            return true;
         case 73:
            if (!this.client.player.hasReducedDebugInfo()) {
               this.copyLookAt(this.client.player.hasPermissionLevel(2), !Screen.hasShiftDown());
            }

            return true;
         case 78:
            if (!this.client.player.hasPermissionLevel(2)) {
               this.debugWarn("debug.creative_spectator.error");
            } else if (!this.client.player.isSpectator()) {
               this.client.player.sendChatMessage("/gamemode spectator");
            } else {
               this.client.player.sendChatMessage("/gamemode " + this.client.interactionManager.getPreviousGameMode().getName());
            }

            return true;
         case 80:
            this.client.options.pauseOnLostFocus = !this.client.options.pauseOnLostFocus;
            this.client.options.write();
            this.debugWarn(this.client.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
            return true;
         case 81:
            this.debugWarn("debug.help.message");
            ChatHud chatHud = this.client.inGameHud.getChatHud();
            chatHud.addMessage(new TranslatableText("debug.reload_chunks.help"));
            chatHud.addMessage(new TranslatableText("debug.show_hitboxes.help"));
            chatHud.addMessage(new TranslatableText("debug.copy_location.help"));
            chatHud.addMessage(new TranslatableText("debug.clear_chat.help"));
            chatHud.addMessage(new TranslatableText("debug.cycle_renderdistance.help"));
            chatHud.addMessage(new TranslatableText("debug.chunk_boundaries.help"));
            chatHud.addMessage(new TranslatableText("debug.advanced_tooltips.help"));
            chatHud.addMessage(new TranslatableText("debug.inspect.help"));
            chatHud.addMessage(new TranslatableText("debug.creative_spectator.help"));
            chatHud.addMessage(new TranslatableText("debug.pause_focus.help"));
            chatHud.addMessage(new TranslatableText("debug.help.help"));
            chatHud.addMessage(new TranslatableText("debug.reload_resourcepacks.help"));
            chatHud.addMessage(new TranslatableText("debug.pause.help"));
            chatHud.addMessage(new TranslatableText("debug.gamemodes.help"));
            return true;
         case 84:
            this.debugWarn("debug.reload_resourcepacks.message");
            this.client.reloadResources();
            return true;
         case 293:
            if (!this.client.player.hasPermissionLevel(2)) {
               this.debugWarn("debug.gamemodes.error");
            } else {
               this.client.openScreen(new GameModeSelectionScreen());
            }

            return true;
         default:
            return false;
         }
      }
   }

   private void copyLookAt(boolean bl, boolean bl2) {
      HitResult hitResult = this.client.crosshairTarget;
      if (hitResult != null) {
         switch(hitResult.getType()) {
         case BLOCK:
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            BlockState blockState = this.client.player.world.getBlockState(blockPos);
            if (bl) {
               if (bl2) {
                  this.client.player.networkHandler.getDataQueryHandler().queryBlockNbt(blockPos, (nbtCompoundx) -> {
                     this.copyBlock(blockState, blockPos, nbtCompoundx);
                     this.debugWarn("debug.inspect.server.block");
                  });
               } else {
                  BlockEntity blockEntity = this.client.player.world.getBlockEntity(blockPos);
                  NbtCompound nbtCompound = blockEntity != null ? blockEntity.writeNbt(new NbtCompound()) : null;
                  this.copyBlock(blockState, blockPos, nbtCompound);
                  this.debugWarn("debug.inspect.client.block");
               }
            } else {
               this.copyBlock(blockState, blockPos, (NbtCompound)null);
               this.debugWarn("debug.inspect.client.block");
            }
            break;
         case ENTITY:
            Entity entity = ((EntityHitResult)hitResult).getEntity();
            Identifier identifier = Registry.ENTITY_TYPE.getId(entity.getType());
            if (bl) {
               if (bl2) {
                  this.client.player.networkHandler.getDataQueryHandler().queryEntityNbt(entity.getEntityId(), (nbtCompoundx) -> {
                     this.copyEntity(identifier, entity.getPos(), nbtCompoundx);
                     this.debugWarn("debug.inspect.server.entity");
                  });
               } else {
                  NbtCompound nbtCompound2 = entity.writeNbt(new NbtCompound());
                  this.copyEntity(identifier, entity.getPos(), nbtCompound2);
                  this.debugWarn("debug.inspect.client.entity");
               }
            } else {
               this.copyEntity(identifier, entity.getPos(), (NbtCompound)null);
               this.debugWarn("debug.inspect.client.entity");
            }
         }

      }
   }

   private void copyBlock(BlockState state, BlockPos pos, @Nullable NbtCompound nbt) {
      if (nbt != null) {
         nbt.remove("x");
         nbt.remove("y");
         nbt.remove("z");
         nbt.remove("id");
      }

      StringBuilder stringBuilder = new StringBuilder(BlockArgumentParser.stringifyBlockState(state));
      if (nbt != null) {
         stringBuilder.append(nbt);
      }

      String string = String.format(Locale.ROOT, "/setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), stringBuilder);
      this.setClipboard(string);
   }

   private void copyEntity(Identifier id, Vec3d pos, @Nullable NbtCompound nbt) {
      String string3;
      if (nbt != null) {
         nbt.remove("UUID");
         nbt.remove("Pos");
         nbt.remove("Dimension");
         String string = nbt.toText().getString();
         string3 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", id.toString(), pos.x, pos.y, pos.z, string);
      } else {
         string3 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", id.toString(), pos.x, pos.y, pos.z);
      }

      this.setClipboard(string3);
   }

   public void onKey(long window, int key, int scancode, int i, int modifiers) {
      if (window == this.client.getWindow().getHandle()) {
         if (this.debugCrashStartTime > 0L) {
            if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 67) || !InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 292)) {
               this.debugCrashStartTime = -1L;
            }
         } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 67) && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 292)) {
            this.switchF3State = true;
            this.debugCrashStartTime = Util.getMeasuringTimeMs();
            this.debugCrashLastLogTime = Util.getMeasuringTimeMs();
            this.debugCrashElapsedTime = 0L;
         }

         ParentElement parentElement = this.client.currentScreen;
         if (i == 1 && (!(this.client.currentScreen instanceof ControlsOptionsScreen) || ((ControlsOptionsScreen)parentElement).time <= Util.getMeasuringTimeMs() - 20L)) {
            if (this.client.options.keyFullscreen.matchesKey(key, scancode)) {
               this.client.getWindow().toggleFullscreen();
               this.client.options.fullscreen = this.client.getWindow().isFullscreen();
               this.client.options.write();
               return;
            }

            if (this.client.options.keyScreenshot.matchesKey(key, scancode)) {
               if (Screen.hasControlDown()) {
               }

               ScreenshotUtils.saveScreenshot(this.client.runDirectory, this.client.getWindow().getFramebufferWidth(), this.client.getWindow().getFramebufferHeight(), this.client.getFramebuffer(), (text) -> {
                  this.client.execute(() -> {
                     this.client.inGameHud.getChatHud().addMessage(text);
                  });
               });
               return;
            }
         }

         boolean bl = parentElement == null || !(parentElement.getFocused() instanceof TextFieldWidget) || !((TextFieldWidget)parentElement.getFocused()).isActive();
         if (i != 0 && key == 66 && Screen.hasControlDown() && bl) {
            Option.NARRATOR.cycle(this.client.options, 1);
            if (parentElement instanceof NarratorOptionsScreen) {
               ((NarratorOptionsScreen)parentElement).updateNarratorButtonText();
            }
         }

         if (parentElement != null) {
            boolean[] bls = new boolean[]{false};
            Screen.wrapScreenError(() -> {
               if (i != 1 && (i != 2 || !this.repeatEvents)) {
                  if (i == 0) {
                     bls[0] = parentElement.keyReleased(key, scancode, modifiers);
                  }
               } else {
                  bls[0] = parentElement.keyPressed(key, scancode, modifiers);
               }

            }, "keyPressed event handler", parentElement.getClass().getCanonicalName());
            if (bls[0]) {
               return;
            }
         }

         if (this.client.currentScreen == null || this.client.currentScreen.passEvents) {
            InputUtil.Key key2 = InputUtil.fromKeyCode(key, scancode);
            if (i == 0) {
               KeyBinding.setKeyPressed(key2, false);
               if (key == 292) {
                  if (this.switchF3State) {
                     this.switchF3State = false;
                  } else {
                     this.client.options.debugEnabled = !this.client.options.debugEnabled;
                     this.client.options.debugProfilerEnabled = this.client.options.debugEnabled && Screen.hasShiftDown();
                     this.client.options.debugTpsEnabled = this.client.options.debugEnabled && Screen.hasAltDown();
                  }
               }
            } else {
               if (key == 293 && this.client.gameRenderer != null) {
                  this.client.gameRenderer.toggleShadersEnabled();
               }

               boolean bl2 = false;
               if (this.client.currentScreen == null) {
                  if (key == 256) {
                     boolean bl3 = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 292);
                     this.client.openPauseMenu(bl3);
                  }

                  bl2 = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 292) && this.processF3(key);
                  this.switchF3State |= bl2;
                  if (key == 290) {
                     this.client.options.hudHidden = !this.client.options.hudHidden;
                  }
               }

               if (bl2) {
                  KeyBinding.setKeyPressed(key2, false);
               } else {
                  KeyBinding.setKeyPressed(key2, true);
                  KeyBinding.onKeyPressed(key2);
               }

               if (this.client.options.debugProfilerEnabled && key >= 48 && key <= 57) {
                  this.client.handleProfilerKeyPress(key - 48);
               }
            }
         }

      }
   }

   private void onChar(long window, int i, int modifiers) {
      if (window == this.client.getWindow().getHandle()) {
         Element element = this.client.currentScreen;
         if (element != null && this.client.getOverlay() == null) {
            if (Character.charCount(i) == 1) {
               Screen.wrapScreenError(() -> {
                  element.charTyped((char)i, modifiers);
               }, "charTyped event handler", element.getClass().getCanonicalName());
            } else {
               char[] var6 = Character.toChars(i);
               int var7 = var6.length;

               for(int var8 = 0; var8 < var7; ++var8) {
                  char c = var6[var8];
                  Screen.wrapScreenError(() -> {
                     element.charTyped(c, modifiers);
                  }, "charTyped event handler", element.getClass().getCanonicalName());
               }
            }

         }
      }
   }

   public void setRepeatEvents(boolean repeatEvents) {
      this.repeatEvents = repeatEvents;
   }

   public void setup(long window) {
      InputUtil.setKeyboardCallbacks(window, (l, i, j, k, m) -> {
         this.client.execute(() -> {
            this.onKey(l, i, j, k, m);
         });
      }, (l, i, j) -> {
         this.client.execute(() -> {
            this.onChar(l, i, j);
         });
      });
   }

   public String getClipboard() {
      return this.clipboard.getClipboard(this.client.getWindow().getHandle(), (error, description) -> {
         if (error != 65545) {
            this.client.getWindow().logGlError(error, description);
         }

      });
   }

   public void setClipboard(String clipboard) {
      this.clipboard.setClipboard(this.client.getWindow().getHandle(), clipboard);
   }

   public void pollDebugCrash() {
      if (this.debugCrashStartTime > 0L) {
         long l = Util.getMeasuringTimeMs();
         long m = 10000L - (l - this.debugCrashStartTime);
         long n = l - this.debugCrashLastLogTime;
         if (m < 0L) {
            if (Screen.hasControlDown()) {
               GlfwUtil.makeJvmCrash();
            }

            throw new CrashException(new CrashReport("Manually triggered debug crash", new Throwable()));
         }

         if (n >= 1000L) {
            if (this.debugCrashElapsedTime == 0L) {
               this.debugWarn("debug.crash.message");
            } else {
               this.debugError("debug.crash.warning", MathHelper.ceil((float)m / 1000.0F));
            }

            this.debugCrashLastLogTime = l;
            ++this.debugCrashElapsedTime;
         }
      }

   }
}
