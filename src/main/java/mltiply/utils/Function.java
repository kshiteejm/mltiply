package mltiply.utils;

public interface Function {
  // y = f(x), where y is typically the normalized loss function value and x is the iteration number
  public double getValue(int iteration);
  public double getSlope(int iteration);
  public double getDeltaValue(int iteration);
}
