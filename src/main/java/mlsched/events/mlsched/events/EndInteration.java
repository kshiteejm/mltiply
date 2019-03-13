package mlsched.events;

import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.Statistics;

public class EndInteration extends Event {

	public EndInteration(double timeStamp, Job j) {
		super(timeStamp, j);
	}

	@Override
	public void eventHandler() {
		// Release all the resources at the end of the iteration
		Main.cluster.availableGPUs += j.currIterAllocation;
		j.currIterAllocation = 0;
//		j.nextIterAllocation = 0;
		
		Statistics statObj = Main.jobStats.get(j.jobId);
		statObj.iterEndTimes.add(Main.currentTime);
		Main.jobStats.put(j.jobId, statObj);
		
		if(j.currIterationNum < j.numIterations) {
			j.jobState = Job.State.WAITING_FOR_RESOURCES;
			Main.eventQueue.add(new ComputeLogicalFairShare(Main.currentTime, j));
//			Main.interJobScheduler.computeLogicalFairShare();
		} else {
			Main.eventQueue.add(new JobCompleted(Main.currentTime, j));
		}
	}
}
