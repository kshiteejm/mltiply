package mltiply.sharepolicies;

import mltiply.apps.Job;
import mltiply.simulator.Simulator;
import mltiply.utils.JobLossFunctionBySlopeComparator;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlaqSharePolicy extends SharePolicy {

  private static Logger LOG = Logger.getLogger(SlaqSharePolicy.class.getName());

  public SlaqSharePolicy(Simulator simulator) {
    super(simulator);
  }

  @Override
  public void computeResShare() {
    int numJobsRunning = simulator.runningJobs.size();
    if (numJobsRunning == 0)
      return;

    int clusterTotalCapacity = simulator.cluster.getClusterMaxResAlloc();
    Comparator<Job> jobLossBySlopeComparator = new JobLossFunctionBySlopeComparator();
    PriorityQueue<Job> maxJobLossSlopeFirst = new PriorityQueue<Job>(numJobsRunning,
        jobLossBySlopeComparator);
    int clusterReservedCapacity = 0;
    for (Job job: simulator.runningJobs) {
      maxJobLossSlopeFirst.add(job);
      // job.resQuota = 0;
      clusterReservedCapacity += job.resQuota;
    }
    int clusterAvailableCapacity = clusterTotalCapacity - clusterReservedCapacity;
    // while (clusterTotCapacity > 0 && !maxJobLossSlopeFirst.isEmpty()) {
    //   Job job = maxJobLossSlopeFirst.poll();
    //   if (job == null)
    //     continue;
    //   job.resQuota = clusterTotCapacity > 10 ? 10 : clusterTotCapacity;
    //   clusterTotCapacity -= job.resQuota;
    // }
    while (clusterAvailableCapacity > 0 && !maxJobLossSlopeFirst.isEmpty()) {
      Job job = maxJobLossSlopeFirst.poll();
      if (job == null)
        continue;
      int allocatedCapacity = clusterAvailableCapacity > 10 - job.resQuota ? 10 - job.resQuota : clusterAvailableCapacity;
      job.resQuota += allocatedCapacity;
      clusterAvailableCapacity -= allocatedCapacity;
    }
  }
}