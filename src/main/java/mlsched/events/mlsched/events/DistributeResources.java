package mlsched.events;

import mlsched.simulator.Main;

public class DistributeResources extends Event {

	public DistributeResources(double timeStamp) {
		super(timeStamp);
	}

	@Override
	public void eventHandler() {
		Main.interJobScheduler.distributeResources();
	}
	
	
	@Override
	public void printInfo() {
		System.out.println("Event = " + this.getClass().toString() + " Timestamp = " + this.timeStamp);
	}
}
