package com.wisr.mlsched;

/**
 * Generic cluster event which will be enqueued into event queue
 */
public abstract class ClusterEvent {
	private long mTimestamp; // Timestamp of event
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
	public ClusterEvent(long timestamp) {
		mTimestamp = timestamp;
	}
	
	/**
	 * Returns the timestamp of event
	 */
	public long getTimestamp() {
		return mTimestamp;
	}
	
	/**
	 * Event handler for this event.
	 */
	public abstract void handleEvent();
	
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