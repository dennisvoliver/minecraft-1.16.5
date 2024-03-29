package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WritableBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.ConfirmScreenActionC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.JigsawGeneratingC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryBlockNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryEntityNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyLockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateJigsawC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.ConfirmScreenActionS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ServerPlayNetworkHandler implements ServerPlayPacketListener {
   private static final Logger LOGGER = LogManager.getLogger();
   public final ClientConnection connection;
   private final MinecraftServer server;
   public ServerPlayerEntity player;
   private int ticks;
   private long lastKeepAliveTime;
   private boolean waitingForKeepAlive;
   private long keepAliveId;
   private int messageCooldown;
   private int creativeItemDropThreshold;
   private final Int2ShortMap transactions = new Int2ShortOpenHashMap();
   private double lastTickX;
   private double lastTickY;
   private double lastTickZ;
   private double updatedX;
   private double updatedY;
   private double updatedZ;
   private Entity topmostRiddenEntity;
   private double lastTickRiddenX;
   private double lastTickRiddenY;
   private double lastTickRiddenZ;
   private double updatedRiddenX;
   private double updatedRiddenY;
   private double updatedRiddenZ;
   private Vec3d requestedTeleportPos;
   private int requestedTeleportId;
   private int teleportRequestTick;
   private boolean floating;
   private int floatingTicks;
   private boolean vehicleFloating;
   private int vehicleFloatingTicks;
   private int movePacketsCount;
   private int lastTickMovePacketsCount;

   public ServerPlayNetworkHandler(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player) {
      this.server = server;
      this.connection = connection;
      connection.setPacketListener(this);
      this.player = player;
      player.networkHandler = this;
      TextStream textStream = player.getTextStream();
      if (textStream != null) {
         textStream.onConnect();
      }

   }

   public void tick() {
      this.syncWithPlayerPosition();
      this.player.prevX = this.player.getX();
      this.player.prevY = this.player.getY();
      this.player.prevZ = this.player.getZ();
      this.player.playerTick();
      this.player.updatePositionAndAngles(this.lastTickX, this.lastTickY, this.lastTickZ, this.player.yaw, this.player.pitch);
      ++this.ticks;
      this.lastTickMovePacketsCount = this.movePacketsCount;
      if (this.floating && !this.player.isSleeping()) {
         if (++this.floatingTicks > 80) {
            LOGGER.warn((String)"{} was kicked for floating too long!", (Object)this.player.getName().getString());
            this.disconnect(new TranslatableText("multiplayer.disconnect.flying"));
            return;
         }
      } else {
         this.floating = false;
         this.floatingTicks = 0;
      }

      this.topmostRiddenEntity = this.player.getRootVehicle();
      if (this.topmostRiddenEntity != this.player && this.topmostRiddenEntity.getPrimaryPassenger() == this.player) {
         this.lastTickRiddenX = this.topmostRiddenEntity.getX();
         this.lastTickRiddenY = this.topmostRiddenEntity.getY();
         this.lastTickRiddenZ = this.topmostRiddenEntity.getZ();
         this.updatedRiddenX = this.topmostRiddenEntity.getX();
         this.updatedRiddenY = this.topmostRiddenEntity.getY();
         this.updatedRiddenZ = this.topmostRiddenEntity.getZ();
         if (this.vehicleFloating && this.player.getRootVehicle().getPrimaryPassenger() == this.player) {
            if (++this.vehicleFloatingTicks > 80) {
               LOGGER.warn((String)"{} was kicked for floating a vehicle too long!", (Object)this.player.getName().getString());
               this.disconnect(new TranslatableText("multiplayer.disconnect.flying"));
               return;
            }
         } else {
            this.vehicleFloating = false;
            this.vehicleFloatingTicks = 0;
         }
      } else {
         this.topmostRiddenEntity = null;
         this.vehicleFloating = false;
         this.vehicleFloatingTicks = 0;
      }

      this.server.getProfiler().push("keepAlive");
      long l = Util.getMeasuringTimeMs();
      if (l - this.lastKeepAliveTime >= 15000L) {
         if (this.waitingForKeepAlive) {
            this.disconnect(new TranslatableText("disconnect.timeout"));
         } else {
            this.waitingForKeepAlive = true;
            this.lastKeepAliveTime = l;
            this.keepAliveId = l;
            this.sendPacket(new KeepAliveS2CPacket(this.keepAliveId));
         }
      }

      this.server.getProfiler().pop();
      if (this.messageCooldown > 0) {
         --this.messageCooldown;
      }

      if (this.creativeItemDropThreshold > 0) {
         --this.creativeItemDropThreshold;
      }

      if (this.player.getLastActionTime() > 0L && this.server.getPlayerIdleTimeout() > 0 && Util.getMeasuringTimeMs() - this.player.getLastActionTime() > (long)(this.server.getPlayerIdleTimeout() * 1000 * 60)) {
         this.disconnect(new TranslatableText("multiplayer.disconnect.idling"));
      }

   }

   public void syncWithPlayerPosition() {
      this.lastTickX = this.player.getX();
      this.lastTickY = this.player.getY();
      this.lastTickZ = this.player.getZ();
      this.updatedX = this.player.getX();
      this.updatedY = this.player.getY();
      this.updatedZ = this.player.getZ();
   }

   public ClientConnection getConnection() {
      return this.connection;
   }

   private boolean isHost() {
      return this.server.isHost(this.player.getGameProfile());
   }

   public void disconnect(Text reason) {
      this.connection.send(new DisconnectS2CPacket(reason), (future) -> {
         this.connection.disconnect(reason);
      });
      this.connection.disableAutoRead();
      ClientConnection var10001 = this.connection;
      this.server.submitAndJoin(var10001::handleDisconnection);
   }

   private <T> void filterText(T text, Consumer<T> consumer, BiFunction<TextStream, T, CompletableFuture<Optional<T>>> backingFilterer) {
      ThreadExecutor<?> threadExecutor = this.player.getServerWorld().getServer();
      Consumer<T> consumer2 = (object) -> {
         if (this.getConnection().isOpen()) {
            consumer.accept(object);
         } else {
            LOGGER.debug("Ignoring packet due to disconnection");
         }

      };
      TextStream textStream = this.player.getTextStream();
      if (textStream != null) {
         ((CompletableFuture)backingFilterer.apply(textStream, text)).thenAcceptAsync((optional) -> {
            optional.ifPresent(consumer2);
         }, threadExecutor);
      } else {
         threadExecutor.execute(() -> {
            consumer2.accept(text);
         });
      }

   }

   private void filterText(String text, Consumer<String> consumer) {
      this.filterText(text, consumer, TextStream::filterText);
   }

   private void filterTexts(List<String> texts, Consumer<List<String>> consumer) {
      this.filterText(texts, consumer, TextStream::filterTexts);
   }

   public void onPlayerInput(PlayerInputC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.method_14218(packet.getSideways(), packet.getForward(), packet.isJumping(), packet.isSneaking());
   }

   private static boolean validatePlayerMove(PlayerMoveC2SPacket packet) {
      if (Doubles.isFinite(packet.getX(0.0D)) && Doubles.isFinite(packet.getY(0.0D)) && Doubles.isFinite(packet.getZ(0.0D)) && Floats.isFinite(packet.getPitch(0.0F)) && Floats.isFinite(packet.getYaw(0.0F))) {
         return Math.abs(packet.getX(0.0D)) > 3.0E7D || Math.abs(packet.getY(0.0D)) > 3.0E7D || Math.abs(packet.getZ(0.0D)) > 3.0E7D;
      } else {
         return true;
      }
   }

   private static boolean validateVehicleMove(VehicleMoveC2SPacket packet) {
      return !Doubles.isFinite(packet.getX()) || !Doubles.isFinite(packet.getY()) || !Doubles.isFinite(packet.getZ()) || !Floats.isFinite(packet.getPitch()) || !Floats.isFinite(packet.getYaw());
   }

   public void onVehicleMove(VehicleMoveC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (validateVehicleMove(packet)) {
         this.disconnect(new TranslatableText("multiplayer.disconnect.invalid_vehicle_movement"));
      } else {
         Entity entity = this.player.getRootVehicle();
         if (entity != this.player && entity.getPrimaryPassenger() == this.player && entity == this.topmostRiddenEntity) {
            ServerWorld serverWorld = this.player.getServerWorld();
            double d = entity.getX();
            double e = entity.getY();
            double f = entity.getZ();
            double g = packet.getX();
            double h = packet.getY();
            double i = packet.getZ();
            float j = packet.getYaw();
            float k = packet.getPitch();
            double l = g - this.lastTickRiddenX;
            double m = h - this.lastTickRiddenY;
            double n = i - this.lastTickRiddenZ;
            double o = entity.getVelocity().lengthSquared();
            double p = l * l + m * m + n * n;
            if (p - o > 100.0D && !this.isHost()) {
               LOGGER.warn((String)"{} (vehicle of {}) moved too quickly! {},{},{}", (Object)entity.getName().getString(), this.player.getName().getString(), l, m, n);
               this.connection.send(new VehicleMoveS2CPacket(entity));
               return;
            }

            boolean bl = serverWorld.isSpaceEmpty(entity, entity.getBoundingBox().contract(0.0625D));
            l = g - this.updatedRiddenX;
            m = h - this.updatedRiddenY - 1.0E-6D;
            n = i - this.updatedRiddenZ;
            entity.move(MovementType.PLAYER, new Vec3d(l, m, n));
            double q = m;
            l = g - entity.getX();
            m = h - entity.getY();
            if (m > -0.5D || m < 0.5D) {
               m = 0.0D;
            }

            n = i - entity.getZ();
            p = l * l + m * m + n * n;
            boolean bl2 = false;
            if (p > 0.0625D) {
               bl2 = true;
               LOGGER.warn((String)"{} (vehicle of {}) moved wrongly! {}", (Object)entity.getName().getString(), this.player.getName().getString(), Math.sqrt(p));
            }

            entity.updatePositionAndAngles(g, h, i, j, k);
            boolean bl3 = serverWorld.isSpaceEmpty(entity, entity.getBoundingBox().contract(0.0625D));
            if (bl && (bl2 || !bl3)) {
               entity.updatePositionAndAngles(d, e, f, j, k);
               this.connection.send(new VehicleMoveS2CPacket(entity));
               return;
            }

            this.player.getServerWorld().getChunkManager().updatePosition(this.player);
            this.player.increaseTravelMotionStats(this.player.getX() - d, this.player.getY() - e, this.player.getZ() - f);
            this.vehicleFloating = q >= -0.03125D && !this.server.isFlightEnabled() && this.method_29780(entity);
            this.updatedRiddenX = entity.getX();
            this.updatedRiddenY = entity.getY();
            this.updatedRiddenZ = entity.getZ();
         }

      }
   }

   private boolean method_29780(Entity entity) {
      return entity.world.method_29546(entity.getBoundingBox().expand(0.0625D).stretch(0.0D, -0.55D, 0.0D)).allMatch(AbstractBlock.AbstractBlockState::isAir);
   }

   public void onTeleportConfirm(TeleportConfirmC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (packet.getTeleportId() == this.requestedTeleportId) {
         this.player.updatePositionAndAngles(this.requestedTeleportPos.x, this.requestedTeleportPos.y, this.requestedTeleportPos.z, this.player.yaw, this.player.pitch);
         this.updatedX = this.requestedTeleportPos.x;
         this.updatedY = this.requestedTeleportPos.y;
         this.updatedZ = this.requestedTeleportPos.z;
         if (this.player.isInTeleportationState()) {
            this.player.onTeleportationDone();
         }

         this.requestedTeleportPos = null;
      }

   }

   public void onRecipeBookData(RecipeBookDataC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      Optional var10000 = this.server.getRecipeManager().get(packet.getRecipeId());
      ServerRecipeBook var10001 = this.player.getRecipeBook();
      var10000.ifPresent(var10001::onRecipeDisplayed);
   }

   public void onRecipeCategoryOptions(RecipeCategoryOptionsC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.getRecipeBook().setCategoryOptions(packet.getCategory(), packet.isGuiOpen(), packet.isFilteringCraftable());
   }

   public void onAdvancementTab(AdvancementTabC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (packet.getAction() == AdvancementTabC2SPacket.Action.OPENED_TAB) {
         Identifier identifier = packet.getTabToOpen();
         Advancement advancement = this.server.getAdvancementLoader().get(identifier);
         if (advancement != null) {
            this.player.getAdvancementTracker().setDisplayTab(advancement);
         }
      }

   }

   public void onRequestCommandCompletions(RequestCommandCompletionsC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      StringReader stringReader = new StringReader(packet.getPartialCommand());
      if (stringReader.canRead() && stringReader.peek() == '/') {
         stringReader.skip();
      }

      ParseResults<ServerCommandSource> parseResults = this.server.getCommandManager().getDispatcher().parse((StringReader)stringReader, this.player.getCommandSource());
      this.server.getCommandManager().getDispatcher().getCompletionSuggestions(parseResults).thenAccept((suggestions) -> {
         this.connection.send(new CommandSuggestionsS2CPacket(packet.getCompletionId(), suggestions));
      });
   }

   public void onUpdateCommandBlock(UpdateCommandBlockC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (!this.server.areCommandBlocksEnabled()) {
         this.player.sendSystemMessage(new TranslatableText("advMode.notEnabled"), Util.NIL_UUID);
      } else if (!this.player.isCreativeLevelTwoOp()) {
         this.player.sendSystemMessage(new TranslatableText("advMode.notAllowed"), Util.NIL_UUID);
      } else {
         CommandBlockExecutor commandBlockExecutor = null;
         CommandBlockBlockEntity commandBlockBlockEntity = null;
         BlockPos blockPos = packet.getBlockPos();
         BlockEntity blockEntity = this.player.world.getBlockEntity(blockPos);
         if (blockEntity instanceof CommandBlockBlockEntity) {
            commandBlockBlockEntity = (CommandBlockBlockEntity)blockEntity;
            commandBlockExecutor = commandBlockBlockEntity.getCommandExecutor();
         }

         String string = packet.getCommand();
         boolean bl = packet.shouldTrackOutput();
         if (commandBlockExecutor != null) {
            CommandBlockBlockEntity.Type type = commandBlockBlockEntity.getCommandBlockType();
            Direction direction = (Direction)this.player.world.getBlockState(blockPos).get(CommandBlock.FACING);
            BlockState blockState2;
            switch(packet.getType()) {
            case SEQUENCE:
               blockState2 = Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
               this.player.world.setBlockState(blockPos, (BlockState)((BlockState)blockState2.with(CommandBlock.FACING, direction)).with(CommandBlock.CONDITIONAL, packet.isConditional()), 2);
               break;
            case AUTO:
               blockState2 = Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
               this.player.world.setBlockState(blockPos, (BlockState)((BlockState)blockState2.with(CommandBlock.FACING, direction)).with(CommandBlock.CONDITIONAL, packet.isConditional()), 2);
               break;
            case REDSTONE:
            default:
               blockState2 = Blocks.COMMAND_BLOCK.getDefaultState();
               this.player.world.setBlockState(blockPos, (BlockState)((BlockState)blockState2.with(CommandBlock.FACING, direction)).with(CommandBlock.CONDITIONAL, packet.isConditional()), 2);
            }

            blockEntity.cancelRemoval();
            this.player.world.setBlockEntity(blockPos, blockEntity);
            commandBlockExecutor.setCommand(string);
            commandBlockExecutor.setTrackingOutput(bl);
            if (!bl) {
               commandBlockExecutor.setLastOutput((Text)null);
            }

            commandBlockBlockEntity.setAuto(packet.isAlwaysActive());
            if (type != packet.getType()) {
               commandBlockBlockEntity.method_23359();
            }

            commandBlockExecutor.markDirty();
            if (!ChatUtil.isEmpty(string)) {
               this.player.sendSystemMessage(new TranslatableText("advMode.setCommand.success", new Object[]{string}), Util.NIL_UUID);
            }
         }

      }
   }

   public void onUpdateCommandBlockMinecart(UpdateCommandBlockMinecartC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (!this.server.areCommandBlocksEnabled()) {
         this.player.sendSystemMessage(new TranslatableText("advMode.notEnabled"), Util.NIL_UUID);
      } else if (!this.player.isCreativeLevelTwoOp()) {
         this.player.sendSystemMessage(new TranslatableText("advMode.notAllowed"), Util.NIL_UUID);
      } else {
         CommandBlockExecutor commandBlockExecutor = packet.getMinecartCommandExecutor(this.player.world);
         if (commandBlockExecutor != null) {
            commandBlockExecutor.setCommand(packet.getCommand());
            commandBlockExecutor.setTrackingOutput(packet.shouldTrackOutput());
            if (!packet.shouldTrackOutput()) {
               commandBlockExecutor.setLastOutput((Text)null);
            }

            commandBlockExecutor.markDirty();
            this.player.sendSystemMessage(new TranslatableText("advMode.setCommand.success", new Object[]{packet.getCommand()}), Util.NIL_UUID);
         }

      }
   }

   public void onPickFromInventory(PickFromInventoryC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.inventory.swapSlotWithHotbar(packet.getSlot());
      this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, this.player.inventory.selectedSlot, this.player.inventory.getStack(this.player.inventory.selectedSlot)));
      this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, packet.getSlot(), this.player.inventory.getStack(packet.getSlot())));
      this.player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(this.player.inventory.selectedSlot));
   }

   public void onRenameItem(RenameItemC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (this.player.currentScreenHandler instanceof AnvilScreenHandler) {
         AnvilScreenHandler anvilScreenHandler = (AnvilScreenHandler)this.player.currentScreenHandler;
         String string = SharedConstants.stripInvalidChars(packet.getName());
         if (string.length() <= 35) {
            anvilScreenHandler.setNewItemName(string);
         }
      }

   }

   public void onUpdateBeacon(UpdateBeaconC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (this.player.currentScreenHandler instanceof BeaconScreenHandler) {
         ((BeaconScreenHandler)this.player.currentScreenHandler).setEffects(packet.getPrimaryEffectId(), packet.getSecondaryEffectId());
      }

   }

   public void onStructureBlockUpdate(UpdateStructureBlockC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (this.player.isCreativeLevelTwoOp()) {
         BlockPos blockPos = packet.getPos();
         BlockState blockState = this.player.world.getBlockState(blockPos);
         BlockEntity blockEntity = this.player.world.getBlockEntity(blockPos);
         if (blockEntity instanceof StructureBlockBlockEntity) {
            StructureBlockBlockEntity structureBlockBlockEntity = (StructureBlockBlockEntity)blockEntity;
            structureBlockBlockEntity.setMode(packet.getMode());
            structureBlockBlockEntity.setStructureName(packet.getStructureName());
            structureBlockBlockEntity.setOffset(packet.getOffset());
            structureBlockBlockEntity.setSize(packet.getSize());
            structureBlockBlockEntity.setMirror(packet.getMirror());
            structureBlockBlockEntity.setRotation(packet.getRotation());
            structureBlockBlockEntity.setMetadata(packet.getMetadata());
            structureBlockBlockEntity.setIgnoreEntities(packet.shouldIgnoreEntities());
            structureBlockBlockEntity.setShowAir(packet.shouldShowAir());
            structureBlockBlockEntity.setShowBoundingBox(packet.shouldShowBoundingBox());
            structureBlockBlockEntity.setIntegrity(packet.getIntegrity());
            structureBlockBlockEntity.setSeed(packet.getSeed());
            if (structureBlockBlockEntity.hasStructureName()) {
               String string = structureBlockBlockEntity.getStructureName();
               if (packet.getAction() == StructureBlockBlockEntity.Action.SAVE_AREA) {
                  if (structureBlockBlockEntity.saveStructure()) {
                     this.player.sendMessage(new TranslatableText("structure_block.save_success", new Object[]{string}), false);
                  } else {
                     this.player.sendMessage(new TranslatableText("structure_block.save_failure", new Object[]{string}), false);
                  }
               } else if (packet.getAction() == StructureBlockBlockEntity.Action.LOAD_AREA) {
                  if (!structureBlockBlockEntity.isStructureAvailable()) {
                     this.player.sendMessage(new TranslatableText("structure_block.load_not_found", new Object[]{string}), false);
                  } else if (structureBlockBlockEntity.loadStructure(this.player.getServerWorld())) {
                     this.player.sendMessage(new TranslatableText("structure_block.load_success", new Object[]{string}), false);
                  } else {
                     this.player.sendMessage(new TranslatableText("structure_block.load_prepare", new Object[]{string}), false);
                  }
               } else if (packet.getAction() == StructureBlockBlockEntity.Action.SCAN_AREA) {
                  if (structureBlockBlockEntity.detectStructureSize()) {
                     this.player.sendMessage(new TranslatableText("structure_block.size_success", new Object[]{string}), false);
                  } else {
                     this.player.sendMessage(new TranslatableText("structure_block.size_failure"), false);
                  }
               }
            } else {
               this.player.sendMessage(new TranslatableText("structure_block.invalid_structure_name", new Object[]{packet.getStructureName()}), false);
            }

            structureBlockBlockEntity.markDirty();
            this.player.world.updateListeners(blockPos, blockState, blockState, 3);
         }

      }
   }

   public void onJigsawUpdate(UpdateJigsawC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (this.player.isCreativeLevelTwoOp()) {
         BlockPos blockPos = packet.getPos();
         BlockState blockState = this.player.world.getBlockState(blockPos);
         BlockEntity blockEntity = this.player.world.getBlockEntity(blockPos);
         if (blockEntity instanceof JigsawBlockEntity) {
            JigsawBlockEntity jigsawBlockEntity = (JigsawBlockEntity)blockEntity;
            jigsawBlockEntity.setAttachmentType(packet.getAttachmentType());
            jigsawBlockEntity.setTargetPool(packet.getTargetPool());
            jigsawBlockEntity.setPool(packet.getPool());
            jigsawBlockEntity.setFinalState(packet.getFinalState());
            jigsawBlockEntity.setJoint(packet.getJointType());
            jigsawBlockEntity.markDirty();
            this.player.world.updateListeners(blockPos, blockState, blockState, 3);
         }

      }
   }

   public void onJigsawGenerating(JigsawGeneratingC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (this.player.isCreativeLevelTwoOp()) {
         BlockPos blockPos = packet.getPos();
         BlockEntity blockEntity = this.player.world.getBlockEntity(blockPos);
         if (blockEntity instanceof JigsawBlockEntity) {
            JigsawBlockEntity jigsawBlockEntity = (JigsawBlockEntity)blockEntity;
            jigsawBlockEntity.generate(this.player.getServerWorld(), packet.getMaxDepth(), packet.shouldKeepJigsaws());
         }

      }
   }

   public void onMerchantTradeSelect(SelectMerchantTradeC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      int i = packet.getTradeId();
      ScreenHandler screenHandler = this.player.currentScreenHandler;
      if (screenHandler instanceof MerchantScreenHandler) {
         MerchantScreenHandler merchantScreenHandler = (MerchantScreenHandler)screenHandler;
         merchantScreenHandler.setRecipeIndex(i);
         merchantScreenHandler.switchTo(i);
      }

   }

   public void onBookUpdate(BookUpdateC2SPacket packet) {
      ItemStack itemStack = packet.getBook();
      if (itemStack.getItem() == Items.WRITABLE_BOOK) {
         NbtCompound nbtCompound = itemStack.getTag();
         if (WritableBookItem.isValid(nbtCompound)) {
            List<String> list = Lists.newArrayList();
            boolean bl = packet.wasSigned();
            if (bl) {
               list.add(nbtCompound.getString("title"));
            }

            NbtList nbtList = nbtCompound.getList("pages", 8);

            int j;
            for(j = 0; j < nbtList.size(); ++j) {
               list.add(nbtList.getString(j));
            }

            j = packet.getSlot();
            if (PlayerInventory.isValidHotbarIndex(j) || j == 40) {
               this.filterTexts(list, bl ? (listx) -> {
                  this.method_31276((String)listx.get(0), listx.subList(1, listx.size()), j);
               } : (listx) -> {
                  this.method_31278(listx, j);
               });
            }
         }
      }
   }

   private void method_31278(List<String> list, int i) {
      ItemStack itemStack = this.player.inventory.getStack(i);
      if (itemStack.getItem() == Items.WRITABLE_BOOK) {
         NbtList nbtList = new NbtList();
         list.stream().map(NbtString::of).forEach(nbtList::add);
         itemStack.putSubTag("pages", nbtList);
      }
   }

   private void method_31276(String string, List<String> list, int i) {
      ItemStack itemStack = this.player.inventory.getStack(i);
      if (itemStack.getItem() == Items.WRITABLE_BOOK) {
         ItemStack itemStack2 = new ItemStack(Items.WRITTEN_BOOK);
         NbtCompound nbtCompound = itemStack.getTag();
         if (nbtCompound != null) {
            itemStack2.setTag(nbtCompound.copy());
         }

         itemStack2.putSubTag("author", NbtString.of(this.player.getName().getString()));
         itemStack2.putSubTag("title", NbtString.of(string));
         NbtList nbtList = new NbtList();
         Iterator var8 = list.iterator();

         while(var8.hasNext()) {
            String string2 = (String)var8.next();
            Text text = new LiteralText(string2);
            String string3 = Text.Serializer.toJson(text);
            nbtList.add(NbtString.of(string3));
         }

         itemStack2.putSubTag("pages", nbtList);
         this.player.inventory.setStack(i, itemStack2);
      }
   }

   public void onQueryEntityNbt(QueryEntityNbtC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (this.player.hasPermissionLevel(2)) {
         Entity entity = this.player.getServerWorld().getEntityById(packet.getEntityId());
         if (entity != null) {
            NbtCompound nbtCompound = entity.writeNbt(new NbtCompound());
            this.player.networkHandler.sendPacket(new NbtQueryResponseS2CPacket(packet.getTransactionId(), nbtCompound));
         }

      }
   }

   public void onQueryBlockNbt(QueryBlockNbtC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (this.player.hasPermissionLevel(2)) {
         BlockEntity blockEntity = this.player.getServerWorld().getBlockEntity(packet.getPos());
         NbtCompound nbtCompound = blockEntity != null ? blockEntity.writeNbt(new NbtCompound()) : null;
         this.player.networkHandler.sendPacket(new NbtQueryResponseS2CPacket(packet.getTransactionId(), nbtCompound));
      }
   }

   public void onPlayerMove(PlayerMoveC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (validatePlayerMove(packet)) {
         this.disconnect(new TranslatableText("multiplayer.disconnect.invalid_player_movement"));
      } else {
         ServerWorld serverWorld = this.player.getServerWorld();
         if (!this.player.notInAnyWorld) {
            if (this.ticks == 0) {
               this.syncWithPlayerPosition();
            }

            if (this.requestedTeleportPos != null) {
               if (this.ticks - this.teleportRequestTick > 20) {
                  this.teleportRequestTick = this.ticks;
                  this.requestTeleport(this.requestedTeleportPos.x, this.requestedTeleportPos.y, this.requestedTeleportPos.z, this.player.yaw, this.player.pitch);
               }

            } else {
               this.teleportRequestTick = this.ticks;
               if (this.player.hasVehicle()) {
                  this.player.updatePositionAndAngles(this.player.getX(), this.player.getY(), this.player.getZ(), packet.getYaw(this.player.yaw), packet.getPitch(this.player.pitch));
                  this.player.getServerWorld().getChunkManager().updatePosition(this.player);
               } else {
                  double d = this.player.getX();
                  double e = this.player.getY();
                  double f = this.player.getZ();
                  double g = this.player.getY();
                  double h = packet.getX(this.player.getX());
                  double i = packet.getY(this.player.getY());
                  double j = packet.getZ(this.player.getZ());
                  float k = packet.getYaw(this.player.yaw);
                  float l = packet.getPitch(this.player.pitch);
                  double m = h - this.lastTickX;
                  double n = i - this.lastTickY;
                  double o = j - this.lastTickZ;
                  double p = this.player.getVelocity().lengthSquared();
                  double q = m * m + n * n + o * o;
                  if (this.player.isSleeping()) {
                     if (q > 1.0D) {
                        this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), packet.getYaw(this.player.yaw), packet.getPitch(this.player.pitch));
                     }

                  } else {
                     ++this.movePacketsCount;
                     int r = this.movePacketsCount - this.lastTickMovePacketsCount;
                     if (r > 5) {
                        LOGGER.debug((String)"{} is sending move packets too frequently ({} packets since last tick)", (Object)this.player.getName().getString(), (Object)r);
                        r = 1;
                     }

                     if (!this.player.isInTeleportationState() && (!this.player.getServerWorld().getGameRules().getBoolean(GameRules.DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
                        float s = this.player.isFallFlying() ? 300.0F : 100.0F;
                        if (q - p > (double)(s * (float)r) && !this.isHost()) {
                           LOGGER.warn((String)"{} moved too quickly! {},{},{}", (Object)this.player.getName().getString(), m, n, o);
                           this.requestTeleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.yaw, this.player.pitch);
                           return;
                        }
                     }

                     Box box = this.player.getBoundingBox();
                     m = h - this.updatedX;
                     n = i - this.updatedY;
                     o = j - this.updatedZ;
                     boolean bl = n > 0.0D;
                     if (this.player.isOnGround() && !packet.isOnGround() && bl) {
                        this.player.jump();
                     }

                     this.player.move(MovementType.PLAYER, new Vec3d(m, n, o));
                     double t = n;
                     m = h - this.player.getX();
                     n = i - this.player.getY();
                     if (n > -0.5D || n < 0.5D) {
                        n = 0.0D;
                     }

                     o = j - this.player.getZ();
                     q = m * m + n * n + o * o;
                     boolean bl2 = false;
                     if (!this.player.isInTeleportationState() && q > 0.0625D && !this.player.isSleeping() && !this.player.interactionManager.isCreative() && this.player.interactionManager.getGameMode() != GameMode.SPECTATOR) {
                        bl2 = true;
                        LOGGER.warn((String)"{} moved wrongly!", (Object)this.player.getName().getString());
                     }

                     this.player.updatePositionAndAngles(h, i, j, k, l);
                     if (this.player.noClip || this.player.isSleeping() || (!bl2 || !serverWorld.isSpaceEmpty(this.player, box)) && !this.isPlayerNotCollidingWithBlocks(serverWorld, box)) {
                        this.floating = t >= -0.03125D && this.player.interactionManager.getGameMode() != GameMode.SPECTATOR && !this.server.isFlightEnabled() && !this.player.abilities.allowFlying && !this.player.hasStatusEffect(StatusEffects.LEVITATION) && !this.player.isFallFlying() && this.method_29780(this.player);
                        this.player.getServerWorld().getChunkManager().updatePosition(this.player);
                        this.player.handleFall(this.player.getY() - g, packet.isOnGround());
                        this.player.setOnGround(packet.isOnGround());
                        if (bl) {
                           this.player.fallDistance = 0.0F;
                        }

                        this.player.increaseTravelMotionStats(this.player.getX() - d, this.player.getY() - e, this.player.getZ() - f);
                        this.updatedX = this.player.getX();
                        this.updatedY = this.player.getY();
                        this.updatedZ = this.player.getZ();
                     } else {
                        this.requestTeleport(d, e, f, k, l);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isPlayerNotCollidingWithBlocks(WorldView world, Box box) {
      Stream<VoxelShape> stream = world.getCollisions(this.player, this.player.getBoundingBox().contract(9.999999747378752E-6D), (entity) -> {
         return true;
      });
      VoxelShape voxelShape = VoxelShapes.cuboid(box.contract(9.999999747378752E-6D));
      return stream.anyMatch((voxelShape2) -> {
         return !VoxelShapes.matchesAnywhere(voxelShape2, voxelShape, BooleanBiFunction.AND);
      });
   }

   public void requestTeleport(double x, double y, double z, float yaw, float pitch) {
      this.requestTeleport(x, y, z, yaw, pitch, Collections.emptySet());
   }

   public void requestTeleport(double x, double y, double z, float yaw, float pitch, Set<PlayerPositionLookS2CPacket.Flag> flags) {
      double d = flags.contains(PlayerPositionLookS2CPacket.Flag.X) ? this.player.getX() : 0.0D;
      double e = flags.contains(PlayerPositionLookS2CPacket.Flag.Y) ? this.player.getY() : 0.0D;
      double f = flags.contains(PlayerPositionLookS2CPacket.Flag.Z) ? this.player.getZ() : 0.0D;
      float g = flags.contains(PlayerPositionLookS2CPacket.Flag.Y_ROT) ? this.player.yaw : 0.0F;
      float h = flags.contains(PlayerPositionLookS2CPacket.Flag.X_ROT) ? this.player.pitch : 0.0F;
      this.requestedTeleportPos = new Vec3d(x, y, z);
      if (++this.requestedTeleportId == Integer.MAX_VALUE) {
         this.requestedTeleportId = 0;
      }

      this.teleportRequestTick = this.ticks;
      this.player.updatePositionAndAngles(x, y, z, yaw, pitch);
      this.player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(x - d, y - e, z - f, yaw - g, pitch - h, flags, this.requestedTeleportId));
   }

   public void onPlayerAction(PlayerActionC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      BlockPos blockPos = packet.getPos();
      this.player.updateLastActionTime();
      PlayerActionC2SPacket.Action action = packet.getAction();
      switch(action) {
      case SWAP_ITEM_WITH_OFFHAND:
         if (!this.player.isSpectator()) {
            ItemStack itemStack = this.player.getStackInHand(Hand.OFF_HAND);
            this.player.setStackInHand(Hand.OFF_HAND, this.player.getStackInHand(Hand.MAIN_HAND));
            this.player.setStackInHand(Hand.MAIN_HAND, itemStack);
            this.player.clearActiveItem();
         }

         return;
      case DROP_ITEM:
         if (!this.player.isSpectator()) {
            this.player.dropSelectedItem(false);
         }

         return;
      case DROP_ALL_ITEMS:
         if (!this.player.isSpectator()) {
            this.player.dropSelectedItem(true);
         }

         return;
      case RELEASE_USE_ITEM:
         this.player.stopUsingItem();
         return;
      case START_DESTROY_BLOCK:
      case ABORT_DESTROY_BLOCK:
      case STOP_DESTROY_BLOCK:
         this.player.interactionManager.processBlockBreakingAction(blockPos, action, packet.getDirection(), this.server.getWorldHeight());
         return;
      default:
         throw new IllegalArgumentException("Invalid player action");
      }
   }

   /**
    * Checks if a player can place a block or fluid from a bucket.
    * 
    * <p>For this to return true, the player must not be actively cooling down.
    */
   private static boolean canPlace(ServerPlayerEntity player, ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         Item item = stack.getItem();
         return (item instanceof BlockItem || item instanceof BucketItem) && !player.getItemCooldownManager().isCoolingDown(item);
      }
   }

   public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      ServerWorld serverWorld = this.player.getServerWorld();
      Hand hand = packet.getHand();
      ItemStack itemStack = this.player.getStackInHand(hand);
      BlockHitResult blockHitResult = packet.getBlockHitResult();
      BlockPos blockPos = blockHitResult.getBlockPos();
      Direction direction = blockHitResult.getSide();
      this.player.updateLastActionTime();
      if (blockPos.getY() < this.server.getWorldHeight()) {
         if (this.requestedTeleportPos == null && this.player.squaredDistanceTo((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D) < 64.0D && serverWorld.canPlayerModifyAt(this.player, blockPos)) {
            ActionResult actionResult = this.player.interactionManager.interactBlock(this.player, serverWorld, itemStack, hand, blockHitResult);
            if (direction == Direction.UP && !actionResult.isAccepted() && blockPos.getY() >= this.server.getWorldHeight() - 1 && canPlace(this.player, itemStack)) {
               Text text = (new TranslatableText("build.tooHigh", new Object[]{this.server.getWorldHeight()})).formatted(Formatting.RED);
               this.player.networkHandler.sendPacket(new GameMessageS2CPacket(text, MessageType.GAME_INFO, Util.NIL_UUID));
            } else if (actionResult.shouldSwingHand()) {
               this.player.swingHand(hand, true);
            }
         }
      } else {
         Text text2 = (new TranslatableText("build.tooHigh", new Object[]{this.server.getWorldHeight()})).formatted(Formatting.RED);
         this.player.networkHandler.sendPacket(new GameMessageS2CPacket(text2, MessageType.GAME_INFO, Util.NIL_UUID));
      }

      this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(serverWorld, blockPos));
      this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(serverWorld, blockPos.offset(direction)));
   }

   public void onPlayerInteractItem(PlayerInteractItemC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      ServerWorld serverWorld = this.player.getServerWorld();
      Hand hand = packet.getHand();
      ItemStack itemStack = this.player.getStackInHand(hand);
      this.player.updateLastActionTime();
      if (!itemStack.isEmpty()) {
         ActionResult actionResult = this.player.interactionManager.interactItem(this.player, serverWorld, itemStack, hand);
         if (actionResult.shouldSwingHand()) {
            this.player.swingHand(hand, true);
         }

      }
   }

   public void onSpectatorTeleport(SpectatorTeleportC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (this.player.isSpectator()) {
         Iterator var2 = this.server.getWorlds().iterator();

         while(var2.hasNext()) {
            ServerWorld serverWorld = (ServerWorld)var2.next();
            Entity entity = packet.getTarget(serverWorld);
            if (entity != null) {
               this.player.teleport(serverWorld, entity.getX(), entity.getY(), entity.getZ(), entity.yaw, entity.pitch);
               return;
            }
         }
      }

   }

   public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {
   }

   public void onBoatPaddleState(BoatPaddleStateC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      Entity entity = this.player.getVehicle();
      if (entity instanceof BoatEntity) {
         ((BoatEntity)entity).setPaddleMovings(packet.isLeftPaddling(), packet.isRightPaddling());
      }

   }

   public void onDisconnected(Text reason) {
      LOGGER.info((String)"{} lost connection: {}", (Object)this.player.getName().getString(), (Object)reason.getString());
      this.server.forcePlayerSampleUpdate();
      this.server.getPlayerManager().broadcastChatMessage((new TranslatableText("multiplayer.player.left", new Object[]{this.player.getDisplayName()})).formatted(Formatting.YELLOW), MessageType.SYSTEM, Util.NIL_UUID);
      this.player.onDisconnect();
      this.server.getPlayerManager().remove(this.player);
      TextStream textStream = this.player.getTextStream();
      if (textStream != null) {
         textStream.onDisconnect();
      }

      if (this.isHost()) {
         LOGGER.info("Stopping singleplayer server as player logged out");
         this.server.stop(false);
      }

   }

   public void sendPacket(Packet<?> packet) {
      this.sendPacket(packet, (GenericFutureListener)null);
   }

   public void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener) {
      if (packet instanceof GameMessageS2CPacket) {
         GameMessageS2CPacket gameMessageS2CPacket = (GameMessageS2CPacket)packet;
         ChatVisibility chatVisibility = this.player.getClientChatVisibility();
         if (chatVisibility == ChatVisibility.HIDDEN && gameMessageS2CPacket.getLocation() != MessageType.GAME_INFO) {
            return;
         }

         if (chatVisibility == ChatVisibility.SYSTEM && !gameMessageS2CPacket.isNonChat()) {
            return;
         }
      }

      try {
         this.connection.send(packet, listener);
      } catch (Throwable var6) {
         CrashReport crashReport = CrashReport.create(var6, "Sending packet");
         CrashReportSection crashReportSection = crashReport.addElement("Packet being sent");
         crashReportSection.add("Packet class", () -> {
            return packet.getClass().getCanonicalName();
         });
         throw new CrashException(crashReport);
      }
   }

   public void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (packet.getSelectedSlot() >= 0 && packet.getSelectedSlot() < PlayerInventory.getHotbarSize()) {
         if (this.player.inventory.selectedSlot != packet.getSelectedSlot() && this.player.getActiveHand() == Hand.MAIN_HAND) {
            this.player.clearActiveItem();
         }

         this.player.inventory.selectedSlot = packet.getSelectedSlot();
         this.player.updateLastActionTime();
      } else {
         LOGGER.warn((String)"{} tried to set an invalid carried item", (Object)this.player.getName().getString());
      }
   }

   public void onGameMessage(ChatMessageC2SPacket packet) {
      String string = StringUtils.normalizeSpace(packet.getChatMessage());
      if (string.startsWith("/")) {
         NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
         this.method_31286(string);
      } else {
         this.filterText(string, this::method_31286);
      }

   }

   private void method_31286(String string) {
      if (this.player.getClientChatVisibility() == ChatVisibility.HIDDEN) {
         this.sendPacket(new GameMessageS2CPacket((new TranslatableText("chat.cannotSend")).formatted(Formatting.RED), MessageType.SYSTEM, Util.NIL_UUID));
      } else {
         this.player.updateLastActionTime();

         for(int i = 0; i < string.length(); ++i) {
            if (!SharedConstants.isValidChar(string.charAt(i))) {
               this.disconnect(new TranslatableText("multiplayer.disconnect.illegal_characters"));
               return;
            }
         }

         if (string.startsWith("/")) {
            this.executeCommand(string);
         } else {
            Text text = new TranslatableText("chat.type.text", new Object[]{this.player.getDisplayName(), string});
            this.server.getPlayerManager().broadcastChatMessage(text, MessageType.CHAT, this.player.getUuid());
         }

         this.messageCooldown += 20;
         if (this.messageCooldown > 200 && !this.server.getPlayerManager().isOperator(this.player.getGameProfile())) {
            this.disconnect(new TranslatableText("disconnect.spam"));
         }

      }
   }

   private void executeCommand(String input) {
      this.server.getCommandManager().execute(this.player.getCommandSource(), input);
   }

   public void onHandSwing(HandSwingC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.updateLastActionTime();
      this.player.swingHand(packet.getHand());
   }

   public void onClientCommand(ClientCommandC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.updateLastActionTime();
      JumpingMount jumpingMount;
      switch(packet.getMode()) {
      case PRESS_SHIFT_KEY:
         this.player.setSneaking(true);
         break;
      case RELEASE_SHIFT_KEY:
         this.player.setSneaking(false);
         break;
      case START_SPRINTING:
         this.player.setSprinting(true);
         break;
      case STOP_SPRINTING:
         this.player.setSprinting(false);
         break;
      case STOP_SLEEPING:
         if (this.player.isSleeping()) {
            this.player.wakeUp(false, true);
            this.requestedTeleportPos = this.player.getPos();
         }
         break;
      case START_RIDING_JUMP:
         if (this.player.getVehicle() instanceof JumpingMount) {
            jumpingMount = (JumpingMount)this.player.getVehicle();
            int i = packet.getMountJumpHeight();
            if (jumpingMount.canJump() && i > 0) {
               jumpingMount.startJumping(i);
            }
         }
         break;
      case STOP_RIDING_JUMP:
         if (this.player.getVehicle() instanceof JumpingMount) {
            jumpingMount = (JumpingMount)this.player.getVehicle();
            jumpingMount.stopJumping();
         }
         break;
      case OPEN_INVENTORY:
         if (this.player.getVehicle() instanceof HorseBaseEntity) {
            ((HorseBaseEntity)this.player.getVehicle()).openInventory(this.player);
         }
         break;
      case START_FALL_FLYING:
         if (!this.player.checkFallFlying()) {
            this.player.stopFallFlying();
         }
         break;
      default:
         throw new IllegalArgumentException("Invalid client command!");
      }

   }

   public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      ServerWorld serverWorld = this.player.getServerWorld();
      Entity entity = packet.getEntity(serverWorld);
      this.player.updateLastActionTime();
      this.player.setSneaking(packet.isPlayerSneaking());
      if (entity != null) {
         double d = 36.0D;
         if (this.player.squaredDistanceTo(entity) < 36.0D) {
            Hand hand = packet.getHand();
            ItemStack itemStack = hand != null ? this.player.getStackInHand(hand).copy() : ItemStack.EMPTY;
            Optional<ActionResult> optional = Optional.empty();
            if (packet.getType() == PlayerInteractEntityC2SPacket.InteractionType.INTERACT) {
               optional = Optional.of(this.player.interact(entity, hand));
            } else if (packet.getType() == PlayerInteractEntityC2SPacket.InteractionType.INTERACT_AT) {
               optional = Optional.of(entity.interactAt(this.player, packet.getHitPosition(), hand));
            } else if (packet.getType() == PlayerInteractEntityC2SPacket.InteractionType.ATTACK) {
               if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof PersistentProjectileEntity || entity == this.player) {
                  this.disconnect(new TranslatableText("multiplayer.disconnect.invalid_entity_attacked"));
                  LOGGER.warn((String)"Player {} tried to attack an invalid entity", (Object)this.player.getName().getString());
                  return;
               }

               this.player.attack(entity);
            }

            if (optional.isPresent() && ((ActionResult)optional.get()).isAccepted()) {
               Criteria.PLAYER_INTERACTED_WITH_ENTITY.test(this.player, itemStack, entity);
               if (((ActionResult)optional.get()).shouldSwingHand()) {
                  this.player.swingHand(hand, true);
               }
            }
         }
      }

   }

   public void onClientStatus(ClientStatusC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.updateLastActionTime();
      ClientStatusC2SPacket.Mode mode = packet.getMode();
      switch(mode) {
      case PERFORM_RESPAWN:
         if (this.player.notInAnyWorld) {
            this.player.notInAnyWorld = false;
            this.player = this.server.getPlayerManager().respawnPlayer(this.player, true);
            Criteria.CHANGED_DIMENSION.trigger(this.player, World.END, World.OVERWORLD);
         } else {
            if (this.player.getHealth() > 0.0F) {
               return;
            }

            this.player = this.server.getPlayerManager().respawnPlayer(this.player, false);
            if (this.server.isHardcore()) {
               this.player.setGameMode(GameMode.SPECTATOR);
               ((GameRules.BooleanRule)this.player.getServerWorld().getGameRules().get(GameRules.SPECTATORS_GENERATE_CHUNKS)).set(false, this.server);
            }
         }
         break;
      case REQUEST_STATS:
         this.player.getStatHandler().sendStats(this.player);
      }

   }

   public void onCloseHandledScreen(CloseHandledScreenC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.closeScreenHandler();
   }

   public void onClickSlot(ClickSlotC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.updateLastActionTime();
      if (this.player.currentScreenHandler.syncId == packet.getSyncId() && this.player.currentScreenHandler.isNotRestricted(this.player)) {
         if (this.player.isSpectator()) {
            DefaultedList<ItemStack> defaultedList = DefaultedList.of();

            for(int i = 0; i < this.player.currentScreenHandler.slots.size(); ++i) {
               defaultedList.add(((Slot)this.player.currentScreenHandler.slots.get(i)).getStack());
            }

            this.player.onHandlerRegistered(this.player.currentScreenHandler, defaultedList);
         } else {
            ItemStack itemStack = this.player.currentScreenHandler.onSlotClick(packet.getSlot(), packet.getButton(), packet.getActionType(), this.player);
            if (ItemStack.areEqual(packet.getStack(), itemStack)) {
               this.player.networkHandler.sendPacket(new ConfirmScreenActionS2CPacket(packet.getSyncId(), packet.getActionId(), true));
               this.player.skipPacketSlotUpdates = true;
               this.player.currentScreenHandler.sendContentUpdates();
               this.player.updateCursorStack();
               this.player.skipPacketSlotUpdates = false;
            } else {
               this.transactions.put(this.player.currentScreenHandler.syncId, packet.getActionId());
               this.player.networkHandler.sendPacket(new ConfirmScreenActionS2CPacket(packet.getSyncId(), packet.getActionId(), false));
               this.player.currentScreenHandler.setPlayerRestriction(this.player, false);
               DefaultedList<ItemStack> defaultedList2 = DefaultedList.of();

               for(int j = 0; j < this.player.currentScreenHandler.slots.size(); ++j) {
                  ItemStack itemStack2 = ((Slot)this.player.currentScreenHandler.slots.get(j)).getStack();
                  defaultedList2.add(itemStack2.isEmpty() ? ItemStack.EMPTY : itemStack2);
               }

               this.player.onHandlerRegistered(this.player.currentScreenHandler, defaultedList2);
            }
         }
      }

   }

   public void onCraftRequest(CraftRequestC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.updateLastActionTime();
      if (!this.player.isSpectator() && this.player.currentScreenHandler.syncId == packet.getSyncId() && this.player.currentScreenHandler.isNotRestricted(this.player) && this.player.currentScreenHandler instanceof AbstractRecipeScreenHandler) {
         this.server.getRecipeManager().get(packet.getRecipe()).ifPresent((recipe) -> {
            ((AbstractRecipeScreenHandler)this.player.currentScreenHandler).fillInputSlots(packet.shouldCraftAll(), recipe, this.player);
         });
      }
   }

   public void onButtonClick(ButtonClickC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.updateLastActionTime();
      if (this.player.currentScreenHandler.syncId == packet.getSyncId() && this.player.currentScreenHandler.isNotRestricted(this.player) && !this.player.isSpectator()) {
         this.player.currentScreenHandler.onButtonClick(this.player, packet.getButtonId());
         this.player.currentScreenHandler.sendContentUpdates();
      }

   }

   public void onCreativeInventoryAction(CreativeInventoryActionC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (this.player.interactionManager.isCreative()) {
         boolean bl = packet.getSlot() < 0;
         ItemStack itemStack = packet.getItemStack();
         NbtCompound nbtCompound = itemStack.getSubTag("BlockEntityTag");
         if (!itemStack.isEmpty() && nbtCompound != null && nbtCompound.contains("x") && nbtCompound.contains("y") && nbtCompound.contains("z")) {
            BlockPos blockPos = new BlockPos(nbtCompound.getInt("x"), nbtCompound.getInt("y"), nbtCompound.getInt("z"));
            BlockEntity blockEntity = this.player.world.getBlockEntity(blockPos);
            if (blockEntity != null) {
               NbtCompound nbtCompound2 = blockEntity.writeNbt(new NbtCompound());
               nbtCompound2.remove("x");
               nbtCompound2.remove("y");
               nbtCompound2.remove("z");
               itemStack.putSubTag("BlockEntityTag", nbtCompound2);
            }
         }

         boolean bl2 = packet.getSlot() >= 1 && packet.getSlot() <= 45;
         boolean bl3 = itemStack.isEmpty() || itemStack.getDamage() >= 0 && itemStack.getCount() <= 64 && !itemStack.isEmpty();
         if (bl2 && bl3) {
            if (itemStack.isEmpty()) {
               this.player.playerScreenHandler.setStackInSlot(packet.getSlot(), ItemStack.EMPTY);
            } else {
               this.player.playerScreenHandler.setStackInSlot(packet.getSlot(), itemStack);
            }

            this.player.playerScreenHandler.setPlayerRestriction(this.player, true);
            this.player.playerScreenHandler.sendContentUpdates();
         } else if (bl && bl3 && this.creativeItemDropThreshold < 200) {
            this.creativeItemDropThreshold += 20;
            this.player.dropItem(itemStack, true);
         }
      }

   }

   public void onConfirmScreenAction(ConfirmScreenActionC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      int i = this.player.currentScreenHandler.syncId;
      if (i == packet.getSyncId() && this.transactions.getOrDefault(i, (short)(packet.getActionId() + 1)) == packet.getActionId() && !this.player.currentScreenHandler.isNotRestricted(this.player) && !this.player.isSpectator()) {
         this.player.currentScreenHandler.setPlayerRestriction(this.player, true);
      }

   }

   public void onSignUpdate(UpdateSignC2SPacket packet) {
      List<String> list = (List)Stream.of(packet.getText()).map(Formatting::strip).collect(Collectors.toList());
      this.filterTexts(list, (listx) -> {
         this.method_31282(packet, listx);
      });
   }

   private void method_31282(UpdateSignC2SPacket updateSignC2SPacket, List<String> list) {
      this.player.updateLastActionTime();
      ServerWorld serverWorld = this.player.getServerWorld();
      BlockPos blockPos = updateSignC2SPacket.getPos();
      if (serverWorld.isChunkLoaded(blockPos)) {
         BlockState blockState = serverWorld.getBlockState(blockPos);
         BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos);
         if (!(blockEntity instanceof SignBlockEntity)) {
            return;
         }

         SignBlockEntity signBlockEntity = (SignBlockEntity)blockEntity;
         if (!signBlockEntity.isEditable() || signBlockEntity.getEditor() != this.player) {
            LOGGER.warn((String)"Player {} just tried to change non-editable sign", (Object)this.player.getName().getString());
            return;
         }

         for(int i = 0; i < list.size(); ++i) {
            signBlockEntity.setTextOnRow(i, new LiteralText((String)list.get(i)));
         }

         signBlockEntity.markDirty();
         serverWorld.updateListeners(blockPos, blockState, blockState, 3);
      }

   }

   public void onKeepAlive(KeepAliveC2SPacket packet) {
      if (this.waitingForKeepAlive && packet.getId() == this.keepAliveId) {
         int i = (int)(Util.getMeasuringTimeMs() - this.lastKeepAliveTime);
         this.player.pingMilliseconds = (this.player.pingMilliseconds * 3 + i) / 4;
         this.waitingForKeepAlive = false;
      } else if (!this.isHost()) {
         this.disconnect(new TranslatableText("disconnect.timeout"));
      }

   }

   public void onPlayerAbilities(UpdatePlayerAbilitiesC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.abilities.flying = packet.isFlying() && this.player.abilities.allowFlying;
   }

   public void onClientSettings(ClientSettingsC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      this.player.setClientSettings(packet);
   }

   public void onCustomPayload(CustomPayloadC2SPacket packet) {
   }

   public void onUpdateDifficulty(UpdateDifficultyC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (this.player.hasPermissionLevel(2) || this.isHost()) {
         this.server.setDifficulty(packet.getDifficulty(), false);
      }
   }

   public void onUpdateDifficultyLock(UpdateDifficultyLockC2SPacket packet) {
      NetworkThreadUtils.forceMainThread(packet, this, (ServerWorld)this.player.getServerWorld());
      if (this.player.hasPermissionLevel(2) || this.isHost()) {
         this.server.setDifficultyLocked(packet.isDifficultyLocked());
      }
   }
}
