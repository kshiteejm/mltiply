package mltiply.events.schedulers;

import mltiply.events.datastructures.*;
import mltiply.events.eventhandlers.EndTaskHandler;
import mltiply.events.simulator.Simulator;

public class TetrisTaskScheduler implements IntraJobScheduler {

  public boolean assignTask(Simulator s, Job job, double time) {
    boolean ret = false;

    Task bestFitTask = null;
    Machine bestFitMachine = null;
    double maxDotProduct = 0.0;

    // find which task from the job is to be run - highest dot product on which machine
    for (Task task: job.runnableTasks) {
      for (Machine m: s.cluster.machines) {
        Resource deficit = m.deficit();
        if (task.demand.isLessThan(deficit)) {
          double dotProduct = task.demand.dot(deficit);
          if (maxDotProduct < dotProduct) {
            bestFitTask = task;
            bestFitMachine = m;
            maxDotProduct = dotProduct;
          }
        }
      }
    }

    // if some task fits then update machine, job, and simulator event queue state
    if (bestFitTask != null) {
      s.cluster.assignTask(bestFitTask, bestFitMachine);
      job.startTask(bestFitTask);
      Event<Task> endTaskEvent = new Event<Task>(time, new EndTaskHandler());
      s.events.add(endTaskEvent);
      ret = true;
    }

    return ret;
  }
}
