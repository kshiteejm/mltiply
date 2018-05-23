package mltiply.sharepolicies;

import mltiply.apps.Job;
import mltiply.simulator.Simulator;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FairSharePolicy extends SharePolicy {

  private static Logger LOG = Logger.getLogger(FairSharePolicy.class.getName());

  public FairSharePolicy(Simulator simulator) {
    super(simulator);
  }

  @Override
  public void computeResShare() {
    int numJobsRunning = simulator.runningJobs.size();
    if (numJobsRunning == 0)
      return;

    int clusterTotCapacity = simulator.cluster.getClusterMaxResAlloc();
    int quotaResShare = clusterTotCapacity/numJobsRunning;
    for (Job job: simulator.runningJobs) {
      job.resQuota = quotaResShare;
      LOG.log(Level.FINE, "Job " + job.jobId + " - Quota " + job.resQuota);
    }
  }
}