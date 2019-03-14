package mlsched.events;

import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.Statistics;

public class ComputeLogicalFairShare extends Event {
	
	public ComputeLogicalFairShare(double timeStamp) {
		super(timeStamp);
		// super(timeStamp, j);
//		Main.jobStats.put(j.jobId,new Statistics(j.jobId, timeStamp));
	}

	@Override
	public void eventHandler() {
		
		if(Main.eventQueue.isEmpty()) {
			
			// There are no more events to be processed in the queue			
			Main.interJobScheduler.computeLogicalFairShare();
			
		} else if(!(Main.eventQueue.first() instanceof ComputeLogicalFairShare)) {
			
			// There is not more ComputeLogicalFairShare events at the head of the
			// queue implies that all Jobs at this time have arrived.
			Main.interJobScheduler.computeLogicalFairShare();
			
		} else if(Main.currentTime < Main.eventQueue.first().timeStamp) {
			
			// We have processed all the ComputeLogicalFairShare at current time.
			Main.interJobScheduler.computeLogicalFairShare();
		}
	}
	
	@Override
	public void printInfo() {
		System.out.println("Event = " + this.getClass().toString() + " Timestamp = " + this.timeStamp);
	}
}
