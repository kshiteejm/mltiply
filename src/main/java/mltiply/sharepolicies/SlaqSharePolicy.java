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

    int clusterTotCapacity = simulator.cluster.getClusterMaxResAlloc();
    Comparator<Job> jobLossBySlopeComparator = new JobLossFunctionBySlopeComparator();
    PriorityQueue<Job> maxJobLossSlopeFirst = new PriorityQueue<Job>(numJobsRunning,
        jobLossBySlopeComparator);
    for (Job job: simulator.runningJobs) {
      maxJobLossSlopeFirst.add(job);
      job.resQuota = 0;
    }
    Job job = maxJobLossSlopeFirst.poll();
    job.resQuota = clusterTotCapacity;
  }
}