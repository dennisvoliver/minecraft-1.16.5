package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Represents an NBT 32-bit floating-point number.
 */
public class NbtFloat extends AbstractNbtNumber {
   public static final NbtFloat ZERO = new NbtFloat(0.0F);
   public static final NbtType<NbtFloat> TYPE = new NbtType<NbtFloat>() {
      public NbtFloat read(DataInput dataInput, int i, NbtTagSizeTracker nbtTagSizeTracker) throws IOException {
         nbtTagSizeTracker.add(96L);
         return NbtFloat.of(dataInput.readFloat());
      }

      public String getCrashReportName() {
         return "FLOAT";
      }

      public String getCommandFeedbackName() {
         return "TAG_Float";
      }

      public boolean isImmutable() {
         return true;
      }
   };
   private final float value;

   private NbtFloat(float value) {
      this.value = value;
   }

   public static NbtFloat of(float value) {
      return value == 0.0F ? ZERO : new NbtFloat(value);
   }

   public void write(DataOutput output) throws IOException {
      output.writeFloat(this.value);
   }

   public byte getType() {
      return 5;
   }

   public NbtType<NbtFloat> getNbtType() {
      return TYPE;
   }

   public String toString() {
      return this.value + "f";
   }

   public NbtFloat copy() {
      return this;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return o instanceof NbtFloat && this.value == ((NbtFloat)o).value;
      }
   }

   public int hashCode() {
      return Float.floatToIntBits(this.value);
   }

   public Text toText(String indent, int depth) {
      Text text = (new LiteralText("f")).formatted(RED);
      return (new LiteralText(String.valueOf(this.value))).append(text).formatted(GOLD);
   }

   public long longValue() {
      return (long)this.value;
   }

   public int intValue() {
      return MathHelper.floor(this.value);
   }

   public short shortValue() {
      return (short)(MathHelper.floor(this.value) & '\uffff');
   }

   public byte byteValue() {
      return (byte)(MathHelper.floor(this.value) & 255);
   }

   public double doubleValue() {
      return (double)this.value;
   }

   public float floatValue() {
      return this.value;
   }

   public Number numberValue() {
      return this.value;
   }
}
