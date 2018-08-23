package mltiply.events.datastructures;

import mltiply.events.eventhandlers.EventHandler;

public class Event {
  public double time;
  public EventHandler eventHandler;

  public Event(double time, EventHandler eventHandler) {
    this.time = time;
    this.eventHandler = eventHandler;
  }

  public double getTime() {
    return time;
  }

  public EventHandler getEventHandler() {
    return eventHandler;
  }
}
