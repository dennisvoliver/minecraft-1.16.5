package net.minecraft.test;

import java.util.function.Consumer;
import net.minecraft.util.BlockRotation;

public class TestFunction {
   private final String batchId;
   private final String structurePath;
   private final String structureName;
   private final boolean required;
   private final Consumer<TestContext> starter;
   private final int tickLimit;
   private final long duration;
   private final BlockRotation field_25306;

   public void start(TestContext parameter) {
      this.starter.accept(parameter);
   }

   public String getStructurePath() {
      return this.structurePath;
   }

   public String getStructureName() {
      return this.structureName;
   }

   public String toString() {
      return this.structurePath;
   }

   public int getTickLimit() {
      return this.tickLimit;
   }

   public boolean isRequired() {
      return this.required;
   }

   public String getBatchId() {
      return this.batchId;
   }

   public long getDuration() {
      return this.duration;
   }

   public BlockRotation method_29424() {
      return this.field_25306;
   }
}
