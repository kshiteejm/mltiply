package mlsched.scheduler;

import java.util.Arrays;
import java.util.Comparator;

import mlsched.events.ResourceAllocated;
import mlsched.simulator.Main;
import mlsched.workload.Job;

public class InterJobScheduler {

	public void computeLogicalFairShare() {
	}
	
	public void distributeResources() {
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
		
		// At the start of each iteration we allocate resources to jobs
		// farthest from their fair share.
		for(Job j : jobArr) {
			if(Main.cluster.availableGPUs >= j.logicalFairShare) {
				Main.cluster.availableGPUs -= j.logicalFairShare;
				j.nextIterAllocation = j.logicalFairShare;
			}
		}
		
		// Remaining resources are allocated in a round robin fashion
		// TODO: Make sure no infinite loop :P
		int numTicks = 0;
		int i = 0;
		while((Main.cluster.availableGPUs > 0) && (numTicks < 2*jobArr.length)) {
			if(jobArr[i].nextIterAllocation < jobArr[i].maxParallelism) {
				numTicks = 0;
				Main.cluster.availableGPUs -= 1;
				jobArr[i].nextIterAllocation += 1;
			} else {
				numTicks += 1;
			}
			i = (i + 1) % jobArr.length;
		}
		
		for(Job j : jobArr) {
			Main.eventQueue.add(new ResourceAllocated(Main.currentTime, j));
		}
	}
}
