package mltiply.simulator;

import mltiply.apps.Job;
import mltiply.apps.Task;
import mltiply.resources.Cluster;
import mltiply.resources.Resources;
import mltiply.schedpolicies.SchedPolicy;
import mltiply.schedulers.InterJobScheduler;
import mltiply.schedulers.IntraJobScheduler;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;

import java.util.Random;
import java.lang.Math;

public class Simulator {

  private static Logger LOG = Logger.getLogger(Simulator.class.getName());

  public enum RunMode {
    Default, Custom;
  }
  public static RunMode runMode = RunMode.Default;

  public enum SharingPolicy {
    Fair, Slaq, Mltiply;
  }
  public SharingPolicy INTER_JOB_POLICY = SharingPolicy.Fair;

  public enum SchedulingPolicy {
    Random, Tetris;
  }
  public SchedulingPolicy INTRA_JOB_POLICY = SchedulingPolicy.Random;

  public enum JobsArrivalPolicy {
    All, One, Distribution, Trace;
  }
  public JobsArrivalPolicy JOBS_ARRIVAL_POLICY = JobsArrivalPolicy.All;

  public int NUM_MACHINES, NUM_DIMENSIONS;
  public int MACHINE_MAX_RESOURCE;
  public double STEP_TIME = 1;
  public double SIM_END_TIME = 120000;
  public double CURRENT_TIME = 0;
  public int NUM_JOBS = 1;

  public Queue<Job> runnableJobs;
  public Queue<Job> runningJobs;
  public Queue<Job> completedJobs;

  public Cluster cluster;
  public double nextTimeToLaunchJob = 0;

  public InterJobScheduler interJobScheduler;
  public IntraJobScheduler intraJobScheduler;

  public Random generator;
  public double POISSON_RATE;

  public Simulator() {

    switch (runMode) {
      case Default:
        NUM_MACHINES = 1;
        NUM_DIMENSIONS = 1;
        MACHINE_MAX_RESOURCE = 100;
        STEP_TIME = 1;
        SIM_END_TIME = 12000;
        JOBS_ARRIVAL_POLICY = JobsArrivalPolicy.All;
        INTER_JOB_POLICY = SharingPolicy.Fair;
        INTRA_JOB_POLICY = SchedulingPolicy.Random;
        NUM_JOBS = 10;
        break;
      case Custom:
        NUM_MACHINES = 1;
        NUM_DIMENSIONS = 1;
        MACHINE_MAX_RESOURCE = 100;
        STEP_TIME = 1;
        SIM_END_TIME = 120000;
        JOBS_ARRIVAL_POLICY = JobsArrivalPolicy.All;
        INTER_JOB_POLICY = SharingPolicy.Slaq;
        INTRA_JOB_POLICY = SchedulingPolicy.Random;
        NUM_JOBS = 100;
        break;
      default:
        System.err.println("Unknown Run Mode");
    }

    CURRENT_TIME = 0;
    runnableJobs = new LinkedList<Job>();
    runningJobs = new LinkedList<Job>();
    completedJobs = new LinkedList<Job>();
    // cluster = new Cluster(NUM_MACHINES, new Resources(NUM_DIMENSIONS, MACHINE_MAX_RESOURCE));
    cluster = new Cluster(NUM_MACHINES, MACHINE_MAX_RESOURCE, this);
    for (int i = 0; i < NUM_JOBS; i++) {
      Job job = new Job(i, 120);
      runnableJobs.add(job);
      LOG.log(INFO, Integer.toString(runnableJobs.size()));
    }
    interJobScheduler = new InterJobScheduler(this);
    intraJobScheduler = new IntraJobScheduler(this);

    // Randomness for Poisson Process.
    generator = new Random();
    POISSON_RATE = 1.0;

  }

  // Based on http://preshing.com/20111007/how-to-generate-random-timings-for-a-poisson-process/
  public double getExpSample() {
    return - Math.log(1.0 - generator.nextDouble()) / POISSON_RATE;
  }

  public void simulate() {

    for (CURRENT_TIME = 0; CURRENT_TIME < SIM_END_TIME; CURRENT_TIME += STEP_TIME) {

      LOG.log(Level.FINE, Double.toString(CURRENT_TIME));

      // finish simulation?
      if (runnableJobs.isEmpty() && runningJobs.isEmpty()) {
        LOG.log(Level.INFO, "=== Simulation Ended at - " + CURRENT_TIME);
        break;
      }

      // any jobs finished?
      List<Job> finishedJobs = new LinkedList<Job>();
      for (Job j: runningJobs) {
        if (j.isFinished())
          finishedJobs.add(j);
      }
      runningJobs.removeAll(finishedJobs);
      completedJobs.addAll(finishedJobs);

      // any tasks finished?
      cluster.finishTasks();
      // any new jobs?
      List<Job> newJobs = new LinkedList<Job>();

      if (JOBS_ARRIVAL_POLICY == JobsArrivalPolicy.All) {
        newJobs.addAll(runnableJobs);
      }

      // Simulate a Poisson Process for Arrival of jobs.
      else if (JOBS_ARRIVAL_POLICY == JobsArrivalPolicy.Distribution) {
        if (CURRENT_TIME >= nextTimeToLaunchJob) {
          LOG.log(Level.INFO, "=== Job Arrived at - " + nextTimeToLaunchJob);
          LOG.log(Level.INFO, "=== Launching Job at - " + CURRENT_TIME);
          nextTimeToLaunchJob = CURRENT_TIME + getExpSample(); // Sample random time for next job to arrive.
          Job nextJob = runnableJobs.peek(); // Pop job from runnable queue.
          newJobs.add(nextJob);
        }
      }

      // Jobs arrive one at a time at each time step.
      else if (JOBS_ARRIVAL_POLICY == JobsArrivalPolicy.One) {
        LOG.log(Level.INFO, "=== Job Arrived at - " + nextTimeToLaunchJob);
        LOG.log(Level.INFO, "=== Launching Job at - " + CURRENT_TIME);
        nextTimeToLaunchJob = CURRENT_TIME + STEP_TIME;
        Job nextJob = runnableJobs.peek();
        newJobs.add(nextJob);
      }

      runnableJobs.removeAll(newJobs);
      runningJobs.addAll(newJobs);
      LOG.log(Level.INFO, "Number of Running Jobs: " + Integer.toString(runningJobs.size()));

      // share cluster across jobs
      // if (finishedJobs.isEmpty() && newJobs.isEmpty())
      //   continue;
      interJobScheduler.schedule();

      // schedule tasks from each job
      for (Job job: runningJobs) {
        intraJobScheduler.schedule(job);
      }
      // System.exit(1);
    }
  }
}
