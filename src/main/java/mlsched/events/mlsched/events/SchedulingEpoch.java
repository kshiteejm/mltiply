package mlsched.events;

import mlsched.simulator.Main;

public class SchedulingEpoch extends Event {

	public double intervalSeconds;
	
	public SchedulingEpoch(double timeStamp, double intervalSeconds) {
		super(timeStamp);
		this.intervalSeconds = intervalSeconds;
	}
	
	@Override
	public void eventHandler() {
		Main.epochNumber += 1;
		Main.eventQueue.add(new ComputeLogicalFairShare(Main.currentTime));
	}
	
	@Override
	public void printInfo() {
		System.out.println("Event = " + this.getClass().toString() + " Timestamp = " + this.timeStamp);
	}
}
