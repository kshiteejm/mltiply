package mlsched.scheduler;

import java.util.Arrays;
import java.util.Comparator;

import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.JobComparator;

public class EqualShareScheduler extends InterJobScheduler {
	
	@Override
	public void computeLogicalFairShare() {
		
		Job[] jobArr = Arrays.copyOf(Main.jobList.toArray(), Main.jobList.toArray().length, Job[].class);
		
		Arrays.sort(jobArr, new JobComparator());
		
		for(Job j : jobArr) {
			
			j.logicalFairShare = Math.min(j.maxParallelism,
					(int)Main.cluster.numGPUs / Main.jobList.size());
			
		}
		
		this.distributeResources();
	}
}
