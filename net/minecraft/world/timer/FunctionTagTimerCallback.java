package net.minecraft.world.timer;

import java.util.Iterator;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class FunctionTagTimerCallback implements TimerCallback<MinecraftServer> {
   private final Identifier name;

   public FunctionTagTimerCallback(Identifier identifier) {
      this.name = identifier;
   }

   public void call(MinecraftServer minecraftServer, Timer<MinecraftServer> timer, long l) {
      CommandFunctionManager commandFunctionManager = minecraftServer.getCommandFunctionManager();
      Tag<CommandFunction> tag = commandFunctionManager.method_29462(this.name);
      Iterator var7 = tag.values().iterator();

      while(var7.hasNext()) {
         CommandFunction commandFunction = (CommandFunction)var7.next();
         commandFunctionManager.execute(commandFunction, commandFunctionManager.getTaggedFunctionSource());
      }

   }

   public static class Serializer extends TimerCallback.Serializer<MinecraftServer, FunctionTagTimerCallback> {
      public Serializer() {
         super(new Identifier("function_tag"), FunctionTagTimerCallback.class);
      }

      public void serialize(NbtCompound nbtCompound, FunctionTagTimerCallback functionTagTimerCallback) {
         nbtCompound.putString("Name", functionTagTimerCallback.name.toString());
      }

      public FunctionTagTimerCallback deserialize(NbtCompound nbtCompound) {
         Identifier identifier = new Identifier(nbtCompound.getString("Name"));
         return new FunctionTagTimerCallback(identifier);
      }
   }
}
