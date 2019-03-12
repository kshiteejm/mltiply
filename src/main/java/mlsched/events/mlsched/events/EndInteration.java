package mlsched.events;

import mlsched.simulator.Main;
import mlsched.workload.Job;

public class EndInteration extends Event {

	public EndInteration(double timeStamp, Job j) {
		super(timeStamp, j);
	}

	@Override
	public void eventHandler() {
		// Release all the resources at the end of the iteration
		Main.cluster.availableGPUs += j.currIterAllocation;
		j.currIterAllocation = 0;
		
		Main.jobStats.get(j.jobId).iterEndTimes.add(Main.currentTime);
		
		if(j.currIterationNum < j.numIterations) {
			j.jobState = Job.State.WAITING_FOR_RESOURCES;
			Main.interJobScheduler.computeLogicalFairShare();
		} else {
			Main.eventQueue.add(new JobCompleted(Main.currentTime, j));
		}
	}
}
