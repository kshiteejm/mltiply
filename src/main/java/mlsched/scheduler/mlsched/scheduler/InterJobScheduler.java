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
		
		// At this point, nextIterAllocation and currIterAllocation are the same.
		// Hence using nextIterAllocation in the JobComparator() is not wrong.
		Collections.sort(Main.jobList, new JobComparator());
		
		for(Job j : Main.jobList) {

			if(j.jobState == Job.State.WAITING_FOR_RESOURCES && 
					Main.cluster.availableGPUs >= j.logicalFairShare) {
				Main.cluster.availableGPUs -= j.logicalFairShare;
				
				// nextIterAllocation is updated here.
				j.nextIterAllocation = j.logicalFairShare;
				
				// TODO: Try this later
				Main.eventQueue.add(new ResourceAllocated(Main.currentTime, j));
			}
		}
		
		// Don't need to sort it here since nothing has changed --> IT'S WRONG.
		//	Collections.sort(Main.jobList, new JobComparator());
		
//		for(Job j : Main.jobList) {
//			Main.eventQueue.add(new ResourceAllocated(Main.currentTime, j));
//		}
	}
}
