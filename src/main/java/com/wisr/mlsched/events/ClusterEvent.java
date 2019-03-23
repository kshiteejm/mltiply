package com.wisr.mlsched.events;

import com.wisr.mlsched.Simulation;

/**
 * Generic cluster event which will be enqueued into event queue
 */
public abstract class ClusterEvent {
	private double mTimestamp; // Timestamp of event
	private int mPriority; // When timestamps of two events are same, priority matters.
	
	public enum EventType {
		JOB_ARRIVAL, // Highest Priority
		END_ITERATION,
		RESOURCE_AVAILABLE,
		START_ITERATION; // Lowest Priority
	}
	
	/**
	 * Constuctor
	 * @param timestamp
	 */
	public ClusterEvent(double timestamp) {
		mTimestamp = timestamp;
	}
	
	/**
	 * Returns the timestamp of event
	 */
	public double getTimestamp() {
		return mTimestamp;
	}
	
	/**
	 * Event handler for this event.
	 */
	public void handleEvent() {
		Simulation.setSimulationTime(mTimestamp);
	}
	
	/**
	 * Getter for priority
	 */
	public int getPriority() {
		return mPriority;
	}
	
	/**
	 * Setter for priority
	 */
	protected void setPriority(EventType eventType) {
		mPriority = eventType.ordinal();
	}
}