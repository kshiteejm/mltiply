package com.wisr.mlsched;

public class StartIterationEvent extends ClusterEvent {

	private IntraJobScheduler mJob;
	public StartIterationEvent(long timestamp, IntraJobScheduler job) {
		super(timestamp);
		mJob = job;
		setPriority(ClusterEvent.EventType.START_ITERATION);
	}

	@Override
	public void handleEvent() {
		mJob.startIteration();
	}
	
}