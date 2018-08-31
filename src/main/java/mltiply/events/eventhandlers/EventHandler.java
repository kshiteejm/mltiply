package mltiply.events.eventhandlers;

import mltiply.events.simulator.Simulator;

public interface EventHandler<E> {
  public void handle(Simulator s, E eventObject, double time);
}
