package mltiply.schedpolicies;

import mltiply.apps.Job;
import mltiply.apps.Task;
import mltiply.simulator.Simulator;

import java.util.ArrayList;

public class RandomSchedPolicy extends  SchedPolicy {

  public RandomSchedPolicy(Simulator simulator) {
    super(simulator);
  }

  @Override
  public void schedule(Job job) {
    if (job.runnableTasks.isEmpty())
      return;

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
  }
}
