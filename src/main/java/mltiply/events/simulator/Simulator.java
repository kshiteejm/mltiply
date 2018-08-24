package mltiply.events.simulator;

import mltiply.events.datastructures.Cluster;
import mltiply.events.datastructures.Event;
import mltiply.events.datastructures.EventQueue;
import mltiply.events.datastructures.Job;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Simulator {

  private static Logger LOG = Logger.getLogger(mltiply.simulator.Simulator.class.getName());

  public int NUM_MACHINES, NUM_DIMENSIONS;
  public double MACHINE_MAX_RESOURCE;

  public Cluster cluster;
  List<Job> runningJobs;

  public EventQueue events = new EventQueue();

  public Simulator() {
    cluster = new Cluster(NUM_MACHINES, MACHINE_MAX_RESOURCE, NUM_DIMENSIONS);
    runningJobs = new LinkedList<Job>();
  }

  public void simulate() {
    while (!events.isEmpty()) {
      Event event = events.poll();
      event.getEventHandler().handle(this);
    }
  }
}
