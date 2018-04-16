package mltiply.utils;

public class Function {
  public String type;
  public double[] parameters;

  public Function() {
    this.type = "sublinear";
    this.parameters = new double[]{0.0, 1.0, -100.015, 66.67};
  }

  public Function(String type, double[] parameters) {
    this.type = type;
    this.parameters = parameters;
  }

  public double getDeltaLoss(int iterBegin, int iterEnd) {
    if (type == "sublinear") {
      double lossBegin = 1/(parameters[0]*iterBegin*iterBegin + parameters[1]*iterBegin + parameters[2]) + parameters[3];
      double lossEnd = 1/(parameters[0]*iterEnd*iterEnd + parameters[1]*iterEnd + parameters[2]) + parameters[3];
      return (lossBegin - lossEnd);
    } else {
      double lossBegin = Math.pow(parameters[0], iterBegin - parameters[1]) + parameters[2];
      double lossEnd = Math.pow(parameters[0], iterEnd - parameters[1]) + parameters[2];
      return  (lossBegin - lossEnd);
    }
  }
}
