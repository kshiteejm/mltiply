package mlsched.events;

import mlsched.simulator.Main;
import mlsched.workload.Job;

public class EndInteration extends Event {

	public EndInteration(double timeStamp, Job j) {
		super(timeStamp, j);
	}

	@Override
	public void eventHandler() {
		if(j.currIterationNum < j.numIterations) {
			j.intraJobScheduler.schedule(j);
		} else {
			Main.eventQueue.add(new JobCompleted(Main.currentTime, j));
		}
	}
}
