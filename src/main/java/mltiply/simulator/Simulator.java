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

import java.util.Random;
import java.lang.Math;

public class Simulator {

  private static Logger LOG = Logger.getLogger(Simulator.class.getName());

  public enum RunMode {
    Default, Custom;
  }
  public static RunMode runMode = RunMode.Default;

  public enum SharingPolicy {
    Fair, Slaq, Mltiply, TimeFair;
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
  public double CURRENT_TIME = 0.0;
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

  // metrics
  public double JAINS_FAIRNESS_INDEX;
  public double MAKESPAN;
  public double AVG_JCT;
  public double TIME_FAIRNESS_INDEX;

  public Simulator(Queue<Job> runnableJobs, SharingPolicy sharingPolicy) {

    // Randomness for Poisson Process.
    generator = new Random();

    // initialize metrics
    JAINS_FAIRNESS_INDEX = 0.0;
    MAKESPAN = 0.0;
    AVG_JCT = 0.0;
    TIME_FAIRNESS_INDEX = 0.0;

    switch (runMode) {
      case Default:
        NUM_MACHINES = 1;
        NUM_DIMENSIONS = 1;
        MACHINE_MAX_RESOURCE = 1000;
        STEP_TIME = 0.01;
        SIM_END_TIME = 120000;
        JOBS_ARRIVAL_POLICY = JobsArrivalPolicy.All;
        INTER_JOB_POLICY = SharingPolicy.Slaq;
        INTRA_JOB_POLICY = SchedulingPolicy.Random;
        POISSON_RATE = 0.5;
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
        POISSON_RATE = 0.00001;
        break;
      default:
        System.err.println("Unknown Run Mode");
    }

    CURRENT_TIME = 0;
    this.runnableJobs = new LinkedList<Job>();
    for (Job job: runnableJobs) {
      this.runnableJobs.add(job.clone());
    }
    runningJobs = new LinkedList<Job>();
    completedJobs = new LinkedList<Job>();
    INTER_JOB_POLICY = sharingPolicy;
    NUM_JOBS = this.runnableJobs.size();

    // initialize cluster
    // cluster = new Cluster(NUM_MACHINES, new Resources(NUM_DIMENSIONS, MACHINE_MAX_RESOURCE));
    cluster = new Cluster(NUM_MACHINES, MACHINE_MAX_RESOURCE, this);


    interJobScheduler = new InterJobScheduler(this);
    intraJobScheduler = new IntraJobScheduler(this);
  }

  // Based on http://preshing.com/20111007/how-to-generate-random-timings-for-a-poisson-process/
  public double getExpSample() {
    return - Math.log(1.0 - generator.nextDouble()) / POISSON_RATE;
  }

  public void simulate() {

    for (CURRENT_TIME = 0; CURRENT_TIME < SIM_END_TIME; CURRENT_TIME += STEP_TIME) {

      LOG.log(Level.FINE, Double.toString(CURRENT_TIME));

      /* any tasks finished?
       * check for completion of running tasks
       * update per job running and completed tasks
       */
      cluster.finishTasks();

      /* any jobs finished?
       * if any job has completed it's iteration limit or target accuracy
       */
      List<Job> finishedJobs = new LinkedList<Job>();
      for (Job job: runningJobs) {
        if (job.isFinished()) {
          LOG.log(Level.FINE, "TIME: " + CURRENT_TIME + " Finished Job " + job.jobId);
          job.endTime = CURRENT_TIME;
          finishedJobs.add(job);
          AVG_JCT += job.endTime - job.startTime;
        }
      }
      runningJobs.removeAll(finishedJobs);
      completedJobs.addAll(finishedJobs);

      /* finish simulation?
       * if there are no runnable and running jobs in the system then we are done
       */
      if (runnableJobs.isEmpty() && runningJobs.isEmpty()) {
        AVG_JCT = AVG_JCT/completedJobs.size();
        JAINS_FAIRNESS_INDEX = JAINS_FAIRNESS_INDEX*STEP_TIME/CURRENT_TIME;
        MAKESPAN = CURRENT_TIME;
        TIME_FAIRNESS_INDEX = 0.0;
        for (Job job: completedJobs) {
          double fairnessIndex = job.getFairnessIndex(cluster.getClusterMaxResAlloc()/NUM_JOBS, CURRENT_TIME);
          if (fairnessIndex >= 0.97)
            TIME_FAIRNESS_INDEX += 1.0;
          // else
          //   LOG.log(Level.INFO, "Fairness Index: " + fairnessIndex);
        }
        TIME_FAIRNESS_INDEX = TIME_FAIRNESS_INDEX/completedJobs.size();
        LOG.log(Level.INFO, "====== Simulation Results ======");
        LOG.log(Level.INFO, "MAKESPAN: " + MAKESPAN);
        LOG.log(Level.INFO, "AVG_JCT: " + AVG_JCT);
        LOG.log(Level.INFO, "JAINS_FAIRNESS_INDEX: " + JAINS_FAIRNESS_INDEX);
        LOG.log(Level.INFO, "TIME_FAIRNESS_INDEX: " + TIME_FAIRNESS_INDEX);
        break;
      }

      /* any new jobs?
       * check for arrival of new jobs - depends on the arrival policy
       */
      List<Job> newJobs = new LinkedList<Job>();

      if (JOBS_ARRIVAL_POLICY == JobsArrivalPolicy.All) {
        newJobs.addAll(runnableJobs);
        runnableJobs.removeAll(newJobs);
      } else if (JOBS_ARRIVAL_POLICY == JobsArrivalPolicy.Distribution) {
        // Simulate a Poisson Process for Arrival of jobs.
        while (CURRENT_TIME >= nextTimeToLaunchJob) {
          // LOG.log(Level.INFO, "=== Job Arrived at - " + nextTimeToLaunchJob);
          // LOG.log(Level.INFO, "=== Launching Job at - " + CURRENT_TIME);
          if(!runnableJobs.isEmpty()) {
            Job nextJob = runnableJobs.poll(); // Pop job from runnable queue.
            newJobs.add(nextJob);
            nextTimeToLaunchJob += getExpSample(); // Sample random time for next job to arrive.
          } else {
            nextTimeToLaunchJob += getExpSample();
            break;
            // TODO: Anything here?
          }

        }
      } else if (JOBS_ARRIVAL_POLICY == JobsArrivalPolicy.One) {
        // Jobs arrive one at a time at each time step.
        LOG.log(Level.INFO, "=== Job Arrived at - " + nextTimeToLaunchJob);
        LOG.log(Level.INFO, "=== Launching Job at - " + CURRENT_TIME);
        nextTimeToLaunchJob = CURRENT_TIME + STEP_TIME;
        Job nextJob = runnableJobs.remove();
        newJobs.add(nextJob);
      }
      for (Job job: newJobs) {
        job.startTime = CURRENT_TIME;
      }
      runningJobs.addAll(newJobs);
      // LOG.log(Level.FINE, "Number of Running Jobs: " + Integer.toString(runningJobs.size()));

      /* inter-job scheduler - share cluster across jobs
       * update every jobs resource quota / resource share in the cluster
       */
      // if (finishedJobs.isEmpty() && newJobs.isEmpty())
      //   continue;
      // if (!finishedJobs.isEmpty() || !newJobs.isEmpty())
      interJobScheduler.schedule();

      /* intra-job scheduler - schedule tasks from each job
       * if cluster has free resources and job has not met resource quota schedule tasks
       * update per job - next set of runnable tasks and also runnable and running tasks
       */
      intraJobScheduler.schedule();
      // for (Job job: runningJobs) {
      //   intraJobScheduler.schedule(job);
      // }
      double _sum_of_squares = 0.0;
      double _square_of_sum = 0.0;
      int numJobsRunning = runningJobs.size();
      for (Job job: runningJobs) {
        _square_of_sum += job.currResUse;
        _sum_of_squares += job.currResUse*job.currResUse;
      }
      _square_of_sum = _square_of_sum*_square_of_sum;
      if (!(numJobsRunning == 0) && !(_sum_of_squares == 0))
        JAINS_FAIRNESS_INDEX += _square_of_sum/(numJobsRunning*_sum_of_squares);
      else
        JAINS_FAIRNESS_INDEX += 1.0;
      if (numJobsRunning == 0 || _sum_of_squares == 0) {
        LOG.log(Level.INFO, "Issues " + CURRENT_TIME);
      }
    }
  }
}
