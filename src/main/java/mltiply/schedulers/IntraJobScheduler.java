package mltiply.schedulers;

import mltiply.apps.Job;
import mltiply.apps.Task;
import mltiply.schedpolicies.RandomSchedPolicy;
import mltiply.schedpolicies.SchedPolicy;
import mltiply.simulator.Simulator;
import mltiply.utils.JobUnallocatedProportionComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IntraJobScheduler {

  private static Logger LOG = Logger.getLogger(IntraJobScheduler.class.getName());

  public SchedPolicy resSchedPolicy;
  Simulator simulator;

  public IntraJobScheduler(Simulator simulator) {
    this.simulator = simulator;
    switch (simulator.INTRA_JOB_POLICY) {
      case Random:
        resSchedPolicy = new RandomSchedPolicy(simulator);
        break;
      default:
        System.err.println("Unknown sharing policy");
    }
  }

  public void schedule() {
    for (Job job: simulator.runningJobs) {
      schedule(job);
    }
    if (simulator.INTER_JOB_POLICY == Simulator.SharingPolicy.Slaq
        || simulator.INTER_JOB_POLICY == Simulator.SharingPolicy.Mltiply)
      leftOverAllocator();
  }

  public void schedule(Job job) {
    if (job.isIterationOver() && job.resQuota > 0) {
      job.initNextIteration();
      LOG.log(Level.FINE, "Time: " + simulator.CURRENT_TIME +
          " Init Next Iteration for Job " + job.jobId + " Iteration " + job.currIterationNum);
    }
    // while tasks can be assigned in my resource
    // share quanta, on any machine, keep assigning
    // otherwise return
    resSchedPolicy.schedule(job);
  }

  public void leftOverAllocator() {
    int numJobsRunning = simulator.runningJobs.size();
    if (numJobsRunning == 0)
      return;
    Comparator<Job> jobUnallocatedProportionComparator = new JobUnallocatedProportionComparator();
    PriorityQueue<Job> minUnallocatedProportionFirst = new PriorityQueue<Job>(numJobsRunning,
        jobUnallocatedProportionComparator);
    for (Job job: simulator.runningJobs) {
      minUnallocatedProportionFirst.add(job);
    }
    int clusterResAvail = simulator.cluster.getClusterResAvail();
    while (clusterResAvail > 0 && !minUnallocatedProportionFirst.isEmpty()) {
      Job job = minUnallocatedProportionFirst.poll();
      if (job == null || job.runnableTasks.size() == 0)
        continue;
      ArrayList<Task> scheduledTasks = new ArrayList<Task>();
      for (Task task: job.runnableTasks) {
        if (simulator.cluster.assignTask(task)) {
          scheduledTasks.add(task);
          job.currResUse += task.demands;
          clusterResAvail -= task.demands;
        } else
          break;
      }
      job.runnableTasks.removeAll(scheduledTasks);
      job.runningTasks.addAll(scheduledTasks);
    }
  }
}