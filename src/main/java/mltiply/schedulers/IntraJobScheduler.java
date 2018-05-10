package mltiply.schedulers;

import mltiply.apps.Job;
import mltiply.schedpolicies.RandomSchedPolicy;
import mltiply.schedpolicies.SchedPolicy;
import mltiply.simulator.Simulator;

public class IntraJobScheduler {

  public SchedPolicy resSchedPolicy;
  Simulator simulator;

  public IntraJobScheduler(Simulator simulator) {
    this.simulator = simulator;
    switch (Simulator.INTRA_JOB_POLICY) {
      case Random:
        resSchedPolicy = new RandomSchedPolicy(simulator);
        break;
      default:
        System.err.println("Unknown sharing policy");
    }
  }

  public void schedule(Job job) {
    // while tasks can be assigned in my resource
    // share quanta, on any machine, keep assigning
    // otherwise return
    resSchedPolicy.schedule(job);
  }
}