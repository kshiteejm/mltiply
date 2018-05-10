package mltiply.resources;

import mltiply.apps.Task;
import mltiply.simulator.Simulator;

import java.util.Map;
import java.util.TreeMap;

public class Cluster {

  public Map<Integer, Machine> machines;
  Simulator simulator;

  // public Cluster(int num_machines, Resources size) {
  //   machines = new TreeMap<Integer, Machine>();
  //   for (int i = 0; i < num_machines; i++) {
  //     machines.put(i, new Machine(i, size));
  //   }
  // }

  public Cluster(int num_machines, int size, Simulator simulator) {
    machines = new TreeMap<Integer, Machine>();
    for (int i = 0; i < num_machines; i++) {
      machines.put(i, new Machine(i, size, simulator));
    }
  }

  public boolean assignTask(Task t) {
    for (Machine machine: machines.values()) {
      if (machine.assignTask(t))
        return true;
    }
    return false;
  }

  public int getClusterMaxResAlloc() {
    int maxClusterResAvail = 0;
    for (Machine machine: machines.values()) {
      maxClusterResAvail += machine.maxResAlloc;
    }
    return maxClusterResAvail;
  }

  public void finishTasks() {
    for (Machine machine: machines.values()) {
      machine.finishTasks();
    }
  }


}
