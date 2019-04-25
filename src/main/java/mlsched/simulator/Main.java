package mlsched.simulator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;

import mlsched.cluster.Cluster;
import mlsched.events.ComputeLogicalFairShare;
import mlsched.events.DistributeResources;
import mlsched.events.EndInteration;
import mlsched.events.EndTask;
import mlsched.events.Event;
import mlsched.events.EventComparator;
import mlsched.events.JobArrived;
import mlsched.events.JobCompleted;
import mlsched.events.ResourceAllocated;
import mlsched.events.SchedulingEpoch;
import mlsched.events.StartIteration;
import mlsched.scheduler.InterJobScheduler;
import mlsched.workload.Job;
import mlsched.workload.Statistics;
import mlsched.workload.Workload;

public class Main {

	public static TreeSet<Event> eventQueue = new TreeSet<>(new EventComparator());
	public static Cluster cluster;

	public static ArrayList<Job> jobList = new ArrayList<>();
	public static HashMap<Integer, Statistics> jobStats = new HashMap<>();
	public static boolean distributeResourcesFlag = false;

	public static InterJobScheduler interJobScheduler;
	public static boolean epochScheduling;

	public static double schedulingInterval;

	public static double startTime = 0;
	public static double currentTime = 0;

	public static long randSeed;

	public static final boolean DEBUG = false;
	public static Random r;
	
	public static Integer eventPriority(Event e) {
		// Ordering of events - ET > EI > JC > JA > SA > CL > DR > RA > SI > everything
		// else we might have missed

		// Scheduling epoch cannot happen before Job Completed
		// But since End Iteration is happening before Scheduling Epoch
		// This will not reflect the next iterations computation at Epoch
		// So add a Distribute Resource event which has lesser priority
		// than Scheduling Epoch.
		if (e instanceof EndTask) {
			return 0;
		} else if (e instanceof EndInteration) {
			return 10;
		} else if (e instanceof JobCompleted) {
			return 20;
		} else if (e instanceof JobArrived) {
			return 30;
		} else if (e instanceof SchedulingEpoch) {
			return 35;
		} else if (e instanceof ComputeLogicalFairShare) {
			return 40;
		} else if (e instanceof DistributeResources) {
			return 45;
		} else if (e instanceof ResourceAllocated) {
			return 50;
		} else if (e instanceof StartIteration) {
			return 60;
		} else {
			return 70;
		}
	}

	public static boolean doubleEquals(double a, double b) {
		final double THRESHOLD = .000000001;

		if (Math.abs(a - b) < THRESHOLD)
			return true;
		else
			return false;
	}

	public static void testOutput(String fileName) {
		File file = new File(fileName);
		Scanner sc = null;
		try {
			sc = new Scanner(file);

			while (sc.hasNextLine()) {
				String s = sc.nextLine();
				String[] parts = s.split("\t");
				Integer jobId = Integer.valueOf(parts[0]);
				double startTime = Double.valueOf(parts[1]);
				double endTime = Double.valueOf(parts[2]);

				assert doubleEquals(jobStats.get(jobId).jobStartTime, startTime);
				assert doubleEquals(jobStats.get(jobId).jobEndTime, endTime);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sc.close();
		}
	}

	public static void main(String[] args) {

		// Start Iteration always happens after Job Arrived. We know
		// Job Arrival times in advance from the workload.

		// Epoch scheduling will be same priority event before computeLogicalFairShare
		// because we only distribute resources after everything is released or jobs
		// have arrived.

		final String Filename = "optimus_uniform.json";

		Workload.parseWorkload(Filename);

		System.out.println("THE SEED IS: " + Main.randSeed);
		
		if (epochScheduling) {
			eventQueue.add(new SchedulingEpoch(currentTime, schedulingInterval));
		}

		while (!eventQueue.isEmpty()) {
			
			Event e = eventQueue.pollFirst();
			currentTime = e.timeStamp;

			if (DEBUG)
				e.printInfo();

			// Enqueue next epoch event in the eventQueue only if we have
			// Some events left to be processed.
			if ((e instanceof SchedulingEpoch) && (!eventQueue.isEmpty())) {
				eventQueue.add(new SchedulingEpoch(currentTime + schedulingInterval, schedulingInterval));
			}

			e.eventHandler();
		}

		System.out.println("\nOVERALL JOB STATS -");
		for (Integer key : jobStats.keySet()) {
			jobStats.get(key).printStats();
		}
		System.out.println("\nDONE");
		
//		testOutput("output1");
	}
}