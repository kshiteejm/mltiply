package mlsched.simulator;

import java.util.TreeSet;

import mlsched.cluster.Cluster;
import mlsched.events.Event;
import mlsched.events.EventComparator;
import mlsched.events.JobArrived;
import mlsched.scheduler.EqualShareScheduler;
import mlsched.scheduler.InterJobScheduler;
import mlsched.workload.Job;
import mlsched.workload.JobComparator;

public class Main {
	
	public static TreeSet<Event> eventQueue = new TreeSet<>(new EventComparator());
	public static Cluster cluster = new Cluster(50);
	public static InterJobScheduler interJobScheduler = new EqualShareScheduler();
	public static TreeSet<Job> jobList = new TreeSet<>(new JobComparator());
	
	public static double startTime = 0;
	public static double currentTime;
	
	public static void main(String[] args) {
		
		for(int i = 0; i < 5; i++) {
			
			int jobId = i, numIter = 1, serialRun = 25, maxParallel = 10;
			Job j = new Job(jobId, numIter, serialRun, maxParallel);
			eventQueue.add(new JobArrived(startTime, j));
		}
		
		// Start Iteration always happens after Job Arrived. We know
		// Job Arrival times in advance from the workload.
		
		while(!eventQueue.isEmpty()) {
			Event e = eventQueue.pollFirst();
			currentTime = e.timeStamp;
			e.printInfo();
			e.eventHandler();
		}
	}
}