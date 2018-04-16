package mltiply.schedulers;

import mltiply.apps.Job;
import mltiply.resources.Cluster;
import mltiply.utils.JobLossComparator;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Slaq {
  public double schedulingEpoch;

  public Slaq(double schedulingEpoch) {
    this.schedulingEpoch = schedulingEpoch;
  }

  public void schedule(Cluster cluster, List<Job> runningJobs) {
    int numCores = cluster.getNumCores();
    Comparator<Job> jobLossComparator = new JobLossComparator();
    PriorityQueue<Job> maxLossJobFirst = new PriorityQueue<Job>(runningJobs.size(), jobLossComparator);

    for (Job job: runningJobs) {
      job.workers = 1;
      numCores -= 1;
      maxLossJobFirst.add(job);
    }
    while (numCores > 0) {
      Job job = maxLossJobFirst.poll();
      job.workers += 1;
      numCores -= 1;
      maxLossJobFirst.add(job);
    }
  }
}
