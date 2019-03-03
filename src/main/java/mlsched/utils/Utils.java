package mlsched.utils;

public class Utils {
  public static double round(double value, int places) {
    double roundedVal = value;
    double roundFactor = Math.pow(10, places);
    roundedVal = roundedVal * roundFactor;
    roundedVal = Math.round(roundedVal);
    roundedVal /= roundFactor;
    return roundedVal;
  }
}
