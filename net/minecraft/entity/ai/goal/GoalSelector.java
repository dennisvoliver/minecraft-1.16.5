package net.minecraft.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages a set of goals, which are competing for certain controls on the mob.
 * Multiple goals can run at the same time, so long as they are all using different controls.
 * 
 * <p>A running goal will always be replaced with a goal with a <i>lower</i> priority, if
 * such a goal exists, it's competing for the same control and its
 * {@link Goal#canStart() canStart()} method returns true. (Note that some goals randomize
 * this method.)
 * 
 * <p>If two goals have the same priority and are competing for the same control, then one
 * goal cannot replace the other if it's running. The goal selector tries to run goals in the order
 * they were added.
 */
public class GoalSelector {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final PrioritizedGoal REPLACEABLE_GOAL = new PrioritizedGoal(Integer.MAX_VALUE, new Goal() {
      public boolean canStart() {
         return false;
      }
   }) {
      public boolean isRunning() {
         return false;
      }
   };
   private final Map<Goal.Control, PrioritizedGoal> goalsByControl = new EnumMap(Goal.Control.class);
   private final Set<PrioritizedGoal> goals = Sets.newLinkedHashSet();
   private final Supplier<Profiler> profiler;
   private final EnumSet<Goal.Control> disabledControls = EnumSet.noneOf(Goal.Control.class);
   private int timeInterval = 3;

   public GoalSelector(Supplier<Profiler> profiler) {
      this.profiler = profiler;
   }

   /**
    * Adds a goal with a certain priority. Goals with <i>lower</i> priorities will replace running goals
    * with a higher priority.
    */
   public void add(int priority, Goal goal) {
      this.goals.add(new PrioritizedGoal(priority, goal));
   }

   public void remove(Goal goal) {
      this.goals.stream().filter((prioritizedGoal) -> {
         return prioritizedGoal.getGoal() == goal;
      }).filter(PrioritizedGoal::isRunning).forEach(PrioritizedGoal::stop);
      this.goals.removeIf((prioritizedGoal) -> {
         return prioritizedGoal.getGoal() == goal;
      });
   }

   public void tick() {
      Profiler profiler = (Profiler)this.profiler.get();
      profiler.push("goalCleanup");
      this.getRunningGoals().filter((prioritizedGoal) -> {
         boolean var2;
         if (prioritizedGoal.isRunning()) {
            Stream var10000 = prioritizedGoal.getControls().stream();
            EnumSet var10001 = this.disabledControls;
            var10001.getClass();
            if (!var10000.anyMatch(var10001::contains) && prioritizedGoal.shouldContinue()) {
               var2 = false;
               return var2;
            }
         }

         var2 = true;
         return var2;
      }).forEach(Goal::stop);
      this.goalsByControl.forEach((control, prioritizedGoal) -> {
         if (!prioritizedGoal.isRunning()) {
            this.goalsByControl.remove(control);
         }

      });
      profiler.pop();
      profiler.push("goalUpdate");
      this.goals.stream().filter((prioritizedGoal) -> {
         return !prioritizedGoal.isRunning();
      }).filter((prioritizedGoal) -> {
         Stream var10000 = prioritizedGoal.getControls().stream();
         EnumSet var10001 = this.disabledControls;
         var10001.getClass();
         return var10000.noneMatch(var10001::contains);
      }).filter((prioritizedGoal) -> {
         return prioritizedGoal.getControls().stream().allMatch((control) -> {
            return ((PrioritizedGoal)this.goalsByControl.getOrDefault(control, REPLACEABLE_GOAL)).canBeReplacedBy(prioritizedGoal);
         });
      }).filter(PrioritizedGoal::canStart).forEach((prioritizedGoal) -> {
         prioritizedGoal.getControls().forEach((control) -> {
            PrioritizedGoal prioritizedGoal2 = (PrioritizedGoal)this.goalsByControl.getOrDefault(control, REPLACEABLE_GOAL);
            prioritizedGoal2.stop();
            this.goalsByControl.put(control, prioritizedGoal);
         });
         prioritizedGoal.start();
      });
      profiler.pop();
      profiler.push("goalTick");
      this.getRunningGoals().forEach(PrioritizedGoal::tick);
      profiler.pop();
   }

   public Stream<PrioritizedGoal> getRunningGoals() {
      return this.goals.stream().filter(PrioritizedGoal::isRunning);
   }

   public void disableControl(Goal.Control control) {
      this.disabledControls.add(control);
   }

   public void enableControl(Goal.Control control) {
      this.disabledControls.remove(control);
   }

   public void setControlEnabled(Goal.Control control, boolean enabled) {
      if (enabled) {
         this.enableControl(control);
      } else {
         this.disableControl(control);
      }

   }
}
