package mlsched.events;

import mlsched.workload.Job;

public class ResourceAllocated extends Event {

	public ResourceAllocated(double timeStamp, Job j) {
		super(timeStamp, j);
	}
	
	@Override
	public void eventHandler() {
		
		// Only start newly arriving jobs if it has enough resources
		//	System.out.println(" job ID " + j.jobId + " nextIterAllocation " + j.nextIterAllocation + " logicalFairShare " + j.logicalFairShare);
		
		if(j.jobState == Job.State.WAITING_FOR_RESOURCES && j.nextIterAllocation>=j.logicalFairShare) {
		
			j.jobState = Job.State.RUNNING;
			
			// Why are we doing this again? --> already done in InterJS line 31
			j.nextIterAllocation = j.logicalFairShare;
			j.currIterAllocation = j.nextIterAllocation;
			System.out.println("resource allocated --- " + j.nextIterAllocation + " to job id " + j.jobId);
			j.intraJobScheduler.schedule(j);
			
		} else {
			// The allocation will be reflected at the end of the iteration
			// for jobs that are in progress
		}
	}
}
