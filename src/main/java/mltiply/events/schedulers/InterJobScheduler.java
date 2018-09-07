package mltiply.events.schedulers;

import mltiply.events.datastructures.Job;
import mltiply.events.datastructures.Resource;
import mltiply.events.simulator.Simulator;

import java.util.Comparator;
import java.util.PriorityQueue;

public abstract class InterJobScheduler {

  abstract public void computeShares(Simulator s);

  public void allocateResource(Simulator s) {
    PriorityQueue<Job> greedyJobQueue = new PriorityQueue<Job>(s.runningJobs.size(), new Comparator<Job>() {
      public int compare(Job o1, Job o2) {
        return -Resource.compare(o1.deficit(), o2.deficit());
      }
    });

    for (Job j: s.runningJobs.values()) {
      greedyJobQueue.add(j);
    }

    
  }
}
