package mltiply.sharepolicies;

import mltiply.apps.Job;
import mltiply.simulator.Simulator;
import mltiply.utils.JobLossFunctionBySlopeComparator;
import mltiply.utils.JobResourceShareComparator;

import java.util.Comparator;
import java.util.PriorityQueue;

public class MltiplySharePolicy extends SharePolicy {
  public MltiplySharePolicy(Simulator simulator) {
    super(simulator);
  }

  @Override
  public void computeResShare() {
    int fairness_coefficient = 20;
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
    int fractionJobs = (numJobsRunning * fairness_coefficient) / 100;
    fractionJobs = 1 > fractionJobs ? 1 : fractionJobs;
    Comparator<Job> jobLossBySlopeComparator = new JobLossFunctionBySlopeComparator();
    PriorityQueue<Job> maxJobLossSlopeFirst = new PriorityQueue<Job>(fractionJobs,
        jobLossBySlopeComparator);
    while (fractionJobs > 0) {
      Job job = minJobResourceShareFirst.poll();
      maxJobLossSlopeFirst.add(job);
      fractionJobs -= 1;
    }
    int clusterAvailableCapacity = clusterTotalCapacity - clusterReservedCapacity;
    while (clusterAvailableCapacity > 0 && !maxJobLossSlopeFirst.isEmpty()) {
      Job job = maxJobLossSlopeFirst.poll();
      if (job == null)
        continue;
      int allocatedCapacity = job.resQuota >= 10 ? 0 : 1;
      // int allocatedCapacity = clusterAvailableCapacity > 10 - job.resQuota ? 10 - job.resQuota : clusterAvailableCapacity;
      job.resQuota += allocatedCapacity;
      clusterAvailableCapacity -= allocatedCapacity;
      if (allocatedCapacity > 0)
        maxJobLossSlopeFirst.add(job);
    }
  }
}