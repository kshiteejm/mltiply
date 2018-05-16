package mltiply.schedulers;

import mltiply.apps.Job;
import mltiply.schedpolicies.RandomSchedPolicy;
import mltiply.schedpolicies.SchedPolicy;
import mltiply.simulator.Simulator;

import java.util.logging.Level;
import java.util.logging.Logger;

public class IntraJobScheduler {

  private static Logger LOG = Logger.getLogger(IntraJobScheduler.class.getName());

  public SchedPolicy resSchedPolicy;
  Simulator simulator;

  public IntraJobScheduler(Simulator simulator) {
    this.simulator = simulator;
    switch (simulator.INTRA_JOB_POLICY) {
      case Random:
        resSchedPolicy = new RandomSchedPolicy(simulator);
        break;
      default:
        System.err.println("Unknown sharing policy");
    }
  }

  public void schedule(Job job) {
    if (job.isIterationOver() && job.resQuota > 0) {
      job.initNextIteration();
      LOG.log(Level.FINE, "Time: " + simulator.CURRENT_TIME +
          " Init Next Iteration for Job " + job.jobId + " Iteration " + job.currIterationNum);
    }
    // while tasks can be assigned in my resource
    // share quanta, on any machine, keep assigning
    // otherwise return
    resSchedPolicy.schedule(job);
  }
}