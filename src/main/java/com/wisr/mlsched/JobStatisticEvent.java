package com.wisr.mlsched;

public class JobStatisticEvent extends ClusterEvent {

	public JobStatisticEvent(double timestamp) {
		super(timestamp);
	}
	
	@Override
	public void handleEvent() {
		super.handleEvent();
		JobStatistics.getInstance().recordJobStatistics();
	}
}