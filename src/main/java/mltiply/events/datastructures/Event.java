package mltiply.events.datastructures;

import mltiply.events.eventhandlers.EventHandler;
import mltiply.events.simulator.Simulator;

public class Event<E> {
  public double time;
  public EventHandler<E> eventHandler;
  public E eventObject;

  public Event(double time, EventHandler<E> eventHandler) {
    this.time = time;
    this.eventHandler = eventHandler;
  }

  public double getTime() {
    return time;
  }

  public EventHandler getEventHandler() {
    return eventHandler;
  }

  public void handle(Simulator simulator) {
    eventHandler.handle(simulator, eventObject, time);
  }
}
