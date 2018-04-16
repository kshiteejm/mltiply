package mltiply.apps;

import mltiply.utils.Function;

public class Job {
  public int jid;
  public int workers;
  public Function loss;
  public int iterations;
  public int iterationsMax;
  public double miniBatchSizeIndicator;
  public double schedulingEpoch;

  public Job(int jid, double schedulingEpoch) {
    this.jid = jid;
    this.workers = 0;
    this.iterations = 0;
    this.loss = new Function();
    this.iterationsMax = 50;
    this.miniBatchSizeIndicator = 0.5;
    this.schedulingEpoch = schedulingEpoch;
  }

  public double getIterationTime() {
    return miniBatchSizeIndicator/workers;
  }

  public double getLossReduction() {
    int numIterations = (int) Math.floor(schedulingEpoch/getIterationTime());
    return loss.getDeltaLoss(iterations, iterations + numIterations);
  }

}
