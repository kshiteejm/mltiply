package mltiply.events.eventhandlers;

import mltiply.events.datastructures.Job;
import mltiply.events.datastructures.Task;
import mltiply.events.simulator.Simulator;

public class EndTaskHandler implements EventHandler<Task> {

  public void handle(Simulator s, Task t, double time) {
    Job j = s.runningJobs.get(t.jobId);
    assert(j != null);

    // 1.finish the task
    j.finishTask(t);
    s.cluster.finishTask(t);

    // 2. init next job iteration if iteration is over
    if (j.isIterationOver()) {
      // also check if job is over
      if (!j.initNextIteration()) {
        j.endTime = time;
        s.runningJobs.remove(j.jobId);
        s.completedJobs.add(j);
      }
    }

    // 2. recompute job shares
    s.interJobScheduler.computeShares(s);

    // 3. assign new task to freed cpu core
  }
}
