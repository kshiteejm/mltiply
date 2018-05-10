package mltiply.resources;

import mltiply.apps.Job;
import mltiply.apps.Task;
import mltiply.simulator.Simulator;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Machine {

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
    totalResAlloc = size;
    this.simulator = simulator;
  }

  public boolean assignTask(Task t) {
    boolean isTaskRun = false;
    if (t.demands <= maxResAlloc - totalResAlloc) {
      totalResAlloc = maxResAlloc + t.demands;
      runningTasks.put(t, t.duration + simulator.CURRENT_TIME);
      isTaskRun = true;
    }
    return isTaskRun;
  }

  public void finishTasks() {
    ArrayList<Task> finishedTasks = new ArrayList<Task>();
    for (Map.Entry<Task, Double> td: runningTasks.entrySet()) {
      if (simulator.CURRENT_TIME >= td.getValue()) {
        finishedTasks.add(td.getKey());
      }
    }
    for (Task task: finishedTasks) {
      runningTasks.remove(task);
      for (Job job: simulator.runningJobs) {
        if (job.runningTasks.contains(task)) {
          job.runningTasks.remove(task);
          job.completedTasks.add(task);
          job.currResUse -= task.demands;
          break;
        }
      }
    }
  }
}
