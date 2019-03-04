package mlsched.events;

import mlsched.simulator.Main;
import mlsched.workload.Job;

public class JobCompleted extends Event {

	public JobCompleted(double timeStamp, Job j) {
		super(timeStamp, j);
	}

	@Override
	public void eventHandler() {
		Main.cluster.availableGPUs += j.resAllocated;
	}
}
