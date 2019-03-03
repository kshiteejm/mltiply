package mltiply.utils;

import mltiply.apps.Job;

import java.util.Comparator;

public class JobLossFunctionByValueComparator implements Comparator<Job> {
  public int compare(Job job1, Job job2) {
    double job1LossValue = job1.lossFunction.getValue(job1.currIterationNum);
    double job2LossValue = job2.lossFunction.getValue(job2.currIterationNum);
    return Double.compare(job1LossValue, job2LossValue);
  }
}
