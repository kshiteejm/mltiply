package mltiply.sharepolicies;

import mltiply.simulator.Simulator;

public abstract class SharePolicy {

  public Simulator simulator;

  public SharePolicy(Simulator simulator) {
    this.simulator = simulator;
  }

  // recompute the resource share allocated for every job
  public void computeResShare() {

  }

}
