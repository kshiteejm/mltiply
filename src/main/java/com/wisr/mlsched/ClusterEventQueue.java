package com.wisr.mlsched;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global event queue to manage cluster level events.
 */
public class ClusterEventQueue {
	
	private static ClusterEventQueue sInstance = null;
	public static TreeSet<ClusterEvent> mEventQueue;
	private static Logger sLog; // Instance of logger
	
	
	/**
	 * Private constructor
	 */
	private ClusterEventQueue() {
		mEventQueue = new TreeSet<ClusterEvent>(new ClusterEventComparator());
		sLog = Logger.getLogger(ClusterEventQueue.class.getSimpleName());
		sLog.setLevel(Simulation.getLogLevel());
	}
	
	/**
	 * Get an instance of the cluster event queue
	 * @return instance of ClusterEventQueue
	 */
	public static ClusterEventQueue getInstance() {
		if(sInstance == null) {
			sInstance = new ClusterEventQueue();
		}
		return sInstance;
	}
	
	/**
	 * Enqueue a cluster event to the cluster event queue
	 * @param event
	 */
	public void enqueueEvent(ClusterEvent event) {
		//sLog.log(Level.ALL, "Enqueuing event " + event.toString());
		mEventQueue.add(event);
	}
	
	/**
	 * Start event processing
	 */
	public void start() {
		sLog.info("Starting cluster event queue");
		while(!mEventQueue.isEmpty()) {
			ClusterEvent event = mEventQueue.pollFirst();
			sLog.info("Processing event " + event.toString() + " at " + Double.toString(
					event.getTimestamp()));
			// TODO: Handle event aggregation?
			event.handleEvent();
		}
	}
	
	/**
	 * Comparator for cluster events
	 */
	private class ClusterEventComparator implements Comparator<ClusterEvent> {

		@Override
		public int compare(ClusterEvent e1, ClusterEvent e2) {
			// First check timestamps
			if(e1.getTimestamp() != e2.getTimestamp()) {
				if(e1.getTimestamp() < e2.getTimestamp()) {
					return -1;
				} else {
					return 1;
				}
			}
			// If timestamps are same, then priority comes into picture
			return e1.getPriority() - e2.getPriority();
		}
	}
}