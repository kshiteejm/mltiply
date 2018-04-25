package mltiply.schedulers;

import mltiply.sharepolicies.FairSharePolicy;
import mltiply.sharepolicies.MltiplySharePolicy;
import mltiply.sharepolicies.SharePolicy;
import mltiply.sharepolicies.SlaqSharePolicy;
import mltiply.simulator.Simulator;

public class InterJobScheduler {
  public SharePolicy resourceSharePolicy;
  Simulator simulator;

  public InterJobScheduler(Simulator simulator) {
    this.simulator = simulator;
    switch (simulator.INTER_JOB_POLICY) {
      case Fair:
        resourceSharePolicy = new FairSharePolicy(this.simulator);
        break;
      case Slaq:
        resourceSharePolicy = new SlaqSharePolicy(this.simulator);
        break;
      case Mltiply:
        resourceSharePolicy = new MltiplySharePolicy(this.simulator);
        break;
      default:
        System.err.println("Unknown Sharing Policy");
    }
  }

  public void schedule() {
    resourceSharePolicy.computeResShare();
  }
}
