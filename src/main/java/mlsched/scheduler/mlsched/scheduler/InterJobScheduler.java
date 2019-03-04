package mlsched.scheduler;

import mlsched.events.ResourceAllocated;
import mlsched.simulator.Main;
import mlsched.workload.Job;

public class InterJobScheduler {

	public InterJobScheduler() {
	}
	
	public void allocGPUs(Job j) {
		if(Main.cluster.availableGPUs >= 10) {
			j.resAllocated += 10;
			Main.cluster.availableGPUs -= 10;
		}
		Main.eventQueue.add(new ResourceAllocated(Main.currentTime, j));
	}
}
