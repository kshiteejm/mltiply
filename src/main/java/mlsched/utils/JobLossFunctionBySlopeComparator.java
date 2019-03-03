package mlsched.utils;

import mlsched.workload.Job;

import java.util.Comparator;

public class JobLossFunctionBySlopeComparator implements Comparator<Job> {
  public int compare(Job job1, Job job2) {
    // double job1LossGradient = job1.lossFunction.getSlope(job1.currIterationNum + 1);
    // double job2LossGradient = job2.lossFunction.getSlope(job2.currIterationNum + 1);
    double job1LossGradient = 0.0, job2LossGradient = 0.0;
    if (job1.runnableTasks.isEmpty())
      job1LossGradient = job1.lossFunction.getValue(job1.currIterationNum + 1) -
          job1.lossFunction.getValue(job1.currIterationNum + 2);
    else
      job1LossGradient = job1.lossFunction.getValue(job1.currIterationNum) -
          job1.lossFunction.getValue(job1.currIterationNum + 1);
    if (job2.runnableTasks.isEmpty())
      job2LossGradient = job2.lossFunction.getValue(job2.currIterationNum + 1) -
          job2.lossFunction.getValue(job2.currIterationNum + 2);
    else
      job2LossGradient = job2.lossFunction.getValue(job2.currIterationNum) -
          job2.lossFunction.getValue(job2.currIterationNum + 1);
    job1.jobLossSlope = job1LossGradient;
    job2.jobLossSlope = job2LossGradient;
    return -Double.compare(job1LossGradient, job2LossGradient);
  }
}
