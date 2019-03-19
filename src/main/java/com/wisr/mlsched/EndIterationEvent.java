package com.wisr.mlsched;

public class EndIterationEvent extends ClusterEvent {

	private IntraJobScheduler mJob;
	public EndIterationEvent(long timestamp, IntraJobScheduler job) {
		super(timestamp);
		mJob = job;
		setPriority(ClusterEvent.EventType.END_ITERATION);
	}

	@Override
	public void handleEvent() {
		mJob.endIteration();
	}
	
}