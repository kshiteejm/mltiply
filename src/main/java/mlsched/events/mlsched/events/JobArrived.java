package mlsched.events;

import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.Statistics;

public class JobArrived extends Event {
	
	public JobArrived(double timeStamp, Job j) {
		super(timeStamp, j);
		Main.jobStats.put(j.jobId, new Statistics(j.jobId, timeStamp));
	}

	@Override
	public void eventHandler() {
		
		Main.jobList.add(j);
		if(!Main.epochScheduling) {
			Main.eventQueue.add(new ComputeLogicalFairShare(Main.currentTime));
		}
	}
}
