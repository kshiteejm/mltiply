package mltiply.events.schedulers;

import mltiply.events.datastructures.Job;
import mltiply.events.datastructures.Resource;
import mltiply.events.simulator.Simulator;

import java.util.Comparator;
import java.util.PriorityQueue;

public abstract class InterJobScheduler {

  abstract public void computeShares(Simulator s);

  public void allocateResource(Simulator s, double time) {
    PriorityQueue<Job> greedyJobQueue = new PriorityQueue<Job>(s.runningJobs.size(), new Comparator<Job>() {
      public int compare(Job o1, Job o2) {
        return -Resource.compare(o1.deficit(), o2.deficit());
      }
    });

    for (Job job: s.runningJobs.values()) {
      greedyJobQueue.add(job);
    }

    // find job most distant from allocated share and assign resource using intra-job scheduler
    while (s.cluster.hasDeficit() && !greedyJobQueue.isEmpty()) {
      Job job = greedyJobQueue.poll();
      boolean isAssigned = s.intraJobScheduler.assignTask(s, job, time);
      if (isAssigned)
        greedyJobQueue.add(job);
    }

    greedyJobQueue.clear();
  }
}
