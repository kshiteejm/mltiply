package mlsched.scheduler;

import java.util.Collections;

import mlsched.events.ResourceAllocated;
import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.JobComparator;

public class InterJobScheduler {

	public boolean schedulingEpoch;

	public InterJobScheduler(boolean schedulingEpoch) {
		this.schedulingEpoch = schedulingEpoch;
	}

	public void computeLogicalFairShare() {
	}

	public void distributeResources() {

		Main.distributeResourcesFlag = true;

		// At this point, nextIterAllocation and currIterAllocation are the same.
		// Hence using nextIterAllocation in the JobComparator() is not wrong.
		Collections.sort(Main.jobList, new JobComparator());

		for (Job j : Main.jobList) {
			if (j.jobState == Job.State.WAITING_FOR_RESOURCES) {
				if (Main.cluster.availableGPUs >= j.logicalFairShare) {
					
					System.out.println("JobID = " + j.jobId + " Resources = " + j.logicalFairShare);
					
					Main.cluster.availableGPUs -= j.logicalFairShare;

					// nextIterAllocation is updated here.
					j.nextIterAllocation = j.logicalFairShare;

					Main.eventQueue.add(new ResourceAllocated(Main.currentTime, j));
				} else {
					break;
				}
			}
		}
	}
}
