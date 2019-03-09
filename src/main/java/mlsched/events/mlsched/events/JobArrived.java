package mlsched.events;

import mlsched.simulator.Main;
import mlsched.workload.Job;

public class JobArrived extends Event {
	
	public JobArrived(double timeStamp, Job j) {
		super(timeStamp, j);
	}

	@Override
	public void eventHandler() {
		Main.jobList.add(j);
		
		if(Main.eventQueue.isEmpty()) {
			
			// There are no more events to be processed in the queue
			// implies that all jobs have arrived.			
			Main.interJobScheduler.computeLogicalFairShare();
			
		} else if(!(Main.eventQueue.first() instanceof JobArrived)) {
			
			// There is not more JobArrival events at the head of the
			// queue implies that all Jobs at this time have arrived.
			Main.interJobScheduler.computeLogicalFairShare();
			
		} else if(Main.currentTime < Main.eventQueue.first().timeStamp) {
			
			// We have processed all the JobArrivals at current time.
			Main.interJobScheduler.computeLogicalFairShare();
		}
	}
}
