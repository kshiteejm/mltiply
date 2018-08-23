package mltiply.events.datastructures;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Machine {

  private static Logger LOG = Logger.getLogger(Machine.class.getName());

  int machineId;
  List<Task> runningTasks;
  Resource maxAlloc;
  Resource currAlloc;

  public Machine(int machineId, double machine_max_resource, int num_dimensions) {
    this.machineId = machineId;
    maxAlloc = new Resource(machine_max_resource, num_dimensions);
    currAlloc = new Resource(num_dimensions);
    runningTasks = new LinkedList<Task>();
  }
}
