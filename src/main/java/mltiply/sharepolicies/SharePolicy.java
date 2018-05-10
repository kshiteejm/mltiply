package mltiply.sharepolicies;

import mltiply.simulator.Simulator;

public abstract class SharePolicy {

  public Simulator simulator;

  public SharePolicy(Simulator _simulator) {
    simulator = _simulator;
  }

  // recompute the resource share allocated for every job
  public void computeResShare() {

  }

}
