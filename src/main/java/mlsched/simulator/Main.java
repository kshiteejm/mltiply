package mlsched.simulator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeSet;

import mlsched.cluster.Cluster;
import mlsched.events.Event;
import mlsched.events.EventComparator;
import mlsched.events.JobArrived;
import mlsched.scheduler.EqualShareScheduler;
import mlsched.scheduler.InterJobScheduler;
import mlsched.workload.Job;
import mlsched.workload.JobComparator;
import mlsched.workload.Statistics;

public class Main {
	
	public static TreeSet<Event> eventQueue = new TreeSet<>(new EventComparator());
	public static Cluster cluster = new Cluster(30);
	public static InterJobScheduler interJobScheduler = new EqualShareScheduler();
	public static ArrayList<Job> jobList = new ArrayList<>();
	public static Hashtable<Integer, Statistics> jobStats = new Hashtable<>();
	
	public static double startTime = 0;
	public static double currentTime = 0;
	
	
	public static void main(String[] args) {
		
		Integer jobId, numIter = 1, serialRun = 30, maxParallel = 10;
		
		for(Integer i = 0; i < 5; i++) {
			jobId = i;
			Job j = new Job(jobId, numIter, serialRun, maxParallel);
			eventQueue.add(new JobArrived(startTime, j));
		}
		
		jobId = 5;
		Job j = new Job(jobId, numIter, serialRun, maxParallel);
		eventQueue.add(new JobArrived(1.0, j));
		
		// Start Iteration always happens after Job Arrived. We know
		// Job Arrival times in advance from the workload.
		
		while(!eventQueue.isEmpty()) {
			Event e = eventQueue.pollFirst();
			currentTime = e.timeStamp;
			e.printInfo();
			e.eventHandler();
		}
		
		for(Statistics s : jobStats.values()) {
			s.printStats();
		}
	}
}