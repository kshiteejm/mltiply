package mltiply.sharepolicies;

import mltiply.apps.Job;
import mltiply.simulator.Simulator;

public class FairSharePolicy extends SharePolicy {

  public FairSharePolicy(Simulator simulator) {
    super(simulator);

  }

  @Override
  public void computeResShare() {
    int clusterTotCapacity = simulator.cluster.getClusterMaxResAlloc();
    int numJobsRunning = simulator.runningJobs.size();
    if (numJobsRunning == 0)
      return;
    int quotaResShare = clusterTotCapacity/numJobsRunning;
    for (Job job: simulator.runningJobs) {
      job.resQuota = quotaResShare;
    }
  }
}