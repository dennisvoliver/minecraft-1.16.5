package net.minecraft.network.packet.s2c.play;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import org.jetbrains.annotations.Nullable;

public class NbtQueryResponseS2CPacket implements Packet<ClientPlayPacketListener> {
   private int transactionId;
   @Nullable
   private NbtCompound nbt;

   public NbtQueryResponseS2CPacket() {
   }

   public NbtQueryResponseS2CPacket(int transactionId, @Nullable NbtCompound nbt) {
      this.transactionId = transactionId;
      this.nbt = nbt;
   }

   public void read(PacketByteBuf buf) throws IOException {
      this.transactionId = buf.readVarInt();
      this.nbt = buf.readNbt();
   }

   public void write(PacketByteBuf buf) throws IOException {
      buf.writeVarInt(this.transactionId);
      buf.writeNbt(this.nbt);
   }

   public void apply(ClientPlayPacketListener clientPlayPacketListener) {
      clientPlayPacketListener.onTagQuery(this);
   }

   @Environment(EnvType.CLIENT)
   public int getTransactionId() {
      return this.transactionId;
   }

   @Nullable
   @Environment(EnvType.CLIENT)
   public NbtCompound getNbt() {
      return this.nbt;
   }

   public boolean isWritingErrorSkippable() {
      return true;
   }
}
