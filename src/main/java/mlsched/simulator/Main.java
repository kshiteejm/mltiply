package mlsched.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import mlsched.cluster.Cluster;
import mlsched.events.ComputeLogicalFairShare;
import mlsched.events.EndInteration;
import mlsched.events.EndTask;
import mlsched.events.Event;
import mlsched.events.EventComparator;
import mlsched.events.JobArrived;
import mlsched.events.JobCompleted;
import mlsched.events.ResourceAllocated;
import mlsched.events.StartIteration;
import mlsched.scheduler.EqualShareScheduler;
import mlsched.scheduler.InterJobScheduler;
import mlsched.workload.Job;
import mlsched.workload.Statistics;

public class Main {
	
	public static TreeSet<Event> eventQueue = new TreeSet<>(new EventComparator());
	public static Cluster cluster = new Cluster(5);
	public static InterJobScheduler interJobScheduler = new EqualShareScheduler();
	public static ArrayList<Job> jobList = new ArrayList<>();
	public static HashMap<Integer, Statistics> jobStats = new HashMap<>();
	
	public static double startTime = 0;
	public static double currentTime = 0;
	
	public static Integer eventPriority(Event e) {
		//	Ordering of events - ET > EI > JC > JA > RA > SI > everything else we might have missed
		
		if(e instanceof EndTask) {
			return 0;
		} else if (e instanceof EndInteration) {
			return 10;
		} else if (e instanceof JobCompleted) {
			return 20;
		} else if (e instanceof JobArrived) {
			return 30;
		} else if (e instanceof ComputeLogicalFairShare) {
			return 40;
		} else if (e instanceof ResourceAllocated) {
			return 50;
		} else if (e instanceof StartIteration) {
			return 60;
		} else {
			return 70;
		}
	}
	
	
	public static void main(String[] args) {
		
		Integer jobId, numIter = 2, serialRun = 2, maxParallel = 2;
		
		for(Integer i = 0; i < 4; i++) {
			jobId = i;
			Job j = new Job(jobId, numIter, serialRun, maxParallel);
			eventQueue.add(new JobArrived(startTime, j));
		}
		
		//	jobId = 5;
		//	Job j = new Job(jobId, numIter, serialRun, maxParallel);
		//	eventQueue.add(new JobArrived(1.0, j));
		
		// Start Iteration always happens after Job Arrived. We know
		// Job Arrival times in advance from the workload.
		
		while(!eventQueue.isEmpty()) {
			Event e = eventQueue.pollFirst();
			currentTime = e.timeStamp;
			e.printInfo();
			e.eventHandler();
		}
		
		System.out.println("\n\nOVERALL JOB STATS -");
		for(Integer key : jobStats.keySet()) {
			jobStats.get(key).printStats();
		}
	}
}