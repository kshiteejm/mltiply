package mltiply.resources;

public class Cluster {
  public Machine[] machines;

  public Cluster(int machines, int cpu) {
    this.machines = new Machine[machines];
    for (int i = 0; i < machines; i++) {
      this.machines[i] = new Machine(i, cpu);
    }
  }

  public int getNumCores() {
    int numCores = 0;
    for (Machine m: machines) {
      numCores += m.cpu;
    }
    return numCores;
  }
}
