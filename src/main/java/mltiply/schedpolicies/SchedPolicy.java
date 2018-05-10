package mltiply.schedpolicies;

import mltiply.apps.Job;
import mltiply.simulator.Simulator;

public abstract class SchedPolicy {

  public Simulator simulator;

  public SchedPolicy(Simulator _simulator) {
    simulator = _simulator;
  }

  public abstract void schedule(Job job);
}

