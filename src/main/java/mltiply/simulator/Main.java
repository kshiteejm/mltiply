package mltiply.simulator;

import mltiply.apps.Job;
import mltiply.resources.Cluster;
import mltiply.schedulers.Slaq;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;

public class Main {

  private static Logger LOG = Logger.getLogger(Main.class.getName());

  public static void runSimulator() {
    LOG.log(INFO, "======= Begin Simulation =======");
    Simulator simulator = new Simulator();
    simulator.simulate();
    LOG.log(INFO, "======= End Simulation =======");
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
