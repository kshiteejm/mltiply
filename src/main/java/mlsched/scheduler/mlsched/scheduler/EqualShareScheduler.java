package mlsched.scheduler;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.JobComparator;

public class EqualShareScheduler extends InterJobScheduler {
	
	@Override
	public void computeLogicalFairShare() {
		
		Collections.sort(Main.jobList, new JobComparator());
		
		for(Job j : Main.jobList) {
			
			j.logicalFairShare = Math.min(j.maxParallelism,
					(int)Main.cluster.numGPUs / Main.jobList.size());
			
		}
		
		this.distributeResources();
	}
}
