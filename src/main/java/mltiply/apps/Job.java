package mltiply.apps;

import mltiply.utils.Function;

public class Job {
  public int jid; // unique job id
  public int workers; // number of workers currently assigned to this job
  public Function loss; // loss as a function of iteration
  public int iterations; // current SGD iteration number for this job
  public int iterationsMax; // limit on the maximum number for this iteration
  public double miniBatchSizeIndicator; // mini-batch size
  public double schedulingEpoch; // time interval when scheduling decision is taken

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
