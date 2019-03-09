package mlsched.events;

import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.Task;

public class StartIteration extends Event {

	public StartIteration(double timeStamp, Job j) {
		super(timeStamp, j);
	}

	@Override
	public void eventHandler() {
		j.currIterationNum++;
		for(Task t : j.runningTasks) {
//			System.out.println("StartIteration adding taskID = " + t.taskId);
			Main.eventQueue.add(new EndTask(Main.currentTime + t.duration, j, t));
		}
		
	}
}
