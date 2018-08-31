package mltiply.events.schedulers;

import mltiply.events.simulator.Simulator;

public interface InterJobScheduler {

  public void computeShares(Simulator s);
}
