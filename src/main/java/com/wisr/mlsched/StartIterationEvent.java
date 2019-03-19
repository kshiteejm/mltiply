package com.wisr.mlsched;

/**
 * Implementation of event when iteration starts for a job
 */
public class StartIterationEvent extends ClusterEvent {

	private IntraJobScheduler mJob;
	public StartIterationEvent(double timestamp, IntraJobScheduler job) {
		super(timestamp);
		mJob = job;
		setPriority(ClusterEvent.EventType.START_ITERATION);
	}

	@Override
	public void handleEvent() {
		super.handleEvent();
		mJob.startIteration();
	}
	
}