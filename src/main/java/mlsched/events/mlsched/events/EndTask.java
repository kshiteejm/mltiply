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
	public void printInfo() {
		System.out.println("Event = " + this.getClass().toString() + "  Timestamp = " + this.timeStamp 
				+ " JobID = " + this.j.jobId + " TaskID = " + this.t.taskId);
	}
	
	@Override
	public void eventHandler() {
		j.runningTasks.remove(t);
		j.completedTasks.add(t);
		
		// If all tasks are completed then it's the end of iteration
		if(j.runnableTasks.isEmpty() && j.runningTasks.isEmpty()) {
			Main.eventQueue.add(new EndInteration(Main.currentTime, j));
		} else if(!j.runnableTasks.isEmpty()) {
			// In future if we schedule tasks for an iteration in batches
//			j.intraJobScheduler.schedule(j);
		} else {
			// We still have other running tasks. So let the last one come
			// here and trigger end of iteration.
		}
	}
}
