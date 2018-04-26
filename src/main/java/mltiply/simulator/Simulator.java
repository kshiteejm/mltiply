package mltiply.simulator;

import mltiply.apps.Job;
import mltiply.resources.Cluster;
import mltiply.schedulers.InterJobScheduler;

import java.util.LinkedList;
import java.util.Queue;

public class Simulator {
  public enum RunMode {
    Default, Custom;
  }
  public static RunMode runMode = RunMode.Default;

  public enum SharingPolicy {
    Fair, Slaq, Mltiply;
  }
  public SharingPolicy INTER_JOB_POLICY = SharingPolicy.Slaq;

  public enum JobsArrivalPolicy {
    All, One, Distribution, Trace;
  }
  public JobsArrivalPolicy JOBS_ARRIVAL_POLICY = JobsArrivalPolicy.All;

  public int NUM_MACHINES, NUM_DIMENSIONS;
  public double MACHINE_MAX_RESOURCE;
  public double SIM_END_TIME = 20000;
  public double STEP_TIME = 1;
  public double CURRENT_TIME = 0;

  public Queue<Job> runnableJobs;
  public Queue<Job> runningJobs;
  public Queue<Job> completedJobs;

  public Cluster cluster;
  public double nextTimeToLaunchJob = 0;

  public InterJobScheduler interJobScheduler;

  public Simulator() {

    switch (runMode) {
      case Default:
        NUM_MACHINES = 1;
        NUM_DIMENSIONS = 1;
        MACHINE_MAX_RESOURCE = 100;
        STEP_TIME = 1;
        SIM_END_TIME = 20000;
        JOBS_ARRIVAL_POLICY = JobsArrivalPolicy.All;
        INTER_JOB_POLICY = SharingPolicy.Slaq;
        break;
      case Custom:
        NUM_MACHINES = 1;
        NUM_DIMENSIONS = 1;
        MACHINE_MAX_RESOURCE = 100;
        STEP_TIME = 1;
        SIM_END_TIME = 20000;
        JOBS_ARRIVAL_POLICY = JobsArrivalPolicy.All;
        INTER_JOB_POLICY = SharingPolicy.Slaq;
        break;
      default:
        System.err.println("Unknown Run Mode");
    }

    CURRENT_TIME = 0;
    runnableJobs = new LinkedList<Job>();
    runningJobs = new LinkedList<Job>();
    completedJobs = new LinkedList<Job>();
    // cluster = new Cluster(NUM_MACHINES, MACHINE_MAX_RESOURCE, NUM_DIMENSIONS);

  }

  public void simulate() {

  }

}
