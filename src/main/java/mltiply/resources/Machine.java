package mltiply.resources;

import mltiply.apps.Job;
import mltiply.apps.Task;
import mltiply.simulator.Simulator;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Machine {

  private static Logger LOG = Logger.getLogger(Machine.class.getName());

  int machineId;
  Map<Task, Double> runningTasks;
  Simulator simulator;

  // Resources maxResAlloc;
  // Resources totalResAlloc;
  //
  // public Machine(int machineId, Resources size) {
  //   this.machineId = machineId;
  //   runningTasks = new TreeMap<Task, Double>();
  //   maxResAlloc = Resources.clone(size);
  //   totalResAlloc = new Resources(size.dimension);
  // }

  int maxResAlloc;
  int totalResAlloc;

  public Machine(int machineId, int size, Simulator simulator) {
    this.machineId = machineId;
    runningTasks = new TreeMap<Task, Double>();
    maxResAlloc = size;
    totalResAlloc = 0;
    this.simulator = simulator;
  }

  /* assign task to this machine - return true if assigned else false
   * 1. check if task demands fit in this machine
   * 2. update current total resource allocation accordingly and the time at which task completes
   */
  public boolean assignTask(Task task) {
    boolean isTaskRun = false;
    LOG.log(Level.FINE, "Machine " + machineId + " - Machine Max " + maxResAlloc +
        " - Machine Total " + totalResAlloc + " - Task Demands " + task.demands);
    if (task.demands <= maxResAlloc - totalResAlloc) {
      totalResAlloc += task.demands;
      runningTasks.put(task, task.duration + simulator.CURRENT_TIME);
      isTaskRun = true;
    }
    return isTaskRun;
  }

  /* finish completed tasks in this machine
   * 1. find all running tasks whose finish time exceeds current simulation time
   * 2. reduce current total resource allocation in this machine
   * 3. find job that completed tasks belong to and update those jobs
   */
  public void finishTasks() {
    ArrayList<Task> finishedTasks = new ArrayList<Task>();
    for (Map.Entry<Task, Double> td: runningTasks.entrySet()) {
      Task task = td.getKey();
      double taskEndTime = td.getValue();
      if (simulator.CURRENT_TIME >= taskEndTime) {
        finishedTasks.add(task);
        totalResAlloc -= task.demands;
        LOG.log(Level.FINE, task.toString());
      }
    }
    for (Task task: finishedTasks) {
      runningTasks.remove(task);
      for (Job job: simulator.runningJobs) {
        if (job.finishTask(task)) {
          LOG.log(Level.FINE, "Time: " + simulator.CURRENT_TIME
              + ". Job " + job.jobId + ", Finished Task " + task.taskId + " from Stage "
              + task.stageId + " Task Duration " + task.duration + " Job Iteration Over " + job.isIterationOver());
          break;
        }
      }
    }
  }
}
