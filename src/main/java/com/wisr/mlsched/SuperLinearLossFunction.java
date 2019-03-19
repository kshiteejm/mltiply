import java.util.Random;

public class SuperLinearLossFunction implements LossFunction {
  public int numIterations;
  public double valueBegin;
  public double valueEnd;
  public double u, b, c;

  private SuperLinearLossFunction(int numIterations, double valueBegin, double valueEnd,
                           double u, double b, double c) {
    this.numIterations = numIterations;
    this.valueBegin = valueBegin;
    this.valueEnd = valueEnd;
    this.u = u;
    this.b = b;
    this.c = c;
  }

  public static SuperLinearLossFunction getRandomSuperlinearFunction(int numIterations, int seed) {
    double tValueBegin = 1.0;
    double tValueEnd = 0.0;
    Random r = new Random(seed);
    double tU = r.nextDouble();
    double tC = Math.pow(tU, numIterations);
    tC = tC/(tC - 1);
    double tB = -Math.log(1 - tC)/Math.log(tU);
    return new SuperLinearLossFunction(numIterations, tValueBegin, tValueEnd,
        tU, tB, tC);
  }

  public double getValue(int iteration) {
    double value = Math.pow(u, iteration - b) + c;
    return value;
  }

  public double getSlope(int iteration) {
    double value = Math.pow(u, iteration - b);
    value = Math.log(u)*value;
    return value;
  }

  public double getDeltaValue(int iteration) {
    return getValue(iteration-1) - getValue(iteration);
  }
}