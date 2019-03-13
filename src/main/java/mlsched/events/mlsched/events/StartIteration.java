package mlsched.events;

import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.Statistics;
import mlsched.workload.Task;

public class StartIteration extends Event {

	public StartIteration(double timeStamp, Job j) {
		super(timeStamp, j);
	}

	@Override
	public void eventHandler() {
		j.currIterationNum++;
		
		if(j.currIterationNum == 1) {
			Statistics statObj = Main.jobStats.get(j.jobId);
			statObj.jobStartTime = Main.currentTime;
			Main.jobStats.put(j.jobId, statObj);
		}
		
		Statistics statObj = Main.jobStats.get(j.jobId);
		statObj.iterStartTimes.add(Main.currentTime);
		Main.jobStats.put(j.jobId, statObj);
		
		for(Task t : j.runningTasks) {
			Main.eventQueue.add(new EndTask(Main.currentTime + t.duration, j, t));
		}
		
	}
}
