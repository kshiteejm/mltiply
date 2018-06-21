package mltiply.schedulers;

import mltiply.sharepolicies.*;
import mltiply.simulator.Simulator;

public class InterJobScheduler {

  public SharePolicy resourceSharePolicy;
  Simulator simulator;

  public InterJobScheduler(Simulator simulator) {
    this.simulator = simulator;
    switch (simulator.INTER_JOB_POLICY) {
      case Fair:
        resourceSharePolicy = new FairSharePolicy(simulator);
        break;
      case Slaq:
        resourceSharePolicy = new SlaqSharePolicy(simulator);
        break;
      case Mltiply:
        resourceSharePolicy = new MltiplySharePolicy(simulator);
        break;
      case TimeFair:
        resourceSharePolicy = new TimeFairSharePolicy(simulator);
        break;
      default:
        System.err.println("Unknown Sharing Policy");
    }
  }

  public void schedule() {
    resourceSharePolicy.computeResShare();
  }
}
