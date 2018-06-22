package mltiply.sharepolicies;

import mltiply.apps.Job;
import mltiply.simulator.Simulator;
import mltiply.utils.JobLossFunctionBySlopeComparator;
import mltiply.utils.JobResourceShareComparator;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FairSharePolicy extends SharePolicy {

  private static Logger LOG = Logger.getLogger(FairSharePolicy.class.getName());

  public FairSharePolicy(Simulator simulator) {
    super(simulator);
  }

  @Override
  public void computeResShare() {
    /* old fair share policy
    int numJobsRunning = simulator.runningJobs.size();
    if (numJobsRunning == 0)
      return;

    int clusterTotCapacity = simulator.cluster.getClusterMaxResAlloc();
    int quotaResShare = clusterTotCapacity/numJobsRunning;
    for (Job job: simulator.runningJobs) {
      job.resQuota = quotaResShare > 10 ? 10 : quotaResShare;
      LOG.log(Level.FINE, "Job " + job.jobId + " - Quota " + job.resQuota);
    }
    */

    int numJobsRunning = simulator.runningJobs.size();
    if (numJobsRunning == 0)
      return;

    int clusterTotalCapacity = simulator.cluster.getClusterMaxResAlloc();
    Comparator<Job> jobResourceShareComparator = new JobResourceShareComparator();
    PriorityQueue<Job> minJobResourceShareFirst = new PriorityQueue<Job>(numJobsRunning,
        jobResourceShareComparator);
    int clusterReservedCapacity = 0;
    for (Job job: simulator.runningJobs) {
      minJobResourceShareFirst.add(job);
      clusterReservedCapacity += job.resQuota;
    }
    int clusterAvailableCapacity = clusterTotalCapacity - clusterReservedCapacity;
    while (clusterAvailableCapacity > 0 && !minJobResourceShareFirst.isEmpty()) {
      Job job = minJobResourceShareFirst.poll();
      if (job == null)
        continue;
      int allocatedCapacity = job.resQuota >= 10 ? 0 : 1;
      // int allocatedCapacity = clusterAvailableCapacity > 10 - job.resQuota ? 10 - job.resQuota : clusterAvailableCapacity;
      job.resQuota += allocatedCapacity;
      clusterAvailableCapacity -= allocatedCapacity;
      if (allocatedCapacity > 0)
        minJobResourceShareFirst.add(job);
    }
  }
}