package mlsched.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import mlsched.cluster.Cluster;
import mlsched.events.Event;
import mlsched.events.JobArrived;
import mlsched.scheduler.EqualShareScheduler;
import mlsched.scheduler.InterJobScheduler;
import mlsched.workload.Job;

public class Main {
	
	public static PriorityQueue<Event> eventQueue = new PriorityQueue<>();
	public static Cluster cluster = new Cluster(50);
	public static InterJobScheduler interJobScheduler = new EqualShareScheduler();
	public static double startTime = 0;
	public static double currentTime;
	public static PriorityQueue<Job> jobList = new PriorityQueue<>();
	
	public static void main(String[] args) {
		
		int jobId = 1, numIter = 1, serialRun = 25, maxParallel = 10;
		Job j = new Job(jobId, numIter, serialRun, maxParallel);
		
		// Start Iteration always happens after Job Arrived. We know
		// Job Arrival times in advance from the workload.
		eventQueue.add(new JobArrived(startTime, j));
		
		while(!eventQueue.isEmpty()) {
			Event e = eventQueue.poll();
			currentTime = e.timeStamp;
			System.out.println(currentTime + "_" + e.toString());
			e.eventHandler();
		}
	}
}