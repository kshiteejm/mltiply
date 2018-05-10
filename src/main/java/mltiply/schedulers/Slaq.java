package mltiply.schedulers;

public class Slaq {
  // public double schedulingEpoch;
  //
  // public Slaq(double schedulingEpoch) {
  //   this.schedulingEpoch = schedulingEpoch;
  // }
  //
  // public void schedule(Cluster cluster, List<Job> runningJobs) {
  //   int numCores = cluster.getNumCores();
  //   Comparator<Job> jobLossComparator = new JobLossComparator();
  //   PriorityQueue<Job> maxLossJobFirst = new PriorityQueue<Job>(runningJobs.size(), jobLossComparator);
  //
  //   for (Job job: runningJobs) {
  //     job.workers = 1;
  //     numCores -= 1;
  //     maxLossJobFirst.add(job);
  //   }
  //   while (numCores > 0) {
  //     Job job = maxLossJobFirst.poll();
  //     job.workers += 1;
  //     numCores -= 1;
  //     maxLossJobFirst.add(job);
  //   }
  // }
}
