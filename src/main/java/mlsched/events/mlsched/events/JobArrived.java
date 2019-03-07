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
		// There are no more events to be processed in the queue
		// implies that all jobs have arrived.
		if(Main.eventQueue.isEmpty()) {
			Main.interJobScheduler.computeLogicalFairShare();
		}
		
		// There is not more JobArrival events at the head of the
		// queue implies that all Jobs at this time have arrived.
		if(!(Main.eventQueue.peek() instanceof JobArrived)) {
			Main.interJobScheduler.computeLogicalFairShare();
		}
		
		// We have processed all the JobArrivals at current time.
		if(Main.currentTime < Main.eventQueue.peek().timeStamp) {
			Main.interJobScheduler.computeLogicalFairShare();
		}
	}
}
