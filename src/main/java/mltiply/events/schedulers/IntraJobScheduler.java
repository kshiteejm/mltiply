package mltiply.events.schedulers;

import mltiply.events.datastructures.Job;
import mltiply.events.simulator.Simulator;

public interface IntraJobScheduler {

  public boolean assignTask(Simulator s, Job j, double time);
}
