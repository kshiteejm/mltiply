package mltiply.events.schedulers;

import mltiply.events.datastructures.Job;
import mltiply.events.datastructures.Resource;
import mltiply.events.simulator.Simulator;

import java.util.Comparator;
import java.util.PriorityQueue;

public class MltiplyShareScheduler extends InterJobScheduler {

  public void computeShares(Simulator s, final double time) { // does use of final sort out issues?
    PriorityQueue<Job> maxSchedulerScore = new PriorityQueue<Job>(s.runningJobs.size(),
        new Comparator<Job>() {
          public int compare(Job o1, Job o2) {
            return -Double.compare(o1.mltiplyScore(s, time), o2.mltiplyScore(s, time));
          }
        }
    );

    Resource maxAlloc = s.cluster.maxAlloc();

    // init nextMaxAllocations of jobs to one each to avoid divide-by-zero errors
    for (Job job: s.runningJobs.values()) {
      job.nextMaxAlloc = new Resource(job.quanta);
      maxAlloc = maxAlloc.minus(job.quanta);
      maxSchedulerScore.add(job);
    }

    // allocate resources in order of priority - higher mltiplyScore implies higher priority
    while (maxAlloc.isGreaterThanZero() && !maxSchedulerScore.isEmpty()) {
      Job job = maxSchedulerScore.poll();
      if (job.quanta.isLessThan(maxAlloc)) {
        job.nextMaxAlloc = job.nextMaxAlloc.add(job.quanta);
        maxAlloc = maxAlloc.minus(job.quanta);
        maxSchedulerScore.add(job);
      }
    }
  }
}