package mltiply.resources;

public class Machine {
  public double cpu;
  public double mem;
  public int mid;

  public Machine(int mid, double cpu) {
    this.mid = mid;
    this.cpu = cpu;
  }

  public Machine(int mid, double cpu, double mem) {
    this.mid = mid;
    this.cpu = cpu;
    this.mem = mem;
  }
}
