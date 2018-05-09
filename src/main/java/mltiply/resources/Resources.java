package mltiply.resources;

public class Resources {

  public double[] resources;
  public int dimension;

  public Resources(int dimension) {
    this.dimension = dimension;
    resources = new double[dimension];
  }

  public Resources(int dimension, double size) {
    this.dimension = dimension;
    resources = new double[dimension];
    for (int i = 0; i < dimension; i++) {
      resources[i] = size;
    }
  }

  public Resources(Resources res) {
    this.dimension = res.dimension;
    resources = new double[dimension];
    for (int i = 0; i < dimension; i++) {
      resources[i] = res.resource(i);
    }
  }

  public static Resources clone(Resources res) {
    Resources clonedRes = new Resources(res);
    return clonedRes;
  }

  public double resource(int index) {
    assert (index >= 0 && index < dimension);
    return resources[index];
  }
}
