package mlsched.events;

import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.Task;

public class EndTask extends Event {
	Task t;

	public EndTask(double timeStamp, Job j, Task t) {
		super(timeStamp, j);
		this.t = t;
	}
	
	@Override
	public void eventHandler() {
		j.currResUse--;
		j.runningTasks.remove(t);
		j.completedTasks.add(t);
		if(j.runnableTasks.isEmpty() && j.runningTasks.isEmpty()) {
			Main.eventQueue.add(new EndInteration(Main.currentTime, j));
		} else if(!j.runnableTasks.isEmpty()) {
			j.intraJobScheduler.schedule(j);
		}
	}
}
