package mltiply.events.simulator;

import mltiply.events.datastructures.Cluster;
import mltiply.events.datastructures.Event;
import mltiply.events.datastructures.EventQueue;

import java.util.logging.Logger;

public class Simulator {

  private static Logger LOG = Logger.getLogger(mltiply.simulator.Simulator.class.getName());

  public int NUM_MACHINES, NUM_DIMENSIONS;
  public double MACHINE_MAX_RESOURCE;

  public Cluster cluster;

  public EventQueue events = new EventQueue();

  public Simulator() {
    cluster = new Cluster(NUM_MACHINES, MACHINE_MAX_RESOURCE, NUM_DIMENSIONS);
  }

  public void simulate() {
    while (!events.isEmpty()) {
      Event event = events.poll();
      event.getEventHandler().handle(this);
    }
  }
}
