package mltiply.events.schedulers;

import mltiply.events.datastructures.Job;
import mltiply.events.simulator.Simulator;

import java.util.Comparator;
import java.util.PriorityQueue;

public class MltiplyShareScheduler extends InterJobScheduler {

  public void computeShares(Simulator s) {
    PriorityQueue<Job> maxSchedulerScore = new PriorityQueue<Job>(s.runningJobs.size(),
        new Comparator<Job>() {
          public int compare(Job o1, Job o2) {
            return 0;
          }
        });
  }
}