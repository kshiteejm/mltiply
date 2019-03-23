package com.wisr.mlsched.events;

import com.wisr.mlsched.job.JobStatistics;

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