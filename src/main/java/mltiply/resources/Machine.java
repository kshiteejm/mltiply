package mltiply.resources;

import mltiply.apps.Task;

import java.util.Map;
import java.util.TreeMap;

public class Machine {
  int machineId;
  Map<Task, Double> runningTasks;

  Resources maxResAlloc;
  Resources totalResAlloc;

  public Machine(int machineId, Resources size) {
    this.machineId = machineId;
    runningTasks = new TreeMap<Task, Double>();
    maxResAlloc = Resources.clone(size);
    totalResAlloc = new Resources(size.dimension);
  }
}
