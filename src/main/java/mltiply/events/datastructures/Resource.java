package mltiply.events.datastructures;

import java.math.BigDecimal;

import static mltiply.events.simulator.Simulator.ROUND_PLACES;

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

  public Resource(Resource r) {
    int dimensions = r.getDimensions();
    resources = new double[dimensions];
    for (int i = 0; i < dimensions; i++) {
      resources[i] = r.resources[i];
    }
  }

  public static boolean equals(double a, double b) {
    if (Math.abs(a - b) >= Resource.getThreshold())
      return false;
    else
      return true;
  }

  public static double getThreshold() {
    return Math.pow(10, -ROUND_PLACES);
  }

  public static double round(double value) {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(ROUND_PLACES, BigDecimal.ROUND_HALF_UP);
    return bd.doubleValue();
  }

  public int getDimensions() {
    return resources.length;
  }

  public double absValue() {
    double ret = 0.0;
    for (int i = 0; i < resources.length; i++) {
      ret += resources[i];
    }
    return ret;
  }

  public double avgAbsValue() {
    double ret = 0.0;
    for (int i = 0; i < resources.length; i++) {
      ret += resources[i];
    }
    ret /= resources.length;
    return ret;
  }

  public Resource add(Resource other) {
    Resource ret = new Resource(this);
    for (int i = 0; i < resources.length; i++) {
      ret.resources[i] += other.resources[i];
    }
    return ret;
  }

  public Resource minus(Resource other) {
    Resource ret = new Resource(this);
    for (int i = 0; i < resources.length; i++) {
      ret.resources[i] -= other.resources[i];
    }
    return ret;
  }

  public double divide(Resource other) {
    double ret = Double.MAX_VALUE;
    for (int i = 0; i < resources.length; i++) {
      double tmp = resources[i]/other.resources[i];
      if (tmp < ret)
        ret = tmp;
    }
    return ret;
  }

  public Resource multiply(double num) {
    Resource ret = new Resource(this);
    for (int i = 0; i < resources.length; i++) {
      ret.resources[i] *= num;
    }
    return ret;
  }

  public boolean equals(Resource other) {
    for (int i = 0; i < resources.length; i++) {
      if (Math.abs(resources[i] - other.resources[i]) >= Resource.getThreshold())
        return false;
    }
    return true;
  }

  public boolean isLessThan(Resource other) {
    for (int i = 0; i < resources.length; i++) {
      if (resources[i] - other.resources[i] >= Resource.getThreshold())
        return false;
    }
    return true;
  }

  public boolean isGreaterThanZero() {
    Resource zero = new Resource(resources.length);
    return zero.isLessThan(this);
  }

  public static int compare(Resource r1, Resource r2) {
    return Double.compare(r1.absValue(), r2.absValue());
  }

  public double dot(Resource other) {
    double ret = 0.0;
    for (int i = 0; i < resources.length; i++) {
      ret += resources[i]*other.resources[i];
    }
    return ret;
  }
}
