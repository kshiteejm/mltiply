package mlsched.scheduler;

import java.util.Comparator;

import mlsched.workload.Job;

public class OptimusJobComparator implements Comparator<Job> {

	@Override
	public int compare(Job j1, Job j2) {
		if(j1.marginalGain > j2.marginalGain) {
			return -1;
		} else {
			return 1;
		}
	}
	


}
