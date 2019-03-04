package mlsched.simulator;

import java.util.PriorityQueue;

import mlsched.cluster.Cluster;
import mlsched.events.Event;
import mlsched.events.JobArrived;
import mlsched.scheduler.InterJobScheduler;
import mlsched.workload.Job;

public class Main {
	
	public static PriorityQueue<Event> eventQueue = new PriorityQueue<Event>();
	public static Cluster cluster = new Cluster(50);
	public static InterJobScheduler interJobScheduler = new InterJobScheduler();
	public static double startTime = 0;
	public static double currentTime;
	
	public static void main(String[] args) {
		
		Job j = new Job(1, 1, 25);
		
		eventQueue.add(new JobArrived(startTime, j));
		
		while(!eventQueue.isEmpty()) {
			Event e = eventQueue.poll();
			currentTime = e.timeStamp;
			System.out.println(currentTime + "_" + e.toString());
			e.eventHandler();
		}
	}
}