package mltiply.utils;

import mltiply.apps.Job;

import java.util.Comparator;

public class JobUnallocatedProportionComparator implements Comparator<Job> {
  public int compare(Job job1, Job job2) {
    return Double.compare(job1.getUnallocatedProportion(), job2.getUnallocatedProportion());
  }
}
