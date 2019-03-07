package mlsched.scheduler;

import java.util.Arrays;
import java.util.Comparator;

import mlsched.simulator.Main;
import mlsched.workload.Job;

public class EqualShareScheduler extends InterJobScheduler {
	
	@Override
	public void computeLogicalFairShare() {
		
		Job[] jobArr = (Job[]) Main.jobList.toArray();
		
		// TODO: Validate that the ordering is indeed correct.
		Arrays.sort(jobArr, new Comparator<Job>() {
			public int compare(Job j1, Job j2) {
				
				int j1Diff = j1.logicalFairShare - j1.currIterAllocation;
				int j2Diff = j2.logicalFairShare - j2.currIterAllocation;
				
				if(j1Diff > j2Diff)
					return 1;
				else
					return -1;
			}
		});
		
		for(Job j : jobArr) {
			j.logicalFairShare = Math.min(j.maxParallelism,
					(int)Main.cluster.numGPUs / Main.jobList.size());
		}
	}
}
