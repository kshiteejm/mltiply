package mlsched.events;

import mlsched.workload.Job;

public class ResourceAllocated extends Event {

	public ResourceAllocated(double timeStamp, Job j) {
		super(timeStamp, j);
	}
	
	@Override
	public void eventHandler() {
		j.intraJobScheduler.schedule(j);
	}
}
