package mltiply.events.eventhandlers;

import mltiply.events.datastructures.Job;
import mltiply.events.simulator.Simulator;

public class StartJobHandler implements EventHandler<Job> {
  /*
   * initialize job - tasks for first iteration
   * change simulator state
   */
  public void handle(Simulator s, Job j, double time) {
    // 1 - Simulator State
    // a. running jobs
    s.runningJobs.put(j.jobId, j);

    // 2 - Job State
    // a. start time
    j.startTime = time;

    // b. nextMaxAlloc for running jobs
    s.interJobScheduler.computeShares(s);

    // c. init job iteration - before actual assignment of resources? - review -
    j.initNextIteration();

    // d.
  }
}