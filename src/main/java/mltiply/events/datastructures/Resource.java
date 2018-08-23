package mltiply.events.datastructures;

public class Resource {
  double[] resources;

  public Resource(double max, int dimensions) {
    resources = new double[dimensions];
    for (int i = 0; i < dimensions; i++) {
      resources[i] = max;
    }
  }

  public Resource(int dimensions) {
    this(0.0, dimensions);
  }
}
