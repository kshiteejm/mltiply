package mlsched.scheduler;

import java.util.Arrays;
import java.util.Comparator;

import mlsched.events.ResourceAllocated;
import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.JobComparator;

public class InterJobScheduler {

	public void computeLogicalFairShare() {
	}
	
	public void distributeResources() {
		
		Job[] jobArr = Arrays.copyOf(Main.jobList.toArray(), Main.jobList.toArray().length, Job[].class);
		
		Arrays.sort(jobArr, new JobComparator());
		
		// At the start of each iteration we allocate resources to jobs
		// farthest from their fair share.
		for(Job j : jobArr) {
			if(Main.cluster.availableGPUs >= j.logicalFairShare) {
				Main.cluster.availableGPUs -= j.logicalFairShare;
				Main.jobList.remove(j);
				j.nextIterAllocation = j.logicalFairShare;
				Main.jobList.add(j);
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
				Main.jobList.remove(jobArr[i]);
				jobArr[i].nextIterAllocation += 1;
				Main.jobList.add(jobArr[i]);
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
