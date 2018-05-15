package mltiply.sharepolicies;

import mltiply.apps.Job;
import mltiply.simulator.Simulator;
import mltiply.utils.JobLossFunctionBySlopeComparator;

import java.util.Comparator;
import java.util.PriorityQueue;

public class SlaqSharePolicy extends SharePolicy {
  public SlaqSharePolicy(Simulator simulator) {
    super(simulator);
  }

  @Override
  public void computeResShare() {
    int clusterTotCapacity = simulator.cluster.getClusterMaxResAlloc();
    Comparator<Job> jobLossBySlopeComparator = new JobLossFunctionBySlopeComparator();
    PriorityQueue<Job> maxJobLossSlopeFirst = new PriorityQueue<Job>(simulator.runningJobs.size(),
        jobLossBySlopeComparator);
    for (Job job: simulator.runningJobs) {
      maxJobLossSlopeFirst.add(job);
      job.resQuota = 0;
    }
    Job job = maxJobLossSlopeFirst.poll();
    job.resQuota = clusterTotCapacity;
  }
}