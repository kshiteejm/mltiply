package mltiply.events.datastructures;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Machine {

  private static Logger LOG = Logger.getLogger(Machine.class.getName());

  int machineId;
  List<Task> runningTasks;
  List<Task> completedTasks;
  Resource maxAlloc;
  Resource currAlloc;

  public Machine(int machineId, double machine_max_resource, int num_dimensions) {
    this.machineId = machineId;
    maxAlloc = new Resource(machine_max_resource, num_dimensions);
    currAlloc = new Resource(num_dimensions);
    runningTasks = new LinkedList<Task>();
  }

  public Resource deficit() {
    return maxAlloc.minus(currAlloc);
  }

  public void finishTask(Task t) {
    runningTasks.remove(t);
    completedTasks.add(t);
    currAlloc = currAlloc.minus(t.demand);
  }

  public void assignTask(Task t) {
    assert(t.demand.isLessThan(deficit()));
    runningTasks.add(t);
    currAlloc = currAlloc.add(t.demand);
  }
}
