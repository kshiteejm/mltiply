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
		int numTasks = j.currIterAllocation;
		for(int i = 0; i < numTasks; i++) {
			j.runnableTasks.add(new Task(j.jobId, j.jobId + "_" + i, j.serialIterationDuration/numTasks));
		}
		
		// Decide which tasks should be added to running tasks
		// All runnable tasks are eligible to run
		
		for(int i = 0; i < j.runnableTasks.size(); i++) {
			j.runningTasks.add(j.runnableTasks.get(i));
		}
		
//		for(Task t : j.runningTasks) {
//			System.out.println("IntraJob: TaskID = " + t.taskId);
//		}
		
		List<Task> tasks = new ArrayList<Task>(j.runnableTasks);
		j.runnableTasks.removeAll(tasks);
		
		Main.eventQueue.add(new StartIteration(Main.currentTime, j));
	}
}
