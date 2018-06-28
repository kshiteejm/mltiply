package mltiply.schedpolicies;

import mltiply.apps.Job;
import mltiply.apps.Task;
import mltiply.simulator.Simulator;
import mltiply.utils.JobUnallocatedProportionComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RandomSchedPolicy extends  SchedPolicy {

  private static Logger LOG = Logger.getLogger(RandomSchedPolicy.class.getName());

  public RandomSchedPolicy(Simulator simulator) {
    super(simulator);
  }

  // @Override
  // public void schedule() {
  //   for (Job job: simulator.runningJobs) {
  //     schedule(job);
  //   }
  // }

  @Override
  public void schedule(Job job) {
    if (job.runnableTasks.isEmpty())
      return;

    LOG.log(Level.FINE, "Job " + job.jobId + ", Resource Quota " + job.resQuota + ", Current Use " + job.currResUse);
    ArrayList<Task> scheduledTasks = new ArrayList<Task>();
    for (Task task: job.runnableTasks) {
      if (job.currResUse < job.resQuota) {
        if (simulator.cluster.assignTask(task)) {
          job.currResUse += task.demands;
          scheduledTasks.add(task);
        }
      } else
        break;
    }
    job.runnableTasks.removeAll(scheduledTasks);
    job.runningTasks.addAll(scheduledTasks);
    LOG.log(Level.FINE, "Job " + job.jobId + ", Number of Running Tasks " + job.runningTasks.size() +
        ", Number of Runnable Tasks " + job.runnableTasks.size());
  }
}
