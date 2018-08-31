package mltiply.events.eventhandlers;

import mltiply.events.datastructures.Job;
import mltiply.events.datastructures.Task;
import mltiply.events.simulator.Simulator;

public class EndTaskHandler implements EventHandler<Task> {

  public void handle(Simulator s, Task t, double time) {
    Job j = s.runningJobs.get(t.jobId);
    assert(j != null);
    j.finishTask(t);
    s.interJobScheduler.computeShares(s);

  }
}
