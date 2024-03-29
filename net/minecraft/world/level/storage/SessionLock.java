package net.minecraft.world.level.storage;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class SessionLock implements AutoCloseable {
   private final FileChannel channel;
   private final FileLock lock;
   private static final ByteBuffer field_25353;

   public static SessionLock create(Path path) throws IOException {
      Path path2 = path.resolve("session.lock");
      if (!Files.isDirectory(path, new LinkOption[0])) {
         Files.createDirectories(path);
      }

      FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

      try {
         fileChannel.write(field_25353.duplicate());
         fileChannel.force(true);
         FileLock fileLock = fileChannel.tryLock();
         if (fileLock == null) {
            throw SessionLock.AlreadyLockedException.create(path2);
         } else {
            return new SessionLock(fileChannel, fileLock);
         }
      } catch (IOException var6) {
         try {
            fileChannel.close();
         } catch (IOException var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }
   }

   private SessionLock(FileChannel channel, FileLock lock) {
      this.channel = channel;
      this.lock = lock;
   }

   public void close() throws IOException {
      try {
         if (this.lock.isValid()) {
            this.lock.release();
         }
      } finally {
         if (this.channel.isOpen()) {
            this.channel.close();
         }

      }

   }

   public boolean isValid() {
      return this.lock.isValid();
   }

   @Environment(EnvType.CLIENT)
   public static boolean isLocked(Path path) throws IOException {
      Path path2 = path.resolve("session.lock");

      try {
         FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.WRITE);
         Throwable var3 = null;

         Throwable var6;
         try {
            FileLock fileLock = fileChannel.tryLock();
            Throwable var5 = null;

            try {
               var6 = fileLock == null;
            } catch (Throwable var33) {
               var6 = var33;
               var5 = var33;
               throw var33;
            } finally {
               if (fileLock != null) {
                  if (var5 != null) {
                     try {
                        fileLock.close();
                     } catch (Throwable var32) {
                        var5.addSuppressed(var32);
                     }
                  } else {
                     fileLock.close();
                  }
               }

            }
         } catch (Throwable var35) {
            var3 = var35;
            throw var35;
         } finally {
            if (fileChannel != null) {
               if (var3 != null) {
                  try {
                     fileChannel.close();
                  } catch (Throwable var31) {
                     var3.addSuppressed(var31);
                  }
               } else {
                  fileChannel.close();
               }
            }

         }

         return (boolean)var6;
      } catch (AccessDeniedException var37) {
         return true;
      } catch (NoSuchFileException var38) {
         return false;
      }
   }

   static {
      byte[] bs = "☃".getBytes(Charsets.UTF_8);
      field_25353 = ByteBuffer.allocateDirect(bs.length);
      field_25353.put(bs);
      field_25353.flip();
   }

   public static class AlreadyLockedException extends IOException {
      private AlreadyLockedException(Path path, String message) {
         super(path.toAbsolutePath() + ": " + message);
      }

      public static SessionLock.AlreadyLockedException create(Path path) {
         return new SessionLock.AlreadyLockedException(path, "already locked (possibly by other Minecraft instance?)");
      }
   }
}
