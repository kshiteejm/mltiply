package mltiply.events.datastructures;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Cluster {

  private static Logger LOG = Logger.getLogger(Cluster.class.getName());

  public List<Machine> machines;

  public Cluster(int num_machines, double machine_max_resource, int num_dimensions) {
    machines = new LinkedList<Machine>();
    for (int i = 0; i < num_machines; i++) {
      Machine machine = new Machine(i, machine_max_resource, num_dimensions);
      machines.add(machine);
    }
  }
}
