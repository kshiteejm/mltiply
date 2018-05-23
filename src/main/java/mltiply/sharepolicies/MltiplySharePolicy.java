package mltiply.sharepolicies;

import mltiply.apps.Job;
import mltiply.simulator.Simulator;
import mltiply.utils.JobResourceShareComparator;

import java.util.Comparator;
import java.util.PriorityQueue;

public class MltiplySharePolicy extends SharePolicy {
  public MltiplySharePolicy(Simulator simulator) {
    super(simulator);
  }

  @Override
  public void computeResShare() {
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
    while (clusterAvailableCapacity >= 0) {

    }
  }
}