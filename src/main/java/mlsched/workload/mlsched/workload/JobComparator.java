package mlsched.workload;

import java.util.Comparator;

public class JobComparator implements Comparator<Job> {
	
	public int compare(Job j1, Job j2) {
		
		int j1Diff = j1.logicalFairShare - j1.nextIterAllocation;
		int j2Diff = j2.logicalFairShare - j2.nextIterAllocation;
		
		if(j1Diff > j2Diff)
			return -1;
		else
			return 1;
	}
}
