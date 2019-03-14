package mlsched.scheduler;

import java.util.Comparator;

import mlsched.workload.Job;

public class SLAQJobComparator implements Comparator<Job> {

	@Override
	public int compare(Job j1, Job j2) {
		if(j1.predLossRed > j2.predLossRed) {
			return -1;
		} else {
			return 1;
		}
	}

}
