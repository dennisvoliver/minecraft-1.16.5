package net.minecraft.screen;

/**
 * An integer property that is stored in a {@link ScreenHandler}.
 * 
 * <p>{@code Property} instances are used for tracking integer properties in property delegates
 * and other sources of integer properties, and sending needed content updates to listeners.
 * 
 * @see ScreenHandler#addProperty
 */
public abstract class Property {
   private int oldValue;

   /**
    * Creates a new property that accesses the {@code index} of the {@code delegate}.
    */
   public static Property create(final PropertyDelegate delegate, final int index) {
      return new Property() {
         public int get() {
            return delegate.get(index);
         }

         public void set(int value) {
            delegate.set(index, value);
         }
      };
   }

   /**
    * Creates a new property that accesses the {@code index} of the {@code array}.
    */
   public static Property create(final int[] array, final int index) {
      return new Property() {
         public int get() {
            return array[index];
         }

         public void set(int value) {
            array[index] = value;
         }
      };
   }

   /**
    * Creates a new property that is not attached to any other objects.
    */
   public static Property create() {
      return new Property() {
         private int value;

         public int get() {
            return this.value;
         }

         public void set(int value) {
            this.value = value;
         }
      };
   }

   public abstract int get();

   public abstract void set(int value);

   /**
    * Returns true if the value of this property has changed since the last call to {@code hasChanged()}.
    */
   public boolean hasChanged() {
      int i = this.get();
      boolean bl = i != this.oldValue;
      this.oldValue = i;
      return bl;
   }
}