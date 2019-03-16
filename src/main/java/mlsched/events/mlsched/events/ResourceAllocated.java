package mlsched.events;

import mlsched.workload.Job;

public class ResourceAllocated extends Event {

	public ResourceAllocated(double timeStamp, Job j) {
		super(timeStamp, j);
	}
	
	@Override
	public void eventHandler() {
		
		// Only start newly arriving jobs if it has enough resources		
		if(j.jobState == Job.State.RESOURCE_ALLOCATED) {
			j.jobState = Job.State.RUNNING;
			j.currIterAllocation = j.nextIterAllocation;
			j.intraJobScheduler.schedule(j);			
		} else {
			// The allocation will be reflected at the end of the iteration
			// for jobs that are in progress
		}
	}
}
