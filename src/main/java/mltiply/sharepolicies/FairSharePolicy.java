package mltiply.sharepolicies;

import mltiply.simulator.Simulator;

public class FairSharePolicy extends SharePolicy {
  double clusterTotCapacity;

  public FairSharePolicy(Simulator simulator) {
    super(simulator);
    clusterTotCapacity = simulator.cluster.getNumCores();
  }

  @Override
  public void computeResShare() {

  }
}