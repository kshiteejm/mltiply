package mltiply.utils;

import mltiply.apps.Job;

import java.util.Comparator;

public class JobLossFunctionBySlopeComparator implements Comparator<Job> {
  public int compare(Job job1, Job job2) {
    double job1LossGradient = job1.lossFunction.getSlope(job1.currIterationNum);
    double job2LossGradient = job2.lossFunction.getSlope(job2.currIterationNum);
    return Double.compare(job1LossGradient, job2LossGradient);
  }
}
