package mlsched.scheduler;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import mlsched.events.ResourceAllocated;
import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.JobComparator;

public class InterJobScheduler {

	public void computeLogicalFairShare() {
	}
	
	public void distributeResources() {
		
		// Job[] jobArr = Arrays.copyOf(Main.jobList.toArray(), Main.jobList.toArray().length, Job[].class);
		
		Collections.sort(Main.jobList, new JobComparator());
		
		// At the start of each iteration we allocate resources to jobs
		// farthest from their fair share.
		for(Job j : Main.jobList) {
			if(Main.cluster.availableGPUs >= j.logicalFairShare) {
				Main.cluster.availableGPUs -= j.logicalFairShare;
				j.nextIterAllocation = j.logicalFairShare;
			}
		}
		
		Collections.sort(Main.jobList, new JobComparator());
		
		// Remaining resources are allocated in a round robin fashion
		// TODO: Make sure no infinite loop :P
		int numTicks = 0;
		int i = 0;
		while((Main.cluster.availableGPUs > 0) && (numTicks < 2*Main.jobList.size())) {
			if(Main.jobList.get(i).nextIterAllocation < Main.jobList.get(i).maxParallelism) {
				numTicks = 0;
				Main.cluster.availableGPUs -= 1;
				Main.jobList.get(i).nextIterAllocation += 1;
			} else {
				numTicks += 1;
			}
			i = (i + 1) % Main.jobList.size();
		}
		
		for(Job j : Main.jobList) {
			Main.eventQueue.add(new ResourceAllocated(Main.currentTime, j));
		}
	}
}
