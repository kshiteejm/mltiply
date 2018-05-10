package mltiply.resources;

import java.util.Map;
import java.util.TreeMap;

public class Cluster {
  public Map<Integer, Machine> machines;

  // public Cluster(int num_machines, Resources size) {
  //   machines = new TreeMap<Integer, Machine>();
  //   for (int i = 0; i < num_machines; i++) {
  //     machines.put(i, new Machine(i, size));
  //   }
  // }

  public Cluster(int num_machines, int size) {
    machines = new TreeMap<Integer, Machine>();
    for (int i = 0; i < num_machines; i++) {
      machines.put(i, new Machine(i, size));
    }
  }
}
