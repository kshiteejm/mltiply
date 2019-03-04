package mlsched.scheduler;

import java.util.ArrayList;
import java.util.List;

import mlsched.events.StartIteration;
import mlsched.simulator.Main;
import mlsched.workload.Job;
import mlsched.workload.Task;

public class IntraJobScheduler {

	public IntraJobScheduler() {
	}
	
	public void schedule(Job j) {
		int numTasks = j.resAllocated - j.currResUse;
		for(int i = 0; i < numTasks; i++) {
			j.runnableTasks.add(new Task(j.jobId, j.jobId + "_" + i, j.serialIterationDuration/numTasks));
			j.currResUse++;
		}
		// Decide which tasks should be added to running tasks

		// TODO: Validate this works
		List<Task> tasks = new ArrayList<Task>(j.runnableTasks);
		j.runningTasks.addAll(tasks);
		j.runnableTasks.removeAll(tasks);
		
		Main.eventQueue.add(new StartIteration(Main.currentTime, j));
	}
}
