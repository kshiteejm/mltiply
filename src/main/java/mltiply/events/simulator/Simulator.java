package mltiply.events.simulator;

import mltiply.events.datastructures.Cluster;
import mltiply.events.datastructures.Event;
import mltiply.events.datastructures.EventQueue;
import mltiply.events.datastructures.Job;
import mltiply.events.schedulers.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Simulator {

  private static Logger LOG = Logger.getLogger(Simulator.class.getName());
  public static int ROUND_PLACES = 2;
  public static int AVG_LOAD = 10;
  public static double FAIR_KNOB = 1.0;

  public int NUM_MACHINES, NUM_DIMENSIONS;
  public double MACHINE_MAX_RESOURCE;

  public Cluster cluster;
  public List<Job> runnableJobs;
  public Map<Integer, Job> runningJobs;
  public List<Job> completedJobs;

  public InterJobScheduler interJobScheduler;
  public IntraJobScheduler intraJobScheduler;

  public EventQueue events = new EventQueue();

  public Simulator() {
    cluster = new Cluster(NUM_MACHINES, MACHINE_MAX_RESOURCE, NUM_DIMENSIONS);
    runnableJobs = new LinkedList<Job>();
    runningJobs = new HashMap<Integer, Job>();
    completedJobs = new LinkedList<Job>();
    interJobScheduler = new MltiplyShareScheduler();
    intraJobScheduler = new TetrisTaskScheduler();
  }

  public void simulate() {
    while (!events.isEmpty()) {
      Event event = events.poll();
      event.handle(this);
    }
  }
}
