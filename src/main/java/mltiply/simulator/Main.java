package mltiply.simulator;

import mltiply.apps.Job;
import mltiply.resources.Cluster;
import mltiply.schedulers.Slaq;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

  private static Logger LOG = Logger.getLogger(Main.class.getName());

  public static void runSimulator() {
    LOG.log(Level.INFO, "======= Begin Simulation =======");

    // initialize runnable jobs
    int NUM_JOBS = 100;
    Queue<Job> runnableJobs = new LinkedList<Job>();
    for (int i = 0; i < NUM_JOBS; i++) {
      Job job = new Job(i, 120);
      runnableJobs.add(job);
      LOG.log(Level.INFO, Integer.toString(runnableJobs.size()));
    }

    double FAIR_MAKESPAN = 0.0, FAIR_AVG_JCT = 0.0, FAIR_JAIN_INDEX = 0.0;
    Simulator simulator = new Simulator(runnableJobs, Simulator.SharingPolicy.Fair);
    simulator.simulate();
    FAIR_MAKESPAN = simulator.MAKESPAN;
    FAIR_AVG_JCT = simulator.AVG_JCT;
    FAIR_JAIN_INDEX = simulator.JAINS_FAIRNESS_INDEX;

    double SLAQ_MAKESPAN = 0.0, SLAQ_AVG_JCT = 0.0, SLAQ_JAIN_INDEX = 0.0;
    simulator = new Simulator(runnableJobs, Simulator.SharingPolicy.Slaq);
    simulator.simulate();
    SLAQ_MAKESPAN = simulator.MAKESPAN;
    SLAQ_AVG_JCT = simulator.AVG_JCT;
    SLAQ_JAIN_INDEX = simulator.JAINS_FAIRNESS_INDEX;

    LOG.log(Level.INFO, "" + runnableJobs.size());
    LOG.log(Level.INFO, "FAIR - " + "MAKESPAN:" + FAIR_MAKESPAN + ", AVG_JCT:" + FAIR_AVG_JCT +
        ", JAIN_INDEX:" + FAIR_JAIN_INDEX);
    LOG.log(Level.INFO, "SLAQ - " + "MAKESPAN:" + SLAQ_MAKESPAN + ", AVG_JCT:" + SLAQ_AVG_JCT +
        ", JAIN_INDEX:" + SLAQ_JAIN_INDEX);

    LOG.log(Level.INFO, "======= End Simulation =======");
  }

  public static void main(String[] args) {
    runSimulator();

    // int numJobs = 10;
    // double ticksMax = 100000;
    // double ticksCurr = 0;
    // double schedulingEpoch = 3.0;
    // String jobArrival = "All";
    //
    // System.out.println("Begin Simulation!");
    //
    // Cluster cluster = new Cluster(1, 20);
    // List<Job> runnableJobs = new LinkedList<Job>();
    // for (int i = 0; i < numJobs; i++) {
    //   runnableJobs.add(new Job(i, schedulingEpoch));
    // }
    // List<Job> runningJobs = new LinkedList<Job>();
    // Slaq slaq = new Slaq(schedulingEpoch);
    //
    // while (ticksCurr < ticksMax && !(runnableJobs.isEmpty() && runningJobs.isEmpty())) {
    //   // update running jobs
    //   if (jobArrival == "All") {
    //     for (Iterator<Job> iterator = runnableJobs.iterator(); iterator.hasNext();) {
    //       Job job = iterator.next();
    //       runningJobs.add(job);
    //       iterator.remove();
    //     }
    //   }
    //
    //   slaq.schedule(cluster, runningJobs);
    //
    //   ticksCurr += schedulingEpoch;
    // }
    //
    // System.out.println("End Simulation!");
  }

}
