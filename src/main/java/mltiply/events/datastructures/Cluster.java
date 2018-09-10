package mltiply.events.datastructures;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Cluster {

  private static Logger LOG = Logger.getLogger(Cluster.class.getName());

  public List<Machine> machines;
  public int num_dimensions;

  public Cluster(int num_machines, double machine_max_resource, int num_dimensions) {
    this.num_dimensions = num_dimensions;
    machines = new LinkedList<Machine>();
    for (int i = 0; i < num_machines; i++) {
      Machine machine = new Machine(i, machine_max_resource, num_dimensions);
      machines.add(machine);
    }
  }

  public void finishTask(Task t) {
    for (Machine m: machines) {
      if (m.runningTasks.contains(t)) {
        m.finishTask(t);
        break;
      }
    }
  }

  public void assignTask(Task t, Machine m) {
    assert(machines.contains(m));
    m.assignTask(t);
  }

  public Resource deficit() {
    Resource deficit = new Resource(num_dimensions);
    for (Machine m: machines) {
      deficit = deficit.add(m.deficit());
    }
    return deficit;
  }

  public boolean hasDeficit() {
    return deficit().isGreaterThanZero();
  }

  public Resource maxAlloc() {
    Resource maxCapacity = new Resource(num_dimensions);
    for (Machine m: machines) {
      maxCapacity = maxCapacity.add(m.maxAlloc);
    }
    return maxCapacity;
  }
}
