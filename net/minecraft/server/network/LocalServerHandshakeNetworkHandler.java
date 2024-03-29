package net.minecraft.server.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ServerHandshakePacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

/**
 * A server handshake network handler that exclusively handles local
 * connections.
 * 
 * <p>A local connection is one between a Minecraft client and the
 * Integrated Server it is running.
 * 
 * @see net.minecraft.server.ServerNetworkIo#bindLocal()
 */
@Environment(EnvType.CLIENT)
public class LocalServerHandshakeNetworkHandler implements ServerHandshakePacketListener {
   private final MinecraftServer server;
   private final ClientConnection connection;

   public LocalServerHandshakeNetworkHandler(MinecraftServer server, ClientConnection connection) {
      this.server = server;
      this.connection = connection;
   }

   public void onHandshake(HandshakeC2SPacket packet) {
      this.connection.setState(packet.getIntendedState());
      this.connection.setPacketListener(new ServerLoginNetworkHandler(this.server, this.connection));
   }

   public void onDisconnected(Text reason) {
   }

   public ClientConnection getConnection() {
      return this.connection;
   }
}
