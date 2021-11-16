package net.minecraft.entity.boss;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class CommandBossBar extends ServerBossBar {
   private final Identifier id;
   private final Set<UUID> playerUuids = Sets.newHashSet();
   private int value;
   private int maxValue = 100;

   public CommandBossBar(Identifier id, Text displayName) {
      super(displayName, BossBar.Color.WHITE, BossBar.Style.PROGRESS);
      this.id = id;
      this.setPercent(0.0F);
   }

   public Identifier getId() {
      return this.id;
   }

   public void addPlayer(ServerPlayerEntity player) {
      super.addPlayer(player);
      this.playerUuids.add(player.getUuid());
   }

   public void addPlayer(UUID uuid) {
      this.playerUuids.add(uuid);
   }

   public void removePlayer(ServerPlayerEntity player) {
      super.removePlayer(player);
      this.playerUuids.remove(player.getUuid());
   }

   public void clearPlayers() {
      super.clearPlayers();
      this.playerUuids.clear();
   }

   public int getValue() {
      return this.value;
   }

   public int getMaxValue() {
      return this.maxValue;
   }

   public void setValue(int value) {
      this.value = value;
      this.setPercent(MathHelper.clamp((float)value / (float)this.maxValue, 0.0F, 1.0F));
   }

   public void setMaxValue(int maxValue) {
      this.maxValue = maxValue;
      this.setPercent(MathHelper.clamp((float)this.value / (float)maxValue, 0.0F, 1.0F));
   }

   public final Text toHoverableText() {
      return Texts.bracketed(this.getName()).styled((style) -> {
         return style.withColor(this.getColor().getTextFormat()).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(this.getId().toString()))).withInsertion(this.getId().toString());
      });
   }

   public boolean addPlayers(Collection<ServerPlayerEntity> players) {
      Set<UUID> set = Sets.newHashSet();
      Set<ServerPlayerEntity> set2 = Sets.newHashSet();
      Iterator var4 = this.playerUuids.iterator();

      UUID uUID3;
      boolean bl2;
      Iterator var7;
      while(var4.hasNext()) {
         uUID3 = (UUID)var4.next();
         bl2 = false;
         var7 = players.iterator();

         while(var7.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var7.next();
            if (serverPlayerEntity.getUuid().equals(uUID3)) {
               bl2 = true;
               break;
            }
         }

         if (!bl2) {
            set.add(uUID3);
         }
      }

      var4 = players.iterator();

      ServerPlayerEntity serverPlayerEntity4;
      while(var4.hasNext()) {
         serverPlayerEntity4 = (ServerPlayerEntity)var4.next();
         bl2 = false;
         var7 = this.playerUuids.iterator();

         while(var7.hasNext()) {
            UUID uUID2 = (UUID)var7.next();
            if (serverPlayerEntity4.getUuid().equals(uUID2)) {
               bl2 = true;
               break;
            }
         }

         if (!bl2) {
            set2.add(serverPlayerEntity4);
         }
      }

      for(var4 = set.iterator(); var4.hasNext(); this.playerUuids.remove(uUID3)) {
         uUID3 = (UUID)var4.next();
         Iterator var11 = this.getPlayers().iterator();

         while(var11.hasNext()) {
            ServerPlayerEntity serverPlayerEntity3 = (ServerPlayerEntity)var11.next();
            if (serverPlayerEntity3.getUuid().equals(uUID3)) {
               this.removePlayer(serverPlayerEntity3);
               break;
            }
         }
      }

      var4 = set2.iterator();

      while(var4.hasNext()) {
         serverPlayerEntity4 = (ServerPlayerEntity)var4.next();
         this.addPlayer(serverPlayerEntity4);
      }

      return !set.isEmpty() || !set2.isEmpty();
   }

   public NbtCompound toNbt() {
      NbtCompound nbtCompound = new NbtCompound();
      nbtCompound.putString("Name", Text.Serializer.toJson(this.name));
      nbtCompound.putBoolean("Visible", this.isVisible());
      nbtCompound.putInt("Value", this.value);
      nbtCompound.putInt("Max", this.maxValue);
      nbtCompound.putString("Color", this.getColor().getName());
      nbtCompound.putString("Overlay", this.getStyle().getName());
      nbtCompound.putBoolean("DarkenScreen", this.shouldDarkenSky());
      nbtCompound.putBoolean("PlayBossMusic", this.hasDragonMusic());
      nbtCompound.putBoolean("CreateWorldFog", this.shouldThickenFog());
      NbtList nbtList = new NbtList();
      Iterator var3 = this.playerUuids.iterator();

      while(var3.hasNext()) {
         UUID uUID = (UUID)var3.next();
         nbtList.add(NbtHelper.fromUuid(uUID));
      }

      nbtCompound.put("Players", nbtList);
      return nbtCompound;
   }

   public static CommandBossBar fromNbt(NbtCompound nbt, Identifier id) {
      CommandBossBar commandBossBar = new CommandBossBar(id, Text.Serializer.fromJson(nbt.getString("Name")));
      commandBossBar.setVisible(nbt.getBoolean("Visible"));
      commandBossBar.setValue(nbt.getInt("Value"));
      commandBossBar.setMaxValue(nbt.getInt("Max"));
      commandBossBar.setColor(BossBar.Color.byName(nbt.getString("Color")));
      commandBossBar.setStyle(BossBar.Style.byName(nbt.getString("Overlay")));
      commandBossBar.setDarkenSky(nbt.getBoolean("DarkenScreen"));
      commandBossBar.setDragonMusic(nbt.getBoolean("PlayBossMusic"));
      commandBossBar.setThickenFog(nbt.getBoolean("CreateWorldFog"));
      NbtList nbtList = nbt.getList("Players", 11);

      for(int i = 0; i < nbtList.size(); ++i) {
         commandBossBar.addPlayer(NbtHelper.toUuid(nbtList.get(i)));
      }

      return commandBossBar;
   }

   public void onPlayerConnect(ServerPlayerEntity player) {
      if (this.playerUuids.contains(player.getUuid())) {
         this.addPlayer(player);
      }

   }

   public void onPlayerDisconnect(ServerPlayerEntity player) {
      super.removePlayer(player);
   }
}
