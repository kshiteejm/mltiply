package mltiply.sharepolicies;

import mltiply.apps.Job;
import mltiply.simulator.Simulator;
import mltiply.utils.JobResourceShareComparator;
import mltiply.utils.JobTimeFairShareComparator;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeFairSharePolicy extends SharePolicy {

  private static Logger LOG = Logger.getLogger(TimeFairSharePolicy.class.getName());

  public TimeFairSharePolicy(Simulator simulator) {
    super(simulator);
  }

  @Override
  public void computeResShare() {
    int numJobsRunning = simulator.runningJobs.size();
    if (numJobsRunning == 0)
      return;

    int clusterTotalCapacity = simulator.cluster.getClusterMaxResAlloc();
    Comparator<Job> jobTimeFairShareComparator = new JobTimeFairShareComparator(simulator.CURRENT_TIME,
        clusterTotalCapacity*1.0/simulator.NUM_JOBS);
    PriorityQueue<Job> minJobTimeFairShareFirst = new PriorityQueue<Job>(numJobsRunning,
        jobTimeFairShareComparator);
    int clusterReservedCapacity = 0;
    for (Job job: simulator.runningJobs) {
      minJobTimeFairShareFirst.add(job);
      clusterReservedCapacity += job.resQuota;
    }
    int clusterAvailableCapacity = clusterTotalCapacity - clusterReservedCapacity;
    while (clusterAvailableCapacity > 0 && !minJobTimeFairShareFirst.isEmpty()) {
      Job job = minJobTimeFairShareFirst.poll();
      if (job == null)
        continue;
      int allocatedCapacity = job.resQuota >= 10 ? 0 : 1;
      // int allocatedCapacity = clusterAvailableCapacity > 10 - job.resQuota ? 10 - job.resQuota : clusterAvailableCapacity;
      job.resQuota += allocatedCapacity;
      clusterAvailableCapacity -= allocatedCapacity;
      if (allocatedCapacity > 0)
        minJobTimeFairShareFirst.add(job);
    }
  }
}