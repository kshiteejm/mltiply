package mltiply.utils;

import mltiply.apps.Job;

import java.util.Comparator;

public class JobLossComparator implements Comparator<Job> {
  public int compare(Job a, Job b) {
    double aLoss = a.getLossReduction();
    double bLoss = b.getLossReduction();
    if (aLoss > bLoss)
      return -1;
    else if (aLoss < bLoss)
      return 1;
    else
      return 0;
  }
}
